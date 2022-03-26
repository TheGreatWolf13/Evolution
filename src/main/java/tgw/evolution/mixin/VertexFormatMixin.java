package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gl.IBufferVertexFormat;

@Mixin(VertexFormat.class)
public abstract class VertexFormatMixin implements IBufferVertexFormat {

    @Override
    public int getStride() {
        return this.getVertexSize();
    }

    @Shadow
    public abstract int getVertexSize();
}
