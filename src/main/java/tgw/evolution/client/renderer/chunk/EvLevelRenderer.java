package tgw.evolution.client.renderer.chunk;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11C;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.client.util.Blending;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.mixin.AccessorRenderSystem;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.AdvancedEntityHitResult;
import tgw.evolution.util.collection.OArrayFIFOQueue;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.I2OHashMap;
import tgw.evolution.util.collection.maps.I2OMap;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;
import tgw.evolution.util.constants.RenderLayer;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.VectorUtil;
import tgw.evolution.world.EvBlockDestructionProgress;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Environment(EnvType.CLIENT)
public class EvLevelRenderer implements IKeyedReloadListener, ResourceManagerReloadListener, AutoCloseable {
    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
    private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
    private static final OList<ResourceLocation> DEPENDENCY = OList.of(ReloadListernerKeys.TEXTURES);
    private static final ThreadLocal<OArrayFIFOQueue<RenderChunkInfo>> QUEUE_CACHE = ThreadLocal.withInitial(OArrayFIFOQueue::new);
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final SheetedDecalTextureGenerator[] breakingBuffers = new SheetedDecalTextureGenerator[10];
    private final BufferHolder bufferHolder;
    private @Nullable EvChunkRenderDispatcher chunkRenderDispatcher;
    private @Nullable VertexBuffer cloudBuffer;
    private @Nullable RenderTarget cloudsTarget;
    private final RenderChunkInfoComparator comparator = new RenderChunkInfoComparator();
    private final Frustum cullingFrustum = new Frustum(new Matrix4f(), new Matrix4f());
    private final I2OMap<EvBlockDestructionProgress> destroyingBlocks = new I2OHashMap<>();
    private final L2OMap<SortedSet<EvBlockDestructionProgress>> destructionProgress = new L2OHashMap<>();
    private @Nullable PostChain entityEffect;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private @Nullable RenderTarget entityTarget;
    private boolean generateClouds = true;
    /**
     * Global block entities; these are always rendered, even if off-screen.
     * Any block entity is added to this if {@link
     * net.minecraft.client.renderer.blockentity.BlockEntityRenderer#shouldRenderOffScreen(net.minecraft.world.level.block.entity.BlockEntity)}
     * returns {@code true}.
     */
    private final RSet<BlockEntity> globalBlockEntities = new RHashSet<>();
    private @Nullable RenderTarget itemEntityTarget;
    private int lastCameraChunkX = Integer.MIN_VALUE;
    private int lastCameraChunkY = Integer.MIN_VALUE;
    private int lastCameraChunkZ = Integer.MIN_VALUE;
    private @Nullable Future<?> lastFullRenderChunkUpdate;
    private int lastTransparencyTick;
    private int lastViewDistance = -1;
    /**
     * Bits as in {@link RenderLayer}
     */
    private byte layersInFrustum;
    private @Nullable ClientLevel level;
    private final LevelEventListener listener;
    private final Minecraft mc;
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);
    private boolean needsFullRenderChunkUpdate = true;
    private final AtomicLong nextFullUpdateMillis = new AtomicLong(0L);
    private @Nullable RenderTarget particlesTarget;
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    private double prevCamX = Double.MIN_VALUE;
    private double prevCamY = Double.MIN_VALUE;
    private double prevCamZ = Double.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private @Nullable CloudStatus prevCloudsType;
    private final FastRandom rainRandom = new FastRandom();
    private final float[] rainSizeX = new float[1_024];
    private final float[] rainSizeZ = new float[1_024];
    private int rainSoundTime;
    private final OList<EvChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks = new OArrayList<>();
    private final EvRenderRegionCache renderCache = new EvRenderRegionCache();
    private volatile @Nullable RenderChunkStorage renderChunkStorage;
    private final OList<EvChunkRenderDispatcher.RenderChunk> renderChunksInFrustum = new OArrayList<>();
    private boolean renderOnThread;
    private int renderedEntities;
    private final SkyFogSetup skyFog = new SkyFogSetup();
    private int ticks;
    private @Nullable RenderTarget translucentTarget;
    private @Nullable PostChain transparencyChain;
    private @Nullable EvViewArea viewArea;
    private @Nullable RenderTarget weatherTarget;
    private double xTransparentOld;
    private double yTransparentOld;
    private double zTransparentOld;

    public EvLevelRenderer(Minecraft mc, RenderBuffers buffers) {
        this.mc = mc;
        this.listener = new LevelEventListener(mc);
        this.entityRenderDispatcher = mc.getEntityRenderDispatcher();
        this.blockEntityRenderDispatcher = mc.getBlockEntityRenderDispatcher();
        this.bufferHolder = new BufferHolder(buffers);
        for (int x = 0; x < 32; ++x) {
            for (int z = 0; z < 32; ++z) {
                float dz = z - 16;
                float dx = x - 16;
                float dist = Mth.sqrt(dz * dz + dx * dx);
                this.rainSizeX[x << 5 | z] = -dx / dist;
                this.rainSizeZ[x << 5 | z] = dz / dist;
            }
        }
        for (int i = 0, len = this.breakingBuffers.length; i < len; i++) {
            //noinspection ObjectAllocationInLoop
            Matrix4f pose = new Matrix4f();
            pose.setIdentity();
            //noinspection ObjectAllocationInLoop
            Matrix3f normal = new Matrix3f();
            normal.setIdentity();
            //noinspection ObjectAllocationInLoop,DataFlowIssue
            this.breakingBuffers[i] = new SheetedDecalTextureGenerator(null, pose, normal);
        }
    }

    public static void addChainedFilledBoxVertices(VertexConsumer builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        builder.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static int getLightColor(BlockAndTintGetter level, int x, int y, int z) {
        return getLightColor(level, level.getBlockState_(x, y, z), x, y, z, false);
    }

    /**
     * Returns the light color in the specified block in the following format:<br>
     * <br>
     * Bit 0 ~ 3: red component, from 0 to 15; <br>
     * Bit 4 ~ 7: green component, from 0 to 15; <br>
     * Bit 16 ~ 19: skylight component, from 0 to 15; <br>
     * Bit 20 ~ 23: blue component, from 0 to 15; <br>
     * <br>
     * Special case: -1 if the block is opaque and needsToCheckForOpaqueness is true.
     */
    public static int getLightColor(BlockAndTintGetter level, BlockState state, int x, int y, int z, boolean needsToCheckForOpaqueness) {
        if (state.emissiveRendering_(level, x, y, z)) {
            return state.getEmissiveLightColor(level, x, y, z);
        }
        if (needsToCheckForOpaqueness && level.getBlockState_(x, y, z).getLightBlock_(level, x, y, z) == level.getMaxLightLevel()) {
            return -1;
        }
        long packed = BlockPos.asLong(x, y, z);
        LevelLightEngine lightEngine = level.getLightEngine();
        int sl = lightEngine.getLayerListener(LightLayer.SKY).getLightValue_(packed);
        int bl = lightEngine.getLayerListener(LightLayer.BLOCK).getLightValue_(packed);
        int rbl = bl & 31;
        int gbl = bl >>> 5 & 31;
        int bbl = bl >>> 10 & 31;
        return DynamicLights.componentsToLightmap(rbl, gbl, bbl, sl);
    }

    public static void renderLineBox(PoseStack matrices, VertexConsumer builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        renderLineBox(matrices, builder, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a, r, g, b);
    }

    public static void renderLineBox(PoseStack matrices, VertexConsumer builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a, float r2, float g2, float b2) {
        Matrix4f pose = matrices.last().pose();
        Matrix3f normal = matrices.last().normal();
        float f = (float) minX;
        float f1 = (float) minY;
        float f2 = (float) minZ;
        float f3 = (float) maxX;
        float f4 = (float) maxY;
        float f5 = (float) maxZ;
        builder.vertex(pose, f, f1, f2).color(r, g2, b2, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f3, f1, f2).color(r, g2, b2, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f, f1, f2).color(r2, g, b2, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f, f4, f2).color(r2, g, b2, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f, f1, f2).color(r2, g2, b, a).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, f, f1, f5).color(r2, g2, b, a).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, f3, f1, f2).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f3, f4, f2).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f3, f4, f2).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(pose, f, f4, f2).color(r, g, b, a).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(pose, f, f4, f2).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, f, f4, f5).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, f, f4, f5).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(pose, f, f1, f5).color(r, g, b, a).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(pose, f, f1, f5).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f3, f1, f5).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f3, f1, f5).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(pose, f3, f1, f2).color(r, g, b, a).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(pose, f, f4, f5).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f3, f4, f5).color(r, g, b, a).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(pose, f3, f1, f5).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f3, f4, f5).color(r, g, b, a).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(pose, f3, f4, f2).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(pose, f3, f4, f5).color(r, g, b, a).normal(normal, 0, 0, 1).endVertex();
    }

    /**
     * Asserts that the specified {@code poseStack} is {@linkplain com.mojang.blaze3d.vertex.PoseStack#clear() clear}.
     *
     * @throws java.lang.IllegalStateException if the specified {@code poseStack} is not clear
     */
    private static void checkPoseStack(PoseStack matrices) {
        if (!matrices.clear()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private static void renderEndSky(PoseStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        for (int i = 0; i < 6; ++i) {
            matrices.pushPose();
            switch (i) {
                case 1 -> matrices.mulPoseX(90);
                case 2 -> matrices.mulPoseX(-90.0F);
                case 3 -> matrices.mulPoseX(180.0F);
                case 4 -> matrices.mulPoseZ(90.0F);
                case 5 -> matrices.mulPoseZ(-90.0F);
            }
            Matrix4f matrix = matrices.last().pose();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            builder.vertex(matrix, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            builder.vertex(matrix, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            builder.vertex(matrix, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            builder.vertex(matrix, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            tesselator.end();
            matrices.popPose();
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void renderShape(PoseStack matrices, VertexConsumer builder, VoxelShape shape, double x, double y, double z, float r, float g, float b, float a) {
        PoseStack.Pose last = matrices.last();
        shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            float dx = (float) (x1 - x0);
            float dy = (float) (y1 - y0);
            float dz = (float) (z1 - z0);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;
            builder.vertex(last.pose(), (float) (x0 + x), (float) (y0 + y), (float) (z0 + z))
                   .color(r, g, b, a)
                   .normal(last.normal(), dx, dy, dz)
                   .endVertex();
            builder.vertex(last.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                   .color(r, g, b, a)
                   .normal(last.normal(), dx, dy, dz)
                   .endVertex();
        });
    }

    public void addRecentlyCompiledChunk(EvChunkRenderDispatcher.RenderChunk chunk) {
        if (this.renderOnThread) {
            this.recentlyCompiledChunks.add(chunk);
        }
        else {
            synchronized (this.recentlyCompiledChunks) {
                this.recentlyCompiledChunks.add(chunk);
            }
        }
    }

    /**
     * Loads all renderers and sets up the basic options usage.
     */
    public void allChanged() {
        this.renderOnThread = EvolutionConfig.SYNC_RENDERING.get();
        if (this.level != null) {
            this.mc.debugRenderer.setRenderHeightmap(EvolutionConfig.RENDER_HEIGHTMAP.get());
            this.graphicsChanged();
            this.level.clearTintCaches();
            if (this.chunkRenderDispatcher == null) {
                this.chunkRenderDispatcher = new EvChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.mc.is64Bit(), this.bufferHolder.chunkBuilderPack());
            }
            else {
                this.chunkRenderDispatcher.setLevel(this.level);
            }
            this.needsFullRenderChunkUpdate = true;
            this.generateClouds = true;
            synchronized (this.recentlyCompiledChunks) {
                this.recentlyCompiledChunks.clear();
            }
            ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
            this.lastViewDistance = this.mc.options.getEffectiveRenderDistance();
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
            }
            this.chunkRenderDispatcher.blockUntilClear();
            synchronized (this.globalBlockEntities) {
                this.globalBlockEntities.clear();
            }
            this.viewArea = new EvViewArea(this.chunkRenderDispatcher, this.level, this.mc.options.getEffectiveRenderDistance(), this);
            if (this.lastFullRenderChunkUpdate != null) {
                try {
                    this.lastFullRenderChunkUpdate.get();
                    this.lastFullRenderChunkUpdate = null;
                }
                catch (Exception exception) {
                    Evolution.warn("Full update failed", exception);
                }
            }
            this.renderChunkStorage = new RenderChunkStorage(this.viewArea.chunks.length);
            this.renderChunksInFrustum.clear();
            Vec3 camPos = this.mc.gameRenderer.getMainCamera().getPosition();
            this.viewArea.repositionCamera(camPos.x, camPos.z);
            this.renderCache.clear();
            ClientEvents.getInstance().allChanged();
        }
    }

    public void blockChanged(int x, int y, int z, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        this.setBlockDirty(x, y, z, (flags & BlockFlags.RENDER_MAINTHREAD) != 0);
    }

    @Override
    public void close() {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }
        if (this.transparencyChain != null) {
            this.transparencyChain.close();
        }
    }

    public int countRenderedChunks() {
        return this.renderChunksInFrustum.size();
    }

    public void destroyBlockProgress(int breakerId, long pos, int progress, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        if (progress >= 0 && progress < 10) {
            EvBlockDestructionProgress blockDestructionProgress = this.destroyingBlocks.get(breakerId);
            if (blockDestructionProgress != null) {
                this.removeProgress(blockDestructionProgress);
            }
            if (blockDestructionProgress == null || blockDestructionProgress.getPos() != pos) {
                blockDestructionProgress = new EvBlockDestructionProgress(breakerId, pos);
                this.destroyingBlocks.put(breakerId, blockDestructionProgress);
            }
            blockDestructionProgress.setLocation(face, hitX, hitY, hitZ);
            blockDestructionProgress.setProgress(progress);
            blockDestructionProgress.updateTick(this.ticks);
            SortedSet<EvBlockDestructionProgress> blockDestructionProgresses = this.destructionProgress.get(blockDestructionProgress.getPos());
            if (blockDestructionProgresses == null) {
                blockDestructionProgresses = new TreeSet<>();
                this.destructionProgress.put(blockDestructionProgress.getPos(), blockDestructionProgresses);
            }
            blockDestructionProgresses.add(blockDestructionProgress);
        }
        else {
            EvBlockDestructionProgress destructionProgress = this.destroyingBlocks.remove(breakerId);
            if (destructionProgress != null) {
                this.removeProgress(destructionProgress);
            }
        }
    }

    public void doEntityOutline() {
        if (this.shouldShowEntityOutlines()) {
            assert this.entityTarget != null;
            Blending.DEFAULT_0_1.apply();
            this.entityTarget.blitToScreen(this.mc.getWindow().getWidth(), this.mc.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public RenderTarget entityTarget() {
        assert this.entityTarget != null;
        return this.entityTarget;
    }

    public @Nullable EvChunkRenderDispatcher getChunkRenderDispatcher() {
        return this.chunkRenderDispatcher;
    }

    /**
     * @return chunk rendering statistics to display on the {@linkplain
     * net.minecraft.client.gui.components.DebugScreenOverlay debug overlay}
     */
    public String getChunkStatistics() {
        assert this.viewArea != null;
        int totalChunks = this.viewArea.chunks.length;
        int visibleChunks = this.countRenderedChunks();
        return "C: " +
               visibleChunks +
               "/" +
               totalChunks +
               " D: " +
               this.lastViewDistance +
               ", " +
               (this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats());
    }

    public RenderTarget getCloudsTarget() {
        assert this.cloudsTarget != null;
        return this.cloudsTarget;
    }

    @Override
    public OList<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    /**
     * @return entity rendering statistics to display on the {@linkplain
     * net.minecraft.client.gui.components.DebugScreenOverlay debug overlay}
     */
    public String getEntityStatistics() {
        assert this.level != null;
        return "E: " +
               this.renderedEntities +
               "/" +
               this.level.getEntityCount() +
               ", SD: " +
               this.level.getServerSimulationDistance();
    }

    public RenderTarget getItemEntityTarget() {
        assert this.itemEntityTarget != null;
        return this.itemEntityTarget;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.LEVEL_RENDERER;
    }

    public double getLastViewDistance() {
        return this.lastViewDistance;
    }

    public RenderTarget getParticlesTarget() {
        assert this.particlesTarget != null;
        return this.particlesTarget;
    }

    public double getTotalChunks() {
        assert this.viewArea != null;
        return this.viewArea.chunks.length;
    }

    public RenderTarget getTranslucentTarget() {
        assert this.translucentTarget != null;
        return this.translucentTarget;
    }

    public RenderTarget getWeatherTarget() {
        assert this.weatherTarget != null;
        return this.weatherTarget;
    }

    /**
     * Handles a global level event. This includes playing sounds that should be heard by any player, regardless of
     * position and dimension, such as the Wither spawning.
     *
     * @param type the type of level event to handle. This method only handles {@linkplain
     *             LevelEvent#SOUND_WITHER_BOSS_SPAWN the wither boss spawn sound}, {@linkplain
     *             LevelEvent#SOUND_DRAGON_DEATH the dragon's death sound}, and {@linkplain
     *             LevelEvent#SOUND_END_PORTAL_SPAWN the end portal spawn sound}.
     */
    public void globalLevelEvent(@LvlEvent int type, int x, int y, int z) {
        assert this.level != null;
        switch (type) {
            case LevelEvent.SOUND_WITHER_BOSS_SPAWN, LevelEvent.SOUND_DRAGON_DEATH, LevelEvent.SOUND_END_PORTAL_SPAWN -> {
                Camera camera = this.mc.gameRenderer.getMainCamera();
                if (camera.isInitialized()) {
                    double d0 = x - camera.getPosition().x;
                    double d1 = y - camera.getPosition().y;
                    double d2 = z - camera.getPosition().z;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = camera.getPosition().x;
                    double d5 = camera.getPosition().y;
                    double d6 = camera.getPosition().z;
                    if (d3 > 0.0D) {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }
                    if (type == LevelEvent.SOUND_WITHER_BOSS_SPAWN) {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    }
                    else if (type == LevelEvent.SOUND_END_PORTAL_SPAWN) {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    }
                    else {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
                    }
                }
            }
        }
    }

    public void graphicsChanged() {
        if (Minecraft.useShaderTransparency()) {
            this.initTransparency();
        }
        else {
            this.deinitTransparency();
        }
    }

    public boolean hasRenderedAllChunks() {
        assert this.chunkRenderDispatcher != null;
        return this.chunkRenderDispatcher.isQueueEmpty();
    }

    public void initOutline() {
        if (this.entityEffect != null) {
            this.entityEffect.close();
        }
        ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");
        try {
            this.entityEffect = new PostChain(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getMainRenderTarget(), resourcelocation);
            this.entityEffect.resize(this.mc.getWindow().getWidth(), this.mc.getWindow().getHeight());
            this.entityTarget = this.entityEffect.getTempTarget("final");
        }
        catch (IOException e) {
            Evolution.warn("Failed to load shader: {}", resourcelocation, e);
            this.entityEffect = null;
            this.entityTarget = null;
        }
        catch (JsonSyntaxException e) {
            Evolution.warn("Failed to parse shader: {}", resourcelocation, e);
            this.entityEffect = null;
            this.entityTarget = null;
        }
    }

    public boolean isChunkCompiled(BlockPos pos) {
        assert this.viewArea != null;
        EvChunkRenderDispatcher.RenderChunk chunk = this.viewArea.getRenderChunkAt(pos);
        return chunk != null && chunk.compiled != EvChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
    }

    public void levelEvent(@LvlEvent int type, int x, int y, int z, int data) {
        this.listener.levelEvent(type, x, y, z, data);
    }

    public LevelEventListener listener() {
        return this.listener;
    }

    public void needsUpdate() {
        this.needsFullRenderChunkUpdate = true;
        this.generateClouds = true;
    }

    @Override
    public void onResourceManagerReload(ResourceManager pResourceManager) {
        this.initOutline();
        if (Minecraft.useShaderTransparency()) {
            this.initTransparency();
        }
    }

    public void prepareCullFrustum(PoseStack matrices, Vec3 camPos, Matrix4f projMatrix) {
        this.cullingFrustum.calculateFrustum(matrices.last().pose(), projMatrix);
        this.cullingFrustum.prepare(camPos.x, camPos.y, camPos.z);
    }

    public void renderClouds(PoseStack matrices, Matrix4f pProjectionMatrix, float pPartialTick, double pCamX, double pCamY, double pCamZ) {
        assert this.level != null;
        float f = this.level.effects().getCloudHeight();
        if (!Float.isNaN(f)) {
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            Blending.DEFAULT.apply();
            RenderSystem.depthMask(true);
            double d1 = (this.ticks + pPartialTick) * 0.03F;
            double d2 = (pCamX + d1) / 12;
            double d3 = f - (float) pCamY + 0.33F;
            double d4 = pCamZ / 12 + 0.33F;
            d2 -= Mth.floor(d2 / 2_048) * 2_048;
            d4 -= Mth.floor(d4 / 2_048) * 2_048;
            float f3 = (float) (d2 - Mth.floor(d2));
            float f4 = (float) (d3 / 4 - Mth.floor(d3 / 4.0)) * 4.0F;
            float f5 = (float) (d4 - Mth.floor(d4));
            Vec3 vec3 = this.level.getCloudColor(pPartialTick);
            int i = (int) Math.floor(d2);
            int j = (int) Math.floor(d3 / 4);
            int k = (int) Math.floor(d4);
            if (i != this.prevCloudX ||
                j != this.prevCloudY ||
                k != this.prevCloudZ ||
                this.mc.options.getCloudsType() != this.prevCloudsType ||
                this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4) {
                this.prevCloudX = i;
                this.prevCloudY = j;
                this.prevCloudZ = k;
                this.prevCloudColor = vec3;
                this.prevCloudsType = this.mc.options.getCloudsType();
                this.generateClouds = true;
            }
            if (this.generateClouds) {
                this.generateClouds = false;
                BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.close();
                }
                this.cloudBuffer = new VertexBuffer();
                this.buildClouds(bufferbuilder, d2, d3, d4, vec3);
                bufferbuilder.end();
                this.cloudBuffer.upload(bufferbuilder);
            }
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX_COLOR_NORMAL);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            FogRenderer.levelFogColor();
            matrices.pushPose();
            matrices.scale(12.0F, 1.0F, 12.0F);
            matrices.translate(-f3, f4, -f5);
            if (this.cloudBuffer != null) {
                int i1 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;
                for (int l = i1; l < 2; ++l) {
                    if (l == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    }
                    else {
                        RenderSystem.colorMask(true, true, true, true);
                    }
                    ShaderInstance shader = RenderSystem.getShader();
                    assert shader != null;
                    this.cloudBuffer.drawWithShader(matrices.last().pose(), pProjectionMatrix, shader);
                }
            }
            matrices.popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    public void renderLevel(PoseStack matrices, float partialTicks, long endTickTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix) {
        assert this.level != null;
        assert this.mc.hitResult != null;
        assert this.mc.player != null;
        ClientEvents.storeProjMatrix(projectionMatrix);
        ClientEvents.storeModelViewMatrix(matrices.last().pose());
        RenderSystem.setShaderGameTime(this.level.getGameTime(), partialTicks);
        this.blockEntityRenderDispatcher.prepare(this.level, camera, this.mc.hitResult);
        //noinspection ConstantConditions
        this.entityRenderDispatcher.prepare(this.level, camera, this.mc.crosshairPickEntity);
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.popPush("light_update_queue");
        this.level.pollLightUpdates();
        profiler.popPush("light_updates");
        this.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, this.level.isLightUpdateQueueEmpty(), true);
        Vec3 camPos = camera.getPosition();
        float camX = (float) camPos.x;
        float camY = (float) camPos.y;
        float camZ = (float) camPos.z;
        profiler.popPush("clear");
        FogRenderer.setupColor(camera, partialTicks, this.level, this.mc.options.getEffectiveRenderDistance(), gameRenderer.getDarkenWorldAmount(partialTicks));
        FogRenderer.levelFogColor();
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        float renderDistance = gameRenderer.getRenderDistance();
        boolean isFoggy = this.level.effects().isFoggyAt(Mth.floor(camX), Mth.floor(camY)) || this.mc.gui.getBossOverlay().shouldCreateWorldFog();
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, isFoggy);
        profiler.popPush("sky");
        AccessorRenderSystem.setShader(GameRenderer.getPositionShader());
        this.renderSky(matrices, partialTicks, camera, isFoggy, this.skyFog.set(camera, renderDistance, isFoggy));
        profiler.popPush("fog");
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDistance, 32.0F), isFoggy);
        profiler.popPush("terrain_setup");
        Frustum frustum = this.cullingFrustum;
        this.setupRender(camera, frustum, this.mc.player.isSpectator());
        profiler.popPush("compile_chunks");
        this.compileChunks(camera, endTickTime);
        profiler.popPush("terrain");
        this.renderChunkLayer(RenderType.solid(), RenderLayer.SOLID, matrices, camX, camY, camZ, projectionMatrix);
        this.renderChunkLayer(RenderType.cutoutMipped(), RenderLayer.CUTOUT_MIPPED, matrices, camX, camY, camZ, projectionMatrix);
        this.renderChunkLayer(RenderType.cutout(), RenderLayer.CUTOUT, matrices, camX, camY, camZ, projectionMatrix);
        if (this.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel(matrices.last().pose());
        }
        else {
            Lighting.setupLevel(matrices.last().pose());
        }
        profiler.popPush("entities");
        this.renderedEntities = 0;
        if (this.itemEntityTarget != null) {
            this.itemEntityTarget.clear(Minecraft.ON_OSX);
            this.itemEntityTarget.copyDepthFrom(this.mc.getMainRenderTarget());
            this.mc.getMainRenderTarget().bindWrite(false);
        }
        if (this.weatherTarget != null) {
            this.weatherTarget.clear(Minecraft.ON_OSX);
        }
        if (this.shouldShowEntityOutlines()) {
            assert this.entityTarget != null;
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.mc.getMainRenderTarget().bindWrite(false);
        }
        boolean hasGlowing = false;
        MultiBufferSource.BufferSource buffer = this.bufferHolder.bufferSource();
        Entity cameraEntity = camera.getEntity();
        for (Entity entity : this.level.entitiesForRendering()) {
            if ((entity != cameraEntity || camera.isDetached()) &&
                (this.entityRenderDispatcher.shouldRender(entity, frustum, camX, camY, camZ) || entity.hasIndirectPassenger(this.mc.player))) {
                //noinspection ObjectAllocationInLoop
                profiler.push(() -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString());
                ++this.renderedEntities;
                if (entity.tickCount == 0) {
                    entity.xOld = entity.getX();
                    entity.yOld = entity.getY();
                    entity.zOld = entity.getZ();
                }
                MultiBufferSource multiBufferSource;
                if (this.shouldShowEntityOutlines() && this.mc.shouldEntityAppearGlowing(entity)) {
                    hasGlowing = true;
                    OutlineBufferSource outlineBuffer = this.bufferHolder.outlineBufferSource();
                    multiBufferSource = outlineBuffer;
                    int color = entity.getTeamColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    outlineBuffer.setColor(r, g, b, 255);
                }
                else {
                    multiBufferSource = buffer;
                }
                this.renderEntity(entity, camX, camY, camZ, partialTicks, matrices, multiBufferSource);
                profiler.pop();
            }
        }
        //Render player in first person
        ClientEvents client = ClientEvents.getInstance();
        if (!camera.isDetached() && cameraEntity.isAlive()) {
            profiler.push(() -> Registry.ENTITY_TYPE.getKey(cameraEntity.getType()).toString());
            this.renderedEntities++;
            MultiBufferSource multiBufferSource;
            if (this.shouldShowEntityOutlines() && this.mc.shouldEntityAppearGlowing(cameraEntity)) {
                hasGlowing = true;
                OutlineBufferSource outlineBuffer = this.bufferHolder.outlineBufferSource();
                multiBufferSource = outlineBuffer;
                int teamColor = cameraEntity.getTeamColor();
                int red = teamColor >> 16 & 255;
                int green = teamColor >> 8 & 255;
                int blue = teamColor & 255;
                outlineBuffer.setColor(red, green, blue, 255);
            }
            else {
                multiBufferSource = buffer;
            }
            ClientRenderer renderer = client.getRenderer();
            renderer.setRenderingPlayer(true);
            this.renderEntity(cameraEntity, camX, camY, camZ, partialTicks, matrices, multiBufferSource);
            renderer.setRenderingPlayer(false);
            profiler.pop();
        }
        buffer.endLastBatch();
        checkPoseStack(matrices);
        buffer.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        profiler.popPush("block_entities");
        for (int i = 0, l = this.renderChunksInFrustum.size(); i < l; i++) {
            EvChunkRenderDispatcher.RenderChunk renderChunk = this.renderChunksInFrustum.get(i);
            if (!renderChunk.hasRenderableTEs()) {
                continue;
            }
            List<BlockEntity> blockEntities = renderChunk.compiled.getRenderableTEs();
            if (!blockEntities.isEmpty()) {
                for (int j = 0, len = blockEntities.size(); j < len; j++) {
                    BlockEntity blockEntity = blockEntities.get(j);
                    //noinspection ObjectAllocationInLoop,ConstantConditions
                    profiler.push(() -> Registry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString());
                    if (renderChunk.visibility != Visibility.INSIDE && !frustum.isVisible(blockEntity.getRenderBoundingBox())) {
                        profiler.pop();
                        continue;
                    }
                    BlockPos bePos = blockEntity.getBlockPos();
                    MultiBufferSource multiBufferSource = buffer;
                    matrices.pushPose();
                    matrices.translate(bePos.getX() - camX, bePos.getY() - camY, bePos.getZ() - camZ);
                    SortedSet<EvBlockDestructionProgress> destructionProgresses = this.destructionProgress.get(bePos.asLong());
                    if (destructionProgresses != null && !destructionProgresses.isEmpty()) {
                        int progress = destructionProgresses.last().getProgress();
                        if (progress >= 0) {
                            PoseStack.Pose entry = matrices.last();
                            SheetedDecalTextureGenerator consumer = this.breakingBuffers[progress];
                            consumer.set(this.bufferHolder.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), entry.pose(), entry.normal());
                            //noinspection ObjectAllocationInLoop
                            multiBufferSource = renderType -> {
                                VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
                                return renderType.affectsCrumbling() ? VertexMultiConsumer.create(consumer, vertexConsumer) : vertexConsumer;
                            };
                        }
                    }
                    this.blockEntityRenderDispatcher.render(blockEntity, partialTicks, matrices, multiBufferSource);
                    matrices.popPose();
                    profiler.pop();
                }
            }
        }
        if (this.renderOnThread) {
            this.renderGlobalTileEntities(frustum, matrices, buffer, camX, camY, camZ, partialTicks);
        }
        else {
            synchronized (this.globalBlockEntities) {
                this.renderGlobalTileEntities(frustum, matrices, buffer, camX, camY, camZ, partialTicks);
            }
        }
        checkPoseStack(matrices);
        buffer.endBatch(RenderType.solid());
        buffer.endBatch(RenderType.endPortal());
        buffer.endBatch(RenderType.endGateway());
        buffer.endBatch(Sheets.solidBlockSheet());
        buffer.endBatch(Sheets.cutoutBlockSheet());
        buffer.endBatch(Sheets.bedSheet());
        buffer.endBatch(Sheets.shulkerBoxSheet());
        buffer.endBatch(Sheets.signSheet());
        buffer.endBatch(Sheets.chestSheet());
        this.bufferHolder.outlineBufferSource().endOutlineBatch();
        if (hasGlowing) {
            assert this.entityEffect != null;
            this.entityEffect.process(partialTicks);
            this.mc.getMainRenderTarget().bindWrite(false);
        }
        profiler.popPush("destroyProgress");
        L2OMap<SortedSet<EvBlockDestructionProgress>> destructions = this.destructionProgress;
        for (long it = destructions.beginIteration(); destructions.hasNextIteration(it); it = destructions.nextEntry(it)) {
            long pos = destructions.getIterationKey(it);
            int x = BlockPos.getX(pos);
            int y = BlockPos.getY(pos);
            int z = BlockPos.getZ(pos);
            double deltaX = x - camX;
            double deltaY = y - camY;
            double deltaZ = z - camZ;
            if (!(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1_024.0)) {
                SortedSet<EvBlockDestructionProgress> destructionProgresses = destructions.getIterationValue(it);
                if (!destructionProgresses.isEmpty()) {
                    EvBlockDestructionProgress last = destructionProgresses.last();
                    int progress = last.getProgress();
                    matrices.pushPose();
                    matrices.translate(deltaX, deltaY, deltaZ);
                    PoseStack.Pose entry = matrices.last();
                    SheetedDecalTextureGenerator consumer = this.breakingBuffers[progress];
                    consumer.set(this.bufferHolder.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), entry.pose(), entry.normal());
                    this.mc.getBlockRenderer().renderBreakingTexture(last.getBlockState(this.level), x, y, z, this.level, matrices, consumer);
                    matrices.popPose();
                }
            }
        }
        checkPoseStack(matrices);
        HitResult hitResult = this.mc.hitResult;
        if (hitResult != null) {
            if (renderBlockOutline && hitResult.getType() == HitResult.Type.BLOCK) {
                profiler.popPush("outline");
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                int x = blockHitResult.posX();
                int y = blockHitResult.posY();
                int z = blockHitResult.posZ();
                if (this.level.getWorldBorder().isWithinBounds_(x, z)) {
                    Block block = this.level.getBlockState_(x, y, z).getBlock();
                    if (block instanceof BlockKnapping) {
                        TEKnapping tile = (TEKnapping) this.level.getBlockEntity_(x, y, z);
                        assert tile != null;
                        client.getRenderer().renderOutlines(matrices, buffer, tile.type.getShape(), camera, x, y, z);
                    }
                    else if (block instanceof BlockMolding) {
                        TEMolding tile = (TEMolding) this.level.getBlockEntity_(x, y, z);
                        assert tile != null;
                        client.getRenderer().renderOutlines(matrices, buffer, tile.molding.getShape(), camera, x, y, z);
                    }
                    client.getRenderer().renderBlockOutlines(matrices, buffer, camera, x, y, z);
                }
            }
            else if (hitResult.getType() == HitResult.Type.ENTITY) {
                if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes() && hitResult instanceof AdvancedEntityHitResult advRayTrace) {
                    if (advRayTrace.getHitbox() != null) {
                        client.getRenderer().renderHitbox(matrices, buffer, advRayTrace.getEntity(), advRayTrace.getHitbox(), camera, partialTicks);
                    }
                }
            }
            if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                assert this.mc.player != null;
                if (this.mc.player.getMainHandItem().getItem() == EvolutionItems.DEBUG_ITEM ||
                    this.mc.player.getOffhandItem().getItem() == EvolutionItems.DEBUG_ITEM) {
                    HitboxEntity<? extends Entity> hitboxes = this.mc.player.getHitboxes();
                    if (hitboxes != null) {
                        client.getRenderer().renderHitbox(matrices, buffer, this.mc.player, hitboxes.getBoxes().get(0), camera, partialTicks);
                    }
                }
            }
        }
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        this.mc.debugRenderer.render(matrices, buffer, camX, camY, camZ);
        this.renderLoadFactor(matrices, buffer, camX, camY, camZ);
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        buffer.endBatch(Sheets.translucentCullBlockSheet());
        buffer.endBatch(Sheets.bannerSheet());
        buffer.endBatch(Sheets.shieldSheet());
        buffer.endBatch(RenderType.armorGlint());
        buffer.endBatch(RenderType.armorEntityGlint());
        buffer.endBatch(RenderType.glint());
        buffer.endBatch(RenderType.glintDirect());
        buffer.endBatch(RenderType.glintTranslucent());
        buffer.endBatch(RenderType.entityGlint());
        buffer.endBatch(RenderType.entityGlintDirect());
        buffer.endBatch(RenderType.waterMask());
        this.bufferHolder.crumblingBufferSource().endBatch();
        //noinspection VariableNotUsedInsideIf
        if (this.transparencyChain != null) {
            buffer.endBatch(RenderType.lines());
            buffer.endBatch();
            assert this.translucentTarget != null;
            this.translucentTarget.clear(Minecraft.ON_OSX);
            this.translucentTarget.copyDepthFrom(this.mc.getMainRenderTarget());
            profiler.popPush("translucent");
            this.renderChunkLayer(RenderType.translucent(), RenderLayer.TRANSLUCENT, matrices, camX, camY, camZ, projectionMatrix);
            profiler.popPush("tripwire");
            this.renderChunkLayer(RenderType.tripwire(), RenderLayer.TRIPWIRE, matrices, camX, camY, camZ, projectionMatrix);
            assert this.particlesTarget != null;
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.mc.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            profiler.popPush("particles");
            this.mc.particleEngine.render(matrices, buffer, lightTexture, camera, partialTicks);
            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        }
        else {
            profiler.popPush("translucent");
            if (this.translucentTarget != null) {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }
            this.renderChunkLayer(RenderType.translucent(), RenderLayer.TRANSLUCENT, matrices, camX, camY, camZ, projectionMatrix);
            buffer.endBatch(RenderType.lines());
            buffer.endBatch();
            profiler.popPush("tripwire");
            this.renderChunkLayer(RenderType.tripwire(), RenderLayer.TRIPWIRE, matrices, camX, camY, camZ, projectionMatrix);
            profiler.popPush("particles");
            this.mc.particleEngine.render(matrices, buffer, lightTexture, camera, partialTicks);
        }
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (this.mc.options.getCloudsType() != CloudStatus.OFF) {
            //noinspection VariableNotUsedInsideIf
            if (this.transparencyChain != null) {
                assert this.cloudsTarget != null;
                this.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderStateShard.CLOUDS_TARGET.setupRenderState();
                profiler.popPush("clouds");
                this.renderClouds(matrices, projectionMatrix, partialTicks, camX, camY, camZ);
                RenderStateShard.CLOUDS_TARGET.clearRenderState();
            }
            else {
                profiler.popPush("clouds");
                AccessorRenderSystem.setShader(GameRenderer.getPositionTexColorNormalShader());
                this.renderClouds(matrices, projectionMatrix, partialTicks, camX, camY, camZ);
            }
        }
        if (this.transparencyChain != null) {
            RenderStateShard.WEATHER_TARGET.setupRenderState();
            profiler.popPush("weather");
            this.renderSnowAndRain(lightTexture, partialTicks, camX, camY, camZ);
            this.renderWorldBorder(camera);
            RenderStateShard.WEATHER_TARGET.clearRenderState();
            this.transparencyChain.process(partialTicks);
            this.mc.getMainRenderTarget().bindWrite(false);
        }
        else {
            RenderSystem.depthMask(false);
            profiler.popPush("weather");
            this.renderSnowAndRain(lightTexture, partialTicks, camX, camY, camZ);
            this.renderWorldBorder(camera);
            RenderSystem.depthMask(true);
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        FogRenderer.setupNoFog();
    }

    public void renderSky(PoseStack matrices, float partialTicks, Camera camera, boolean isFoggy, SkyFogSetup skyFogSetup) {
        skyFogSetup.setup();
        assert this.level != null;
        if (this.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
            SkyRenderer skyRenderer = ClientEvents.getInstance().getSkyRenderer();
            if (skyRenderer != null) {
                skyRenderer.render(partialTicks, matrices, this.level, this.mc, skyFogSetup);
            }
        }
        else if (!isFoggy) {
            FogType fogtype = camera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA) {
                if (camera.getEntity() instanceof LivingEntity living) {
                    if (living.hasEffect(MobEffects.BLINDNESS)) {
                        return;
                    }
                }
                if (this.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
                    renderEndSky(matrices);
                }
            }
        }
    }

    public void resize(int width, int height) {
        this.needsUpdate();
        if (this.entityEffect != null) {
            this.entityEffect.resize(width, height);
        }
        if (this.transparencyChain != null) {
            this.transparencyChain.resize(width, height);
        }
    }

    public void setBlockDirty(int x, int y, int z, BlockState oldState, BlockState newState) {
        if (this.mc.getModelManager().requiresRender(oldState, newState)) {
            this.setBlocksDirty(x, y, z, x, y, z);
        }
    }

    /**
     * Re-renders all blocks in the specified range.
     */
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int z = minZ - 1; z <= maxZ + 1; ++z) {
            int secZ = SectionPos.blockToSectionCoord(z);
            for (int x = minX - 1; x <= maxX + 1; ++x) {
                int secX = SectionPos.blockToSectionCoord(x);
                for (int y = minY - 1; y <= maxY + 1; ++y) {
                    this.setSectionDirty(secX, SectionPos.blockToSectionCoord(y), secZ);
                }
            }
        }
    }

    /**
     * @param level the level to set, or {@code null} to clear
     */
    public void setLevel(@Nullable ClientLevel level) {
        this.lastCameraChunkX = Integer.MIN_VALUE;
        this.lastCameraChunkY = Integer.MIN_VALUE;
        this.lastCameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(level);
        this.level = level;
        //noinspection VariableNotUsedInsideIf
        if (level != null) {
            this.allChanged();
        }
        else {
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }
            if (this.chunkRenderDispatcher != null) {
                this.chunkRenderDispatcher.dispose();
                this.chunkRenderDispatcher = null;
            }
            this.globalBlockEntities.clear();
            this.renderChunkStorage = null;
            this.renderChunksInFrustum.clear();
            if (this.lastFullRenderChunkUpdate != null) {
                try {
                    this.lastFullRenderChunkUpdate.get();
                }
                catch (Exception ignored) {
                }
                finally {
                    this.lastFullRenderChunkUpdate = null;
                }
            }
            this.recentlyCompiledChunks.clear();
            this.destroyingBlocks.clear();
            this.destructionProgress.clear();
            this.renderCache.clear();
        }
        this.listener.setLevel(level);
    }

    public void setSectionDirty(int sectionX, int sectionY, int sectionZ) {
        this.setSectionDirty(sectionX, sectionY, sectionZ, false);
    }

    public void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ) {
        for (int z = sectionZ - 1; z <= sectionZ + 1; ++z) {
            for (int x = sectionX - 1; x <= sectionX + 1; ++x) {
                for (int y = sectionY - 1; y <= sectionY + 1; ++y) {
                    this.setSectionDirty(x, y, z);
                }
            }
        }
    }

    public void tick() {
        ++this.ticks;
        if (this.ticks % 20 == 0) {
            I2OMap<EvBlockDestructionProgress> destroyingBlocks = this.destroyingBlocks;
            for (long it = destroyingBlocks.beginIteration(); destroyingBlocks.hasNextIteration(it); it = destroyingBlocks.nextEntry(it)) {
                EvBlockDestructionProgress progress = destroyingBlocks.getIterationValue(it);
                int i = progress.getUpdatedRenderTick();
                if (this.ticks - i > 400) {
                    it = destroyingBlocks.removeIteration(it);
                    this.removeProgress(progress);
                }
            }
        }
    }

    public void tickRain(Camera camera) {
        assert this.level != null;
        float rainAmount = this.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
        if (rainAmount > 0.0F) {
            FastRandom random = this.rainRandom.setSeedAndReturn(this.ticks * 312_987_231L);
            BlockPos cameraPos = camera.getBlockPosition();
            int camX = cameraPos.getX();
            int camY = cameraPos.getY();
            int camZ = cameraPos.getZ();
            int rainX = Integer.MIN_VALUE;
            int rainY = 0;
            int rainZ = 0;
            int total = (int) (100.0F * rainAmount * rainAmount) / (this.mc.options.particles == ParticleStatus.DECREASED ? 2 : 1);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int i = 0; i < total; ++i) {
                int offX = random.nextInt(21) - 10;
                int offZ = random.nextInt(21) - 10;
                int topX = camX + offX;
                int topZ = camZ + offZ;
                int topY = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, topX, topZ);
                if (topY > this.level.getMinBuildHeight() && topY <= camY + 10 && topY >= camY - 10) {
                    Biome biome = this.level.getBiome_(topX, topY, topZ).value();
                    if (biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(mutable.set(topX, topY, topZ))) {
                        rainX = topX;
                        rainY = topY - 1;
                        rainZ = topZ;
                        if (this.mc.options.particles == ParticleStatus.MINIMAL) {
                            break;
                        }
                        double dx = random.nextDouble();
                        double dz = random.nextDouble();
                        BlockState state = this.level.getBlockState_(rainX, rainY, rainZ);
                        FluidState fluid = this.level.getFluidState_(rainX, rainY, rainZ);
                        VoxelShape shape = state.getCollisionShape_(this.level, rainX, rainY, rainZ);
                        this.level.addParticle(fluid.is(FluidTags.LAVA) || state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state) ? ParticleTypes.RAIN : ParticleTypes.SMOKE,
                                               rainX + dx,
                                               rainY + Math.max(shape.max(Direction.Axis.Y, dx, dz), fluid.getHeight_(this.level, rainX, rainY, rainZ)),
                                               rainZ + dz,
                                               0, 0, 0
                        );
                    }
                }
            }
            if (rainX != Integer.MIN_VALUE && random.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (rainY > camY + 1 && this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, camX, camZ) > camY) {
                    this.level.playLocalSound(rainX + 0.5, rainY + 0.5, rainZ + 0.5, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                }
                else {
                    this.level.playLocalSound(rainX + 0.5, rainY + 0.5, rainZ + 0.5, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

    public void updateGlobalBlockEntities(RSet<BlockEntity> toRemove, RSet<BlockEntity> toAdd) {
        if (this.renderOnThread) {
            this.globalBlockEntities.removeAll(toRemove);
            this.globalBlockEntities.addAll(toAdd);
        }
        else {
            synchronized (this.globalBlockEntities) {
                this.globalBlockEntities.removeAll(toRemove);
                this.globalBlockEntities.addAll(toAdd);
            }
        }
    }

    public boolean visibleFrustumCulling(AABB bb) {
        return this.cullingFrustum.isVisible(bb);
    }

    public boolean visibleOcclusionCulling(double x, double y, double z) {
        assert this.viewArea != null;
        EvChunkRenderDispatcher.RenderChunk chunk = this.viewArea.getRenderChunkAt(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        if (chunk == null || chunk.isCompletelyEmpty()) {
            return true;
        }
        return chunk.visibility != Visibility.OUTSIDE;
    }

    public boolean visibleOcclusionCulling(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        assert this.viewArea != null;
        int x0 = Mth.floor(minX);
        int y0 = Mth.floor(minY);
        int z0 = Mth.floor(minZ);
        int x1 = Mth.ceil(maxX);
        int y1 = Mth.ceil(maxY);
        int z1 = Mth.ceil(maxZ);
        int secX0 = SectionPos.blockToSectionCoord(x0);
        int secX1 = SectionPos.blockToSectionCoord(x1);
        int secY0 = SectionPos.blockToSectionCoord(y0);
        int secY1 = SectionPos.blockToSectionCoord(y1);
        int secZ0 = SectionPos.blockToSectionCoord(z0);
        int secZ1 = SectionPos.blockToSectionCoord(z1);
        for (int x = x0, secX = secX0; secX <= secX1; ++secX, x += 16) {
            for (int y = y0, secY = secY0; secY <= secY1; ++secY, y += 16) {
                for (int z = z0, secZ = secZ0; secZ <= secZ1; ++secZ, z += 16) {
                    EvChunkRenderDispatcher.RenderChunk chunk = this.viewArea.getRenderChunkAt(x, y, z);
                    if (chunk == null || chunk.isCompletelyEmpty()) {
                        return true;
                    }
                    if (chunk.visibility != Visibility.OUTSIDE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean shouldShowEntityOutlines() {
        return !this.mc.gameRenderer.isPanoramicMode() &&
               this.entityTarget != null &&
               this.entityEffect != null &&
               this.mc.player != null;
    }

    private void applyFrustum(Frustum frustum) {
        assert Minecraft.getInstance().isSameThread() : "applyFrustum called from wrong thread: " + Thread.currentThread().getName();
        this.mc.getProfiler().push("apply_frustum");
        assert this.viewArea != null;
        this.viewArea.resetVisibility();
        this.renderChunksInFrustum.clear();
        RenderChunkStorage storage = this.renderChunkStorage;
        this.layersInFrustum = 0;
        if (storage != null) {
            OList<EvChunkRenderDispatcher.RenderChunk> renderChunks = storage.renderChunks;
            for (int i = 0, len = renderChunks.size(); i < len; i++) {
                EvChunkRenderDispatcher.RenderChunk chunk = renderChunks.get(i);
                if (chunk.isCompletelyEmpty() && !chunk.isDirty()) {
                    chunk.visibility = Visibility.OUTSIDE;
                    continue;
                }
                int x = chunk.getX();
                int y = chunk.getY();
                int z = chunk.getZ();
                @Visibility int visibility = frustum.intersectWith(x, y, z, x + 16, y + 16, z + 16);
                chunk.visibility = visibility;
                if (visibility != Visibility.OUTSIDE) {
                    this.renderChunksInFrustum.add(chunk);
                    this.layersInFrustum |= chunk.getRenderLayers();
                }
            }
        }
        this.mc.getProfiler().pop();
    }

    private void buildClouds(BufferBuilder builder, double x, double y, double z, Vec3 cloudColor) {
        float f3 = Mth.floor(x) * 0.003_906_25F;
        float f4 = Mth.floor(z) * 0.003_906_25F;
        float f5 = (float) cloudColor.x;
        float f6 = (float) cloudColor.y;
        float f7 = (float) cloudColor.z;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float f17 = (float) Math.floor(y / 4.0D) * 4.0F;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            float f8 = f5 * 0.9F;
            float f9 = f6 * 0.9F;
            float f10 = f7 * 0.9F;
            float f11 = f5 * 0.7F;
            float f12 = f6 * 0.7F;
            float f13 = f7 * 0.7F;
            float f14 = f5 * 0.8F;
            float f15 = f6 * 0.8F;
            float f16 = f7 * 0.8F;
            for (int k = -3; k <= 4; ++k) {
                for (int l = -3; l <= 4; ++l) {
                    float f18 = k * 8;
                    float f19 = l * 8;
                    if (f17 > -5.0F) {
                        builder.vertex(f18, f17, f19 + 8.0F)
                               .uv(f18 * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                               .color(f11, f12, f13, 0.8F)
                               .normal(0.0F, -1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18 + 8.0F, f17, f19 + 8.0F)
                               .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                               .color(f11, f12, f13, 0.8F)
                               .normal(0.0F, -1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18 + 8.0F, f17, f19)
                               .uv((f18 + 8.0F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                               .color(f11, f12, f13, 0.8F)
                               .normal(0.0F, -1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18, f17, f19)
                               .uv(f18 * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                               .color(f11, f12, f13, 0.8F)
                               .normal(0.0F, -1.0F, 0.0F)
                               .endVertex();
                    }
                    if (f17 <= 5.0F) {
                        builder.vertex(f18, f17 + 4.0F - 9.765_625E-4F, f19 + 8.0F)
                               .uv(f18 * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                               .color(f5, f6, f7, 0.8F)
                               .normal(0.0F, 1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18 + 8.0F, f17 + 4.0F - 9.765_625E-4F, f19 + 8.0F)
                               .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                               .color(f5, f6, f7, 0.8F)
                               .normal(0.0F, 1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18 + 8.0F, f17 + 4.0F - 9.765_625E-4F, f19)
                               .uv((f18 + 8.0F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                               .color(f5, f6, f7, 0.8F)
                               .normal(0.0F, 1.0F, 0.0F)
                               .endVertex();
                        builder.vertex(f18, f17 + 4.0F - 9.765_625E-4F, f19)
                               .uv(f18 * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                               .color(f5, f6, f7, 0.8F)
                               .normal(0.0F, 1.0F, 0.0F)
                               .endVertex();
                    }
                    if (k > -1) {
                        for (int i1 = 0; i1 < 8; ++i1) {
                            builder.vertex(f18 + i1, f17, f19 + 8.0F)
                                   .uv((f18 + i1 + 0.5F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(-1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + i1, f17 + 4.0F, f19 + 8.0F)
                                   .uv((f18 + i1 + 0.5F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(-1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + i1, f17 + 4.0F, f19)
                                   .uv((f18 + i1 + 0.5F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(-1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + i1, f17, f19)
                                   .uv((f18 + i1 + 0.5F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(-1.0F, 0.0F, 0.0F)
                                   .endVertex();
                        }
                    }
                    if (k <= 1) {
                        for (int j2 = 0; j2 < 8; ++j2) {
                            builder.vertex(f18 + j2 + 1.0F - 9.765_625E-4F, f17, f19 + 8.0F)
                                   .uv((f18 + j2 + 0.5F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + j2 + 1.0F - 9.765_625E-4F, f17 + 4.0F, f19 + 8.0F)
                                   .uv((f18 + j2 + 0.5F) * 0.003_906_25F + f3, (f19 + 8.0F) * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + j2 + 1.0F - 9.765_625E-4F, f17 + 4.0F, f19)
                                   .uv((f18 + j2 + 0.5F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(1.0F, 0.0F, 0.0F)
                                   .endVertex();
                            builder.vertex(f18 + j2 + 1.0F - 9.765_625E-4F, f17, f19)
                                   .uv((f18 + j2 + 0.5F) * 0.003_906_25F + f3, f19 * 0.003_906_25F + f4)
                                   .color(f8, f9, f10, 0.8F)
                                   .normal(1.0F, 0.0F, 0.0F)
                                   .endVertex();
                        }
                    }
                    if (l > -1) {
                        for (int k2 = 0; k2 < 8; ++k2) {
                            builder.vertex(f18, f17 + 4.0F, f19 + k2)
                                   .uv(f18 * 0.003_906_25F + f3, (f19 + k2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, -1.0F)
                                   .endVertex();
                            builder.vertex(f18 + 8.0F, f17 + 4.0F, f19 + k2)
                                   .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + k2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, -1.0F)
                                   .endVertex();
                            builder.vertex(f18 + 8.0F, f17, f19 + k2)
                                   .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + k2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, -1.0F)
                                   .endVertex();
                            builder.vertex(f18, f17, f19 + k2)
                                   .uv(f18 * 0.003_906_25F + f3, (f19 + k2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, -1.0F)
                                   .endVertex();
                        }
                    }
                    if (l <= 1) {
                        for (int l2 = 0; l2 < 8; ++l2) {
                            builder.vertex(f18, f17 + 4.0F, f19 + l2 + 1.0F - 9.765_625E-4F)
                                   .uv(f18 * 0.003_906_25F + f3, (f19 + l2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, 1.0F)
                                   .endVertex();
                            builder.vertex(f18 + 8.0F, f17 + 4.0F, f19 + l2 + 1.0F - 9.765_625E-4F)
                                   .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + l2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, 1.0F)
                                   .endVertex();
                            builder.vertex(f18 + 8.0F, f17, f19 + l2 + 1.0F - 9.765_625E-4F)
                                   .uv((f18 + 8.0F) * 0.003_906_25F + f3, (f19 + l2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, 1.0F)
                                   .endVertex();
                            builder.vertex(f18, f17, f19 + l2 + 1.0F - 9.765_625E-4F)
                                   .uv(f18 * 0.003_906_25F + f3, (f19 + l2 + 0.5F) * 0.003_906_25F + f4)
                                   .color(f14, f15, f16, 0.8F)
                                   .normal(0.0F, 0.0F, 1.0F)
                                   .endVertex();
                        }
                    }
                }
            }
        }
        else {
            for (int i = -32; i < 32; i += 32) {
                for (int j = -32; j < 32; j += 32) {
                    builder.vertex(i, f17, j + 32)
                           .uv(i * 0.003_906_25F + f3, (j + 32) * 0.003_906_25F + f4)
                           .color(f5, f6, f7, 0.8F)
                           .normal(0.0F, -1.0F, 0.0F)
                           .endVertex();
                    builder.vertex(i + 32, f17, j + 32)
                           .uv((i + 32) * 0.003_906_25F + f3, (j + 32) * 0.003_906_25F + f4)
                           .color(f5, f6, f7, 0.8F)
                           .normal(0.0F, -1.0F, 0.0F)
                           .endVertex();
                    builder.vertex(i + 32, f17, j)
                           .uv((i + 32) * 0.003_906_25F + f3, j * 0.003_906_25F + f4)
                           .color(f5, f6, f7, 0.8F)
                           .normal(0.0F, -1.0F, 0.0F)
                           .endVertex();
                    builder.vertex(i, f17, j)
                           .uv(i * 0.003_906_25F + f3, j * 0.003_906_25F + f4)
                           .color(f5, f6, f7, 0.8F)
                           .normal(0.0F, -1.0F, 0.0F)
                           .endVertex();
                }
            }
        }
    }

    private boolean closeToBorder(int posX, int posZ, EvChunkRenderDispatcher.RenderChunk chunk) {
        int secX = SectionPos.blockToSectionCoord(posX);
        int secZ = SectionPos.blockToSectionCoord(posZ);
        int oriX = SectionPos.blockToSectionCoord(chunk.getX());
        int oriZ = SectionPos.blockToSectionCoord(chunk.getZ());
        return !ChunkMap.isChunkInRange(oriX, oriZ, secX, secZ, this.lastViewDistance - 2);
    }

    private void compileChunks(Camera camera, long endTickTime) {
        assert this.level != null;
        assert this.chunkRenderDispatcher != null;
        ProfilerFiller profiler = this.mc.getProfiler();
        profiler.push("populate_chunks_to_compile");
        BlockPos cameraPos = camera.getBlockPosition();
        for (int i = 0, l = this.renderChunksInFrustum.size(); i < l; i++) {
            EvChunkRenderDispatcher.RenderChunk chunk = this.renderChunksInFrustum.get(i);
            int chunkX = SectionPos.blockToSectionCoord(chunk.getX());
            int chunkZ = SectionPos.blockToSectionCoord(chunk.getZ());
            if (chunk.isDirty() && this.level.getChunk(chunkX, chunkZ).isClientLightReady()) {
                if (this.renderOnThread) {
                    profiler.push("build_on_thread");
                    this.chunkRenderDispatcher.rebuildChunkSync(chunk);
                    chunk.setNotDirty();
                    if (Util.getNanos() - endTickTime >= -1_000_000) {
                        profiler.pop();
                        break;
                    }
                    profiler.pop();
                }
                else {
                    boolean buildSync = false;
                    if (this.mc.options.prioritizeChunkUpdates == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                        buildSync = chunk.isDirtyFromPlayer();
                    }
                    else if (this.mc.options.prioritizeChunkUpdates == PrioritizeChunkUpdates.NEARBY) {
                        buildSync = chunk.isDirtyFromPlayer() || VectorUtil.distSqr(cameraPos, chunk.getX() + 8, chunk.getY() + 8, chunk.getZ() + 8) <= 24 * 24;
                    }
                    if (buildSync) {
                        profiler.push("build_near_sync");
                        this.chunkRenderDispatcher.rebuildChunkSync(chunk);
                        chunk.setNotDirty();
                        profiler.pop();
                    }
                    else {
                        profiler.push("schedule_async_compile");
                        chunk.rebuildChunkAsync(this.chunkRenderDispatcher, this.renderCache);
                        chunk.setNotDirty();
                        if (this.chunkRenderDispatcher.getToBatchCount() > 100 || Util.getNanos() - endTickTime >= -1_000_000) {
                            profiler.pop();
                            break;
                        }
                        profiler.pop();
                    }
                }
            }
        }
        this.renderCache.clear();
        profiler.popPush("upload");
        this.chunkRenderDispatcher.uploadAllPendingUploads();
        profiler.pop();
    }

    private void deinitTransparency() {
        if (this.transparencyChain != null) {
            assert this.translucentTarget != null;
            assert this.itemEntityTarget != null;
            assert this.particlesTarget != null;
            assert this.weatherTarget != null;
            assert this.cloudsTarget != null;
            this.transparencyChain.close();
            this.translucentTarget.destroyBuffers();
            this.itemEntityTarget.destroyBuffers();
            this.particlesTarget.destroyBuffers();
            this.weatherTarget.destroyBuffers();
            this.cloudsTarget.destroyBuffers();
            this.transparencyChain = null;
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
        }
    }

    private boolean drawLayer(int size,
                              @Nullable Uniform uniform,
                              @RenderLayer int renderType,
                              float camX,
                              float camY,
                              float camZ) {
        boolean drewStuff = false;
        if (renderType != RenderLayer.TRANSLUCENT) {
            for (int i = 0; i < size; ++i) {
                if (this.drawLayerInternal(i, uniform, renderType, camX, camY, camZ)) {
                    drewStuff = true;
                }
            }
        }
        else {
            for (int i = size - 1; i >= 0; --i) {
                if (this.drawLayerInternal(i, uniform, renderType, camX, camY, camZ)) {
                    drewStuff = true;
                }
            }
        }
        return drewStuff;
    }

    private boolean drawLayerInternal(int i,
                                      @Nullable Uniform uniform,
                                      @RenderLayer int renderType,
                                      float camX,
                                      float camY,
                                      float camZ) {
        EvChunkRenderDispatcher.RenderChunk chunk = this.renderChunksInFrustum.get(i);
        if (!chunk.isEmpty(renderType)) {
            VertexBuffer buffer = chunk.getBuffer(renderType);
            if (uniform != null) {
                uniform.set(chunk.getX() - camX, chunk.getY() - camY, chunk.getZ() - camZ);
                uniform.upload();
            }
            buffer.drawChunkLayer();
            return true;
        }
        return false;
    }

    /**
     * @param cameraChunkPos the minimum block position of the chunk the camera is in
     * @return the specified {@code chunk} offset in the specified {@code facing}, or {@code null} if it can't
     * be seen by the camera at the specified {@code cameraChunkPos}
     */
    private @Nullable EvChunkRenderDispatcher.RenderChunk getRelativeFrom(int camX, int camY, int camZ,
                                                                          EvChunkRenderDispatcher.RenderChunk chunk,
                                                                          Direction facing) {
        int originX = chunk.getX();
        int originY = chunk.getY();
        int originZ = chunk.getZ();
        switch (facing) {
            case UP -> originY += 16;
            case DOWN -> originY -= 16;
            case EAST -> originX += 16;
            case WEST -> originX -= 16;
            case SOUTH -> originZ += 16;
            case NORTH -> originZ -= 16;
        }
        int view = this.lastViewDistance * 16;
        if (Mth.abs(camX - originX) > view) {
            return null;
        }
        if (Mth.abs(camY - originY) > view) {
            return null;
        }
        if (Mth.abs(camZ - originZ) > view) {
            return null;
        }
        assert this.level != null;
        if (originY >= this.level.getMinBuildHeight() && originY < this.level.getMaxBuildHeight()) {
            assert this.viewArea != null;
            return this.viewArea.getRenderChunkAt(originX, originY, originZ);
        }
        return null;
    }

    private void initTransparency() {
        this.deinitTransparency();
        ResourceLocation shader = new ResourceLocation("shaders/post/transparency.json");
        try {
            PostChain chain = new PostChain(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getMainRenderTarget(), shader);
            chain.resize(this.mc.getWindow().getWidth(), this.mc.getWindow().getHeight());
            this.transparencyChain = chain;
            this.translucentTarget = chain.getTempTarget("translucent");
            this.itemEntityTarget = chain.getTempTarget("itemEntity");
            this.particlesTarget = chain.getTempTarget("particles");
            this.weatherTarget = chain.getTempTarget("weather");
            this.cloudsTarget = chain.getTempTarget("clouds");
        }
        catch (Exception exception) {
            //noinspection InstanceofCatchParameter
            String message = "Failed to " + (exception instanceof JsonSyntaxException ? "parse" : "load") + " shader: " + shader;
            EvLevelRenderer.TransparencyShaderException shaderException = new EvLevelRenderer.TransparencyShaderException(message, exception);
            if (this.mc.getResourcePackRepository().getSelectedIds().size() > 1) {
                Component component;
                try {
                    component = new TextComponent(this.mc.getResourceManager().getResource(shader).getSourceName());
                }
                catch (IOException e) {
                    component = null;
                }
                this.mc.options.graphicsMode = GraphicsStatus.FANCY;
                this.mc.clearResourcePacksOnError(shaderException, component);
            }
            else {
                CrashReport crashreport = this.mc.fillReport(new CrashReport(message, shaderException));
                this.mc.options.graphicsMode = GraphicsStatus.FANCY;
                this.mc.options.save();
                Evolution.error(message, shaderException);
                this.mc.emergencySave();
                Minecraft.crash(crashreport);
            }
        }
    }

    private void initializeQueueForFullUpdate(Camera camera, OArrayFIFOQueue queue) {
        assert this.level != null;
        assert this.viewArea != null;
        Vec3 camPos = camera.getPosition();
        BlockPos camBlockPos = camera.getBlockPosition();
        EvChunkRenderDispatcher.RenderChunk chunk = this.viewArea.getRenderChunkAt(camBlockPos);
        if (chunk == null) {
            int x = Mth.floor(camPos.x / 16) * 16;
            int y = camBlockPos.getY() > this.level.getMinBuildHeight() ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
            int z = Mth.floor(camPos.z / 16) * 16;
            OList<RenderChunkInfo> list = new OArrayList<>();
            for (int i1 = -this.lastViewDistance; i1 <= this.lastViewDistance; ++i1) {
                for (int j1 = -this.lastViewDistance; j1 <= this.lastViewDistance; ++j1) {
                    EvChunkRenderDispatcher.RenderChunk otherChunk = this.viewArea.getRenderChunkAt(x + SectionPos.sectionToBlockCoord(i1, 8),
                                                                                                    y,
                                                                                                    z + SectionPos.sectionToBlockCoord(j1, 8));
                    if (otherChunk != null) {
                        //noinspection ObjectAllocationInLoop
                        list.add(new RenderChunkInfo(otherChunk, null, 0));
                    }
                }
            }
            list.sort(this.comparator.setCameraPos(camBlockPos));
            queue.enqueueMany(list);
        }
        else {
            queue.enqueue(new EvLevelRenderer.RenderChunkInfo(chunk, null, 0));
        }
    }

    private boolean needsFrustumUpdate(Camera camera) {
        double camRotX = Math.floor(camera.getXRot() / 2.0F);
        double camRotY = Math.floor(camera.getYRot() / 2.0F);
        if (camRotX != this.prevCamRotX || camRotY != this.prevCamRotY) {
            this.prevCamRotX = camRotX;
            this.prevCamRotY = camRotY;
            this.needsFrustumUpdate.set(false);
            return true;
        }
        if (this.needsFrustumUpdate.compareAndSet(true, false)) {
            this.prevCamRotX = camRotX;
            this.prevCamRotY = camRotY;
            return true;
        }
        return false;
    }

    private void partialUpdate(RenderChunkStorage storage, Vec3 camPos, boolean smartCull) {
        if (!this.recentlyCompiledChunks.isEmpty()) {
            OArrayFIFOQueue<RenderChunkInfo> queue = QUEUE_CACHE.get();
            assert queue.isEmpty();
            for (int i = 0, len = this.recentlyCompiledChunks.size(); i < len; i++) {
                EvChunkRenderDispatcher.RenderChunk chunk = this.recentlyCompiledChunks.get(i);
                RenderChunkInfo renderChunkInfo = storage.renderInfoMap.get(chunk);
                if (renderChunkInfo != null && renderChunkInfo.chunk == chunk) {
                    queue.enqueue(renderChunkInfo);
                }
            }
            this.recentlyCompiledChunks.clear();
            this.updateRenderChunks(storage, camPos, queue, smartCull);
            this.needsFrustumUpdate.set(true);
            queue.clear();
        }
    }

    private void removeProgress(EvBlockDestructionProgress progress) {
        long i = progress.getPos();
        Set<EvBlockDestructionProgress> set = this.destructionProgress.get(i);
        set.remove(progress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(i);
        }
    }

    private void renderChunkLayer(RenderType renderType,
                                  @RenderLayer int renderLayer,
                                  PoseStack matrices,
                                  float camX,
                                  float camY,
                                  float camZ,
                                  Matrix4f projectionMatrix) {
        assert RenderSystem.isOnRenderThread();
        assert this.chunkRenderDispatcher != null;
        if ((this.layersInFrustum & 1 << renderLayer) == 0) {
            return;
        }
        renderType.setupRenderState();
        int size = this.renderChunksInFrustum.size();
        ProfilerFiller profiler = this.mc.getProfiler();
        if (renderLayer == RenderLayer.TRANSLUCENT) {
            profiler.push("translucent_sort");
            if (this.shouldSortTransparent(camX, camY, camZ)) {
                this.xTransparentOld = camX;
                this.yTransparentOld = camY;
                this.zTransparentOld = camZ;
                int j = 0;
                for (int i = 0; i < size; i++) {
                    if (j < 15 && this.renderChunksInFrustum.get(i).resortTransparency(this.chunkRenderDispatcher, this.renderOnThread)) {
                        ++j;
                    }
                }
            }
            profiler.pop();
        }
        profiler.push(RenderHelper.renderLayerName(renderLayer));
        VertexFormat format = renderType.format();
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;
        BufferUploader.reset();
        for (int k = 0; k < 12; ++k) {
            //Avoid allocating fixed name strings
            shader.setSampler(RenderHelper.SAMPLER_NAMES[k], RenderSystem.getShaderTexture(k));
        }
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(matrices.last().pose());
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(projectionMatrix);
        }
        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }
        if (shader.TEXTURE_MATRIX != null) {
            shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        RenderSystem.setupShaderLights(shader);
        shader.apply();
        Uniform uniform = shader.CHUNK_OFFSET;
        boolean drewStuff = this.drawLayer(size, uniform, renderLayer, camX, camY, camZ);
        if (uniform != null) {
            uniform.set(Vector3f.ZERO);
        }
        shader.clear();
        if (drewStuff) {
            format.clearBufferState();
        }
        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        profiler.pop();
        renderType.clearRenderState();
    }

    private void renderEntity(Entity entity,
                              double camX,
                              double camY,
                              double camZ,
                              float partialTick,
                              PoseStack matrices,
                              MultiBufferSource bufferSource) {
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        this.entityRenderDispatcher.render(entity, x - camX, y - camY, z - camZ, yRot, partialTick, matrices, bufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, partialTick));
    }

    private void renderGlobalTileEntities(Frustum frustum, PoseStack matrices, MultiBufferSource buffer, float camX, float camY, float camZ, float partialTicks) {
        for (long it = this.globalBlockEntities.beginIteration(); this.globalBlockEntities.hasNextIteration(it); it = this.globalBlockEntities.nextEntry(it)) {
            BlockEntity blockEntity = this.globalBlockEntities.getIteration(it);
            if (!frustum.isVisible(blockEntity.getRenderBoundingBox())) {
                continue;
            }
            BlockPos bePos = blockEntity.getBlockPos();
            matrices.pushPose();
            matrices.translate(bePos.getX() - camX, bePos.getY() - camY, bePos.getZ() - camZ);
            this.blockEntityRenderDispatcher.render(blockEntity, partialTicks, matrices, buffer);
            matrices.popPose();
        }
    }

    private void renderLoadFactor(PoseStack matrices, MultiBufferSource buffer, float camX, float camY, float camZ) {
        if (Minecraft.getInstance().showOnlyReducedInfo() || this.level == null) {
            return;
        }
        ClientEvents.CLIENT_INTEGRITY_STORAGE.render(this.level, matrices, buffer, camX, camY, camZ);
    }

    private void renderSnowAndRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ) {
        ClientLevel level = this.level;
        assert level != null;
        float f = level.getRainLevel(partialTick);
        if (!(f <= 0.0F)) {
            lightTexture.turnOnLightLayer();
            int i = Mth.floor(camX);
            int j = Mth.floor(camY);
            int k = Mth.floor(camZ);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (Minecraft.useFancyGraphics()) {
                l = 10;
            }
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int i1 = -1;
            float f1 = this.ticks + partialTick;
            RenderSystem.setShader(RenderHelper.SHADER_PARTICLE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            int cy = Mth.floor(camY);
            for (int z = k - l; z <= k + l; ++z) {
                for (int x = i - l; x <= i + l; ++x) {
                    int l1 = (z - k + 16) * 32 + x - i + 16;
                    double d0 = this.rainSizeX[l1] * 0.5;
                    double d1 = this.rainSizeZ[l1] * 0.5;
                    Biome biome = level.getBiome_(x, cy, z).value();
                    if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
                        int i2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                        int y = j - l;
                        int k2 = j + l;
                        if (y < i2) {
                            y = i2;
                        }
                        if (k2 < i2) {
                            k2 = i2;
                        }
                        if (y != k2) {
                            this.rainRandom.setSeed(3_121L * x * x + x * 45_238_971L ^ 418_711L * z * z + z * 13_761L);
                            int l2 = Math.max(i2, j);
                            if (biome.warmEnoughToRain(mutablePos.set(x, y, z))) {
                                if (i1 != 0) {
                                    if (i1 >= 0) {
                                        tesselator.end();
                                    }
                                    i1 = 0;
                                    RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }
                                int i3 = this.ticks + x * x * 3_121 + x * 45_238_971 + z * z * 418_711 + z * 13_761 & 31;
                                float f2 = -(i3 + partialTick) / 32.0F * (3.0F + this.rainRandom.nextFloat());
                                double d2 = x + 0.5 - camX;
                                double d4 = z + 0.5 - camZ;
                                float f3 = (float) Math.sqrt(d2 * d2 + d4 * d4) / l;
                                float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
                                int j3 = getLightColor(level, x, l2, z);
                                builder.vertex(x - camX - d0 + 0.5, k2 - camY, z - camZ - d1 + 0.5)
                                       .uv(0.0F, y * 0.25F + f2)
                                       .color(1.0F, 1.0F, 1.0F, f4)
                                       .uv2(j3)
                                       .endVertex();
                                builder.vertex(x - camX + d0 + 0.5, k2 - camY, z - camZ + d1 + 0.5)
                                       .uv(1.0F, y * 0.25F + f2)
                                       .color(1.0F, 1.0F, 1.0F, f4)
                                       .uv2(j3)
                                       .endVertex();
                                builder.vertex(x - camX + d0 + 0.5, y - camY, z - camZ + d1 + 0.5)
                                       .uv(1.0F, k2 * 0.25F + f2)
                                       .color(1.0F, 1.0F, 1.0F, f4)
                                       .uv2(j3)
                                       .endVertex();
                                builder.vertex(x - camX - d0 + 0.5, y - camY, z - camZ - d1 + 0.5)
                                       .uv(0.0F, k2 * 0.25F + f2)
                                       .color(1.0F, 1.0F, 1.0F, f4)
                                       .uv2(j3)
                                       .endVertex();
                            }
                            else {
                                if (i1 != 1) {
                                    if (i1 == 0) {
                                        tesselator.end();
                                    }
                                    i1 = 1;
                                    RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }
                                float f5 = -((this.ticks & 511) + partialTick) / 512.0F;
                                float f6 = (float) (this.rainRandom.nextDouble() + f1 * 0.01 * (float) this.rainRandom.nextGaussian());
                                float f7 = (float) (this.rainRandom.nextDouble() + (f1 * (float) this.rainRandom.nextGaussian()) * 0.001);
                                double d3 = x + 0.5 - camX;
                                double d5 = z + 0.5 - camZ;
                                float f8 = (float) Math.sqrt(d3 * d3 + d5 * d5) / l;
                                float f9 = ((1.0F - f8 * f8) * 0.3F + 0.5F) * f;
                                int k3 = getLightColor(level, x, l2, z);
                                int l3 = k3 >> 16 & '\uffff';
                                int i4 = k3 & '\uffff';
                                int j4 = (l3 * 3 + 240) / 4;
                                int k4 = (i4 * 3 + 240) / 4;
                                builder.vertex(x - camX - d0 + 0.5, k2 - camY, z - camZ - d1 + 0.5)
                                       .uv(0.0F + f6, y * 0.25F + f5 + f7)
                                       .color(1.0F, 1.0F, 1.0F, f9)
                                       .uv2(k4, j4)
                                       .endVertex();
                                builder.vertex(x - camX + d0 + 0.5, k2 - camY, z - camZ + d1 + 0.5)
                                       .uv(1.0F + f6, y * 0.25F + f5 + f7)
                                       .color(1.0F, 1.0F, 1.0F, f9)
                                       .uv2(k4, j4)
                                       .endVertex();
                                builder.vertex(x - camX + d0 + 0.5, y - camY, z - camZ + d1 + 0.5)
                                       .uv(1.0F + f6, k2 * 0.25F + f5 + f7)
                                       .color(1.0F, 1.0F, 1.0F, f9)
                                       .uv2(k4, j4)
                                       .endVertex();
                                builder.vertex(x - camX - d0 + 0.5, y - camY, z - camZ - d1 + 0.5)
                                       .uv(0.0F + f6, k2 * 0.25F + f5 + f7)
                                       .color(1.0F, 1.0F, 1.0F, f9)
                                       .uv2(k4, j4)
                                       .endVertex();
                            }
                        }
                    }
                }
            }
            if (i1 >= 0) {
                tesselator.end();
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lightTexture.turnOffLightLayer();
        }
    }

    private void renderWorldBorder(Camera camera) {
        assert this.level != null;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        WorldBorder worldborder = this.level.getWorldBorder();
        double d0 = this.mc.options.getEffectiveRenderDistance() * 16;
        if (!(camera.getPosition().x < worldborder.getMaxX() - d0) ||
            !(camera.getPosition().x > worldborder.getMinX() + d0) ||
            !(camera.getPosition().z < worldborder.getMaxZ() - d0) ||
            !(camera.getPosition().z > worldborder.getMinZ() + d0)) {
            double d1 = 1.0D - worldborder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d0;
            d1 = Math.pow(d1, 4.0D);
            d1 = Mth.clamp(d1, 0.0D, 1.0D);
            double d2 = camera.getPosition().x;
            double d3 = camera.getPosition().z;
            double d4 = this.mc.gameRenderer.getDepthFar();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE,
                                           GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            PoseStack internalMat = RenderSystem.getModelViewStack();
            internalMat.pushPose();
            RenderSystem.applyModelViewMatrix();
            int i = worldborder.getStatus().getColor();
            float f = (i >> 16 & 255) / 255.0F;
            float f1 = (i >> 8 & 255) / 255.0F;
            float f2 = (i & 255) / 255.0F;
            RenderSystem.setShaderColor(f, f1, f2, (float) d1);
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();
            float f3 = (Util.getMillis() % 3_000L) / 3_000.0F;
            float f6 = (float) (d4 - Mth.frac(camera.getPosition().y));
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            double d5 = Math.max(Mth.floor(d3 - d0), worldborder.getMinZ());
            double d6 = Math.min(Mth.ceil(d3 + d0), worldborder.getMaxZ());
            if (d2 > worldborder.getMaxX() - d0) {
                float f7 = 0.0F;
                for (double d7 = d5; d7 < d6; f7 += 0.5F) {
                    double d8 = Math.min(1, d6 - d7);
                    float f8 = (float) d8 * 0.5F;
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 - d3).uv(f3 - f7, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 - d3).uv(f3 - f7, f3 + 0.0F).endVertex();
                    ++d7;
                }
            }
            if (d2 < worldborder.getMinX() + d0) {
                float f9 = 0.0F;
                for (double d9 = d5; d9 < d6; f9 += 0.5F) {
                    double d12 = Math.min(1, d6 - d9);
                    float f12 = (float) d12 * 0.5F;
                    bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 - d3).uv(f3 + f9, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 - d3).uv(f3 + f9, f3 + 0.0F).endVertex();
                    ++d9;
                }
            }
            d5 = Math.max(Mth.floor(d2 - d0), worldborder.getMinX());
            d6 = Math.min(Mth.ceil(d2 + d0), worldborder.getMaxX());
            if (d3 > worldborder.getMaxZ() - d0) {
                float f10 = 0.0F;
                for (double d10 = d5; d10 < d6; f10 += 0.5F) {
                    double d13 = Math.min(1, d6 - d10);
                    float f13 = (float) d13 * 0.5F;
                    bufferbuilder.vertex(d10 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + f6).endVertex();
                    bufferbuilder.vertex(d10 + d13 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + f6).endVertex();
                    bufferbuilder.vertex(d10 + d13 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(d10 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + 0.0F).endVertex();
                    ++d10;
                }
            }
            if (d3 < worldborder.getMinZ() + d0) {
                float f11 = 0.0F;
                for (double d11 = d5; d11 < d6; f11 += 0.5F) {
                    double d14 = Math.min(1, d6 - d11);
                    float f14 = (float) d14 * 0.5F;
                    bufferbuilder.vertex(d11 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + f6).endVertex();
                    bufferbuilder.vertex(d11 + d14 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + f6).endVertex();
                    bufferbuilder.vertex(d11 + d14 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(d11 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + 0.0F).endVertex();
                    ++d11;
                }
            }
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            internalMat.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
        }
    }

    private void setBlockDirty(int x, int y, int z, boolean reRenderOnMainThread) {
        for (int dz = z - 1; dz <= z + 1; ++dz) {
            int secZ = SectionPos.blockToSectionCoord(dz);
            for (int dx = x - 1; dx <= x + 1; ++dx) {
                int secX = SectionPos.blockToSectionCoord(dx);
                for (int dy = y - 1; dy <= y + 1; ++dy) {
                    this.setSectionDirty(secX, SectionPos.blockToSectionCoord(dy), secZ, reRenderOnMainThread);
                }
            }
        }
    }

    private void setSectionDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
        assert this.viewArea != null;
        this.viewArea.setDirty(sectionX, sectionY, sectionZ, reRenderOnMainThread);
    }

    private void setupRender(Camera camera, Frustum frustum, boolean isSpectator) {
        assert this.level != null;
        assert this.chunkRenderDispatcher != null;
        assert this.viewArea != null;
        if (this.mc.options.getEffectiveRenderDistance() != this.lastViewDistance) {
            this.allChanged();
        }
        ProfilerFiller profiler = this.mc.getProfiler();
        profiler.push("camera");
        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;
        int secX = SectionPos.posToSectionCoord(camX);
        int secY = SectionPos.posToSectionCoord(camY);
        int secZ = SectionPos.posToSectionCoord(camZ);
        if (this.lastCameraChunkX != secX || this.lastCameraChunkY != secY || this.lastCameraChunkZ != secZ) {
            this.lastCameraChunkX = secX;
            this.lastCameraChunkY = secY;
            this.lastCameraChunkZ = secZ;
            this.viewArea.repositionCamera(camX, camZ);
        }
        this.chunkRenderDispatcher.setCamera((float) camX, (float) camY, (float) camZ);
        profiler.popPush("culling");
        double currentCamX = Math.floor(camX / 8.0);
        double currentCamY = Math.floor(camY / 8.0);
        double currentCamZ = Math.floor(camZ / 8.0);
        this.needsFullRenderChunkUpdate = this.needsFullRenderChunkUpdate || currentCamX != this.prevCamX || currentCamY != this.prevCamY || currentCamZ != this.prevCamZ;
        this.nextFullUpdateMillis.updateAndGet(l -> {
            if (l > 0L && System.currentTimeMillis() > l) {
                this.needsFullRenderChunkUpdate = true;
                return 0L;
            }
            return l;
        });
        this.prevCamX = currentCamX;
        this.prevCamY = currentCamY;
        this.prevCamZ = currentCamZ;
        profiler.popPush("update");
        boolean smartCull;
        if (!isSpectator) {
            smartCull = true;
        }
        else {
            BlockPos pos = camera.getBlockPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            smartCull = !this.level.getBlockState_(x, y, z).isSolidRender_(this.level, x, y, z);
        }
        if (this.needsFullRenderChunkUpdate && (this.lastFullRenderChunkUpdate == null || this.lastFullRenderChunkUpdate.isDone())) {
            profiler.push("full_update_schedule");
            this.needsFullRenderChunkUpdate = false;
            this.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
                OArrayFIFOQueue<RenderChunkInfo> queue = QUEUE_CACHE.get();
                queue.clear();
                this.initializeQueueForFullUpdate(camera, queue);
                RenderChunkStorage renderChunkStorage = new RenderChunkStorage(this.viewArea.chunks.length);
                this.updateRenderChunks(renderChunkStorage, camPos, queue, smartCull);
                queue.clear();
                this.renderChunkStorage = renderChunkStorage;
                this.needsFrustumUpdate.set(true);
            });
            profiler.pop();
        }
        profiler.push("partial_update");
        RenderChunkStorage storage = this.renderChunkStorage;
        assert storage != null;
        if (this.renderOnThread) {
            this.partialUpdate(storage, camPos, smartCull);
        }
        else {
            synchronized (this.recentlyCompiledChunks) {
                this.partialUpdate(storage, camPos, smartCull);
            }
        }
        profiler.pop();
        if (this.needsFrustumUpdate(camera)) {
            this.applyFrustum(frustum.offsetToFullyIncludeCameraCube(8));
        }
        profiler.pop();
    }

    private boolean shouldSortTransparent(double camX, double camY, double camZ) {
        double dx = camX - this.xTransparentOld;
        double dy = camY - this.yTransparentOld;
        double dz = camZ - this.zTransparentOld;
        if (this.ticks - this.lastTransparencyTick >= 5) {
            this.lastTransparencyTick = this.ticks;
            return dx != 0 || dy != 0 || dz != 0;
        }
        return dx * dx + dy * dy + dz * dz > 1;
    }

    private void updateRenderChunks(RenderChunkStorage storage,
                                    Vec3 viewVector,
                                    OArrayFIFOQueue<RenderChunkInfo> queue,
                                    boolean shouldCull) {
        assert this.level != null;
        assert this.viewArea != null;
        int cameraX = Mth.floor(viewVector.x / 16) * 16;
        int cameraY = Mth.floor(viewVector.y / 16) * 16;
        int cameraZ = Mth.floor(viewVector.z / 16) * 16;
        int centerX = cameraX + 8;
        int centerY = cameraY + 8;
        int centerZ = cameraZ + 8;
        Entity.setViewScale(Mth.clamp(this.mc.options.getEffectiveRenderDistance() / 8.0, 1, 2.5) * this.mc.options.entityDistanceScaling);
        RenderInfoMap infoMap = storage.renderInfoMap;
        while (!queue.isEmpty()) {
            RenderChunkInfo info = queue.dequeue();
            EvChunkRenderDispatcher.RenderChunk chunk = info.chunk;
            storage.add(chunk);
            int chunkX = chunk.getX();
            int chunkY = chunk.getY();
            int chunkZ = chunk.getZ();
            Direction nearestDir = Direction.getNearest(chunkX - cameraX, chunkY - cameraY, chunkZ - cameraZ);
            boolean far = Math.abs(chunkX - cameraX) > 60 || Math.abs(chunkY - cameraY) > 60 || Math.abs(chunkZ - cameraZ) > 60;
            boolean askedForUpdate = false;
            directions:
            for (Direction dir : DirectionUtil.ALL) {
                EvChunkRenderDispatcher.RenderChunk chunkAtDir = this.getRelativeFrom(cameraX, cameraY, cameraZ, chunk, dir);
                if (chunkAtDir == null) {
                    if (!askedForUpdate && !this.closeToBorder(cameraX, cameraZ, chunk)) {
                        this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                        askedForUpdate = true;
                    }
                }
                else {
                    if (!shouldCull || !info.hasDirection(dir.getOpposite())) {
                        if (shouldCull) {
                            if (info.hasSourceDirections()) {
                                EvChunkRenderDispatcher.CompiledChunk compiled = chunk.compiled;
                                boolean cull = false;
                                for (Direction dirForCull : DirectionUtil.ALL) {
                                    if (info.hasSourceDirection(dirForCull) && compiled.facesCanSeeEachother(dirForCull.getOpposite(), dir)) {
                                        cull = true;
                                        break;
                                    }
                                }
                                if (!cull) {
                                    continue;
                                }
                            }
                            if (far) {
                                if (info.hasSourceDirections() && !info.hasSourceDirection(nearestDir)) {
                                    EvChunkRenderDispatcher.RenderChunk chunkAtNearest = this.getRelativeFrom(cameraX, cameraY, cameraZ, chunk,
                                                                                                              nearestDir.getOpposite());
                                    if (chunkAtNearest == null) {
                                        continue;
                                    }
                                    if (infoMap.get(chunkAtNearest) == null) {
                                        continue;
                                    }
                                }
                                byte dx = 0;
                                byte dy = 0;
                                byte dz = 0;
                                switch (dir.getAxis()) {
                                    case X -> {
                                        if (centerX > chunkAtDir.getX()) {
                                            dx = 16;
                                        }
                                        if (centerY < chunkAtDir.getY()) {
                                            dy = 16;
                                        }
                                        if (centerZ < chunkAtDir.getZ()) {
                                            dz = 16;
                                        }
                                    }
                                    case Y -> {
                                        if (centerX < chunkAtDir.getX()) {
                                            dx = 16;
                                        }
                                        if (centerY > chunkAtDir.getY()) {
                                            dy = 16;
                                        }
                                        if (centerZ < chunkAtDir.getZ()) {
                                            dz = 16;
                                        }
                                    }
                                    case Z -> {
                                        if (centerX < chunkAtDir.getX()) {
                                            dx = 16;
                                        }
                                        if (centerY < chunkAtDir.getY()) {
                                            dy = 16;
                                        }
                                        if (centerZ > chunkAtDir.getZ()) {
                                            dz = 16;
                                        }
                                    }
                                }
                                double chunkAtDirX = chunkAtDir.getX() + dx;
                                double chunkAtDirY = chunkAtDir.getY() + dy;
                                double chunkAtDirZ = chunkAtDir.getZ() + dz;
                                double deltaX = viewVector.x - chunkAtDirX;
                                double deltaY = viewVector.y - chunkAtDirY;
                                double deltaZ = viewVector.z - chunkAtDirZ;
                                double norm = VectorUtil.norm(deltaX, deltaY, deltaZ) * 28;
                                deltaX *= norm;
                                deltaY *= norm;
                                deltaZ *= norm;
                                while (VectorUtil.subtractLengthSqr(viewVector, chunkAtDirX, chunkAtDirY, chunkAtDirZ) > 3_600) {
                                    chunkAtDirX += deltaX;
                                    chunkAtDirY += deltaY;
                                    chunkAtDirZ += deltaZ;
                                    if (chunkAtDirY > this.level.getMaxBuildHeight() || chunkAtDirY < this.level.getMinBuildHeight()) {
                                        break;
                                    }
                                    EvChunkRenderDispatcher.RenderChunk chunkAt = this.viewArea.getRenderChunkAt(Mth.floor(chunkAtDirX),
                                                                                                                 Mth.floor(chunkAtDirY),
                                                                                                                 Mth.floor(chunkAtDirZ));
                                    if (chunkAt == null || infoMap.get(chunkAt) == null) {
                                        continue directions;
                                    }
                                }
                            }
                        }
                        RenderChunkInfo infoAtDir = infoMap.get(chunkAtDir);
                        if (infoAtDir != null) {
                            infoAtDir.addSourceDirection(dir);
                        }
                        else if (!chunkAtDir.hasAllNeighbors()) {
                            if (!this.closeToBorder(cameraX, cameraZ, chunk)) {
                                this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                            }
                        }
                        else {
                            //noinspection ObjectAllocationInLoop
                            RenderChunkInfo newInfo = new RenderChunkInfo(chunkAtDir, dir, info.step + 1);
                            newInfo.setDirections(info.directions, dir);
                            queue.enqueue(newInfo);
                            infoMap.put(chunkAtDir, newInfo);
                        }
                    }
                }
            }
        }
    }

    public static class RenderChunkInfo {
        public final EvChunkRenderDispatcher.RenderChunk chunk;
        public final int step;
        private byte directions;
        private byte sourceDirections;

        public RenderChunkInfo(EvChunkRenderDispatcher.RenderChunk chunk, @Nullable Direction sourceDir, int step) {
            this.chunk = chunk;
            if (sourceDir != null) {
                this.addSourceDirection(sourceDir);
            }
            this.step = step;
        }

        public void addSourceDirection(Direction direction) {
            this.sourceDirections |= (byte) (this.sourceDirections | 1 << direction.ordinal());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RenderChunkInfo other)) {
                return false;
            }
            EvChunkRenderDispatcher.RenderChunk chunk = this.chunk;
            EvChunkRenderDispatcher.RenderChunk otherChunk = other.chunk;
            return chunk.getX() == otherChunk.getX() && chunk.getY() == otherChunk.getY() && chunk.getZ() == otherChunk.getZ();
        }

        public boolean hasDirection(Direction facing) {
            return (this.directions & 1 << facing.ordinal()) != 0;
        }

        public boolean hasSourceDirection(Direction dir) {
            return (this.sourceDirections & 1 << dir.ordinal()) > 0;
        }

        public boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        @Override
        public int hashCode() {
            EvChunkRenderDispatcher.RenderChunk chunk = this.chunk;
            return (chunk.getY() + chunk.getZ() * 31) * 31 + chunk.getX();
        }

        public void setDirections(byte dir, Direction facing) {
            this.directions |= (byte) (dir | 1 << facing.ordinal());
        }
    }

    private static class RenderChunkInfoComparator implements Comparator<RenderChunkInfo> {

        private BlockPos cameraPos = BlockPos.ZERO;

        @Override
        public int compare(RenderChunkInfo o1, RenderChunkInfo o2) {
            EvChunkRenderDispatcher.RenderChunk chunk1 = o1.chunk;
            double dist1 = this.cameraPos.distToLowCornerSqr(chunk1.getX() + 8, chunk1.getY() + 8, chunk1.getZ() + 8);
            EvChunkRenderDispatcher.RenderChunk chunk2 = o2.chunk;
            double dist2 = this.cameraPos.distToLowCornerSqr(chunk2.getX() + 8, chunk2.getY() + 8, chunk2.getZ() + 8);
            return Double.compare(dist1, dist2);
        }

        public RenderChunkInfoComparator setCameraPos(BlockPos pos) {
            this.cameraPos = pos;
            return this;
        }
    }

    public static class RenderChunkStorage {
        public final RenderInfoMap renderInfoMap;
        private final OList<EvChunkRenderDispatcher.RenderChunk> renderChunks;
        private final LSet visited = new LHashSet();

        public RenderChunkStorage(int size) {
            this.renderInfoMap = new RenderInfoMap(size);
            this.renderChunks = new OArrayList<>(size);
        }

        public void add(EvChunkRenderDispatcher.RenderChunk chunk) {
            if (this.visited.add(BlockPos.asLong(chunk.getX(), chunk.getY(), chunk.getZ()))) {
                this.renderChunks.add(chunk);
            }
        }
    }

    public static class RenderInfoMap {
        private final RenderChunkInfo[] infos;

        RenderInfoMap(int size) {
            this.infos = new RenderChunkInfo[size];
        }

        public @Nullable RenderChunkInfo get(EvChunkRenderDispatcher.RenderChunk renderChunk) {
            int i = renderChunk.index;
            return i >= 0 && i < this.infos.length ? this.infos[i] : null;
        }

        public void put(EvChunkRenderDispatcher.RenderChunk renderChunk, RenderChunkInfo info) {
            this.infos[renderChunk.index] = info;
        }
    }

    public static class TransparencyShaderException extends RuntimeException {
        public TransparencyShaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
