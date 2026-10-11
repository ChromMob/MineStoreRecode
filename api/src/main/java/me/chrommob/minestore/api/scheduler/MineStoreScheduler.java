package me.chrommob.minestore.api.scheduler;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MineStoreScheduler {
    private static final int DELAY = 100;
    private static final long FAILED_TASK_RETRY_DELAY = 10000;
    private final Set<MineStoreScheduledTask> delayed = ConcurrentHashMap.newKeySet();
    private final Set<MineStoreScheduledTask> tasks = ConcurrentHashMap.newKeySet();
    private final Queue<MineStoreScheduledTask> toExecute = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public MineStoreScheduler() {
        start();
    }

    public void runDelayed(MineStoreScheduledTask task) {
        delayed.add(task);
    }

    public void addTask(MineStoreScheduledTask task) {
        tasks.add(task);
        task.lastExecuteAt = 0;
    }

    public void removeTask(MineStoreScheduledTask mineStoreScheduledTask) {
        tasks.remove(mineStoreScheduledTask);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                Set<MineStoreScheduledTask> tasks = new HashSet<>(this.tasks);
                for (MineStoreScheduledTask task : tasks) {
                    if (task.shouldRun()) {
                        toExecute.add(task);
                        task.markExecuted();
                    }
                }
                Set<MineStoreScheduledTask> delayed = new HashSet<>(this.delayed);
                for (MineStoreScheduledTask task : delayed) {
                    if (task.shouldRun()) {
                        toExecute.add(task);
                        this.delayed.remove(task);
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(() -> {
            MineStoreScheduledTask task;
            while ((task = toExecute.poll()) != null) {
                try {
                    task.runnable.accept(task);
                } catch (Throwable throwable) {
                    logError("Error in executing: " + task.name + " scheduler", throwable);
                    // A repeating task that threw before rescheduling itself would otherwise never run again.
                    if (task.lastExecuteAt == task.nextExecuteAt) {
                        task.delay(FAILED_TASK_RETRY_DELAY);
                    }
                }
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);
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

    public void stop() {
        executor.shutdown();
    }
}
