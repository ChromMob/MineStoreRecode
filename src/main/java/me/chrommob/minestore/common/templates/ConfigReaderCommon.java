package me.chrommob.minestore.common.templates;

public interface ConfigReaderCommon {
    public enum COMMAND_MODE {
        WEBLISTENER,
        WEBSOCKET
    }

    void init();

    void reload();

    COMMAND_MODE commandMode();

    String storeUrl();

    boolean secretEnabled();

    String secretKey();
}
