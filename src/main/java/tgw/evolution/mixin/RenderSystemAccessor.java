package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    /**
     * Sets the shader directly, without the need for a supplier. This should be only used when we are guaranteed to be on the main thread.
     */
    @Accessor
    static void setShader(@Nullable ShaderInstance shader) {
        throw new AbstractMethodError();
    }
}
