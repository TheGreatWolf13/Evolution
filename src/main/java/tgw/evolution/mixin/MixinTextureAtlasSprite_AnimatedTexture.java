package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.config.EvolutionConfig;

import java.util.List;

@Mixin(TextureAtlasSprite.AnimatedTexture.class)
public abstract class MixinTextureAtlasSprite_AnimatedTexture {

    @Shadow public int frame;
    @Shadow @Final public List<TextureAtlasSprite.FrameInfo> frames;
    @Shadow public int subFrame;
    @Shadow @Final private @Nullable TextureAtlasSprite.InterpolationData interpolationData;

    /**
     * @author TheGreatWolf
     * @reason Add evolution check via overwrite, since this is a hot path, so an injection allocates a lot of memory
     */
    @Overwrite
    public void tick() {
        //Evolution start
        if (!EvolutionConfig.ANIMATED_TEXTURES.get() || Minecraft.getInstance().isPaused()) {
            return;
        }
        //Evolution end
        ++this.subFrame;
        TextureAtlasSprite.FrameInfo frameInfo = this.frames.get(this.frame);
        if (this.subFrame >= frameInfo.time) {
            int i = frameInfo.index;
            this.frame = (this.frame + 1) % this.frames.size();
            this.subFrame = 0;
            int j = this.frames.get(this.frame).index;
            if (i != j) {
                this.uploadFrame(j);
            }
        }
        else if (this.interpolationData != null) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(
                        () -> this.interpolationData.uploadInterpolatedFrame((TextureAtlasSprite.AnimatedTexture) (Object) this));
            }
            else {
                this.interpolationData.uploadInterpolatedFrame((TextureAtlasSprite.AnimatedTexture) (Object) this);
            }
        }

    }

    @Shadow
    protected abstract void uploadFrame(int pFrameIndex);
}
