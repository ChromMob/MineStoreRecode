package me.chrommob.minestore.common.commandGetters.dataTypes;

public class ParsedResponse {
    private final TYPE type;
    private final COMMAND_TYPE commandType;
    private final String command;
    private final String username;
    private final int requestId;
    private final String authId;


    public ParsedResponse(TYPE type, COMMAND_TYPE commandType, String command, String username, int requestId) {
        this.type = type;
        this.commandType = commandType;
        this.command = command;
        this.username = username;
        this.requestId = requestId;
        this.authId = null;
    }

    public ParsedResponse(TYPE type, String username, String authId, int requestId) {
        this.type = type;
        this.commandType = null;
        this.command = null;
        this.username = username;
        this.requestId = requestId;
        this.authId = authId;
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
        return requestId;
    }

    public String authId() {
        return authId;
    }

    public enum TYPE {
        AUTH,
        COMMAND
    }

    public enum COMMAND_TYPE {
        ONLINE,
        OFFLINE
    }

    public ParsedResponse clone() {
        return new ParsedResponse(type, commandType, command, username, requestId);
    }
}
