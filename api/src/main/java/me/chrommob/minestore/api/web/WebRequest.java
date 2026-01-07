package me.chrommob.minestore.api.web;

import com.google.gson.reflect.TypeToken;
import me.chrommob.minestore.api.generic.ParamBuilder;

import java.util.HashMap;
import java.util.Map;

public class WebRequest<T> {
    public enum Type {
        GET,
        POST,
        DELETE
    }

    private final String customUrl;
    private final boolean requiresApiKey;
    private final Type type;
    private final String path;
    private final ParamBuilder paramBuilder;
    private final Class<T> clazz;
    private final TypeToken<T> typeToken;
    private final byte[] body;
    private final Map<String, String> headers;

    public static class Builder<T> {
        private String customUrl = null;
        private Type type = Type.GET;
        private String path = null;
        private ParamBuilder paramBuilder = null;
        private final Class<T> clazz;
        private final TypeToken<T> typeToken;
        private byte[] body;
        private final Map<String, String> headers = new HashMap<>();
        private boolean requiresApiKey = false;

        public Builder(Class<T> clazz) {
            this.clazz = clazz;
            this.typeToken = null;
        }
        public Builder(TypeToken<T> typeToken) {
            this.typeToken = typeToken;
            this.clazz = null;
        }

        public Builder<T> customUrl(String customUrl) {
            this.customUrl = customUrl;
            return this;
        }
        public Builder<T> type(Type type) {
            this.type = type;
            return this;
        }
        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }
        public Builder<T> paramBuilder(ParamBuilder paramBuilder) {
            this.paramBuilder = paramBuilder;
            return this;
        }
        public Builder<T> header(String key, String value) {
            headers.put(key, value);
            return this;
        }
        public Builder<T> requiresApiKey(boolean requiresApiKey) {
            this.requiresApiKey = requiresApiKey;
            return this;
        }
        public Builder<T> body(byte[] body) {
            this.body = body;
            return this;
        }
        public Builder<T> strBody(String body) {
            this.body = body.getBytes();
            return this;
        }
        public WebRequest<T> build() {
            return new WebRequest<>(customUrl, path, type, paramBuilder, clazz, typeToken, requiresApiKey, body, headers);
        }
    }

    public WebRequest(String customUrl, String path, Type type, ParamBuilder paramBuilder, Class<T> clazz, TypeToken<T> typeToken, boolean requiresApiKey, byte[] body, Map<String, String> headers) {
        this.customUrl = customUrl;
        this.type = type;
        this.path = path;
        this.paramBuilder = paramBuilder;
        this.clazz = clazz;
        this.typeToken = typeToken;
        this.requiresApiKey = requiresApiKey;
        this.body = body;
        this.headers = headers;
    }

    public String getCustomUrl() {
        return customUrl;
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

    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    public TypeToken<T> getTypeToken() {
        return typeToken;
    }

    public byte[] getBody() {
        return body;
    }
}
