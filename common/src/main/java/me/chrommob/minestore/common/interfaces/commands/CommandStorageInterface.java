package me.chrommob.minestore.common.interfaces.commands;

import me.chrommob.minestore.common.commandGetters.dataTypes.ParsedResponse;

public interface CommandStorageInterface {
    void onPlayerJoin(String username);
    void listener(ParsedResponse command);
    void init();
}
