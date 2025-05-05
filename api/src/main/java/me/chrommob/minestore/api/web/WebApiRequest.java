package me.chrommob.minestore.api.web;

import me.chrommob.minestore.api.generic.ParamBuilder;

public class WebApiRequest <T> {
    public enum Type {
        GET,
        POST
    }
    private final Type type;
    private final String path;
    private final ParamBuilder paramBuilder;
    private final Class<T> clazz;

    public WebApiRequest(String path, Type type, Class<T> clazz) {
        this(path, type, null, clazz);
    }

    public WebApiRequest(String path, Type type, ParamBuilder paramBuilder, Class<T> clazz) {
        this.type = type;
        this.path = path;
        this.paramBuilder = paramBuilder;
        this.clazz = clazz;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getParams() {
        if (paramBuilder == null) {
            return "";
        }
        return paramBuilder.build();
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
