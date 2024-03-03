package tgw.evolution.mixin;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.resources.ResourceManagerHelperImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadInstance.class)
public abstract class MixinSimpleReloadInstance {
    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static ReloadInstance create(ResourceManager manager,
                                        List<PreparableReloadListener> reloaders,
                                        Executor prepareExecutor,
                                        Executor applyExecutor,
                                        CompletableFuture<Unit> future,
                                        boolean bl) {
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
