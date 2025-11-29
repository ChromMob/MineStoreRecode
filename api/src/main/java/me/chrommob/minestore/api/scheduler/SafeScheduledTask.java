package me.chrommob.minestore.api.scheduler;

import me.chrommob.minestore.api.Registries;

import java.util.Arrays;

public class SafeScheduledTask {
    public static MineStoreScheduledTask wrap(String baseName, SafeTaskHandler handler) {
        return new MineStoreScheduledTask(baseName, (task) -> {
            long delay = 10000; // fallback delay
            try {
                delay = handler.run();
            } catch (Exception e) {
                Registries.LOGGER.get().log("Error in executing: " + baseName + " scheduler");
                if (e.getMessage() != null) {
                    Registries.LOGGER.get().log(e.getMessage());
                }
                if (e.getStackTrace() != null) {
                    Registries.LOGGER.get().log(Arrays.toString(e.getStackTrace()));
                }
                if (e.getCause() != null) {
                    Registries.LOGGER.get().log(e.getCause().toString());
                }
            } finally {
                task.delay(delay);
            }
        });
    }

    @FunctionalInterface
    public interface SafeTaskHandler {
        long run() throws Exception;
    }
}
