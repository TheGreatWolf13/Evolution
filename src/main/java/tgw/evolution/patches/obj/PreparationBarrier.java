package tgw.evolution.patches.obj;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PreparationBarrier implements PreparableReloadListener.PreparationBarrier {

    private final Executor executor;
    private final CompletableFuture future;
    private final PreparableReloadListener preparableReloadListener;
    private final SimpleReloadInstance simpleReloadInstance;

    public PreparationBarrier(CompletableFuture future, Executor executor, PreparableReloadListener preparableReloadListener, SimpleReloadInstance simpleReloadInstance) {
        this.future = future;
        this.executor = executor;
        this.preparableReloadListener = preparableReloadListener;
        this.simpleReloadInstance = simpleReloadInstance;
    }

    @Override
    public <T> CompletableFuture<T> wait(T object) {
        this.executor.execute(() -> {
            this.simpleReloadInstance.preparingListeners.remove(this.preparableReloadListener);
            if (this.simpleReloadInstance.preparingListeners.isEmpty()) {
                this.simpleReloadInstance.allPreparations.complete(Unit.INSTANCE);
            }
        });
        return this.simpleReloadInstance.allPreparations.thenCombine(this.future, (unit, object2) -> object);
    }
}
