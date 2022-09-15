package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.math.MathHelper;

import java.util.List;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements HM {

    @Shadow
    public boolean visible;
    @Shadow
    public float x;
    @Shadow
    public float xRot;
    @Shadow
    public float y;
    @Shadow
    public float yRot;
    @Shadow
    public float z;
    @Shadow
    public float zRot;
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Override
    public void addRotationX(float dx) {
        this.xRot += dx;
    }

    @Override
    public void addRotationY(float dy) {
        this.yRot += dy;
    }

    @Override
    public void addRotationZ(float dz) {
        this.zRot += dz;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, use quick matrix transformations
     * <p>
     * <p>
     * Obs.: When trying to use VertexSink, the player model geometry gets distorted, i dont know why :(
     */
    @Overwrite
    private void compile(PoseStack.Pose matrices,
                         VertexConsumer vertexConsumer,
                         int light,
                         int overlay,
                         float red,
                         float green,
                         float blue,
                         float alpha) {
        IMatrix4fPatch poseExt = MathHelper.getExtendedMatrix(matrices.pose());
        IMatrix3fPatch normalExt = MathHelper.getExtendedMatrix(matrices.normal());
        for (ModelPart.Cube cube : this.cubes) {
            for (ModelPart.Polygon polygon : cube.polygons) {
                float normX = normalExt.transformVecX(polygon.normal);
                float normY = normalExt.transformVecY(polygon.normal);
                float normZ = normalExt.transformVecZ(polygon.normal);
                for (ModelPart.Vertex vertex : polygon.vertices) {
                    float x1 = vertex.pos.x() / 16.0F;
                    float y1 = vertex.pos.y() / 16.0F;
                    float z1 = vertex.pos.z() / 16.0F;
                    float x2 = poseExt.transformVecX(x1, y1, z1);
                    float y2 = poseExt.transformVecY(x1, y1, z1);
                    float z2 = poseExt.transformVecZ(x1, y1, z1);
                    vertexConsumer.vertex(x2, y2, z2, red, green, blue, alpha, vertex.u, vertex.v, overlay, light, normX, normY, normZ);
                }
            }
        }
    }

    @Shadow
    public abstract void copyFrom(ModelPart pModelPart);

    @Override
    public float getPivotX() {
        return this.x;
    }

    @Override
    public float getPivotY() {
        return this.y;
    }

    @Override
    public float getPivotZ() {
        return this.z;
    }

    @Override
    public void setPivotX(float x) {
        this.x = x;
    }

    @Override
    public void setPivotY(float y) {
        this.y = y;
    }

    @Override
    public void setPivotZ(float z) {
        this.z = z;
    }

    @Override
    public void setRotationX(float rotX) {
        this.xRot = rotX;
    }

    @Override
    public void setRotationY(float rotY) {
        this.yRot = rotY;
    }

    @Override
    public void setRotationZ(float rotZ) {
        this.zRot = rotZ;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void translateAndRotate(PoseStack matrices) {
        matrices.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        IPoseStackPatch mat = MathHelper.getExtendedMatrix(matrices);
        if (this.zRot != 0.0F) {
            mat.mulPoseZRad(this.zRot);
        }
        if (this.yRot != 0.0F) {
            mat.mulPoseYRad(this.yRot);
        }
        if (this.xRot != 0.0F) {
            mat.mulPoseXRad(this.xRot);
        }
    }

    @Override
    public float xRot() {
        return this.xRot;
    }

    @Override
    public float yRot() {
        return this.yRot;
    }

    @Override
    public float zRot() {
        return this.zRot;
    }
}
