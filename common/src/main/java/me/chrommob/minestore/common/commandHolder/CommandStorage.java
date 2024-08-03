package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.commands.CommandStorageInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandStorage implements CommandStorageInterface {
    private final MineStoreCommon plugin;
    public CommandStorage(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    private Map<String, List<String>> commands;
    private Map<String, List<StoredCommand>> newCommands;

    private void remove(String username) {
        plugin.debug("Removing " + username + " from command storage");
        commands.remove(username);
        plugin.commandDumper().update(commands);
    }

    private void removeNewCommand(String username) {
        plugin.debug("Removing " + username + " from new command storage");
        newCommands.remove(username);
        plugin.newCommandDumper().update(newCommands);
    }

    private void add(String username, String command) {
        plugin.debug("Adding " + command + " to " + username + " in command storage");
        if (commands.containsKey(username)) {
            commands.get(username).add(command);
        } else {
            commands.put(username, new ArrayList<>(Collections.singletonList(command)));
        }
        plugin.commandDumper().update(commands);
    }

    private void addNewCommand(String username, String command, int requestId) {
        plugin.debug("Adding " + command + " to " + username + " in new command storage");
        if (newCommands.containsKey(username)) {
            newCommands.get(username).add(new StoredCommand(command, requestId));
        } else {
            newCommands.put(username, new ArrayList<>(Collections.singletonList(new StoredCommand(command, requestId))));
        }
        plugin.newCommandDumper().update(newCommands);
    }

    @Override
    public void onPlayerJoin(String username) {
        if (MineStoreCommon.version().requires("3.0.0")) {
            if (newCommands.containsKey(username)) {
                plugin.debug("Executing new commands for " + username);
                newCommands.get(username).forEach(storedCommand -> {
                    if ((boolean) plugin.configReader().get(ConfigKey.COMMAND_LOGGING)) {
                        plugin.log("Executing command: " + storedCommand.command());
                    }
                    plugin.commandExecuter().execute(storedCommand.command());
                    plugin.commandGetter().postExecuted(String.valueOf(storedCommand.requestId()));
                });
                removeNewCommand(username);
            }
            return;
        }
        if (commands.containsKey(username)) {
            plugin.debug("Executing commands for " + username);
            commands.get(username).forEach(command -> {
                if ((boolean) plugin.configReader().get(ConfigKey.COMMAND_LOGGING)) {
                    plugin.log("Executing command: " + command);
                }
                plugin.commandExecuter().execute(command);
            });
            remove(username);
        }
    }

    @Override
    public void listener(ParsedResponse command) {
        if (command.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE) {
            handleOnlineCommand(command.command(), command.username(), command.commandId());
        } else {
            handleOfflineCommand(command.command());
        }
    }

    private void handleOnlineCommand(String command, String username, int requestId) {
        boolean isOnline = plugin.commandExecuter().isOnline(username);
        if (isOnline) {
            if ((boolean) plugin.configReader().get(ConfigKey.COMMAND_LOGGING)) {
                plugin.log("Executing command: " + command);
            }
            plugin.commandExecuter().execute(command);
            if (MineStoreCommon.version().requires("3.0.0")) {
                plugin.commandGetter().postExecuted(String.valueOf(requestId));
            }
            return;
        }
        if (MineStoreCommon.version().requires("3.0.0")) {
            addNewCommand(username, command, requestId);
            return;
        }
        add(username, command);
    }

    private void handleOfflineCommand(String command) {
        if ((boolean) plugin.configReader().get(ConfigKey.COMMAND_LOGGING)) {
            plugin.log("Executing command: " + command);
        }
        plugin.commandExecuter().execute(command);
    }

    @Override
    public void init() {
        commands = plugin.commandDumper().load();
        newCommands = plugin.newCommandDumper().load();
    }
}
