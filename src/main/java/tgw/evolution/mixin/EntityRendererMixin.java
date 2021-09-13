package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawInBatch" +
                                                                             "(Lnet/minecraft/util/text/ITextComponent;" +
                                                                             "FFIZLnet/minecraft/util/math/vector/Matrix4f;" +
                                                                             "Lnet/minecraft/client/renderer/IRenderTypeBuffer;ZII)I", ordinal = 0)
            , index = 7)
    private boolean modifyRenderName(boolean renderThroughWalls) {
        return false;
    }
}
