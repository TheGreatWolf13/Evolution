package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin {

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

    /**
     * @author JellySquid
     * <p>
     * Use faster specialized function
     */
    @Overwrite
    public void translate(double x, double y, double z) {
        PoseStack.Pose entry = this.poseStack.getLast();
        IMatrix4fPatch mat = MathHelper.getExtendedMatrix(entry.pose());
        mat.translate((float) x, (float) y, (float) z);
    }
}
