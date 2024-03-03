package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemTransform.class)
public abstract class MixinItemTransform {

    @Shadow @Final public Vector3f rotation;
    @Shadow @Final public Vector3f scale;
    @Shadow @Final public Vector3f translation;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void apply(boolean leftHanded, PoseStack poseStack) {
        if ((Object) this != ItemTransform.NO_TRANSFORM) {
            float rotX = this.rotation.x();
            float rotY = this.rotation.y();
            float rotZ = this.rotation.z();
            int off;
            if (leftHanded) {
                off = -1;
                rotY = -rotY;
                rotZ = -rotZ;
            }
            else {
                off = 1;
            }
            poseStack.translate(off * this.translation.x(), this.translation.y(), this.translation.z());
            poseStack.rotateXYZ(rotX, rotY, rotZ, true);
            poseStack.scale(this.scale.x(), this.scale.y(), this.scale.z());
        }
    }
}
