package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.PatchPoseStack;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.hitbox.hrs.HR;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class MixinPoseStack implements PatchPoseStack, HR {

    //TODO try to remove this field
    @Mutable @Shadow @Final private Deque<PoseStack.Pose> poseStack;
    @Unique private OList<PoseStack.Pose> poses;
    @Unique private int size;

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public boolean clear() {
        return this.size == 1;
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public PoseStack.Pose last() {
        return this.poses.get(this.size - 1);
    }

    /**
     * @author JellySquid
     * @reason Use faster specialized function
     */
    @Overwrite
    public void mulPose(Quaternion q) {
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().rotate(q);
        entry.normal().rotate(q);
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public void mulPoseMatrix(Matrix4f matrix) {
        this.poses.get(this.size - 1).pose().multiply(matrix);
    }

    @Override
    public void mulPoseXRad(float radian) {
        radian /= 2.0f;
        float i = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().rotateX(i, r);
        entry.normal().rotateX(i, r);
    }

    @Override
    public void mulPoseYRad(float radian) {
        radian /= 2.0f;
        float j = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().rotateY(j, r);
        entry.normal().rotateY(j, r);
    }

    @Override
    public void mulPoseZRad(float radian) {
        radian /= 2.0f;
        float k = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().rotateZ(k, r);
        entry.normal().rotateZ(k, r);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci) {
        this.poses = new OArrayList<>();
        this.poses.add(this.poseStack.getLast());
        this.size = 1;
        //noinspection ConstantConditions
        this.poseStack = null;
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public void popPose() {
        --this.size;
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public void pushPose() {
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        if (this.poses.size() > this.size) {
            PoseStack.Pose newPose = this.poses.get(this.size++);
            newPose.pose().load(entry.pose());
            newPose.normal().load(entry.normal());
        }
        else {
            this.poses.add(new PoseStack.Pose(entry.pose().copy(), entry.normal().copy()));
            ++this.size;
        }
    }

    @Override
    public void reset() {
        this.size = 1;
        this.setIdentity();
    }

    @Override
    public void rotate(float i, float j, float k, float r) {
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().rotate(i, j, k, r);
        entry.normal().rotate(i, j, k, r);
    }

    @Override
    public void rotateXHR(float xRot) {
        this.mulPoseX(xRot);
    }

    @Override
    public void rotateYHR(float yRot) {
        this.mulPoseY(yRot);
    }

    @Override
    public void rotateZHR(float zRot) {
        this.mulPoseZ(zRot);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void scale(float x, float y, float z) {
        PoseStack.Pose pose = this.poses.get(this.size - 1);
        pose.pose().scale(x, y, z);
        if (x == y && y == z) {
            if (x > 0.0F) {
                return;
            }
            pose.normal().mul(-1.0F);
        }
        float f = 1.0F / x;
        float f1 = 1.0F / y;
        float f2 = 1.0F / z;
        float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
        pose.normal().scale(f3 * f, f3 * f1, f3 * f2);
    }

    @Override
    public void scaleHR(float scaleX, float scaleY, float scaleZ) {
        this.scale(scaleX, scaleY, scaleZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public void setIdentity() {
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().setIdentity();
        entry.normal().setIdentity();
    }

    /**
     * @author TheGreatWolf
     * @reason Recycle Poses
     */
    @Overwrite
    public void translate(double x, double y, double z) {
        PoseStack.Pose entry = this.poses.get(this.size - 1);
        entry.pose().multiplyWithTranslation((float) x, (float) y, (float) z);
    }

    @Override
    public void translateHR(float x, float y, float z) {
        this.translate(x, y, z);
    }
}
