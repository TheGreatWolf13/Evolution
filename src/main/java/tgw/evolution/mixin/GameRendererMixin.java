package tgw.evolution.mixin;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IGameRendererPatch;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.collection.I2OMap;
import tgw.evolution.util.collection.I2OOpenHashMap;
import tgw.evolution.util.math.MathHelper;

import java.io.IOException;
import java.util.Locale;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements IGameRendererPatch {

    @Shadow
    @Final
    public static int EFFECT_NONE;
    @Shadow
    @Final
    private static Logger LOGGER;
    private final I2OMap<PostChain> postEffects = new I2OOpenHashMap<>();
    @Shadow
    public boolean effectActive;
    @Shadow
    private int effectIndex;
    @Shadow
    private long lastActiveTime;
    @Mutable
    @Shadow
    @Final
    private LightTexture lightTexture;
    @Shadow
    @Final
    private Camera mainCamera;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Nullable
    private PostChain postEffect;
    @Shadow
    private float renderDistance;
    @Shadow
    @Final
    private ResourceManager resourceManager;
    @Shadow
    private int tick;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;

    @Shadow
    protected abstract void bobHurt(PoseStack pMatrixStack, float pPartialTicks);

    @Shadow
    public abstract float getDepthFar();

    @Shadow
    protected abstract double getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);

    /**
     * @author TheGreatWolf
     * @reason Modify the near clipping plane to improve first person camera.
     */
    @Overwrite
    public Matrix4f getProjectionMatrix(double fov) {
        PoseStack matrices = new PoseStack();
        matrices.last().pose().setIdentity();
        if (this.zoom != 1.0F) {
            matrices.translate(this.zoomX, -this.zoomY, 0);
            matrices.scale(this.zoom, this.zoom, 1.0F);
        }
        matrices.last()
                .pose()
                .multiply(
                        Matrix4f.perspective(fov, this.minecraft.getWindow().getWidth() / (float) this.minecraft.getWindow().getHeight(), 0.006_25f,
                                             this.getDepthFar()));
        return matrices.last().pose();
    }

    /**
     * @author TheGreatWolf
     * @reason Remove {@code this.effectActive = false} when the shader fails to load, preventing other shaders from being unloaded.
     */
    @Overwrite
    public void loadEffect(ResourceLocation resLoc) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }
        try {
            this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), resLoc);
            this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.effectActive = true;
        }
        catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", resLoc, e);
            this.effectIndex = EFFECT_NONE;
        }
        catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse shader: {}", resLoc, e);
            this.effectIndex = EFFECT_NONE;
        }
    }

    @Override
    public void loadShader(int shaderId, ResourceLocation resLoc) {
        if (!this.postEffects.containsKey(shaderId)) {
            try {
                PostChain shader = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(),
                                                 resLoc);
                shader.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
                this.effectActive = true;
                this.postEffects.put(shaderId, shader);
            }
            catch (IOException e) {
                LOGGER.warn("Failed to load shader: {}", resLoc, e);
            }
            catch (JsonSyntaxException e) {
                LOGGER.warn("Failed to parse shader: {}", resLoc, e);
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructor(Minecraft mc, ResourceManager resourceManager, RenderBuffers buffers, CallbackInfo ci) {
        this.lightTexture = new LightTextureEv((GameRenderer) (Object) this, mc);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle Evolution's hitboxes and reach distance.
     */
    @Overwrite
    public void pick(float partialTicks) {
        Entity entity = this.minecraft.player;
        if (entity != null) {
            //noinspection VariableNotUsedInsideIf
            if (this.minecraft.level != null) {
                this.minecraft.getProfiler().push("pick");
                double reachDistance = this.minecraft.player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
                Vec3 cameraPos = entity.getEyePosition(partialTicks);
                this.minecraft.hitResult = entity.pick(reachDistance, partialTicks, false);
                if (this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                    reachDistance = cameraPos.distanceTo(this.minecraft.hitResult.getLocation());
                }
                EntityHitResult leftRayTrace = MathHelper.rayTraceEntitiesFromEyes(this.minecraft.player, partialTicks, reachDistance);
                if (leftRayTrace != null) {
                    this.minecraft.hitResult = leftRayTrace;
                    this.minecraft.crosshairPickEntity = leftRayTrace.getEntity();
                    ClientEvents.getInstance().leftRayTrace = leftRayTrace;
                    ClientEvents.getInstance().leftPointedEntity = leftRayTrace.getEntity();
                }
                else {
                    this.minecraft.crosshairPickEntity = null;
                    ClientEvents.getInstance().leftRayTrace = null;
                    ClientEvents.getInstance().leftPointedEntity = null;
                }
                ClientEvents.getInstance().rightRayTrace = null;
                ClientEvents.getInstance().rightPointedEntity = null;
                this.minecraft.getProfiler().pop();
            }
        }
    }

    @Nullable
    @Redirect(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                                       "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyShouldRenderBlockOutline(Minecraft mc) {
        return mc.player;
    }

    /**
     * @author TheGreatWolf
     * @reason Make gui render even when {@link net.minecraft.client.Options#hideGui} is {@code true} since every render overlay of the gui already
     * makes a check of its own. This allows us to render gui elements which should always remain on the screen (such as helmet overlays).
     */
    @Overwrite
    public void render(float partialTicks, long nanoTime, boolean renderLevel) {
        if (!this.minecraft.isWindowActive() &&
            this.minecraft.options.pauseOnLostFocus &&
            (!this.minecraft.options.touchscreen || !this.minecraft.mouseHandler.isRightPressed())) {
            if (Util.getMillis() - this.lastActiveTime > 500L) {
                this.minecraft.pauseGame(false);
            }
        }
        else {
            this.lastActiveTime = Util.getMillis();
        }
        if (!this.minecraft.noRender) {
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() /
                                this.minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() /
                                this.minecraft.getWindow().getScreenHeight());
            RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            if (renderLevel && this.minecraft.level != null) {
                this.minecraft.getProfiler().push("level");
                this.renderLevel(partialTicks, nanoTime, new PoseStack());
                this.tryTakeScreenshotIfNeeded();
                this.minecraft.levelRenderer.doEntityOutline();
                if (this.effectActive) {
                    if (this.postEffect != null) {
                        RenderSystem.disableBlend();
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableTexture();
                        RenderSystem.resetTextureMatrix();
                        this.postEffect.process(partialTicks);
                    }
                    for (PostChain effect : this.postEffects.values()) {
                        RenderSystem.disableBlend();
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableTexture();
                        RenderSystem.resetTextureMatrix();
                        effect.process(partialTicks);
                    }
                }
                this.minecraft.getMainRenderTarget().bindWrite(true);
            }
            Window window = this.minecraft.getWindow();
            RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            Matrix4f orthoMat = Matrix4f.orthographic(0.0F, (float) (window.getWidth() / window.getGuiScale()), 0.0F,
                                                      (float) (window.getHeight() / window.getGuiScale()), 1_000.0F,
                                                      ForgeHooksClient.getGuiFarPlane());
            RenderSystem.setProjectionMatrix(orthoMat);
            PoseStack internalMat = RenderSystem.getModelViewStack();
            internalMat.setIdentity();
            internalMat.translate(0, 0, 1_000 - ForgeHooksClient.getGuiFarPlane());
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            PoseStack matrices = new PoseStack();
            if (renderLevel && this.minecraft.level != null) {
                this.minecraft.getProfiler().popPush("gui");
                if (this.minecraft.player != null) {
                    float interpPortalTime = Mth.lerp(partialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
                    if (interpPortalTime > 0.0F &&
                        this.minecraft.player.hasEffect(MobEffects.CONFUSION) &&
                        this.minecraft.options.screenEffectScale < 1.0F) {
                        this.renderConfusionOverlay(interpPortalTime * (1.0F - this.minecraft.options.screenEffectScale));
                    }
                }
                if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                    this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(),
                                                       this.minecraft.getWindow().getGuiScaledHeight(), partialTicks);
                }
                //Removed these two lines of code from the if block above and added them here
                this.minecraft.gui.render(matrices, partialTicks);
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                this.minecraft.getProfiler().pop();
            }
            if (this.minecraft.getOverlay() != null) {
                try {
                    this.minecraft.getOverlay().render(matrices, mouseX, mouseY, this.minecraft.getDeltaFrameTime());
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable(t, "Rendering overlay");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
                    crashReportCategory.setDetail("Overlay name", () -> this.minecraft.getOverlay().getClass().getCanonicalName());
                    throw new ReportedException(crashReport);
                }
            }
            else if (this.minecraft.screen != null) {
                try {
                    ForgeHooksClient.drawScreen(this.minecraft.screen, matrices, mouseX, mouseY, this.minecraft.getDeltaFrameTime());
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable(t, "Rendering screen");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Screen render details");
                    crashReportCategory.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    crashReportCategory.setDetail("Mouse location",
                                                  () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", mouseX, mouseY,
                                                                      this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos()));
                    crashReportCategory.setDetail("Screen size",
                                                  () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
                                                                      this.minecraft.getWindow().getGuiScaledWidth(),
                                                                      this.minecraft.getWindow().getGuiScaledHeight(),
                                                                      this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(),
                                                                      this.minecraft.getWindow().getGuiScale()));
                    throw new ReportedException(crashReport);
                }
                try {
                    if (this.minecraft.screen != null) {
                        this.minecraft.screen.handleDelayedNarration();
                    }
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable(t, "Narrating screen");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Screen details");
                    crashReportCategory.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                    throw new ReportedException(crashReport);
                }
            }
        }
    }

    @Shadow
    protected abstract void renderConfusionOverlay(float pScalar);

    @Shadow
    protected abstract void renderItemActivationAnimation(int pWidthsp, int pHeightScaled, float pPartialTicks);

    /**
     * @author TheGreatWolf
     * @reason Remove references to hand rendering, as even if we cancel the event, a lot of calculations still run.
     */
    @Overwrite
    public void renderLevel(float partialTicks, long finishTimeNano, PoseStack matrices) {
        this.lightTexture.updateLightTexture(partialTicks);
        assert this.minecraft.player != null;
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        this.pick(partialTicks);
        this.minecraft.getProfiler().push("center");
        boolean shouldRenderOutline = this.shouldRenderBlockOutline();
        this.minecraft.getProfiler().popPush("camera");
        Camera camera = this.mainCamera;
        this.renderDistance = this.minecraft.options.getEffectiveRenderDistance() * 16;
        PoseStack posestack = new PoseStack();
        double fov = this.getFov(camera, partialTicks, true);
        posestack.last().pose().multiply(this.getProjectionMatrix(fov));
        this.bobHurt(posestack, partialTicks);
        float portalWarp = Mth.lerp(partialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime) *
                           this.minecraft.options.screenEffectScale *
                           this.minecraft.options.screenEffectScale;
        if (portalWarp > 0.0F) {
            int i = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float f1 = 5.0F / (portalWarp * portalWarp + 5.0F) - portalWarp * 0.04F;
            f1 *= f1;
            Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            posestack.mulPose(vector3f.rotationDegrees((this.tick + partialTicks) * i));
            posestack.scale(1.0F / f1, 1.0F, 1.0F);
            float f2 = -(this.tick + partialTicks) * i;
            posestack.mulPose(vector3f.rotationDegrees(f2));
        }
        Matrix4f matrix4f = posestack.last().pose();
        this.resetProjectionMatrix(matrix4f);
        assert this.minecraft.level != null;
        camera.setup(this.minecraft.level,
                     this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity(),
                     !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), partialTicks);
        IPoseStackPatch extendedMatrix = MathHelper.getExtendedMatrix(matrices);
//        extendedMatrix.mulPoseZ(0);
        extendedMatrix.mulPoseX(camera.getXRot());
        extendedMatrix.mulPoseY(camera.getYRot() + 180.0F);
        Matrix3f matrix3f = matrices.last().normal().copy();
        if (matrix3f.invert()) {
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }
        this.minecraft.levelRenderer.prepareCullFrustum(matrices, camera.getPosition(),
                                                        this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov)));
        this.minecraft.levelRenderer.renderLevel(matrices, partialTicks, finishTimeNano, shouldRenderOutline, camera, (GameRenderer) (Object) this,
                                                 this.lightTexture, matrix4f);
        this.minecraft.getProfiler().pop();
    }

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f pMatrix);

    /**
     * @author TheGreatWolf
     * @reason Resize shaders.
     */
    @Overwrite
    public void resize(int width, int height) {
        if (this.postEffect != null) {
            this.postEffect.resize(width, height);
        }
        for (PostChain shader : this.postEffects.values()) {
            shader.resize(width, height);
        }
        this.minecraft.levelRenderer.resize(width, height);
    }

    @Shadow
    protected abstract boolean shouldRenderBlockOutline();

    @Override
    public void shutdownAllShaders() {
        for (PostChain effect : this.postEffects.values()) {
            effect.close();
        }
        this.postEffects.clear();
    }

    @Override
    public void shutdownShader(int shaderId) {
        PostChain shader = this.postEffects.remove(shaderId);
        if (shader != null) {
            shader.close();
        }
    }

    @Shadow
    protected abstract void tryTakeScreenshotIfNeeded();
}
