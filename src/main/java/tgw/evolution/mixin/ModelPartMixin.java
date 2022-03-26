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
import tgw.evolution.util.math.MathHelper;

import java.util.List;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

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

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations, use quick matrix transformations
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

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void translateAndRotate(PoseStack matrices) {
        matrices.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        IPoseStackPatch matricesExt = MathHelper.getExtendedMatrix(matrices);
        if (this.zRot != 0.0F) {
            matricesExt.mulPoseZRad(this.zRot);
        }
        if (this.yRot != 0.0F) {
            matricesExt.mulPoseYRad(this.yRot);
        }
        if (this.xRot != 0.0F) {
            matricesExt.mulPoseXRad(this.xRot);
        }
    }
}
