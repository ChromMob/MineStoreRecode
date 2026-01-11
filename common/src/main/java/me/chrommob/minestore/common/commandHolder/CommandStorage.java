package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreExecuteEvent;
import me.chrommob.minestore.api.event.types.MineStoreExecuteIntentEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.CheckResponse;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;
import me.chrommob.minestore.common.config.ConfigKeys;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandStorage {
    private final MineStoreCommon plugin;
    public CommandStorage(MineStoreCommon plugin) {
        this.plugin = plugin;
        MineStoreEventBus.registerListener(plugin.getInternalAddon(), MineStorePlayerJoinEvent.class, event -> onPlayerJoin(event.getUsername()));
    }
    private Map<String, List<String>> commands;
    private Map<String, List<StoredCommand>> newCommands;

    private void remove(String username, String command) {
        plugin.debug(this.getClass(), "Removing " + command + " for " + username + " from command storage");
        commands.get(username).remove(command);
        plugin.commandDumper().update(commands);
    }

    private void removeNewCommand(StoredCommand storedCommand, String username) {
        plugin.debug(this.getClass(), "Removing " + storedCommand.command() + " for " + username + " from new command storage");
        newCommands.get(username).remove(storedCommand);
        plugin.newCommandDumper().update(newCommands);
    }

    private void addCommand(String username, String command, int requestId) {
        plugin.debug(this.getClass(), "Adding " + command + " to " + username + " in command storage");
        username = username.toLowerCase();
        if (MineStoreCommon.version().requires("3.0.0")) {
            addNewCommand(username, command, requestId);
        } else {
            add(username, command);
        }
    }

    private void add(String username, String command) {
        if (commands.containsKey(username)) {
            commands.get(username).add(command);
        } else {
            commands.put(username, new ArrayList<>(Collections.singletonList(command)));
        }
        plugin.commandDumper().update(commands);
    }

    private void addNewCommand(String username, String command, int requestId) {
        if (newCommands.containsKey(username)) {
            newCommands.get(username).add(new StoredCommand(command, requestId));
        } else {
            newCommands.put(username, new ArrayList<>(Collections.singletonList(new StoredCommand(command, requestId))));
        }
        plugin.newCommandDumper().update(newCommands);
    }

    private void onPlayerJoin(String username) {
        username = username.toLowerCase();
        List<ParsedResponse> parsedResponses;
        if (MineStoreCommon.version().requires("3.0.0")) {
            parsedResponses = playerJoinNew(username);
        } else {
            parsedResponses = playerJoinOld(username);
        }
        if (parsedResponses.isEmpty()) {
            return;
        }
        plugin.debug(this.getClass(), "Executing new commands for " + username);
        executeWithOnlineCheck(parsedResponses, false);
    }

    private List<ParsedResponse> playerJoinNew(String username) {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        if (!newCommands.containsKey(username)) {
            return parsedResponses;
        }
        for (StoredCommand storedCommand : newCommands.get(username)) {
            parsedResponses.add(storedCommand.toParsedResponse(username));
        }
        return parsedResponses;
    }

    private List<ParsedResponse> playerJoinOld(String username) {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        if (!commands.containsKey(username)) {
            return parsedResponses;
        }
        for (String storedCommand : commands.get(username)) {
            parsedResponses.add(new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, storedCommand, username, 0));
        }
        return parsedResponses;
    }

    public void listener(List<ParsedResponse> commands) {
        List<ParsedResponse> onlineCommands = new ArrayList<>();
        for (ParsedResponse command : commands) {
            if (command.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE) {
                onlineCommands.add(command);
                continue;
            }
            execute(command);
        }
        handleOnlineCommands(onlineCommands);
    }

    /**
     * Filters the given list of commands, if they are offline, they will be added to the new command storage.
     * If they are online, they will be passed to {@link #executeWithOnlineCheck(List, boolean)} for further processing.
     *
     * @param parsedCommands The commands to filter.
     */
    private void handleOnlineCommands(List<ParsedResponse> parsedCommands) {
        List<ParsedResponse> online = new ArrayList<>();
        for (ParsedResponse parsedResponse : parsedCommands) {
            if (Registries.COMMAND_EXECUTER.get().isOnline(parsedResponse.username())) {
                online.add(parsedResponse);
                continue;
            }
            addCommand(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId());
        }
        executeWithOnlineCheck(online, true);
    }

    private void executeWithOnlineCheck(List<ParsedResponse> parsedCommands, boolean newCommands) {
        if (parsedCommands.isEmpty()) {
            return;
        }
        if (MineStoreCommon.version().requires(3, 2, 5)) {
            Set<Integer> toCheckIds = new HashSet<>();
            for (ParsedResponse parsedResponse : parsedCommands) {
                toCheckIds.add(parsedResponse.commandId());
            }
            plugin.webListener().checkCommands(toCheckIds).thenAcceptAsync(checkResponses -> {
                List<ParsedResponse> successful = new ArrayList<>();
                if (!checkResponses.status()) {
                    plugin.log("Failed to check commands: " + checkResponses.error());
                    return;
                }
                Set<Integer> successIds = new HashSet<>();
                Map<Integer, String> errors = new HashMap<>();
                for (CheckResponse.CheckResponses checkResponse : checkResponses.results()) {
                    if (!checkResponse.status()) {
                        if (checkResponse.error() != null) {
                            errors.put(checkResponse.cmd_id(), checkResponse.error());
                        } else {
                            errors.put(checkResponse.cmd_id(), "Unknown error");
                        }
                        continue;
                    }
                    successIds.add(checkResponse.cmd_id());
                }
                for (ParsedResponse parsedResponse : parsedCommands) {
                    if (!successIds.contains(parsedResponse.commandId()) && errors.containsKey(parsedResponse.commandId())) {
                        removeNewCommand(StoredCommand.fromParsedResponse(parsedResponse), parsedResponse.username());
                        plugin.debug(this.getClass(), "Command " + parsedResponse.command() + " with id " + parsedResponse.commandId() + " failed to execute. Error: " + errors.get(parsedResponse.commandId()));
                        continue;
                    }
                    successful.add(parsedResponse);
                }
                executeWithApiCheck(successful, newCommands);
            });
        } else {
            executeWithApiCheck(parsedCommands, newCommands);
        }
    }

    private void executeWithApiCheck(List<ParsedResponse> parsedCommands, boolean newCommands) {
        CompletableFuture.runAsync(() -> {
            for (ParsedResponse parsedResponse : parsedCommands) {
                if (!shouldExecute(parsedResponse)) {
                    if (!newCommands) {
                        continue;
                    }
                    addCommand(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId());
                    continue;
                }
                execute(parsedResponse);
                try {
                    //Give the server some time to process the command
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                if (MineStoreCommon.version().requires(3, 0, 0)) {
                    plugin.webListener().postExecuted(String.valueOf(parsedResponse.commandId()));
                }
                if (newCommands) {
                    continue;
                }
                if (MineStoreCommon.version().requires(3, 0, 0)) {
                    removeNewCommand(StoredCommand.fromParsedResponse(parsedResponse), parsedResponse.username());
                } else {
                    remove(parsedResponse.username(), parsedResponse.command());
                }
            }
        }).exceptionally(e -> {
            plugin.debug(this.getClass(), e);
            return null;
        });
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

    public void init() {
        commands = plugin.commandDumper().load();
        newCommands = plugin.newCommandDumper().load();
    }
}
