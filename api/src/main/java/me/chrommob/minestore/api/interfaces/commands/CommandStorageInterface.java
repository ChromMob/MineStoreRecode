package me.chrommob.minestore.api.interfaces.commands;

public interface CommandStorageInterface {
    void onPlayerJoin(String username);
    void listener(ParsedResponse command);
    void init();
}
