package me.chrommob.minestore.common.commandHolder.type;

import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;

import java.util.UUID;

/**
 * A command from the store queue, saved from the moment it is received until the store has confirmed its execution.
 */
public class PendingCommand {
    // Own id, the store's command id can be reused (e.g. when a command is resent from the panel).
    private final String key;
    private final int id;
    private final String username;
    private final String command;
    private final boolean onlineRequired;
    // The store confirmed that it handed the command out.
    private boolean delivered;
    // The command ran here, but the store has not confirmed that yet.
    private boolean executed;

    public PendingCommand(int id, String username, String command, boolean onlineRequired, boolean delivered) {
        this.key = UUID.randomUUID().toString();
        this.id = id;
        this.username = username;
        this.command = command;
        this.onlineRequired = onlineRequired;
        this.delivered = delivered;
    }

    public String key() {
        return key;
    }

    public int id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String command() {
        return command;
    }

    public boolean onlineRequired() {
        return onlineRequired;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void markDelivered() {
        delivered = true;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void markExecuted() {
        executed = true;
    }

    public ParsedResponse toParsedResponse() {
        return new ParsedResponse(ParsedResponse.TYPE.COMMAND, onlineRequired ? ParsedResponse.COMMAND_TYPE.ONLINE : ParsedResponse.COMMAND_TYPE.OFFLINE, command, username, id);
    }
}
