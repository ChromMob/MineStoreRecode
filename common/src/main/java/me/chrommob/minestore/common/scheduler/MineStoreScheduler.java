package me.chrommob.minestore.common.scheduler;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MineStoreScheduler {
    private static final int DELAY = 100;
    private final Set<MineStoreScheduledTask> tasks = new HashSet<>();
    private final Queue<MineStoreScheduledTask> toExecute = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public MineStoreScheduler() {
        start();
    }

    public void addTask(MineStoreScheduledTask task) {
        tasks.add(task);
        task.lastExecuteAt = 0;
        task.nextExecuteAt = System.currentTimeMillis();
    }

    public void removeTask(MineStoreScheduledTask mineStoreScheduledTask) {
        tasks.remove(mineStoreScheduledTask);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            Set<MineStoreScheduledTask> tasks = new HashSet<>(this.tasks);
            for (MineStoreScheduledTask task : tasks) {
                if (task.shouldRun()) {
                    toExecute.add(task);
                    task.markExecuted();
                }
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(() -> {
            MineStoreScheduledTask task;
            while ((task = toExecute.poll()) != null) {
                task.runnable.accept(task);
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdown();
    }
}
