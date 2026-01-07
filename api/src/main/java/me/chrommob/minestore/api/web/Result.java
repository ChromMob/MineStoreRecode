package me.chrommob.minestore.api.web;

public class Result<V, E> {
    private final V value;
    private final E context;
    private final boolean isError;

    public Result(V value, E context, boolean isError) {
        this.value = value;
        this.context = context;
        this.isError = isError;
    }

    public V value() {
        return value;
    }

    public E context() {
        return context;
    }

    public boolean isError() {
        return isError;
    }
}
