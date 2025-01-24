package me.chrommob.minestore.api.interfaces;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Registry<T> {
    private Set<Consumer<T>> listeners = new HashSet<>();
    private T value = null;

    public Registry() {
    }

    public Registry(T value) {
        set(value);
    }

    public void set(T value) {
        this.value = value;
        for (Consumer<T> listener : listeners) {
            listener.accept(value);
        }
    }

    public T get() {
        return value;
    }

    public void listen(Consumer<T> listener) {
        this.listeners.add(listener);
        if (value != null) {
            listener.accept(value);
        }
    }
}
