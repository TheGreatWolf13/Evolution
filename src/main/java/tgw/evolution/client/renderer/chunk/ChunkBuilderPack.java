package tgw.evolution.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import tgw.evolution.util.constants.RenderLayer;

public class ChunkBuilderPack {

    public static final RenderType[] RENDER_TYPES = {RenderType.solid(),
                                                     RenderType.cutoutMipped(),
                                                     RenderType.cutout(),
                                                     RenderType.translucent(),
                                                     RenderType.tripwire()};
    private final BufferBuilder[] builders = new BufferBuilder[5];

    public ChunkBuilderPack() {
        for (int i = 0, len = this.builders.length; i < len; i++) {
            //noinspection ObjectAllocationInLoop
            this.builders[i] = new BufferBuilder(RENDER_TYPES[i].bufferSize());
        }
    }

    public ChunkBuilderPack(ChunkBufferBuilderPack legacy) {
        this.builders[RenderLayer.SOLID] = legacy.builder(RenderType.solid());
        this.builders[RenderLayer.CUTOUT_MIPPED] = legacy.builder(RenderType.cutoutMipped());
        this.builders[RenderLayer.CUTOUT] = legacy.builder(RenderType.cutout());
        this.builders[RenderLayer.TRANSLUCENT] = legacy.builder(RenderType.translucent());
        this.builders[RenderLayer.TRIPWIRE] = legacy.builder(RenderType.tripwire());
    }

    public BufferBuilder builder(@RenderLayer int renderLayer) {
        return this.builders[renderLayer];
    }

    public void clearAll() {
        for (BufferBuilder builder : this.builders) {
            builder.clear();
        }
    }

    public void discardAll() {
        for (BufferBuilder builder : this.builders) {
            builder.discard();
        }
    }
}
