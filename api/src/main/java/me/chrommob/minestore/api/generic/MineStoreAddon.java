package me.chrommob.minestore.api.generic;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;

import java.util.Collections;
import java.util.List;

public abstract class MineStoreAddon {

    private ConfigWrapper configWrapper;

    public void setConfigWrapper(ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    abstract public void onEnable();
    abstract public String getName();

    public List<ConfigKey> getConfigKeys() {
        return Collections.emptyList();
    }

    public ConfigKey getConfigKey(String key) {
        if (configWrapper == null) {
            return null;
        }
        return configWrapper.getKey(key);
    }
}
