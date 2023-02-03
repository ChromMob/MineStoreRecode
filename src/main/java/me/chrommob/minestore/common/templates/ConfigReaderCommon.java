package me.chrommob.minestore.common.templates;

import java.io.File;

public interface ConfigReaderCommon {
    File dataFolder();

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

    boolean debug();
}
