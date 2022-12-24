package tgw.evolution.mixin;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;

import javax.annotation.Nullable;

@Mixin(FaceBakery.class)
public abstract class FaceBakeryMixin {

    @Shadow
    public static Direction calculateFacing(int[] pFaceData) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static BlockFaceUV recomputeUVs(BlockFaceUV pUv,
                                           Direction pFacing,
                                           Transformation pModelRotation,
                                           ResourceLocation pModelLocation) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Optimize
     */
    @Overwrite
    public BakedQuad bakeQuad(Vector3f from,
                              Vector3f to,
                              BlockElementFace face,
                              TextureAtlasSprite sprite,
                              Direction facing,
                              ModelState transform,
                              @Nullable BlockElementRotation rotation,
                              boolean shade,
                              ResourceLocation modelLocation) {
        BlockFaceUV faceUV = face.uv;
        if (transform.isUvLocked()) {
            faceUV = recomputeUVs(face.uv, facing, transform.getRotation(), modelLocation);
        }
        float[] tempUV = RenderHelper.TEMP_UV.get();
        System.arraycopy(faceUV.uvs, 0, tempUV, 0, tempUV.length);
        float ratio = sprite.uvShrinkRatio();
        float f1 = (2 * faceUV.uvs[0] + 2 * faceUV.uvs[2]) / 4.0F;
        float f2 = (2 * faceUV.uvs[1] + 2 * faceUV.uvs[3]) / 4.0F;
        faceUV.uvs[0] = Mth.lerp(ratio, faceUV.uvs[0], f1);
        faceUV.uvs[2] = Mth.lerp(ratio, faceUV.uvs[2], f1);
        faceUV.uvs[1] = Mth.lerp(ratio, faceUV.uvs[1], f2);
        faceUV.uvs[3] = Mth.lerp(ratio, faceUV.uvs[3], f2);
        int[] vertices = this.makeVertices(faceUV, sprite, facing, this.setupShape(from, to), transform.getRotation(), rotation, shade);
        Direction direction = calculateFacing(vertices);
        System.arraycopy(tempUV, 0, faceUV.uvs, 0, tempUV.length);
        if (rotation == null) {
            this.recalculateWinding(vertices, direction);
        }
        ForgeHooksClient.fillNormal(vertices, direction);
        return new BakedQuad(vertices, face.tintIndex, direction, sprite, shade);
    }

    @Shadow
    protected abstract int[] makeVertices(BlockFaceUV pUvs,
                                          TextureAtlasSprite pSprite,
                                          Direction pOrientation,
                                          float[] pPosDiv16,
                                          Transformation pRotation,
                                          @org.jetbrains.annotations.Nullable BlockElementRotation pPartRotation, boolean pShade);

    @Shadow
    protected abstract void recalculateWinding(int[] pVertices, Direction pDirection);

    @Shadow
    protected abstract float[] setupShape(Vector3f pPos1, Vector3f pPos2);
}
