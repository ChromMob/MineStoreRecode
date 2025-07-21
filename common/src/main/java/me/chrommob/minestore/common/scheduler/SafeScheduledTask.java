package me.chrommob.minestore.common.scheduler;

import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.common.MineStoreCommon;

public class SafeScheduledTask {
    public static MineStoreScheduledTask wrap(Class<?> baseClass, String baseName, MineStoreCommon plugin, SafeTaskHandler handler) {
        return new MineStoreScheduledTask(baseName, (task) -> {
            long delay = 10000; // fallback delay
            try {
                delay = handler.run();
            } catch (Exception e) {
                plugin.log("ERROR IN EXECUTING " + baseName + " PLEASE CONTACT SUPPORT IMMEDIATELY");
                plugin.debug(baseClass, e);
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
