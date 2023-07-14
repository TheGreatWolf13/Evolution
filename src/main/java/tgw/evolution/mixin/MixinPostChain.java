package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchRenderTarget;

import java.util.List;
import java.util.Map;

@Mixin(PostChain.class)
public abstract class MixinPostChain {

    @Shadow @Final private Map<String, RenderTarget> customRenderTargets;
    @Shadow @Final private List<RenderTarget> fullSizedTargets;
    @Shadow private int screenHeight;
    @Shadow @Final private RenderTarget screenTarget;
    @Shadow private int screenWidth;

    /**
     * @author TheGreatWolf
     * @reason Enable stencil if needed.
     */
    @Overwrite
    public void addTempTarget(String string, int width, int height) {
        RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        if (((PatchRenderTarget) this.screenTarget).isStencilEnabled()) {
            //noinspection ConstantConditions
            ((PatchRenderTarget) target).enableStencil();
        }
        this.customRenderTargets.put(string, target);
        if (width == this.screenWidth && height == this.screenHeight) {
            this.fullSizedTargets.add(target);
        }
    }
}
