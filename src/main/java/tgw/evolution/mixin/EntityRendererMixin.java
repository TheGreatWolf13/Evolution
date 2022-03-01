package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    /**
     * Prevents name tags from being visible though walls.
     */
    @ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch" +
                                                                             "(Lnet/minecraft/network/chat/Component;" +
                                                                             "FFIZLcom/mojang/math/Matrix4f;" +
                                                                             "Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I", ordinal = 0)
            , index = 7)
    private boolean modifyRenderName(boolean renderThroughWalls) {
        return false;
    }
}
