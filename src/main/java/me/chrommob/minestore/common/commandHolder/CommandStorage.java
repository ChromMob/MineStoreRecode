package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandStorage {
    private Map<String, String> commands;
    public CommandStorage() {
        commands = new ConcurrentHashMap<>();
    }


    public void listener(ParsedResponse command) {
        if (command.type() == ParsedResponse.TYPE.AUTH) {
            return;
        }
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
        commands.put(username, command);
    }

    private void handleOfflineCommand(String command) {
        MineStoreCommon.getInstance().commandExecuter().execute(command);
    }
}
