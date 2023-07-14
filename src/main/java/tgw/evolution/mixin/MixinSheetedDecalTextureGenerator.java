package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.constants.CommonRotations;

@Mixin(SheetedDecalTextureGenerator.class)
public abstract class MixinSheetedDecalTextureGenerator extends DefaultedVertexConsumer {

    @Shadow @Final private Matrix4f cameraInversePose;
    @Shadow @Final private VertexConsumer delegate;
    @Shadow private int lightCoords;
    @Shadow @Final private Matrix3f normalInversePose;
    @Shadow private float nx;
    @Shadow private float ny;
    @Shadow private float nz;
    @Shadow private int overlayU;
    @Shadow private int overlayV;
    @Shadow private float x;
    @Shadow private float y;
    @Shadow private float z;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Override
    @Overwrite
    public void endVertex() {
        Matrix3f normalInversePoseExt = this.normalInversePose;
        float nx = normalInversePoseExt.transformVecX(this.nx, this.ny, this.nz);
        float ny = normalInversePoseExt.transformVecY(this.nx, this.ny, this.nz);
        float nz = normalInversePoseExt.transformVecZ(this.nx, this.ny, this.nz);
        Direction direction = Direction.getNearest(nx, ny, nz);
        Vector4f vector4f = new Vector4f(this.x, this.y, this.z, 1.0F);
        vector4f.transform(this.cameraInversePose);
        vector4f.transform(CommonRotations.YP180);
        vector4f.transform(CommonRotations.XN90);
        vector4f.transform(direction.getRotation());
        float f = -vector4f.x();
        float f1 = -vector4f.y();
        this.delegate.vertex(this.x, this.y, this.z)
                     .color(1.0F, 1.0F, 1.0F, 1.0F)
                     .uv(f, f1)
                     .overlayCoords(this.overlayU, this.overlayV)
                     .uv2(this.lightCoords)
                     .normal(this.nx, this.ny, this.nz)
                     .endVertex();
        this.resetState();
    }

    @Shadow
    protected abstract void resetState();
}
