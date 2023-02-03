package me.chrommob.minestore.common.commandGetters.dataTypes;

public class ParsedResponse {
    private final TYPE type;
    private final COMMAND_TYPE commandType;
    private final String command;
    private final String username;
    private final int commandId;
    private final String authId;


    public ParsedResponse(TYPE type, COMMAND_TYPE commandType, String command, String username, int commandId) {
        this.type = type;
        this.commandType = commandType;
        this.command = command;
        this.username = username;
        this.commandId = commandId;
        this.authId = null;
    }

    public ParsedResponse(TYPE type, String username, String authId) {
        this.type = type;
        this.commandType = null;
        this.command = null;
        this.username = username;
        this.commandId = -1;
        this.authId = authId;
    }

    public enum TYPE {
        AUTH,
        COMMAND
    }

    public enum COMMAND_TYPE {
        ONLINE,
        OFFLINE
    }

    public TYPE type() {
        return type;
    }

    public COMMAND_TYPE commandType() {
        return commandType;
    }

    public String command() {
        return command;
    }

    public String username() {
        return username;
    }

    public int commandId() {
        return commandId;
    }

    public String authId() {
        return authId;
    }
}
