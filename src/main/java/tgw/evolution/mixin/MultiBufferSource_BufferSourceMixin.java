package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ICrashReset;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class MultiBufferSource_BufferSourceMixin implements ICrashReset {

    @Shadow
    @Final
    protected BufferBuilder builder;
    @Shadow
    @Final
    protected Map<RenderType, BufferBuilder> fixedBuffers;
    @Shadow
    protected Optional<RenderType> lastState;
    @Shadow
    @Final
    protected Set<BufferBuilder> startedBuffers;

    @Shadow
    public abstract void endBatch(RenderType pRenderType);

    /**
     * @author TheGreatWolf
     * @reason No allocation for optionals.
     */
    @Overwrite
    public void endBatch() {
        if (this.lastState.isPresent()) {
            RenderType renderType = this.lastState.get();
            VertexConsumer buffer = this.getBuffer(renderType);
            if (buffer == this.builder) {
                this.endBatch(renderType);
            }
        }
        for (RenderType renderType : this.fixedBuffers.keySet()) {
            this.endBatch(renderType);
        }
    }

    @Shadow
    public abstract VertexConsumer getBuffer(RenderType pRenderType);

    @Override
    public void resetAfterCrash() {
        ((ICrashReset) this.builder).resetAfterCrash();
        for (BufferBuilder buffer : this.startedBuffers) {
            ((ICrashReset) buffer).resetAfterCrash();
        }
    }
}
