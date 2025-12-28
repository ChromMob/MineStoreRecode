package me.chrommob.minestore.api.scheduler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MineStoreScheduledTask {
    protected String name;
    protected Consumer<MineStoreScheduledTask> runnable;
    protected long lastExecuteAt;
    protected long nextExecuteAt = System.nanoTime();

    public MineStoreScheduledTask(String name, Consumer<MineStoreScheduledTask> runnable) {
        this.name = name + "-" + UUID.randomUUID();
        this.runnable = runnable;
    }

    public MineStoreScheduledTask(String name, Runnable runnable, long delay) {
        this.runnable = (task) -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                task.delay(delay);
            }
        };
        this.name = name + "-" + UUID.randomUUID();
        delay(delay);
    }


    public void delay(long delayMillis) {
        this.nextExecuteAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis);
    }

    public boolean shouldRun() {
        return System.nanoTime() >= nextExecuteAt && lastExecuteAt != nextExecuteAt;
    }

    public void markExecuted() {
        this.lastExecuteAt = this.nextExecuteAt;
    }
}