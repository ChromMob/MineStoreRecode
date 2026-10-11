package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreExecuteEvent;
import me.chrommob.minestore.api.event.types.MineStoreExecuteIntentEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.storage.PendingCommandStore;
import me.chrommob.minestore.common.commandHolder.type.CheckResponse;
import me.chrommob.minestore.common.commandHolder.type.PendingCommand;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;
import me.chrommob.minestore.common.config.ConfigKeys;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps every command from the store queue in {@link PendingCommandStore} from the moment it is received
 * until the store confirmed its execution, so a crash or failed request never loses a purchase.
 */
public class CommandStorage {
    private final MineStoreCommon plugin;
    // Used from the scheduler, command execution and player join threads, so access is synchronized on this.
    private final Map<String, PendingCommand> pending = new LinkedHashMap<>();
    // Entries being validated or executed right now, so a poll and a join never run the same entry twice.
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();
    private PendingCommandStore store;
    // Store versions before 3.0.0 have no delivery confirmation: username -> commands waiting for the player.
    private Map<String, List<String>> legacyCommands;

    public CommandStorage(MineStoreCommon plugin) {
        this.plugin = plugin;
        MineStoreEventBus.registerListener(plugin.getInternalAddon(), MineStorePlayerJoinEvent.class, event -> onPlayerJoin(event.getUsername()));
    }

    public synchronized void init() {
        legacyCommands = plugin.commandDumper().load();
        store = new PendingCommandStore(plugin);
        if (!store.exists()) {
            migrateSavedCommands();
            return;
        }
        for (PendingCommand command : store.load()) {
            pending.put(command.key(), command);
        }
    }

    // Imports savedCommands.json from older plugin versions once. Those commands were delivered already.
    private void migrateSavedCommands() {
        for (Map.Entry<String, List<StoredCommand>> entry : plugin.newCommandDumper().load().entrySet()) {
            for (StoredCommand storedCommand : entry.getValue()) {
                PendingCommand command = new PendingCommand(storedCommand.requestId(), entry.getKey(), storedCommand.command(), true, true);
                pending.put(command.key(), command);
            }
        }
        store.save(pending.values());
        plugin.newCommandDumper().markMigrated();
    }

    private void onPlayerJoin(String username) {
        // init() can run more than once (failed start, then a successful retry), only the current storage handles joins.
        if (plugin.commandStorage() != this) {
            return;
        }
        if (!MineStoreCommon.version().requires("3.0.0")) {
            legacyJoin(username.toLowerCase());
            return;
        }
        List<PendingCommand> commands = new ArrayList<>();
        synchronized (this) {
            for (PendingCommand command : pending.values()) {
                if (command.username().equalsIgnoreCase(username) && !command.isExecuted()) {
                    commands.add(command);
                }
            }
        }
        if (commands.isEmpty()) {
            return;
        }
        plugin.debug(this.getClass(), "Executing pending commands for " + username);
        run(commands);
    }

    /**
     * Saves newly received commands. This has to happen before the store is told they were delivered,
     * because the store never hands out a delivered command again.
     * A command with the same id whose delivery is not confirmed yet is the same command handed out again and is skipped;
     * after a confirmed delivery the same id is a new command (e.g. resent from the panel).
     */
    public List<PendingCommand> receive(List<ParsedResponse> commands) {
        List<PendingCommand> received = new ArrayList<>();
        synchronized (this) {
            for (ParsedResponse parsedResponse : commands) {
                if (hasUndelivered(parsedResponse.commandId())) {
                    continue;
                }
                PendingCommand command = new PendingCommand(parsedResponse.commandId(), parsedResponse.username(), parsedResponse.command(), parsedResponse.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE, false);
                pending.put(command.key(), command);
                received.add(command);
            }
            if (!received.isEmpty()) {
                store.save(pending.values());
            }
        }
        return received;
    }

    private boolean hasUndelivered(int id) {
        for (PendingCommand command : pending.values()) {
            if (command.id() == id && !command.isDelivered()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs everything that has not run yet, e.g. commands saved before a crash or restart.
     */
    public void runWaiting() {
        List<PendingCommand> commands = new ArrayList<>();
        synchronized (this) {
            for (PendingCommand command : pending.values()) {
                if (!command.isExecuted()) {
                    commands.add(command);
                }
            }
        }
        run(commands);
    }

    /**
     * Runs offline commands right away and online commands whose player is online.
     * Everything else stays pending until the player joins.
     */
    public void run(List<PendingCommand> commands) {
        List<PendingCommand> online = new ArrayList<>();
        for (PendingCommand command : commands) {
            if (!inFlight.add(command.key())) {
                continue;
            }
            if (!command.onlineRequired()) {
                try {
                    execute(command.toParsedResponse());
                    markExecuted(command);
                } catch (Exception e) {
                    logExecutionFailure(command, e);
                } finally {
                    inFlight.remove(command.key());
                }
                continue;
            }
            if (Registries.COMMAND_EXECUTER.get().isOnline(command.username())) {
                online.add(command);
            } else {
                inFlight.remove(command.key());
            }
        }
        validateAndExecute(online);
    }

    private void validateAndExecute(List<PendingCommand> commands) {
        if (commands.isEmpty()) {
            return;
        }
        if (!MineStoreCommon.version().requires(3, 2, 5)) {
            executeOnline(commands);
            return;
        }
        Set<Integer> toCheckIds = new HashSet<>();
        for (PendingCommand command : commands) {
            toCheckIds.add(command.id());
        }
        plugin.webListener().checkCommands(toCheckIds).thenAcceptAsync(checkResponses -> {
            if (checkResponses.isRequestFailed()) {
                release(commands, "Could not validate commands with the store");
                return;
            }
            if (!checkResponses.status()) {
                release(commands, "Failed to check commands: " + checkResponses.error());
                return;
            }
            Set<Integer> successIds = new HashSet<>();
            Map<Integer, String> errors = new HashMap<>();
            if (checkResponses.results() != null) {
                for (CheckResponse.CheckResponses checkResponse : checkResponses.results()) {
                    if (!checkResponse.status()) {
                        errors.put(checkResponse.cmd_id(), checkResponse.error() != null ? checkResponse.error() : "Unknown error");
                        continue;
                    }
                    successIds.add(checkResponse.cmd_id());
                }
            }
            List<PendingCommand> valid = new ArrayList<>();
            for (PendingCommand command : commands) {
                if (!successIds.contains(command.id()) && errors.containsKey(command.id())) {
                    // The store rejected it, so it never runs.
                    plugin.debug(this.getClass(), "Command " + command.command() + " with id " + command.id() + " failed to execute. Error: " + errors.get(command.id()));
                    remove(Collections.singletonList(command));
                    inFlight.remove(command.key());
                    continue;
                }
                valid.add(command);
            }
            executeOnline(valid);
        }).exceptionally(e -> {
            plugin.debug(this.getClass(), e);
            release(commands, "Error while validating commands");
            return null;
        });
    }

    // The commands stay pending and are tried again on the player's next join.
    private void release(List<PendingCommand> commands, String reason) {
        plugin.log(reason + ", " + commands.size() + " command(s) will be retried when the player joins again.");
        for (PendingCommand command : commands) {
            inFlight.remove(command.key());
        }
    }

    private void executeOnline(List<PendingCommand> commands) {
        if (commands.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                for (PendingCommand command : commands) {
                    ParsedResponse parsedResponse = command.toParsedResponse();
                    if (!shouldExecute(parsedResponse)) {
                        // Cancelled by an addon for now, it stays pending for the next join.
                        continue;
                    }
                    try {
                        execute(parsedResponse);
                    } catch (Exception e) {
                        logExecutionFailure(command, e);
                        continue;
                    }
                    markExecuted(command);
                    //Give the server some time to process the command
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                for (PendingCommand command : commands) {
                    inFlight.remove(command.key());
                }
            }
        }).exceptionally(e -> {
            plugin.log("Error while executing commands: " + e);
            plugin.debug(this.getClass(), e);
            return null;
        });
    }

    private void logExecutionFailure(PendingCommand command, Exception e) {
        plugin.log("Failed to execute command \"" + command.command() + "\" for " + command.username() + ", it will be retried when the player joins again.");
        plugin.debug(this.getClass(), e);
    }

    private synchronized void markExecuted(PendingCommand command) {
        if (!pending.containsKey(command.key())) {
            return;
        }
        command.markExecuted();
        store.save(pending.values());
    }

    public synchronized List<PendingCommand> undelivered() {
        List<PendingCommand> commands = new ArrayList<>();
        for (PendingCommand command : pending.values()) {
            if (!command.isDelivered()) {
                commands.add(command);
            }
        }
        return commands;
    }

    /**
     * Executed commands the store can be told about, their delivery has to be confirmed first.
     */
    public synchronized List<PendingCommand> executedUnconfirmed() {
        List<PendingCommand> commands = new ArrayList<>();
        for (PendingCommand command : pending.values()) {
            if (command.isDelivered() && command.isExecuted()) {
                commands.add(command);
            }
        }
        return commands;
    }

    public synchronized void markDelivered(Collection<PendingCommand> commands) {
        for (PendingCommand command : commands) {
            command.markDelivered();
        }
        store.save(pending.values());
    }

    public synchronized void remove(Collection<PendingCommand> commands) {
        for (PendingCommand command : commands) {
            pending.remove(command.key());
        }
        store.save(pending.values());
    }

    public boolean shouldExecute(ParsedResponse parsedResponse) {
        MineStoreExecuteIntentEvent intent = new MineStoreExecuteIntentEvent(parsedResponse.commandType(), parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId());
        intent.call();
        return !intent.isCancelled();
    }

    private void execute(ParsedResponse parsedResponse) {
        String command = parsedResponse.command();
        String username = parsedResponse.username();
        int requestId = parsedResponse.commandId();
        MineStoreExecuteEvent event = new MineStoreExecuteEvent(username, command, requestId);
        event.call();
        if (!event.isCancelled() &&ConfigKeys.COMMAND_EXEC_LOGGING.getValue()) {
            plugin.log("Executing command: " + command);
        }
        Registries.COMMAND_EXECUTER.get().execute(event);
    }

    // ---- Store versions before 3.0.0, which have no delivery confirmation. ----

    public void legacyListener(List<ParsedResponse> commands) {
        List<ParsedResponse> online = new ArrayList<>();
        for (ParsedResponse command : commands) {
            if (command.commandType() != ParsedResponse.COMMAND_TYPE.ONLINE) {
                execute(command);
                continue;
            }
            if (Registries.COMMAND_EXECUTER.get().isOnline(command.username())) {
                online.add(command);
                continue;
            }
            legacyAdd(command.username(), command.command());
        }
        legacyExecute(online, false);
    }

    private void legacyJoin(String username) {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        synchronized (this) {
            List<String> userCommands = legacyCommands.get(username);
            if (userCommands == null) {
                return;
            }
            for (String storedCommand : userCommands) {
                parsedResponses.add(new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, storedCommand, username, 0));
            }
        }
        legacyExecute(parsedResponses, true);
    }

    private void legacyExecute(List<ParsedResponse> parsedCommands, boolean fromStorage) {
        if (parsedCommands.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            for (ParsedResponse parsedResponse : parsedCommands) {
                if (!shouldExecute(parsedResponse)) {
                    if (!fromStorage) {
                        legacyAdd(parsedResponse.username(), parsedResponse.command());
                    }
                    continue;
                }
                try {
                    execute(parsedResponse);
                } catch (Exception e) {
                    plugin.log("Failed to execute command \"" + parsedResponse.command() + "\" for " + parsedResponse.username() + ", it will be retried when the player joins again.");
                    plugin.debug(this.getClass(), e);
                    if (!fromStorage) {
                        legacyAdd(parsedResponse.username(), parsedResponse.command());
                    }
                    continue;
                }
                if (fromStorage) {
                    legacyRemove(parsedResponse.username(), parsedResponse.command());
                }
                try {
                    //Give the server some time to process the command
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).exceptionally(e -> {
            plugin.log("Error while executing commands: " + e);
            plugin.debug(this.getClass(), e);
            return null;
        });
    }

    private synchronized void legacyAdd(String username, String command) {
        legacyCommands.computeIfAbsent(username.toLowerCase(), k -> new ArrayList<>()).add(command);
        plugin.commandDumper().update(legacyCommands);
    }

    private synchronized void legacyRemove(String username, String command) {
        List<String> userCommands = legacyCommands.get(username.toLowerCase());
        if (userCommands == null) {
            return;
        }
        userCommands.remove(command);
        plugin.commandDumper().update(legacyCommands);
    }
}
