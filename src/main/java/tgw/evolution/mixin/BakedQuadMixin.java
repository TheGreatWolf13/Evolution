package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IBakedQuadPatch;

import static tgw.evolution.client.util.ModelQuadUtil.*;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements IBakedQuadPatch {

    @Shadow
    @Final
    protected int[] vertices;

    @Override
    public int getColor(int idx) {
        return this.vertices[vertexOffset(idx) + COLOR_INDEX];
    }

    @Override
    public float getTexU(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + TEXTURE_INDEX]);
    }

    @Override
    public float getTexV(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + TEXTURE_INDEX + 1]);
    }

    @Override
    public float getX(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx)]);
    }

    @Override
    public float getY(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + 1]);
    }

    @Override
    public float getZ(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + 2]);
    }
}
