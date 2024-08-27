package me.chrommob.minestore.addons.events;

import me.chrommob.minestore.addons.MineStoreAddon;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MineStoreEventBus {

    private static final Map<MineStoreAddon, AddonListeners> listeners = new ConcurrentHashMap<>();


    public static <T extends MineStoreEvent> void registerListener(MineStoreAddon addon, Class<T> event, Consumer<T> listener) {
        listeners.computeIfAbsent(addon, k -> new AddonListeners(addon))
                .registerListener(event, listener);
    }

    public static void unregisterListeners(MineStoreAddon addon) {
        listeners.remove(addon);
    }

    public static List<String> getRegisteredEvents(MineStoreAddon addon) {
        return listeners.get(addon).listeners.keySet().stream().map(Class::getSimpleName).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    static <T extends MineStoreEvent> void fireEvent(T event) {
        for (AddonListeners addonListeners : listeners.values()) {
            if (addonListeners != null) {
                Set<Consumer<? extends MineStoreEvent>> consumers = addonListeners.listeners.get(event.getClass());
                if (consumers != null) {
                    for (Consumer<? extends MineStoreEvent> consumer : consumers) {
                        ((Consumer<T>) consumer).accept(event);
                    }
                }
            }
        }
    }

    static class AddonListeners {
        private final Map<Class<? extends MineStoreEvent>, Set<Consumer<? extends MineStoreEvent>>> listeners = new ConcurrentHashMap<>();
        private final MineStoreAddon addon;
        public AddonListeners(MineStoreAddon addon) {
            this.addon = addon;
        }

        public void registerListener(Class<? extends MineStoreEvent> event, Consumer<? extends MineStoreEvent> listener) {
            listeners.computeIfAbsent(event, k -> new HashSet<>())
                    .add(listener);
        }
    }
}
