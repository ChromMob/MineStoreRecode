package me.chrommob.minestore.api.event;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MineStoreEventBus {

    private static final Map<MineStoreAddon, AddonListeners> listeners = new ConcurrentHashMap<>();

    /**
     * Registers a listener for the specified event.
     *
     * @param addon The addon that is listening for the event.
     * @param event The event that the addon is listening for.
     * @param listener The listener that will be called when the event is fired.
     */
    public static <T extends MineStoreEvent> void registerListener(MineStoreAddon addon, Class<T> event, Consumer<T> listener) {
        listeners.computeIfAbsent(addon, k -> new AddonListeners(addon))
                .registerListener(event, listener);
    }

    /**
     * Unregisters all listeners for the specified addon.
     *
     * @param addon The addon that is no longer listening for events.
     */
    public static void unregisterListeners(MineStoreAddon addon) {
        listeners.remove(addon);
    }

    /**
     * Gets a list of all events that the specified addon is listening for.
     *
     * @param addon The addon that is listening for events.
     * @return A list of event names.
     */
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
                        // Isolate listeners so one failing addon cannot stop the others (e.g. command delivery on join).
                        try {
                            ((Consumer<T>) consumer).accept(event);
                        } catch (Throwable throwable) {
                            logError("Error while passing " + event.getClass().getSimpleName() + " to " + addonListeners.addon.getName(), throwable);
                        }
                    }
                }
            }
        }
    }

    private static void logError(String message, Throwable throwable) {
        LoggerCommon logger = Registries.LOGGER.get();
        if (logger == null) {
            System.err.println(message);
            throwable.printStackTrace();
            return;
        }
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        logger.log(message + System.lineSeparator() + stackTrace);
    }

    static class AddonListeners {
        private final Map<Class<? extends MineStoreEvent>, Set<Consumer<? extends MineStoreEvent>>> listeners = new ConcurrentHashMap<>();
        private final MineStoreAddon addon;
        public AddonListeners(MineStoreAddon addon) {
            this.addon = addon;
        }

        public void registerListener(Class<? extends MineStoreEvent> event, Consumer<? extends MineStoreEvent> listener) {
            // Concurrent, listeners are registered while events fire on other threads.
            listeners.computeIfAbsent(event, k -> ConcurrentHashMap.newKeySet())
                    .add(listener);
        }

        @Override
        public String toString() {
           StringBuilder builder = new StringBuilder();
           builder.append("Addon: ");
           builder.append(addon.getName());
           builder.append("\n");
           builder.append("Listeners: ");
           for (Class<? extends MineStoreEvent> event : listeners.keySet()) {
               builder.append(event.getSimpleName());
               builder.append(", ");
           }
            return builder.toString();
        }
    }
}
