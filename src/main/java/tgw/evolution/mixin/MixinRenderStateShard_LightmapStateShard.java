package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.client.renderer.RenderStateShard$LightmapStateShard")
public abstract class MixinRenderStateShard_LightmapStateShard {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void method_23551(boolean bl) {
        if (bl) {
            Minecraft.getInstance().gameRenderer.lightTexture_().turnOffLightLayer();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void method_23552(boolean bl) {
        if (bl) {
            Minecraft.getInstance().gameRenderer.lightTexture_().turnOnLightLayer();
        }
    }
}
