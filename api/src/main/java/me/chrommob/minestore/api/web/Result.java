package me.chrommob.minestore.api.web;

public class Result<V, E> {
    private final V value;
    private final E error;

    public Result(V value, E error) {
        this.value = value;
        this.error = error;
    }

    public V value() {
        return value;
    }

    public E error() {
        return error;
    }
}
