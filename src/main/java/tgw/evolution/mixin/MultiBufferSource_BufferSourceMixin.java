package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ICrashReset;

import java.util.Set;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class MultiBufferSource_BufferSourceMixin implements ICrashReset {

    @Shadow
    @Final
    protected BufferBuilder builder;

    @Shadow
    @Final
    protected Set<BufferBuilder> startedBuffers;

    @Override
    public void resetAfterCrash() {
        ((ICrashReset) this.builder).resetAfterCrash();
        for (BufferBuilder buffer : this.startedBuffers) {
            ((ICrashReset) buffer).resetAfterCrash();
        }
    }
}
