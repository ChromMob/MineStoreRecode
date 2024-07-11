package me.chrommob.minestore.addons.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MineStoreEventBus {

    private static final Map<Class<? extends MineStoreEvent>, Set<Consumer<? extends MineStoreEvent>>> listeners = new ConcurrentHashMap<>();

    public static <T extends MineStoreEvent> void registerListener(Class<T> event, Consumer<T> listener) {
        listeners.computeIfAbsent(event, k -> ConcurrentHashMap.newKeySet())
                .add(listener);
    }

    @SuppressWarnings("unchecked")
    static <T extends MineStoreEvent> void fireEvent(T event) {
        Set<Consumer<? extends MineStoreEvent>> consumers = listeners.get(event.getClass());
        if (consumers != null) {
            for (Consumer<? extends MineStoreEvent> consumer : consumers) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }
}
