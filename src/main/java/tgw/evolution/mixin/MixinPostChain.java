package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(PostChain.class)
public abstract class MixinPostChain {

    @Shadow @Final private Map<String, RenderTarget> customRenderTargets;
    @Shadow @Final private List<RenderTarget> fullSizedTargets;
    @Shadow private float lastStamp;
    @Shadow @Final private List<PostPass> passes;
    @Shadow private int screenHeight;
    @Shadow @Final private RenderTarget screenTarget;
    @Shadow private int screenWidth;
    @Shadow private float time;

    /**
     * @author TheGreatWolf
     * @reason Enable stencil if needed.
     */
    @Overwrite
    public void addTempTarget(String string, int width, int height) {
        RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        if (this.screenTarget.isStencilEnabled()) {
            target.enableStencil();
        }
        this.customRenderTargets.put(string, target);
        if (width == this.screenWidth && height == this.screenHeight) {
            this.fullSizedTargets.add(target);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void process(float partialTicks) {
        if (partialTicks < this.lastStamp) {
            this.time += 1.0F - this.lastStamp + partialTicks;
        }
        else {
            this.time += partialTicks - this.lastStamp;
        }
        this.lastStamp = partialTicks;
        while (this.time > 20) {
            this.time -= 20;
        }
        List<PostPass> passes = this.passes;
        for (int i = 0, len = passes.size(); i < len; ++i) {
            passes.get(i).process(this.time / 20);
        }
    }
}
