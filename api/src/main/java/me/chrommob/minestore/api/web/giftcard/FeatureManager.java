package me.chrommob.minestore.api.web.giftcard;

import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiRequest;
import me.chrommob.minestore.api.web.Wrapper;

import java.util.function.Function;

public class FeatureManager {
    private final Wrapper<Function<WebApiRequest<?>, Result<?, ? extends Exception>>> requestHandler;

    public FeatureManager(Wrapper<Function<WebApiRequest<?>, Result<?, ? extends Exception>>> requestHandler) {
        this.requestHandler = requestHandler;
    }

    @SuppressWarnings("unchecked")
    protected <V> Result<V, Exception> request(WebApiRequest<V> request) {
        if (requestHandler.get() == null) {
            return new Result<>(null, new Exception("Handler is not initialized"));
        }
        return (Result<V, Exception>) requestHandler.get().apply(request);
    }
}
