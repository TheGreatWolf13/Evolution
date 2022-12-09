package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.util.collection.RList;

import java.util.function.Function;

public abstract class ModelAgeableList<E extends Entity> extends EntityModel<E> {

    private final float babyBodyScale;
    private final float babyHeadScale;
    private final float babyYHeadOffset;
    private final float babyZHeadOffset;
    private final float bodyYOffset;
    private final boolean scaleHead;

    protected ModelAgeableList(boolean scaleHead, float babyYHeadOffset, float babyZHeadOffset) {
        this(scaleHead, babyYHeadOffset, babyZHeadOffset, 2.0F, 2.0F, 24.0F);
    }

    protected ModelAgeableList(boolean scaleHead,
                               float babyYHeadOffset,
                               float babyZHeadOffset,
                               float babyHeadScale,
                               float babyBodyScale,
                               float bodyYOffset) {
        this(RenderHelper.RENDER_TYPE_ENTITY_CUTOUT_NO_CULL, scaleHead, babyYHeadOffset, babyZHeadOffset, babyHeadScale, babyBodyScale, bodyYOffset);
    }

    protected ModelAgeableList(Function<ResourceLocation, RenderType> renderType,
                               boolean scaleHead,
                               float babyYHeadOffset,
                               float babyZHeadOffset,
                               float babyHeadScale,
                               float babyBodyScale,
                               float bodyYOffset) {
        super(renderType);
        this.scaleHead = scaleHead;
        this.babyYHeadOffset = babyYHeadOffset;
        this.babyZHeadOffset = babyZHeadOffset;
        this.babyHeadScale = babyHeadScale;
        this.babyBodyScale = babyBodyScale;
        this.bodyYOffset = bodyYOffset;
    }

    protected ModelAgeableList() {
        this(false, 5.0F, 2.0F);
    }

    protected abstract RList<ModelPart> bodyParts();

    protected abstract RList<ModelPart> headParts();

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        if (this.young) {
            matrices.pushPose();
            if (this.scaleHead) {
                float headScale = 1.5F / this.babyHeadScale;
                matrices.scale(headScale, headScale, headScale);
            }
            matrices.translate(0, this.babyYHeadOffset / 16.0, this.babyZHeadOffset / 16.0);
            RList<ModelPart> headParts = this.headParts();
            for (int i = 0, l = headParts.size(); i < l; i++) {
                headParts.get(i).render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            matrices.popPose();
            matrices.pushPose();
            float bodyScale = 1.0F / this.babyBodyScale;
            matrices.scale(bodyScale, bodyScale, bodyScale);
            matrices.translate(0, this.bodyYOffset / 16.0, 0);
            RList<ModelPart> bodyParts = this.bodyParts();
            for (int i = 0, l = bodyParts.size(); i < l; i++) {
                bodyParts.get(i).render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            matrices.popPose();
        }
        else {
            RList<ModelPart> headParts = this.headParts();
            for (int i = 0, l = headParts.size(); i < l; i++) {
                headParts.get(i).render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
            RList<ModelPart> bodyParts = this.bodyParts();
            for (int i = 0, l = bodyParts.size(); i < l; i++) {
                bodyParts.get(i).render(matrices, consumer, light, overlay, red, green, blue, alpha);
            }
        }
    }
}
