package tgw.evolution.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ChunkRenderDispatcher.class)
public abstract class ChunkRenderDispatcherMixin {

    @ModifyVariable(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/renderer/LevelRenderer;" +
                             "Ljava/util/concurrent/Executor;ZLnet/minecraft/client/renderer/ChunkBufferBuilderPack;I)V", index = 9, at =
    @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayListWithExpectedSize(I)Ljava/util/ArrayList;", remap = false))
    private int modifyThreadPoolSize(int prev) {
        // Do not allow any resources to be allocated
        return 0;
    }
}
