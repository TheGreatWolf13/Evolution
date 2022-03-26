package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.client.models.pipeline.ModelQuadFlags;
import tgw.evolution.patches.IBakedQuadPatch;

import static tgw.evolution.client.util.ModelQuadUtil.*;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements IBakedQuadPatch {

    @Shadow
    @Final
    protected TextureAtlasSprite sprite;
    @Shadow
    @Final
    protected int tintIndex;
    @Shadow
    @Final
    protected int[] vertices;
    private int cachedFlags;

    @Override
    public int getColor(int idx) {
        return this.vertices[vertexOffset(idx) + COLOR_INDEX];
    }

    @Override
    public int getFlags() {
        return this.cachedFlags;
    }

    @Override
    public int getLight(int idx) {
        return this.vertices[vertexOffset(idx) + LIGHT_INDEX];
    }

    @Override
    public int getNormal(int idx) {
        return this.vertices[vertexOffset(idx) + NORMAL_INDEX];
    }

    @Override
    public TextureAtlasSprite getSprite() {
        return this.sprite;
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
    public int getTintIndex() {
        return this.tintIndex;
    }

    @Override
    public float getX(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + POSITION_INDEX]);
    }

    @Override
    public float getY(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + POSITION_INDEX + 1]);
    }

    @Override
    public float getZ(int idx) {
        return Float.intBitsToFloat(this.vertices[vertexOffset(idx) + POSITION_INDEX + 2]);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(int[] vertexData, int colorIndex, Direction face, TextureAtlasSprite sprite, boolean shade, CallbackInfo ci) {
        this.cachedFlags = ModelQuadFlags.getQuadFlags((BakedQuad) (Object) this);
    }
}
