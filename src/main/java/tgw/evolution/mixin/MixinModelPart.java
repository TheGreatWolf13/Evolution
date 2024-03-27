package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchModelPart;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.hitbox.hms.HM;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class MixinModelPart implements HM, PatchModelPart {

    @Shadow public boolean visible;
    @Shadow public float x;
    @Shadow public float xRot;
    @Shadow public float y;
    @Shadow public float yRot;
    @Shadow public float z;
    @Shadow public float zRot;
    @Shadow @Final private Map<String, ModelPart> children;
    @Shadow @Final private List<ModelPart.Cube> cubes;
    @Unique private boolean renderChild;

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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void render(PoseStack matrices, VertexConsumer builder, int light, int overlay, float r, float g, float b, float a) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                matrices.pushPose();
                this.translateAndRotate(matrices);
                this.compile(matrices.last(), builder, light, overlay, r, g, b, a);
                O2OMap<String, ModelPart> children = (O2OMap<String, ModelPart>) this.children;
                for (long it = children.beginIteration(); children.hasNextIteration(it); it = children.nextEntry(it)) {
                    children.getIterationValue(it).render(matrices, builder, light, overlay, r, g, b, a);
                }
                matrices.popPose();
            }
        }
        else if (this.renderChild && !this.children.isEmpty()) {
            matrices.pushPose();
            this.translateAndRotate(matrices);
            O2OMap<String, ModelPart> children = (O2OMap<String, ModelPart>) this.children;
            for (long it = children.beginIteration(); children.hasNextIteration(it); it = children.nextEntry(it)) {
                children.getIterationValue(it).render(matrices, builder, light, overlay, r, g, b, a);
            }
            matrices.popPose();
        }
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

    @Override
    public void shouldRenderChildrenEvenWhenNotVisible(boolean render) {
        this.renderChild = render;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void translateAndRotate(PoseStack matrices) {
        matrices.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        if (this.zRot != 0.0F) {
            matrices.mulPoseZRad(this.zRot);
        }
        if (this.yRot != 0.0F) {
            matrices.mulPoseYRad(this.yRot);
        }
        if (this.xRot != 0.0F) {
            matrices.mulPoseXRad(this.xRot);
        }
    }

    @Override
    public void translateX(float x) {
        this.x += x;
    }

    @Override
    public void translateY(float y) {
        this.y += y;
    }

    @Override
    public void translateZ(float z) {
        this.z += z;
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void compile(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f poseMat = matrices.pose();
        Matrix3f normalMat = matrices.normal();
        List<ModelPart.Cube> cubes = this.cubes;
        for (int i = 0, len = cubes.size(); i < len; ++i) {
            for (ModelPart.Polygon polygon : cubes.get(i).polygons) {
                float normX = normalMat.transformVecX(polygon.normal);
                float normY = normalMat.transformVecY(polygon.normal);
                float normZ = normalMat.transformVecZ(polygon.normal);
                for (ModelPart.Vertex vertex : polygon.vertices) {
                    float x1 = vertex.pos.x() / 16.0F;
                    float y1 = vertex.pos.y() / 16.0F;
                    float z1 = vertex.pos.z() / 16.0F;
                    float x2 = poseMat.transformVecX(x1, y1, z1);
                    float y2 = poseMat.transformVecY(x1, y1, z1);
                    float z2 = poseMat.transformVecZ(x1, y1, z1);
                    vertexConsumer.vertex(x2, y2, z2, red, green, blue, alpha, vertex.u, vertex.v, overlay, light, normX, normY, normZ);
                }
            }
        }
    }
}
