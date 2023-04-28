package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;
import me.chrommob.minestore.common.interfaces.commands.CommandStorageInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandStorage implements CommandStorageInterface {
    private Map<String, List<String>> commands;

    private void remove(String username) {
        MineStoreCommon.getInstance().debug("Removing " + username + " from command storage");
        commands.remove(username);
        MineStoreCommon.getInstance().commandDumper().update(commands);
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

    @Override
    public void onPlayerJoin(String username) {
        if (commands.containsKey(username)) {
            MineStoreCommon.getInstance().debug("Executing commands for " + username);
            commands.get(username).forEach(MineStoreCommon.getInstance().commandExecuter()::execute);
            remove(username);
        }
    }

    @Override
    public void listener(ParsedResponse command) {
        if (command.commandType() == ParsedResponse.COMMAND_TYPE.ONLINE) {
            handleOnlineCommand(command.command(), command.username());
        } else {
            handleOfflineCommand(command.command());
        }
    }

    private void handleOnlineCommand(String command, String username) {
        boolean isOnline = MineStoreCommon.getInstance().commandExecuter().isOnline(username);
        if (isOnline) {
            MineStoreCommon.getInstance().commandExecuter().execute(command);
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
    }
}
