package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.MathHelper;

import java.util.Deque;

@Mixin(MatrixStack.class)
public abstract class MatrixStackMixin {

    @Shadow
    @Final
    private Deque<MatrixStack.Entry> poseStack;

    /**
     * @reason Use faster specialized function
     * @author JellySquid
     */
    @Overwrite
    public void mulPose(Quaternion q) {
        MatrixStack.Entry entry = this.poseStack.getLast();
        IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(entry.pose());
        mat4.rotate(q);
        IMatrix3fPatch mat3 = MathHelper.getExtendedMatrix(entry.normal());
        mat3.rotate(q);
    }

    /**
     * @reason Use faster specialized function
     * @author JellySquid
     */
    @Overwrite
    public void translate(double x, double y, double z) {
        MatrixStack.Entry entry = this.poseStack.getLast();
        IMatrix4fPatch mat = MathHelper.getExtendedMatrix(entry.pose());
        mat.translate((float) x, (float) y, (float) z);
    }
}
