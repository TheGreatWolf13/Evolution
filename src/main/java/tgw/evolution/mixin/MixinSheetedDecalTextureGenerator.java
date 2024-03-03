package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    @Overwrite
    public void endVertex() {
        float nx = this.normalInversePose.transformVecX(this.nx, this.ny, this.nz);
        float ny = this.normalInversePose.transformVecY(this.nx, this.ny, this.nz);
        float nz = this.normalInversePose.transformVecZ(this.nx, this.ny, this.nz);
        Direction direction = Direction.getNearest(nx, ny, nz);
        float x0 = this.cameraInversePose.transformVecX(this.x, this.y, this.z);
        float y0 = this.cameraInversePose.transformVecY(this.x, this.y, this.z);
        float z0 = this.cameraInversePose.transformVecZ(this.x, this.y, this.z);
        //YP 180
        x0 = -x0;
        z0 = -z0;
        //XN90
        float x1 = x0;
        float y1 = -z0;
        float z1 = y0;
        //Direction
        float x2 = x1;
        float y2 = y1;
        switch (direction) {
            case DOWN -> {
                y2 = -y1;
            }
            case SOUTH -> {
                y2 = -z1;
            }
            case NORTH -> {
                x2 = -x1;
                y2 = -z1;
            }
            case WEST -> {
                x2 = -y1;
                y2 = -z1;
            }
            case EAST -> {
                x2 = y1;
                y2 = -z1;
            }
        }
        this.delegate.vertex(this.x, this.y, this.z)
                     .color(1.0F, 1.0F, 1.0F, 1.0F)
                     .uv(-x2, -y2)
                     .overlayCoords(this.overlayU, this.overlayV)
                     .uv2(this.lightCoords)
                     .normal(this.nx, this.ny, this.nz)
                     .endVertex();
        this.resetState();
    }

    @Shadow
    protected abstract void resetState();
}
