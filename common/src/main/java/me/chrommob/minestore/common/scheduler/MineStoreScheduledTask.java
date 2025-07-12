package me.chrommob.minestore.common.scheduler;

import java.util.UUID;
import java.util.function.Consumer;

public class MineStoreScheduledTask {
    protected String name;
    protected Consumer<MineStoreScheduledTask> runnable;
    protected long lastExecuteAt;
    protected long nextExecuteAt;

    public MineStoreScheduledTask(String name, Consumer<MineStoreScheduledTask> runnable) {
        this.name = name + "-" + UUID.randomUUID();
        this.runnable = runnable;
    }

    public MineStoreScheduledTask(String name, Runnable runnable, long delay) {
        this.runnable = (task) -> {
            runnable.run();
            task.delay(delay);
        };
        this.name = name + "-" + UUID.randomUUID();
    }

    public void delay(long delay) {
        this.nextExecuteAt = System.currentTimeMillis() + delay;
    }
}
