package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchNativeImage;

import java.nio.IntBuffer;

@Mixin(NativeImage.class)
public abstract class Mixin_CF_NativeImage implements AutoCloseable, PatchNativeImage {

    @Shadow public long pixels;
    @Unique private @Nullable IntBuffer buffer;
    @Mutable @Shadow @Final @RestoreFinal private NativeImage.Format format;
    @Mutable @Shadow @Final @RestoreFinal private int height;
    @Mutable @Shadow @Final @RestoreFinal private long size;
    @Mutable @Shadow @Final @RestoreFinal private boolean useStbFree;
    @Mutable @Shadow @Final @RestoreFinal private int width;

    @DummyConstructor
    public Mixin_CF_NativeImage() {
    }

    @ModifyConstructor
    public Mixin_CF_NativeImage(NativeImage.Format format, int width, int height, boolean clear) {
        if (width > 0 && height > 0) {
            this.format = format;
            this.width = width;
            this.height = height;
            this.size = (long) width * height * format.components();
            this.useStbFree = false;
            this.buffer = MemoryUtil.memAllocInt(width * height);
            this.pixels = MemoryUtil.memAddress(this.buffer);
        }
        else {
            throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
        }
    }

    @Override
    public @Nullable IntBuffer getBuffer() {
        return this.buffer;
    }
}
