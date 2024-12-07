package tgw.evolution.mixin;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.client.gui.overlays.Overlays;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.client.util.Shader;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.patches.PatchGameRenderer;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.I2OHashMap;
import tgw.evolution.util.collection.maps.I2OMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.MathHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public abstract class Mixin_CF_GameRenderer implements PatchGameRenderer {

    @Unique private static final Vector3f NAUSEA_VECTOR = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
    @Shadow @Final public static int EFFECT_NONE;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private static @Nullable ShaderInstance blockShader;
    @Shadow private static @Nullable ShaderInstance newEntityShader;
    @Shadow private static @Nullable ShaderInstance particleShader;
    @Shadow private static @Nullable ShaderInstance positionColorLightmapShader;
    @Shadow private static @Nullable ShaderInstance positionColorShader;
    @Shadow private static @Nullable ShaderInstance positionColorTexLightmapShader;
    @Shadow private static @Nullable ShaderInstance positionColorTexShader;
    @Shadow private static @Nullable ShaderInstance positionShader;
    @Shadow private static @Nullable ShaderInstance positionTexColorNormalShader;
    @Shadow private static @Nullable ShaderInstance positionTexColorShader;
    @Shadow private static @Nullable ShaderInstance positionTexLightmapColorShader;
    @Shadow private static @Nullable ShaderInstance positionTexShader;
    @Shadow private static @Nullable ShaderInstance rendertypeArmorCutoutNoCullShader;
    @Shadow private static @Nullable ShaderInstance rendertypeArmorEntityGlintShader;
    @Shadow private static @Nullable ShaderInstance rendertypeArmorGlintShader;
    @Shadow private static @Nullable ShaderInstance rendertypeBeaconBeamShader;
    @Shadow private static @Nullable ShaderInstance rendertypeCrumblingShader;
    @Shadow private static @Nullable ShaderInstance rendertypeCutoutMippedShader;
    @Shadow private static @Nullable ShaderInstance rendertypeCutoutShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEndGatewayShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEndPortalShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEnergySwirlShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityAlphaShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityCutoutNoCullShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityCutoutNoCullZOffsetShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityCutoutShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityDecalShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityGlintDirectShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityGlintShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityNoOutlineShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityShadowShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntitySmoothCutoutShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntitySolidShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityTranslucentCullShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEntityTranslucentShader;
    @Shadow private static @Nullable ShaderInstance rendertypeEyesShader;
    @Shadow private static @Nullable ShaderInstance rendertypeGlintDirectShader;
    @Shadow private static @Nullable ShaderInstance rendertypeGlintShader;
    @Shadow private static @Nullable ShaderInstance rendertypeGlintTranslucentShader;
    @Shadow private static @Nullable ShaderInstance rendertypeItemEntityTranslucentCullShader;
    @Shadow private static @Nullable ShaderInstance rendertypeLeashShader;
    @Shadow private static @Nullable ShaderInstance rendertypeLightningShader;
    @Shadow private static @Nullable ShaderInstance rendertypeLinesShader;
    @Shadow private static @Nullable ShaderInstance rendertypeOutlineShader;
    @Shadow private static @Nullable ShaderInstance rendertypeSolidShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTextIntensitySeeThroughShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTextIntensityShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTextSeeThroughShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTextShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTranslucentMovingBlockShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTranslucentNoCrumblingShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTranslucentShader;
    @Shadow private static @Nullable ShaderInstance rendertypeTripwireShader;
    @Shadow private static @Nullable ShaderInstance rendertypeWaterMaskShader;
    @Shadow private float darkenWorldAmount;
    @Shadow private float darkenWorldAmountO;
    @Shadow public boolean effectActive;
    @Shadow private int effectIndex;
    @Shadow private @Nullable ItemStack itemActivationItem;
    @Shadow private float itemActivationOffX;
    @Shadow private float itemActivationOffY;
    @Shadow private int itemActivationTicks;
    @Mutable @Shadow @Final @RestoreFinal public ItemInHandRenderer itemInHandRenderer;
    @Shadow private long lastActiveTime;
    @Mutable @Shadow @Final @RestoreFinal private LightTexture lightTexture;
    @Mutable @Shadow @Final @RestoreFinal private Camera mainCamera;
    @Mutable @Shadow @Final @RestoreFinal private MapRenderer mapRenderer;
    @Unique private final PoseStack matrices = new PoseStack();
    @Mutable @Shadow @Final @RestoreFinal private Minecraft minecraft;
    @Mutable @Shadow @Final @RestoreFinal private OverlayTexture overlayTexture;
    @Shadow private @Nullable PostChain postEffect;
    @Unique private final I2OMap<PostChain> postEffects = new I2OHashMap<>();
    @Unique private final Matrix4f projMatrix = new Matrix4f();
    @Unique private final PoseStack projectionMatrices = new PoseStack();
    @DeleteField @Shadow @Final private Random random;
    @Unique private final FastRandom random_;
    @Shadow private boolean renderBlockOutline;
    @Mutable @Shadow @Final @RestoreFinal private RenderBuffers renderBuffers;
    @Shadow private float renderDistance;
    @Shadow private boolean renderHand;
    @Mutable @Shadow @Final @RestoreFinal private ResourceManager resourceManager;
    @DeleteField @Shadow @Final private Map<String, ShaderInstance> shaders;
    @Unique private final O2OMap<String, ShaderInstance> shaders_;
    @Shadow private int tick;
    @Shadow private float zoom;
    @Shadow private float zoomX;
    @Shadow private float zoomY;

    @ModifyConstructor
    public Mixin_CF_GameRenderer(Minecraft minecraft, ResourceManager resourceManager, RenderBuffers renderBuffers) {
        this.random_ = new FastRandom();
        this.renderHand = true;
        this.renderBlockOutline = true;
        this.lastActiveTime = Util.getMillis();
        this.overlayTexture = new OverlayTexture();
        this.zoom = 1.0f;
        this.effectIndex = EFFECT_NONE;
        this.mainCamera = new Camera();
        this.shaders_ = new O2OHashMap<>();
        this.minecraft = minecraft;
        this.resourceManager = resourceManager;
        this.itemInHandRenderer = minecraft.getItemInHandRenderer();
        this.mapRenderer = new MapRenderer(minecraft.getTextureManager());
        this.lightTexture = new LightTextureEv((GameRenderer) (Object) this, minecraft);
        this.renderBuffers = renderBuffers;
        this.postEffect = null;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void bobHurt(PoseStack matrices, float partialTicks) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity entity) {
            float hurtTime = entity.hurtTime - partialTicks;
            if (entity.isDeadOrDying()) {
                matrices.mulPoseZ(40.0F - 8_000.0F / (Math.min(entity.deathTime + partialTicks, 20.0F) + 200.0F));
            }
            if (hurtTime < 0.0F) {
                return;
            }
            hurtTime /= entity.hurtDuration;
            hurtTime = Mth.sin(hurtTime * hurtTime * hurtTime * hurtTime * Mth.PI);
            float hurtDir = entity.hurtDir;
            matrices.mulPoseY(-hurtDir);
            matrices.mulPoseZ(-hurtTime * 14.0F);
            matrices.mulPoseY(hurtDir);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void displayItemActivation(ItemStack itemStack) {
        this.itemActivationItem = itemStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random_.nextFloat() * 2.0F - 1.0F;
        this.itemActivationOffY = this.random_.nextFloat() * 2.0F - 1.0F;
    }

    @Shadow
    public abstract float getDepthFar();

    @Shadow
    protected abstract double getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);

    /**
     * @author TheGreatWolf
     * @reason Modify the near clipping plane to improve first person camera; Avoid most allocations.
     */
    @Overwrite
    public Matrix4f getProjectionMatrix(double fov) {
        Matrix4f matrix = this.projMatrix;
        matrix.setIdentity();
        if (this.zoom != 1.0F) {
            matrix.multiplyWithTranslation(this.zoomX, -this.zoomY, 0);
            matrix.scale(this.zoom, this.zoom, 1.0F);
        }
        matrix.multiplyWithPerspective(fov, this.minecraft.getWindow().getWidth() / (float) this.minecraft.getWindow().getHeight(), 0.012_5f, this.getDepthFar());
        return matrix;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public @Nullable ShaderInstance getShader(@Nullable String name) {
        return name == null ? null : this.shaders_.get(name);
    }

    /**
     * @author TheGreatWolf
     * @reason Remove {@code this.effectActive = false} when the shader fails to load, preventing other shaders from being unloaded.
     */
    @Overwrite
    private void loadEffect(ResourceLocation resLoc) {
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
    public void loadShader(@Shader int shaderId, ResourceLocation resLoc) {
        if (!this.postEffects.containsKey(shaderId)) {
            try {
                PostChain shader = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), resLoc);
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
                double reachDistance = this.minecraft.player.getAttributeValue(EvolutionAttributes.REACH_DISTANCE);
                Vec3 cameraPos = entity.getEyePosition(partialTicks);
                this.minecraft.hitResult = entity.pick(reachDistance, partialTicks, false);
                HitResult hitResult = this.minecraft.hitResult;
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    reachDistance = Math.sqrt(cameraPos.distanceToSqr(hitResult.x(), hitResult.y(), hitResult.z()));
                }
                EntityHitResult leftRayTrace = MathHelper.rayTraceEntitiesFromEyes(this.minecraft.player, partialTicks, reachDistance);
                if (leftRayTrace != null) {
                    this.minecraft.hitResult = leftRayTrace;
                    this.minecraft.crosshairPickEntity = leftRayTrace.getEntity();
                    EvolutionClient.leftRayTrace = leftRayTrace;
                    EvolutionClient.leftPointedEntity = leftRayTrace.getEntity();
                }
                else {
                    this.minecraft.crosshairPickEntity = null;
                    EvolutionClient.leftRayTrace = null;
                    EvolutionClient.leftPointedEntity = null;
                }
                EvolutionClient.rightRayTrace = null;
                EvolutionClient.rightPointedEntity = null;
                this.minecraft.getProfiler().pop();
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private ShaderInstance preloadShader(ResourceProvider resourceProvider, String name, VertexFormat format) {
        try {
            ShaderInstance shaderInstance = new ShaderInstance(resourceProvider, name, format);
            this.shaders_.put(name, shaderInstance);
            return shaderInstance;
        }
        catch (Exception e) {
            throw new IllegalStateException("could not preload shader " + name, e);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("resource")
    @Overwrite
    public void reloadShaders(ResourceManager resourceManager) {
        RenderSystem.assertOnRenderThread();
        OList<Program> programList = new OArrayList<>();
        programList.addAll(Program.Type.FRAGMENT.getPrograms().values());
        programList.addAll(Program.Type.VERTEX.getPrograms().values());
        for (int i = 0, len = programList.size(); i < len; ++i) {
            programList.get(i).close();
        }
        OList<Pair<ShaderInstance, Consumer<ShaderInstance>>> list = new OArrayList<>(this.shaders_.size());
        try {
            list.add(Pair.of(new ShaderInstance(resourceManager, "block", DefaultVertexFormat.BLOCK), shaderInstance -> blockShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "new_entity", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> newEntityShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "particle", DefaultVertexFormat.PARTICLE), shaderInstance -> particleShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position", DefaultVertexFormat.POSITION), shaderInstance -> positionShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_color", DefaultVertexFormat.POSITION_COLOR), shaderInstance -> positionColorShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_color_lightmap", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), shaderInstance -> positionColorLightmapShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX), shaderInstance -> positionColorTexShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shaderInstance -> positionColorTexLightmapShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_tex", DefaultVertexFormat.POSITION_TEX), shaderInstance -> positionTexShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR), shaderInstance -> positionTexColorShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_tex_color_normal", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), shaderInstance -> positionTexColorNormalShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "position_tex_lightmap_color", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR), shaderInstance -> positionTexLightmapColorShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_solid", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeSolidShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_cutout_mipped", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeCutoutMippedShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_cutout", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeCutoutShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_translucent", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeTranslucentShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_translucent_moving_block", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeTranslucentMovingBlockShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_translucent_no_crumbling", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeTranslucentNoCrumblingShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeArmorCutoutNoCullShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_solid", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntitySolidShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_cutout", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityCutoutShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityCutoutNoCullShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityCutoutNoCullZOffsetShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeItemEntityTranslucentCullShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityTranslucentCullShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_translucent", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityTranslucentShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntitySmoothCutoutShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_beacon_beam", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeBeaconBeamShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_decal", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityDecalShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_no_outline", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityNoOutlineShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_shadow", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityShadowShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_alpha", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEntityAlphaShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_eyes", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEyesShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_energy_swirl", DefaultVertexFormat.NEW_ENTITY), shaderInstance -> rendertypeEnergySwirlShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), shaderInstance -> rendertypeLeashShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_water_mask", DefaultVertexFormat.POSITION), shaderInstance -> rendertypeWaterMaskShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_outline", DefaultVertexFormat.POSITION_COLOR_TEX), shaderInstance -> rendertypeOutlineShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_armor_glint", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeArmorGlintShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_armor_entity_glint", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeArmorEntityGlintShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_glint_translucent", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeGlintTranslucentShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_glint", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeGlintShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_glint_direct", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeGlintDirectShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_glint", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeEntityGlintShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_entity_glint_direct", DefaultVertexFormat.POSITION_TEX), shaderInstance -> rendertypeEntityGlintDirectShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shaderInstance -> rendertypeTextShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shaderInstance -> rendertypeTextIntensityShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shaderInstance -> rendertypeTextSeeThroughShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shaderInstance -> rendertypeTextIntensitySeeThroughShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_lightning", DefaultVertexFormat.POSITION_COLOR), shaderInstance -> rendertypeLightningShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_tripwire", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeTripwireShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_end_portal", DefaultVertexFormat.POSITION), shaderInstance -> rendertypeEndPortalShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_end_gateway", DefaultVertexFormat.POSITION), shaderInstance -> rendertypeEndGatewayShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL), shaderInstance -> rendertypeLinesShader = shaderInstance));
            list.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_crumbling", DefaultVertexFormat.BLOCK), shaderInstance -> rendertypeCrumblingShader = shaderInstance));
        }
        catch (IOException e) {
            for (int i = 0, len = list.size(); i < len; ++i) {
                list.get(i).getFirst().close();
            }
            throw new RuntimeException("could not reload shaders", e);
        }
        this.shutdownShaders();
        for (int i = 0, len = list.size(); i < len; ++i) {
            Pair<ShaderInstance, Consumer<ShaderInstance>> pair = list.get(i);
            ShaderInstance shader = pair.getFirst();
            this.shaders_.put(shader.getName(), shader);
            pair.getSecond().accept(shader);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Make gui render even when {@link net.minecraft.client.Options#hideGui} is {@code true} since every render overlay of the gui already
     * makes a check of its own. This allows us to render gui elements which should always remain on the screen (such as helmet overlays).
     */
    @Overwrite
    public void render(float partialTicks, long startTime, boolean renderLevel) {
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
            Window window = this.minecraft.getWindow();
            int guiScaledWidth = window.getGuiScaledWidth();
            int guiScaledHeight = window.getGuiScaledHeight();
            int width = window.getWidth();
            int height = window.getHeight();
            double guiScale = window.getGuiScale();
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * guiScaledWidth / window.getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * guiScaledHeight / window.getScreenHeight());
            RenderSystem.viewport(0, 0, width, height);
            boolean setupHud = false;
            if (renderLevel && this.minecraft.level != null) {
                this.minecraft.getProfiler().push("level");
                this.renderLevel(partialTicks, startTime, this.matrices.reset());
                this.tryTakeScreenshotIfNeeded();
                this.minecraft.lvlRenderer().doEntityOutline();
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                Matrix4f orthoMat = Matrix4f.orthographic(0.0F, (float) (width / guiScale), 0.0F, (float) (height / guiScale), 1_000, 3_000);
                RenderSystem.setProjectionMatrix(orthoMat);
                PoseStack internalMat = RenderSystem.getModelViewStack();
                internalMat.setIdentity();
                internalMat.translate(0, 0, -2_000);
                RenderSystem.applyModelViewMatrix();
                Lighting.setupFor3DItems();
                setupHud = true;
                Overlays.renderAllGame(this.minecraft, (EvolutionGui) this.minecraft.gui, this.matrices.reset(), partialTicks, guiScaledWidth, guiScaledHeight);
                if (this.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableTexture();
                    RenderSystem.resetTextureMatrix();
                    if (this.postEffect != null) {
                        this.postEffect.process(partialTicks);
                    }
                    I2OMap<PostChain> postEffects = this.postEffects;
                    for (long it = postEffects.beginIteration(); postEffects.hasNextIteration(it); it = postEffects.nextEntry(it)) {
                        postEffects.getIterationValue(it).process(partialTicks);
                    }
                }
                this.minecraft.getMainRenderTarget().bindWrite(true);
            }
            if (!setupHud) {
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                Matrix4f orthoMat = Matrix4f.orthographic(0.0F, (float) (width / guiScale), 0.0F, (float) (height / guiScale), 1_000, 3_000);
                RenderSystem.setProjectionMatrix(orthoMat);
                PoseStack internalMat = RenderSystem.getModelViewStack();
                internalMat.setIdentity();
                internalMat.translate(0, 0, -2_000);
                RenderSystem.applyModelViewMatrix();
                Lighting.setupFor3DItems();
            }
            this.matrices.reset();
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
                    this.renderItemActivationAnimation(guiScaledWidth, guiScaledHeight, partialTicks);
                }
                //Removed these two lines of code from the if block above and added them here
                this.minecraft.gui.render(this.matrices, partialTicks);
                RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                this.minecraft.getProfiler().pop();
            }
            if (this.minecraft.getOverlay() != null) {
                try {
                    this.minecraft.getOverlay().render(this.matrices, mouseX, mouseY, this.minecraft.getDeltaFrameTime());
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
                    this.minecraft.screen.render(this.matrices, mouseX, mouseY, this.minecraft.getDeltaFrameTime());
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
                                                                      guiScaledWidth, guiScaledHeight, width, height, guiScale));
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
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void renderLevel(float partialTicks, long endTickTime, PoseStack matrices) {
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
        double fov = this.getFov(camera, partialTicks, true);
        EvolutionClient.fov = (float) fov;
        PoseStack projMatrices = this.projectionMatrices.reset();
        projMatrices.last().pose().multiply(this.getProjectionMatrix(fov));
        this.bobHurt(projMatrices, partialTicks);
        float portalWarp = Mth.lerp(partialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime) *
                           this.minecraft.options.screenEffectScale *
                           this.minecraft.options.screenEffectScale;
        if (portalWarp > 0.0F) {
            int i = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float f1 = 5.0F / (portalWarp * portalWarp + 5.0F) - portalWarp * 0.04F;
            f1 *= f1;
            projMatrices.mulPose(NAUSEA_VECTOR.rotationDegrees((this.tick + partialTicks) * i));
            projMatrices.scale(1.0F / f1, 1.0F, 1.0F);
            float f2 = -(this.tick + partialTicks) * i;
            projMatrices.mulPose(NAUSEA_VECTOR.rotationDegrees(f2));
        }
        Matrix4f projMatrix = projMatrices.last().pose();
        this.resetProjectionMatrix(projMatrix);
        assert this.minecraft.level != null;
        camera.setup(this.minecraft.level,
                     this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity(),
                     !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), partialTicks);
        matrices.mulPoseX(camera.getXRot());
        matrices.mulPoseY(camera.getYRot() + 180.0F);
        Matrix3f matrix3f = matrices.last().normal().copy();
        if (matrix3f.invert()) {
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }
        EvLevelRenderer levelRenderer = this.minecraft.lvlRenderer();
        levelRenderer.prepareCullFrustum(matrices, camera.getPosition(), this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov)));
        levelRenderer.renderLevel(matrices, partialTicks, endTickTime, shouldRenderOutline, camera, (GameRenderer) (Object) this, this.lightTexture, projMatrix);
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
        I2OMap<PostChain> postEffects = this.postEffects;
        for (long it = postEffects.beginIteration(); postEffects.hasNextIteration(it); it = postEffects.nextEntry(it)) {
            postEffects.getIterationValue(it).resize(width, height);
        }
        this.minecraft.lvlRenderer().resize(width, height);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private boolean shouldRenderBlockOutline() {
        assert this.minecraft.level != null;
        assert this.minecraft.gameMode != null;
        if (!this.renderBlockOutline) {
            return false;
        }
        Entity camera = this.minecraft.player;
        if (!this.minecraft.options.hideGui && camera instanceof Player player) {
            if (!player.getAbilities().mayBuild) {
                ItemStack itemStack = ((LivingEntity) camera).getMainHandItem();
                HitResult hitResult = this.minecraft.hitResult;
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    int x = blockHitResult.posX();
                    int y = blockHitResult.posY();
                    int z = blockHitResult.posZ();
                    BlockState blockState = this.minecraft.level.getBlockState_(x, y, z);
                    if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                        return blockState.getMenuProvider(this.minecraft.level, new BlockPos(x, y, z)) != null;
                    }
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, new BlockPos(x, y, z), false);
                    Registry<Block> registry = this.minecraft.level.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
                    return !itemStack.isEmpty() && (itemStack.hasAdventureModeBreakTagForBlock(registry, blockInWorld) || itemStack.hasAdventureModePlaceTagForBlock(registry, blockInWorld));
                }
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void shutdownAllShaders() {
        I2OMap<PostChain> postEffects = this.postEffects;
        for (long it = postEffects.beginIteration(); postEffects.hasNextIteration(it); it = postEffects.nextEntry(it)) {
            postEffects.getIterationValue(it).close();
        }
        postEffects.clear();
    }

    @Override
    public void shutdownShader(@Shader int shaderId) {
        PostChain shader = this.postEffects.remove(shaderId);
        if (shader != null) {
            shader.close();
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void shutdownShaders() {
        RenderSystem.assertOnRenderThread();
        O2OMap<String, ShaderInstance> shaders = this.shaders_;
        for (long it = shaders.beginIteration(); shaders.hasNextIteration(it); it = shaders.nextEntry(it)) {
            shaders.getIterationValue(it).close();
        }
        shaders.clear();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private void takeAutoScreenshot(Path path) {
        EvLevelRenderer levelRenderer = this.minecraft.lvlRenderer();
        if (levelRenderer.countRenderedChunks() > 10 && levelRenderer.hasRenderedAllChunks()) {
            NativeImage screenshot = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
            Util.ioPool().execute(() -> {
                int i = screenshot.getWidth();
                int j = screenshot.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                }
                else {
                    l = (j - i) / 2;
                    j = i;
                }
                try {
                    NativeImage image = new NativeImage(64, 64, false);
                    try {
                        screenshot.resizeSubRectTo(k, l, i, j, image);
                        image.writeToFile(path);
                    }
                    catch (Throwable t) {
                        try {
                            image.close();
                        }
                        catch (Throwable s) {
                            t.addSuppressed(s);
                        }
                        throw t;
                    }

                    image.close();
                }
                catch (IOException e) {
                    LOGGER.warn("Couldn't save auto screenshot", e);
                }
                finally {
                    screenshot.close();
                }
            });
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            assert this.minecraft.player != null;
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        this.mainCamera.tick();
        ++this.tick;
//        this.itemInHandRenderer.tick();
        this.minecraft.lvlRenderer().tickRain(this.mainCamera);
        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05F;
            if (this.darkenWorldAmount > 1) {
                this.darkenWorldAmount = 1.0F;
            }
        }
        else if (this.darkenWorldAmount > 0) {
            this.darkenWorldAmount -= 0.012_5F;
        }
        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    @Shadow
    protected abstract void tickFov();

    @Shadow
    protected abstract void tryTakeScreenshotIfNeeded();
}
