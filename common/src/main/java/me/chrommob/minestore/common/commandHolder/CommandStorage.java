package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.types.MineStoreExecuteEvent;
import me.chrommob.minestore.api.event.types.MineStoreExecuteIntentEvent;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.CheckResponse;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;

import java.util.*;
import java.util.stream.Collectors;

public class CommandStorage {
    private final MineStoreCommon plugin;
    public CommandStorage(MineStoreCommon plugin) {
        this.plugin = plugin;
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
        newCommands.remove(username);
        plugin.newCommandDumper().update(newCommands);
    }

    private void add(String username, String command) {
        plugin.debug(this.getClass(), "Adding " + command + " to " + username + " in command storage");
        if (commands.containsKey(username)) {
            commands.get(username).add(command);
        } else {
            commands.put(username, new ArrayList<>(Collections.singletonList(command)));
        }
        plugin.commandDumper().update(commands);
    }

    private void addNewCommand(String username, String command, int requestId) {
        plugin.debug(this.getClass(), "Adding " + command + " to " + username + " in new command storage");
        if (newCommands.containsKey(username)) {
            newCommands.get(username).add(new StoredCommand(command, requestId));
        } else {
            newCommands.put(username, new ArrayList<>(Collections.singletonList(new StoredCommand(command, requestId))));
        }
        plugin.newCommandDumper().update(newCommands);
    }

    public void onPlayerJoin(String username) {
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
        handleOnlineCommands(parsedResponses, false);
    }

    private List<ParsedResponse> playerJoinNew(String username) {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        if (!newCommands.containsKey(username)) {
            return parsedResponses;
        }
        for (StoredCommand storedCommand : newCommands.get(username)) {
            if (!shouldExecute(storedCommand.toParsedResponse(username))) {
                continue;
            }
            parsedResponses.add(storedCommand.toParsedResponse(username));
            removeNewCommand(storedCommand, username);
        }
        return parsedResponses;
    }

    private List<ParsedResponse> playerJoinOld(String username) {
        List<ParsedResponse> parsedResponses = new ArrayList<>();
        if (!commands.containsKey(username)) {
            return parsedResponses;
        }
        for (String storedCommand : commands.get(username)) {
            if (!shouldExecute(new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, storedCommand, username, 0))) {
                continue;
            }
            parsedResponses.add(new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, storedCommand, username, 0));
            remove(username, storedCommand);
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
            handleOfflineCommand(command);
        }
        handleOnlineCommands(onlineCommands, true);
    }

    private void handleOnlineCommands(List<ParsedResponse> parsedCommands, boolean newCommands) {
        if (!MineStoreCommon.version().requires(3, 0, 0)) {
            for (ParsedResponse parsedResponse : parsedCommands) {
                if (Registries.COMMAND_EXECUTER.get().isOnline(parsedResponse.username()) && shouldExecute(parsedResponse)) {
                    handleOfflineCommand(parsedResponse);
                    continue;
                }
                if (!newCommands) {
                    continue;
                }
                add(parsedResponse.username(), parsedResponse.command());
            }
            return;
        }
        Map<String, List<ParsedResponse>> commands = new HashMap<>();
        for (ParsedResponse parsedCommand : parsedCommands) {
            String username = parsedCommand.username();
            username = username.toLowerCase();
            if (!commands.containsKey(username)) {
                commands.put(username, new ArrayList<>(Collections.singletonList(parsedCommand)));
            } else {
                commands.get(username).add(parsedCommand);
            }
        }
        List<ParsedResponse> toCheck = new ArrayList<>();
        Set<Integer> toCheckIds = new HashSet<>();
        for (Map.Entry<String, List<ParsedResponse>> entry : commands.entrySet()) {
            String username = entry.getKey();
            List<ParsedResponse> parsedResponses = entry.getValue();
            username = username.toLowerCase();
            boolean isOnline = Registries.COMMAND_EXECUTER.get().isOnline(username);
            plugin.debug(this.getClass(), "Player " + username + " is " + (isOnline ? "online" : "offline"));
            if (isOnline || !newCommands) {
                if (!newCommands) {
                    toCheck.addAll(parsedResponses);
                    toCheckIds.addAll(parsedResponses.stream().map(ParsedResponse::commandId).collect(Collectors.toSet()));
                } else {
                    for (ParsedResponse parsedResponse : parsedResponses) {
                        if (!shouldExecute(parsedResponse)) {
                            addNewCommand(username, parsedResponse.command(), parsedResponse.commandId());
                            continue;
                        }
                        toCheck.add(parsedResponse);
                        toCheckIds.add(parsedResponse.commandId());
                    }
                }
                continue;
            }
            for (ParsedResponse parsedResponse : parsedResponses) {
                addNewCommand(username, parsedResponse.command(), parsedResponse.commandId());
            }
        }
        if (!MineStoreCommon.version().requires(3, 2, 5)) {
            for (ParsedResponse parsedResponse : toCheck) {
                if (handleOfflineCommand(parsedResponse)) {
                    continue;
                }
                addNewCommand(parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId());
            }
            return;
        }
        if (toCheck.isEmpty()) {
            plugin.debug(this.getClass(), "Did not find any commands to check");
            for (ParsedResponse parsedResponse : parsedCommands) {
                plugin.debug(this.getClass(), "Possible options: " + parsedResponse.command() + " with id: " + parsedResponse.commandId() + " for player: " + parsedResponse.username());
            }
            return;
        }
        plugin.webListener().checkCommands(toCheckIds).thenAcceptAsync(checkResponses -> {
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
            for (ParsedResponse parsedResponse : toCheck) {
                if (!successIds.contains(parsedResponse.commandId()) && errors.containsKey(parsedResponse.commandId())) {
                    plugin.debug(this.getClass(), "Command " + parsedResponse.command() + " with id " + parsedResponse.commandId() + " failed to execute. Error: " + errors.get(parsedResponse.commandId()));
                    continue;
                }
                plugin.webListener().postExecuted(String.valueOf(parsedResponse.commandId()));
                handleOfflineCommand(parsedResponse);
            }
        });
    }

    public boolean shouldExecute(ParsedResponse parsedResponse) {
        MineStoreExecuteIntentEvent intent = new MineStoreExecuteIntentEvent(parsedResponse.commandType(), parsedResponse.username(), parsedResponse.command(), parsedResponse.commandId());
        intent.call();
        return !intent.isCancelled();
    }

    private boolean handleOfflineCommand(ParsedResponse parsedResponse) {
        String command = parsedResponse.command();
        String username = parsedResponse.username();
        int requestId = parsedResponse.commandId();
        if (plugin.pluginConfig().getKey("command-execution-logging").getAsBoolean()) {
            plugin.log("Executing command: " + command);
        }
        MineStoreExecuteEvent event = new MineStoreExecuteEvent(username, command, requestId);
        event.call();
        Registries.COMMAND_EXECUTER.get().execute(event);
        return true;
    }

    public void init() {
        commands = plugin.commandDumper().load();
        newCommands = plugin.newCommandDumper().load();
    }
}
