package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.math.ColorMixer;

@Mixin(targets = "net.minecraft.client.renderer.texture.TextureAtlasSprite$InterpolationData")
public abstract class TextureAtlasSprite_InterpolationDataMixin {

    @Shadow
    @Final
    TextureAtlasSprite this$0;
    @Shadow
    @Final
    private NativeImage[] activeFrame;

    /**
     * @author JellySquid
     * @reason Drastic optimizations
     */
    @Overwrite
    public void uploadInterpolatedFrame(TextureAtlasSprite.AnimatedTexture animation) {
        TextureAtlasSprite.FrameInfo animationFrame = animation.frames.get(animation.frame);
        int curIndex = animationFrame.index;
        int nextIndex = animation.frames.get((animation.frame + 1) % animation.frames.size()).index;
        if (curIndex == nextIndex) {
            return;
        }
        float delta = 1.0F - animation.subFrame / (float) animationFrame.time;
        int f1 = ColorMixer.getStartRatio(delta);
        int f2 = ColorMixer.getEndRatio(delta);
        for (int layer = 0; layer < this.activeFrame.length; layer++) {
            int width = this.this$0.getWidth() >> layer;
            int height = this.this$0.getHeight() >> layer;
            int curX = curIndex % animation.frameRowSize * width;
            int curY = curIndex / animation.frameRowSize * height;
            int nextX = nextIndex % animation.frameRowSize * width;
            int nextY = nextIndex / animation.frameRowSize * height;
            NativeImage src = this.this$0.mainImage[layer];
            NativeImage dst = this.activeFrame[layer];
            // Source pointers
            long s1p = src.pixels + curX + (long) curY * src.getWidth() * 4;
            long s2p = src.pixels + nextX + (long) nextY * src.getWidth() * 4;
            // Destination pointers
            long dp = dst.pixels;
            int pixelCount = width * height;
            for (int i = 0; i < pixelCount; i++) {
                MemoryUtil.memPutInt(dp, ColorMixer.mixARGB(MemoryUtil.memGetInt(s1p), MemoryUtil.memGetInt(s2p), f1, f2));
                s1p += 4;
                s2p += 4;
                dp += 4;
            }
        }
        this.this$0.upload(0, 0, this.activeFrame);
    }
}
