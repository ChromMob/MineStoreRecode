package me.chrommob.minestore.common.config;

public class Configuration {
    private String location;
    private Object defaultValue;
    public Configuration(String location, Object defaultValue) {
        this.location = location;
        this.defaultValue = defaultValue;
    }


    public String getLocation() {
        return location;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
