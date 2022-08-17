package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin extends EntityModel {

    @Shadow
    @Final
    private float babyBodyScale;
    @Shadow
    @Final
    private float babyHeadScale;
    @Shadow
    @Final
    private float babyYHeadOffset;
    @Shadow
    @Final
    private float babyZHeadOffset;
    @Shadow
    @Final
    private float bodyYOffset;
    @Shadow
    @Final
    private boolean scaleHead;

    @Shadow
    protected abstract Iterable<ModelPart> bodyParts();

    @Shadow
    protected abstract Iterable<ModelPart> headParts();

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations
     */
    @Override
    @Overwrite
    public void renderToBuffer(PoseStack matrices, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        if (this.young) {
            matrices.pushPose();
            if (this.scaleHead) {
                float headScale = 1.5F / this.babyHeadScale;
                matrices.scale(headScale, headScale, headScale);
            }
            matrices.translate(0, this.babyYHeadOffset / 16.0, this.babyZHeadOffset / 16.0);
            for (ModelPart model : this.headParts()) {
                model.render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            matrices.popPose();
            matrices.pushPose();
            float bodyScale = 1.0F / this.babyBodyScale;
            matrices.scale(bodyScale, bodyScale, bodyScale);
            matrices.translate(0, this.bodyYOffset / 16.0, 0);
            for (ModelPart model : this.bodyParts()) {
                model.render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            matrices.popPose();
        }
        else {
            for (ModelPart model : this.headParts()) {
                model.render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            for (ModelPart model : this.bodyParts()) {
                model.render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
        }
    }
}
