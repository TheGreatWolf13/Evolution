package tgw.evolution.patches.obj;

import net.minecraft.server.packs.resources.SimpleReloadInstance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class ReloadExecutor2 implements Executor {

    private final Executor executor;
    private final SimpleReloadInstance simpleReloadInstance;

    public ReloadExecutor2(Executor executor, SimpleReloadInstance simpleReloadInstance) {
        this.executor = executor;
        this.simpleReloadInstance = simpleReloadInstance;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        ++this.simpleReloadInstance.startedReloads;
        this.executor.execute(() -> {
            command.run();
            ++this.simpleReloadInstance.finishedReloads;
        });
    }
}
