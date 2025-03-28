package me.chrommob.minestore.platforms.fabric.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.chrommob.minestore.api.interfaces.scheduler.CommonScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricScheduler implements CommonScheduler {
    private Set<Runnable> tasks = ConcurrentHashMap.newKeySet();

    public FabricScheduler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Set<Runnable> running = new HashSet<>(this.tasks);
            this.tasks.clear();
            running.forEach(Runnable::run);
        });
    }

    @Override
    public void run(Runnable runnable) {
        this.tasks.add(runnable);
    }

}
