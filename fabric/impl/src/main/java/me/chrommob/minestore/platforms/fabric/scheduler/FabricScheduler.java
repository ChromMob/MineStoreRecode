package me.chrommob.minestore.platforms.fabric.scheduler;

import me.chrommob.minestore.api.interfaces.scheduler.CommonScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FabricScheduler implements CommonScheduler {
    // Drained with poll(), so a task added from another thread during a tick is never lost.
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public FabricScheduler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            // Only what was queued before this tick runs now, tasks added meanwhile wait for the next tick.
            int count = tasks.size();
            for (int i = 0; i < count; i++) {
                Runnable task = tasks.poll();
                if (task == null) {
                    break;
                }
                // One failing task must not drop the others, e.g. purchased commands queued in the same tick.
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void run(Runnable runnable) {
        this.tasks.add(runnable);
    }

}
