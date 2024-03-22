package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.obj.PreparationBarrier;
import tgw.evolution.patches.obj.ReloadExecutor;
import tgw.evolution.patches.obj.ReloadExecutor2;
import tgw.evolution.resources.ResourceManagerHelperImpl;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OHashSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(SimpleReloadInstance.class)
public abstract class Mixin_CF_SimpleReloadInstance<S> implements ReloadInstance {

    @Mutable @Shadow @Final @RestoreFinal public CompletableFuture<Unit> allPreparations;
    @Mutable @Shadow @Final @RestoreFinal public Set<PreparableReloadListener> preparingListeners;
    @Mutable @Shadow @Final @RestoreFinal protected CompletableFuture<List<S>> allDone;
    @Mutable @Shadow @Final @RestoreFinal private AtomicInteger doneTaskCounter;
    @Mutable @Shadow @Final @RestoreFinal private int listenerCount;
    @Mutable @Shadow @Final @RestoreFinal private AtomicInteger startedTaskCounter;

    @ModifyConstructor
    public Mixin_CF_SimpleReloadInstance(Executor executor, Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, SimpleReloadInstance.StateFactory<S> stateFactory, CompletableFuture<Unit> completableFuture) {
        this.allPreparations = new CompletableFuture<>();
        this.startedTaskCounter = new AtomicInteger();
        this.doneTaskCounter = new AtomicInteger();
        this.listenerCount = list.size();
        this.startedTaskCounter.incrementAndGet();
        completableFuture.thenRun(this.doneTaskCounter::incrementAndGet);
        OList<CompletableFuture<S>> listOfFutures = new OArrayList<>();
        this.preparingListeners = new OHashSet<>(list);
        CompletableFuture<?> previousFuture = completableFuture;
        for (int i = 0, len = list.size(); i < len; ++i) {
            PreparableReloadListener preparableReloadListener = list.get(i);
            CompletableFuture<S> currentFuture = stateFactory.create(new PreparationBarrier(previousFuture, executor2, preparableReloadListener, (SimpleReloadInstance) (Object) this),
                                                                     resourceManager,
                                                                     preparableReloadListener,
                                                                     new ReloadExecutor(this.doneTaskCounter, executor, this.startedTaskCounter),
                                                                     new ReloadExecutor2(executor2, (SimpleReloadInstance) (Object) this)
            );
            listOfFutures.add(currentFuture);
            previousFuture = currentFuture;
        }
        this.allDone = Util.sequenceFailFast(listOfFutures);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static ReloadInstance create(ResourceManager manager, List<PreparableReloadListener> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> future, boolean bl) {
        PackType type = manager instanceof MultiPackResourceManager multiPack ? multiPack.getResourceType() : null;
        if (bl) {
            List<PreparableReloadListener> sorted = ResourceManagerHelperImpl.sort(type, reloaders);
            return new ProfiledReloadInstance(manager, sorted, prepareExecutor, applyExecutor, future);
        }
        List<PreparableReloadListener> sorted = ResourceManagerHelperImpl.sort(type, reloaders);
        return of(manager, sorted, prepareExecutor, applyExecutor, future);
    }

    @Shadow
    public static SimpleReloadInstance<Void> of(ResourceManager resourceManager,
                                                List<PreparableReloadListener> list,
                                                Executor executor,
                                                Executor executor2,
                                                CompletableFuture<Unit> completableFuture) {
        throw new AbstractMethodError();
    }
}
