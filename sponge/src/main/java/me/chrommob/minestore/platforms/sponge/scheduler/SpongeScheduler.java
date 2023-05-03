package me.chrommob.minestore.platforms.sponge.scheduler;

import me.chrommob.minestore.common.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.platforms.sponge.MineStoreSponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;

public class SpongeScheduler implements CommonScheduler {
    private MineStoreSponge plugin;
    private SpongeExecutorService executorService;

    public SpongeScheduler(MineStoreSponge plugin) {
        this.plugin = plugin;
        this.executorService = Sponge.getScheduler().createSyncExecutor(plugin);
    }

    @Override
    public void run(Runnable runnable) {
        executorService.execute(runnable);
    }
}
