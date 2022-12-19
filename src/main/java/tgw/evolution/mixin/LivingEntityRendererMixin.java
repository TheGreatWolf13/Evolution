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
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.hitbox.hms.HMEntity;
import tgw.evolution.util.hitbox.hrs.HR;
import tgw.evolution.util.hitbox.hrs.HRLivingEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T>
        implements HRLivingEntity<T, HMEntity<T>> {

    @Mutable
    @Shadow
    @Final
    protected List<RenderLayer<T, M>> layers;
    @Shadow
    protected M model;
    private float ageInTicks;
    private float headPitch;
    private float limbSwing;
    private float limbSwingAmount;
    private float netHeadYaw;

    public LivingEntityRendererMixin(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Shadow
    public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
        throw new AbstractMethodError();
    }

    @Shadow
    @Nullable
    protected abstract RenderType getRenderType(T p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_);

    @Shadow
    protected abstract float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks);

    @Shadow
    protected abstract boolean isBodyVisible(T pLivingEntity);

    @Override
    public HMEntity<T> model() {
        return (HMEntity<T>) this.model;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityRendererProvider.Context context, EntityModel model, float shadowRadius, CallbackInfo ci) {
        this.layers = new OArrayList<>();
    }

    @Nullable
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private ArrayList proxyInit() {
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Improve HMs
     */
    @Override
    @Overwrite
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrices, MultiBufferSource buf, int light) {
        if (MinecraftForge.EVENT_BUS.post(
                new RenderLivingEvent.Pre<>(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTicks, matrices, buf, light))) {
            return;
        }
        matrices.pushPose();
        this.renderOrInit(entity, (HR) matrices, partialTicks);
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
        MinecraftForge.EVENT_BUS.post(
                new RenderLivingEvent.Post<>(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTicks, matrices, buf, light));
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
