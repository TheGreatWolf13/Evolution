package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.renderer.ICrashReset;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(MultiBufferSource.BufferSource.class)
public abstract class MixinMultiBufferSource_BufferSource implements ICrashReset {

    @Shadow @Final protected BufferBuilder builder;
    @Shadow @Final protected Map<RenderType, BufferBuilder> fixedBuffers;
    @Shadow protected Optional<RenderType> lastState;
    @Mutable @Shadow @Final protected Set<BufferBuilder> startedBuffers;

    @Shadow
    public abstract void endBatch(RenderType pRenderType);

    /**
     * @reason _
     * @author TheGreatWolf
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

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;" +
                                                                    "startedBuffers:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(MultiBufferSource.BufferSource instance, Set<BufferBuilder> value) {
        this.startedBuffers = new OHashSet<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;", remap = false))
    private @Nullable HashSet onInitRemoveSet() {
        return null;
    }

    @Override
    public void resetAfterCrash() {
        ((ICrashReset) this.builder).resetAfterCrash();
        OSet<BufferBuilder> startedBuffers = (OSet<BufferBuilder>) this.startedBuffers;
        for (BufferBuilder e = startedBuffers.fastEntries(); e != null; e = startedBuffers.fastEntries()) {
            ((ICrashReset) e).resetAfterCrash();
        }
    }
}
