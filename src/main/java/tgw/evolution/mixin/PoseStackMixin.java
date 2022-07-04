package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.math.MathHelper;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin implements IPoseStackPatch {
    @Shadow
    @Final
    private Deque<PoseStack.Pose> poseStack;

    /**
     * @author JellySquid
     * <p>
     * Use faster specialized function
     */
    @Overwrite
    public void mulPose(Quaternion q) {
        PoseStack.Pose entry = this.poseStack.getLast();
        IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(entry.pose());
        mat4.rotate(q);
        IMatrix3fPatch mat3 = MathHelper.getExtendedMatrix(entry.normal());
        mat3.rotate(q);
    }

    @Override
    public void mulPoseXRad(float radian) {
        radian /= 2.0f;
        float i = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poseStack.getLast();
        IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(entry.pose());
        mat4.rotateX(i, r);
        IMatrix3fPatch mat3 = MathHelper.getExtendedMatrix(entry.normal());
        mat3.rotateX(i, r);
    }

    @Override
    public void mulPoseYRad(float radian) {
        radian /= 2.0f;
        float j = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poseStack.getLast();
        IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(entry.pose());
        mat4.rotateY(j, r);
        IMatrix3fPatch mat3 = MathHelper.getExtendedMatrix(entry.normal());
        mat3.rotateY(j, r);
    }

    @Override
    public void mulPoseZRad(float radian) {
        radian /= 2.0f;
        float k = (float) Math.sin(radian);
        float r = (float) Math.cos(radian);
        PoseStack.Pose entry = this.poseStack.getLast();
        IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(entry.pose());
        mat4.rotateZ(k, r);
        IMatrix3fPatch mat3 = MathHelper.getExtendedMatrix(entry.normal());
        mat3.rotateZ(k, r);
    }

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void scale(float x, float y, float z) {
        PoseStack.Pose pose = this.poseStack.getLast();
        MathHelper.getExtendedMatrix(pose.pose()).scale(x, y, z);
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
        MathHelper.getExtendedMatrix(pose.normal()).scale(f3 * f, f3 * f1, f3 * f2);
    }
}