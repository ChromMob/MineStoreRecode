package me.chrommob.minestore.common.commandHolder.type;

public class StoredCommand {
    private final String command;
    private final int requestId;

    public StoredCommand(String command, int requestId) {
        this.command = command;
        this.requestId = requestId;
    }

    public String command() {
        return command;
    }

    public int requestId() {
        return requestId;
    }
}
