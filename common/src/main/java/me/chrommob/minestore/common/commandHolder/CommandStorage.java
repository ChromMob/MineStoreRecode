package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.commandHolder.type.StoredCommand;
import me.chrommob.minestore.common.interfaces.commands.CommandStorageInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandStorage implements CommandStorageInterface {
    private Map<String, List<String>> commands;
    private Map<String, List<StoredCommand>> newCommands;

    private void remove(String username) {
        MineStoreCommon.getInstance().debug("Removing " + username + " from command storage");
        commands.remove(username);
        MineStoreCommon.getInstance().commandDumper().update(commands);
    }

    private void removeNewCommand(String username) {
        MineStoreCommon.getInstance().debug("Removing " + username + " from new command storage");
        newCommands.remove(username);
        MineStoreCommon.getInstance().newCommandDumper().update(newCommands);
    }

    private void add(String username, String command) {
        MineStoreCommon.getInstance().debug("Adding " + command + " to " + username + " in command storage");
        if (commands.containsKey(username)) {
            commands.get(username).add(command);
        } else {
            commands.put(username, new ArrayList<>(Collections.singletonList(command)));
        }
        MineStoreCommon.getInstance().commandDumper().update(commands);
    }

    private void addNewCommand(String username, String command, int requestId) {
        MineStoreCommon.getInstance().debug("Adding " + command + " to " + username + " in new command storage");
        if (newCommands.containsKey(username)) {
            newCommands.get(username).add(new StoredCommand(command, requestId));
        } else {
            newCommands.put(username, new ArrayList<>(Collections.singletonList(new StoredCommand(command, requestId))));
        }
        MineStoreCommon.getInstance().newCommandDumper().update(newCommands);
    }

    @Override
    public void onPlayerJoin(String username) {
        if (MineStoreCommon.getInstance().version().requires("3.0.0")) {
            if (newCommands.containsKey(username)) {
                MineStoreCommon.getInstance().debug("Executing new commands for " + username);
                newCommands.get(username).forEach(storedCommand -> {
                    MineStoreCommon.getInstance().commandExecuter().execute(storedCommand.command());
                    MineStoreCommon.getInstance().commandGetter().postExecuted(String.valueOf(storedCommand.requestId()));
                });
                removeNewCommand(username);
            }
            return;
        }
        if (commands.containsKey(username)) {
            MineStoreCommon.getInstance().debug("Executing commands for " + username);
            commands.get(username).forEach(MineStoreCommon.getInstance().commandExecuter()::execute);
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
        boolean isOnline = MineStoreCommon.getInstance().commandExecuter().isOnline(username);
        if (isOnline) {
            MineStoreCommon.getInstance().commandExecuter().execute(command);
            return;
        }
        if (MineStoreCommon.getInstance().version().requires("3.0.0")) {
            addNewCommand(username, command, requestId);
            return;
        }
        add(username, command);
    }

    private void handleOfflineCommand(String command) {
        MineStoreCommon.getInstance().commandExecuter().execute(command);
    }

    @Override
    public void init() {
        commands = MineStoreCommon.getInstance().commandDumper().load();
        newCommands = MineStoreCommon.getInstance().newCommandDumper().load();
    }
}
