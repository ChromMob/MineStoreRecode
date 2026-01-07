package me.chrommob.minestore.api.web;

import java.util.function.Function;

public class FeatureManager {
    private final Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler;

    public FeatureManager(Wrapper<Function<WebRequest<?>, Result<?, WebContext>>> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @SuppressWarnings("unchecked")
    protected <V> Result<V, WebContext> request(WebRequest<V> request) {
        if (requestHandler.get() == null) {
            return new Result<>(null, new WebContext("Handler is not initialized"), true);
        }
        return (Result<V, WebContext>) requestHandler.get().apply(request);
    }
}
