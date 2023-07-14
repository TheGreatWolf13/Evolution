package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.hitbox.hms.HMEntity;
import tgw.evolution.util.hitbox.hrs.HRLivingEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>
        implements HRLivingEntity<T, HMEntity<T>> {

    @Mutable @Shadow @Final protected List<RenderLayer<T, M>> layers;
    @Shadow protected M model;
    @Unique private float ageInTicks;
    @Unique private float headPitch;
    @Unique private float limbSwing;
    @Unique private float limbSwingAmount;
    @Unique private float netHeadYaw;

    public MixinLivingEntityRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Shadow
    public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract @Nullable RenderType getRenderType(T p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_);

    @Shadow
    protected abstract float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks);

    @Shadow
    protected abstract boolean isBodyVisible(T pLivingEntity);

    @Override
    public HMEntity<T> model() {
        return (HMEntity<T>) this.model;
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;" +
                                                                    "layers:Ljava/util/List;", opcode = Opcodes.PUTFIELD))
    private void onInit(LivingEntityRenderer instance, List<RenderLayer<T, M>> value) {
        this.layers = new OArrayList<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
    private @Nullable ArrayList onInitRemoveList() {
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Improve HMs
     */
    @Override
    @Overwrite
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrices, MultiBufferSource buf, int light) {
        matrices.pushPose();
        this.renderOrInit(entity, matrices, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(entity);
        boolean translucent = !bodyVisible && mc.player != null && !entity.isInvisibleTo(mc.player);
        boolean glowing = mc.shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, bodyVisible, translucent, glowing);
        if (renderType != null) {
            VertexConsumer buffer = buf.getBuffer(renderType);
            int overlay = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(matrices, buffer, light, overlay, 1.0F, 1.0F, 1.0F, translucent ? 0.15F : 1.0F);
        }
        if (!entity.isSpectator()) {
            for (int i = 0, l = this.layers.size(); i < l; i++) {
                this.layers.get(i)
                           .render(matrices, buf, light, entity, this.limbSwing, this.limbSwingAmount, partialTicks, this.ageInTicks, this.netHeadYaw,
                                   this.headPitch);
            }
        }
        matrices.popPose();
        super.render(entity, entityYaw, partialTicks, matrices, buf, light);
    }

    @Override
    public void setAgeInTicks(float ageInTicks) {
        this.ageInTicks = ageInTicks;
    }

    @Override
    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    @Override
    public void setLimbSwing(float limbSwing) {
        this.limbSwing = limbSwing;
    }

    @Override
    public void setLimbSwingAmount(float limbSwingAmount) {
        this.limbSwingAmount = limbSwingAmount;
    }

    @Override
    public void setNetHeadYaw(float netHeadYaw) {
        this.netHeadYaw = netHeadYaw;
    }
}
