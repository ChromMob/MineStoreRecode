package me.chrommob.minestore.common.commandHolder.type;

import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;

public class StoredCommand {
    private final String command;
    private final int requestId;

    public StoredCommand(String command, int requestId) {
        this.command = command;
        this.requestId = requestId;
    }

    public ParsedResponse toParsedResponse(String username) {
        return new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, command, username, requestId);
    }

    public String command() {
        return command;
    }

    public int requestId() {
        return requestId;
    }
}
