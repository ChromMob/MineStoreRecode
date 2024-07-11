package me.chrommob.minestore.addons.events.types;

import me.chrommob.minestore.addons.events.MineStoreEvent;

public class CustomEventExample extends MineStoreEvent {
    private final String message;

    public CustomEventExample(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
