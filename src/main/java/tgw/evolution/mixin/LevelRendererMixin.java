package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfig;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.math.VectorUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("VariableNotUsedInsideIf")
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    public static Direction[] DIRECTIONS;
    @Shadow
    @Final
    private static double CEILED_SECTION_DIAGONAL;
    private final BlockPos.MutableBlockPos destructionPos = new BlockPos.MutableBlockPos();
    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Shadow
    private boolean captureFrustum;
    @Shadow
    @Nullable
    private Frustum capturedFrustum;
    @Shadow
    @Nullable
    private ChunkRenderDispatcher chunkRenderDispatcher;
    @Shadow
    @Nullable
    private RenderTarget cloudsTarget;
    @Shadow
    private int culledEntities;
    @Shadow
    private Frustum cullingFrustum;
    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;
    @Shadow
    @Nullable
    private PostChain entityEffect;
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow
    @Nullable
    private RenderTarget entityTarget;
    @Shadow
    @Final
    private Vector3d frustumPos;
    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;
    @Shadow
    @Nullable
    private RenderTarget itemEntityTarget;
    @Shadow
    @Nullable
    private ClientLevel level;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private AtomicLong nextFullUpdateMillis;
    @Shadow
    @Nullable
    private RenderTarget particlesTarget;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    @Final
    private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;
    @Shadow
    private int renderedEntities;
    @Shadow
    @Nullable
    private RenderTarget translucentTarget;
    @Shadow
    @Nullable
    private PostChain transparencyChain;
    @Shadow
    @Nullable
    private ViewArea viewArea;
    @Shadow
    @Nullable
    private RenderTarget weatherTarget;
    @Shadow
    private double xTransparentOld;
    @Shadow
    private double yTransparentOld;
    @Shadow
    private double zTransparentOld;

    @Shadow
    protected abstract void captureFrustum(Matrix4f pViewMatrix,
                                           Matrix4f pProjectionMatrix,
                                           double pCamX,
                                           double pCamY,
                                           double pCamZ,
                                           Frustum pFrustum);

    @Shadow
    protected abstract void checkPoseStack(PoseStack pPoseStack);

    @Shadow
    protected abstract boolean closeToBorder(BlockPos pPos, ChunkRenderDispatcher.RenderChunk pChunk);

    /**
     * @author MGSchultz
     * Avoid unnecessary allocations.
     */
    @Overwrite
    private void compileChunks(Camera camera) {
        this.minecraft.getProfiler().push("populate_chunks_to_compile");
        RenderRegionCache renderRegionCache = new RenderRegionCache();
        BlockPos cameraPos = camera.getBlockPosition();
        //Use faster list
        List<ChunkRenderDispatcher.RenderChunk> list = new ReferenceArrayList<>();
        for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
            ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
            //Avoid allocating a new ChunkPos just to use its coordinates
            int chunkX = SectionPos.blockToSectionCoord(renderChunk.getOrigin().getX());
            int chunkZ = SectionPos.blockToSectionCoord(renderChunk.getOrigin().getZ());
            if (renderChunk.isDirty() && this.level.getChunk(chunkX, chunkZ).isClientLightReady()) {
                boolean flag = false;
                if (this.minecraft.options.prioritizeChunkUpdates != PrioritizeChunkUpdates.NEARBY) {
                    if (this.minecraft.options.prioritizeChunkUpdates == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                        flag = renderChunk.isDirtyFromPlayer();
                    }
                }
                else {
                    //Avoid allocating a BlockPos just to calculate distance
                    int chunkCenterPosX = renderChunk.getOrigin().getX() + 8;
                    int chunkCenterPosY = renderChunk.getOrigin().getY() + 8;
                    int chunkCenterPosZ = renderChunk.getOrigin().getZ() + 8;
                    flag = !ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.get() &&
                           (VectorUtil.distSqr(cameraPos, chunkCenterPosX, chunkCenterPosY, chunkCenterPosZ) < 768 ||
                            renderChunk.isDirtyFromPlayer()); // the target is the else block below, so invert the forge addition to get there early
                }
                if (flag) {
                    this.minecraft.getProfiler().push("build_near_sync");
                    this.chunkRenderDispatcher.rebuildChunkSync(renderChunk, renderRegionCache);
                    renderChunk.setNotDirty();
                    this.minecraft.getProfiler().pop();
                }
                else {
                    list.add(renderChunk);
                }
            }
        }
        this.minecraft.getProfiler().popPush("upload");
        this.chunkRenderDispatcher.uploadAllPendingUploads();
        this.minecraft.getProfiler().popPush("schedule_async_compile");
        for (ChunkRenderDispatcher.RenderChunk renderChunk : list) {
            renderChunk.rebuildChunkAsync(this.chunkRenderDispatcher, renderRegionCache);
            renderChunk.setNotDirty();
        }
        this.minecraft.getProfiler().pop();
    }

    @Shadow
    @Nullable
    protected abstract ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos pCameraChunkPos,
                                                                         ChunkRenderDispatcher.RenderChunk pRenderChunk,
                                                                         Direction pFacing);

    @Shadow
    public abstract void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData);

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations
     */
    @Overwrite
    private void renderChunkLayer(RenderType renderType, PoseStack matrices, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        RenderSystem.assertOnRenderThread();
        renderType.setupRenderState();
        if (renderType == RenderType.translucent()) {
            this.minecraft.getProfiler().push("translucent_sort");
            double d0 = camX - this.xTransparentOld;
            double d1 = camY - this.yTransparentOld;
            double d2 = camZ - this.zTransparentOld;
            if (d0 * d0 + d1 * d1 + d2 * d2 > 1) {
                this.xTransparentOld = camX;
                this.yTransparentOld = camY;
                this.zTransparentOld = camZ;
                int j = 0;
                for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
                    if (j < 15 && renderChunkInfo.chunk.resortTransparency(renderType, this.chunkRenderDispatcher)) {
                        ++j;
                    }
                }
            }
            this.minecraft.getProfiler().pop();
        }
        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + renderType);
        boolean flag = renderType != RenderType.translucent();
        ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = this.renderChunksInFrustum.listIterator(
                flag ? 0 : this.renderChunksInFrustum.size());
        VertexFormat vertexformat = renderType.format();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        BufferUploader.reset();
        for (int k = 0; k < 12; ++k) {
            int i = RenderSystem.getShaderTexture(k);
            //Avoid allocating fixed name strings
            shaderinstance.setSampler(RenderHelper.SAMPLER_NAMES[k], i);
        }
        if (shaderinstance.MODEL_VIEW_MATRIX != null) {
            shaderinstance.MODEL_VIEW_MATRIX.set(matrices.last().pose());
        }
        if (shaderinstance.PROJECTION_MATRIX != null) {
            shaderinstance.PROJECTION_MATRIX.set(projectionMatrix);
        }
        if (shaderinstance.COLOR_MODULATOR != null) {
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (shaderinstance.FOG_START != null) {
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (shaderinstance.FOG_END != null) {
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (shaderinstance.FOG_COLOR != null) {
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (shaderinstance.TEXTURE_MATRIX != null) {
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shaderinstance.GAME_TIME != null) {
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;
        boolean flag1 = false;
        while (true) {
            if (flag) {
                if (!objectlistiterator.hasNext()) {
                    break;
                }
            }
            else if (!objectlistiterator.hasPrevious()) {
                break;
            }
            LevelRenderer.RenderChunkInfo renderChunkInfo = flag ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.RenderChunk chunk = renderChunkInfo.chunk;
            if (!chunk.getCompiledChunk().isEmpty(renderType)) {
                VertexBuffer vertexbuffer = chunk.getBuffer(renderType);
                BlockPos blockpos = chunk.getOrigin();
                if (uniform != null) {
                    uniform.set((float) (blockpos.getX() - camX), (float) (blockpos.getY() - camY), (float) (blockpos.getZ() - camZ));
                    uniform.upload();
                }
                vertexbuffer.drawChunkLayer();
                flag1 = true;
            }
        }
        if (uniform != null) {
            uniform.set(Vector3f.ZERO);
        }
        shaderinstance.clear();
        if (flag1) {
            vertexformat.clearBufferState();
        }
        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        this.minecraft.getProfiler().pop();
        renderType.clearRenderState();
    }

    @Shadow
    public abstract void renderClouds(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, double pCamX, double pCamY, double pCamZ);

    @Shadow
    protected abstract void renderDebug(Camera pCamera);

    @Shadow
    protected abstract void renderEntity(Entity p_109518_,
                                         double p_109519_,
                                         double p_109520_,
                                         double p_109521_,
                                         float p_109522_,
                                         PoseStack p_109523_,
                                         MultiBufferSource p_109524_);

    @Shadow
    protected abstract void renderHitOutline(PoseStack pPoseStack,
                                             VertexConsumer pConsumer,
                                             Entity pEntity,
                                             double pCamX,
                                             double pCamY,
                                             double pCamZ,
                                             BlockPos pPos,
                                             BlockState pState);

    /**
     * @author MGSchultz
     * <p>
     * Implement first person renderer in a clean way, also small level optimizations
     */
    @Overwrite
    public void renderLevel(PoseStack matrices,
                            float partialTick,
                            long finishNanoTime,
                            boolean renderBlockOutline,
                            Camera camera,
                            GameRenderer gameRenderer,
                            LightTexture lightTexture,
                            Matrix4f projectionMatrix) {
        RenderSystem.setShaderGameTime(this.level.getGameTime(), partialTick);
        this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.popPush("light_update_queue");
        this.level.pollLightUpdates();
        profiler.popPush("light_updates");
        this.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, this.level.isLightUpdateQueueEmpty(), true);
        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;
        Matrix4f pose = matrices.last().pose();
        profiler.popPush("culling");
        boolean hasFrustrum = this.capturedFrustum != null;
        Frustum frustum;
        if (hasFrustrum) {
            frustum = this.capturedFrustum;
            frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
        }
        else {
            frustum = this.cullingFrustum;
        }
        this.minecraft.getProfiler().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.captureFrustum(pose, projectionMatrix, camPos.x, camPos.y, camPos.z, hasFrustrum ? new Frustum(pose, projectionMatrix) : frustum);
            this.captureFrustum = false;
        }
        profiler.popPush("clear");
        FogRenderer.setupColor(camera, partialTick, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(),
                               gameRenderer.getDarkenWorldAmount(partialTick));
        FogRenderer.levelFogColor();
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        float renderDistance = gameRenderer.getRenderDistance();
        boolean isFoggy = this.minecraft.level.effects().isFoggyAt(Mth.floor(camX), Mth.floor(camY)) ||
                          this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, isFoggy, partialTick);
        profiler.popPush("sky");
        RenderSystem.setShader(GameRenderer::getPositionShader);
        this.renderSky(matrices, projectionMatrix, partialTick,
                       () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, isFoggy, partialTick));
        profiler.popPush("fog");
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDistance, 32.0F), isFoggy, partialTick);
        profiler.popPush("terrain_setup");
        this.setupRender(camera, frustum, hasFrustrum, this.minecraft.player.isSpectator());
        profiler.popPush("compilechunks");
        this.compileChunks(camera);
        profiler.popPush("terrain");
        this.renderChunkLayer(RenderType.solid(), matrices, camX, camY, camZ, projectionMatrix);
        this.minecraft.getModelManager()
                      .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                      .setBlurMipmap(false, this.minecraft.options.mipmapLevels >
                                            0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        this.renderChunkLayer(RenderType.cutoutMipped(), matrices, camX, camY, camZ, projectionMatrix);
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        this.renderChunkLayer(RenderType.cutout(), matrices, camX, camY, camZ, projectionMatrix);
        if (this.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel(matrices.last().pose());
        }
        else {
            Lighting.setupLevel(matrices.last().pose());
        }
        profiler.popPush("entities");
        this.renderedEntities = 0;
        this.culledEntities = 0;
        if (this.itemEntityTarget != null) {
            this.itemEntityTarget.clear(Minecraft.ON_OSX);
            this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        if (this.weatherTarget != null) {
            this.weatherTarget.clear(Minecraft.ON_OSX);
        }
        if (this.shouldShowEntityOutlines()) {
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        boolean hasGlowing = false;
        MultiBufferSource.BufferSource buffer = this.renderBuffers.bufferSource();
        for (Entity entity : this.level.entitiesForRendering()) {
            if ((this.entityRenderDispatcher.shouldRender(entity, frustum, camX, camY, camZ) || entity.hasIndirectPassenger(this.minecraft.player)) &&
                (entity != camera.getEntity() ||
                 camera.isDetached() ||
                 camera.getEntity() instanceof LivingEntity && ((LivingEntity) camera.getEntity()).isSleeping())) {
                ++this.renderedEntities;
                if (entity.tickCount == 0) {
                    entity.xOld = entity.getX();
                    entity.yOld = entity.getY();
                    entity.zOld = entity.getZ();
                }
                MultiBufferSource multiBufferSource;
                if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
                    hasGlowing = true;
                    OutlineBufferSource outlineBuffer = this.renderBuffers.outlineBufferSource();
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
                this.renderEntity(entity, camX, camY, camZ, partialTick, matrices, multiBufferSource);
            }
        }
        //Render player in first person
        Entity entity = camera.getEntity();
        if (EvolutionConfig.CLIENT.firstPersonRenderer.get() &&
            !camera.isDetached() &&
            entity.isAlive() &&
            !(entity instanceof LivingEntity living && living.isSleeping())) {
            this.renderedEntities++;
            MultiBufferSource multiBufferSource;
            if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
                hasGlowing = true;
                OutlineBufferSource outlineBuffer = this.renderBuffers.outlineBufferSource();
                multiBufferSource = outlineBuffer;
                int teamColor = entity.getTeamColor();
                int red = teamColor >> 16 & 255;
                int green = teamColor >> 8 & 255;
                int blue = teamColor & 255;
                outlineBuffer.setColor(red, green, blue, 255);
            }
            else {
                multiBufferSource = buffer;
            }
            ClientEvents.getInstance().getRenderer().isRenderingPlayer = true;
            this.renderEntity(entity, camX, camY, camZ, partialTick, matrices, multiBufferSource);
            ClientEvents.getInstance().getRenderer().isRenderingPlayer = false;
        }
        buffer.endLastBatch();
        this.checkPoseStack(matrices);
        buffer.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        buffer.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        profiler.popPush("blockentities");
        for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
            List<BlockEntity> blockEntities = renderChunkInfo.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!blockEntities.isEmpty()) {
                for (BlockEntity blockEntity : blockEntities) {
                    if (!frustum.isVisible(blockEntity.getRenderBoundingBox())) {
                        continue;
                    }
                    BlockPos bePos = blockEntity.getBlockPos();
                    MultiBufferSource multiBufferSource = buffer;
                    matrices.pushPose();
                    matrices.translate(bePos.getX() - camX, bePos.getY() - camY, bePos.getZ() - camZ);
                    SortedSet<BlockDestructionProgress> destructionProgresses = this.destructionProgress.get(bePos.asLong());
                    if (destructionProgresses != null && !destructionProgresses.isEmpty()) {
                        int progress = destructionProgresses.last().getProgress();
                        if (progress >= 0) {
                            PoseStack.Pose entry = matrices.last();
                            //noinspection ObjectAllocationInLoop
                            VertexConsumer consumer = new SheetedDecalTextureGenerator(
                                    this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), entry.pose(),
                                    entry.normal());
                            //noinspection ObjectAllocationInLoop
                            multiBufferSource = renderType -> {
                                VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
                                return renderType.affectsCrumbling() ? VertexMultiConsumer.create(consumer, vertexConsumer) : vertexConsumer;
                            };
                        }
                    }
                    this.blockEntityRenderDispatcher.render(blockEntity, partialTick, matrices, multiBufferSource);
                    matrices.popPose();
                }
            }
        }
        synchronized (this.globalBlockEntities) {
            for (BlockEntity blockEntity : this.globalBlockEntities) {
                if (!frustum.isVisible(blockEntity.getRenderBoundingBox())) {
                    continue;
                }
                BlockPos bePos = blockEntity.getBlockPos();
                matrices.pushPose();
                matrices.translate(bePos.getX() - camX, bePos.getY() - camY, bePos.getZ() - camZ);
                this.blockEntityRenderDispatcher.render(blockEntity, partialTick, matrices, buffer);
                matrices.popPose();
            }
        }
        this.checkPoseStack(matrices);
        buffer.endBatch(RenderType.solid());
        buffer.endBatch(RenderType.endPortal());
        buffer.endBatch(RenderType.endGateway());
        buffer.endBatch(Sheets.solidBlockSheet());
        buffer.endBatch(Sheets.cutoutBlockSheet());
        buffer.endBatch(Sheets.bedSheet());
        buffer.endBatch(Sheets.shulkerBoxSheet());
        buffer.endBatch(Sheets.signSheet());
        buffer.endBatch(Sheets.chestSheet());
        this.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (hasGlowing) {
            this.entityEffect.process(partialTick);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        profiler.popPush("destroyProgress");
        for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> destructionEntry : this.destructionProgress.long2ObjectEntrySet()) {
            //Replaced with a fixed MutableBlockPos instance
            this.destructionPos.set(destructionEntry.getLongKey());
            double deltaX = this.destructionPos.getX() - camX;
            double deltaY = this.destructionPos.getY() - camY;
            double deltaZ = this.destructionPos.getZ() - camZ;
            if (!(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1_024.0D)) {
                SortedSet<BlockDestructionProgress> destructionProgresses = destructionEntry.getValue();
                if (destructionProgresses != null && !destructionProgresses.isEmpty()) {
                    int progress = destructionProgresses.last().getProgress();
                    matrices.pushPose();
                    matrices.translate(deltaX, deltaY, deltaZ);
                    PoseStack.Pose entry = matrices.last();
                    //noinspection ObjectAllocationInLoop
                    VertexConsumer consumer = new SheetedDecalTextureGenerator(
                            this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), entry.pose(),
                            entry.normal());
                    this.minecraft.getBlockRenderer()
                                  .renderBreakingTexture(this.level.getBlockState(this.destructionPos), this.destructionPos, this.level, matrices,
                                                         consumer);
                    matrices.popPose();
                }
            }
        }
        this.checkPoseStack(matrices);
        HitResult hitResult = this.minecraft.hitResult;
        if (renderBlockOutline && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            profiler.popPush("outline");
            BlockPos resultPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState state = this.level.getBlockState(resultPos);
            if (!ForgeHooksClient.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, partialTick, matrices, buffer)) {
                if (!state.isAir() && this.level.getWorldBorder().isWithinBounds(resultPos)) {
                    VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
                    this.renderHitOutline(matrices, consumer, camera.getEntity(), camX, camY, camZ, resultPos, state);
                }
            }
        }
        //Added check for miss
        else if (hitResult != null && (hitResult.getType() == HitResult.Type.ENTITY || hitResult.getType() == HitResult.Type.MISS)) {
            ForgeHooksClient.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, partialTick, matrices, buffer);
        }
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        this.minecraft.debugRenderer.render(matrices, buffer, camX, camY, camZ);
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
        this.renderBuffers.crumblingBufferSource().endBatch();
        if (this.transparencyChain != null) {
            buffer.endBatch(RenderType.lines());
            buffer.endBatch();
            this.translucentTarget.clear(Minecraft.ON_OSX);
            this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            profiler.popPush("translucent");
            this.renderChunkLayer(RenderType.translucent(), matrices, camX, camY, camZ, projectionMatrix);
            profiler.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), matrices, camX, camY, camZ, projectionMatrix);
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            profiler.popPush("particles");
            this.minecraft.particleEngine.render(matrices, buffer, lightTexture, camera, partialTick, frustum);
            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        }
        else {
            profiler.popPush("translucent");
            if (this.translucentTarget != null) {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }
            this.renderChunkLayer(RenderType.translucent(), matrices, camX, camY, camZ, projectionMatrix);
            buffer.endBatch(RenderType.lines());
            buffer.endBatch();
            profiler.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), matrices, camX, camY, camZ, projectionMatrix);
            profiler.popPush("particles");
            this.minecraft.particleEngine.render(matrices, buffer, lightTexture, camera, partialTick, frustum);
        }
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            if (this.transparencyChain != null) {
                this.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderStateShard.CLOUDS_TARGET.setupRenderState();
                profiler.popPush("clouds");
                this.renderClouds(matrices, projectionMatrix, partialTick, camX, camY, camZ);
                RenderStateShard.CLOUDS_TARGET.clearRenderState();
            }
            else {
                profiler.popPush("clouds");
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                this.renderClouds(matrices, projectionMatrix, partialTick, camX, camY, camZ);
            }
        }
        if (this.transparencyChain != null) {
            RenderStateShard.WEATHER_TARGET.setupRenderState();
            profiler.popPush("weather");
            this.renderSnowAndRain(lightTexture, partialTick, camX, camY, camZ);
            this.renderWorldBorder(camera);
            RenderStateShard.WEATHER_TARGET.clearRenderState();
            this.transparencyChain.process(partialTick);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        else {
            RenderSystem.depthMask(false);
            profiler.popPush("weather");
            this.renderSnowAndRain(lightTexture, partialTick, camX, camY, camZ);
            this.renderWorldBorder(camera);
            RenderSystem.depthMask(true);
        }
        this.renderDebug(camera);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        FogRenderer.setupNoFog();
    }

    @Shadow
    public abstract void renderSky(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, Runnable pSkyFogSetup);

    @Shadow
    protected abstract void renderSnowAndRain(LightTexture pLightTexture, float pPartialTick, double pCamX, double pCamY, double pCamZ);

    @Shadow
    protected abstract void renderWorldBorder(Camera pCamera);

    @Shadow
    protected abstract void setupRender(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_);

    @Shadow
    protected abstract boolean shouldShowEntityOutlines();

    /**
     * @author MGSchultz
     * Avoid hotpath allocations.
     * This method is a mess, I have no idea of what it does and how it does that.
     */
    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Overwrite
    private void updateRenderChunks(LinkedHashSet<LevelRenderer.RenderChunkInfo> chunkInfos,
                                    LevelRenderer.RenderInfoMap infoMap,
                                    Vec3 viewVector,
                                    Queue<LevelRenderer.RenderChunkInfo> infoQueue,
                                    boolean shouldCull) {
        BlockPos chunkPos = new BlockPos(Mth.floor(viewVector.x / 16) * 16, Mth.floor(viewVector.y / 16) * 16, Mth.floor(viewVector.z / 16) * 16);
        BlockPos chunkCentre = chunkPos.offset(8, 8, 8);
        Entity.setViewScale(
                Mth.clamp(this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1, 2.5) * this.minecraft.options.entityDistanceScaling);
        //Added MutableBlockPos to prevent allocation of multiple BlockPos instances in the loops below
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        while (!infoQueue.isEmpty()) {
            LevelRenderer.RenderChunkInfo renderChunkInfo = infoQueue.poll();
            ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
            chunkInfos.add(renderChunkInfo);
            Direction nearestDir = Direction.getNearest(renderChunk.getOrigin().getX() - chunkPos.getX(),
                                                        renderChunk.getOrigin().getY() - chunkPos.getY(),
                                                        renderChunk.getOrigin().getZ() - chunkPos.getZ());
            boolean flag = Math.abs(renderChunk.getOrigin().getX() - chunkPos.getX()) > 60 ||
                           Math.abs(renderChunk.getOrigin().getY() - chunkPos.getY()) > 60 ||
                           Math.abs(renderChunk.getOrigin().getZ() - chunkPos.getZ()) > 60;
            for (Direction dir : DIRECTIONS) {
                ChunkRenderDispatcher.RenderChunk relativeFrom = this.getRelativeFrom(chunkPos, renderChunk, dir);
                if (relativeFrom == null) {
                    if (!this.closeToBorder(chunkPos, renderChunk)) {
                        this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                    }
                }
                else if (!shouldCull || !renderChunkInfo.hasDirection(dir.getOpposite())) {
                    if (shouldCull && renderChunkInfo.hasSourceDirections()) {
                        ChunkRenderDispatcher.CompiledChunk compiledChunk = renderChunk.getCompiledChunk();
                        boolean flag1 = false;
                        for (int j = 0; j < DIRECTIONS.length; ++j) {
                            if (renderChunkInfo.hasSourceDirection(j) && compiledChunk.facesCanSeeEachother(DIRECTIONS[j].getOpposite(), dir)) {
                                flag1 = true;
                                break;
                            }
                        }
                        if (!flag1) {
                            continue;
                        }
                    }
                    if (shouldCull && flag && renderChunkInfo.hasSourceDirections() && !renderChunkInfo.hasSourceDirection(nearestDir.ordinal())) {
                        ChunkRenderDispatcher.RenderChunk relativeFrom1 = this.getRelativeFrom(chunkPos, renderChunk, nearestDir.getOpposite());
                        if (relativeFrom1 == null) {
                            continue;
                        }
                        LevelRenderer.RenderChunkInfo renderChunkInfo1 = infoMap.get(relativeFrom1);
                        if (renderChunkInfo1 == null) {
                            continue;
                        }
                    }
                    if (shouldCull && flag) {
                        BlockPos blockpos2;
                        byte b0;
                        label140:
                        {
                            label139:
                            {
                                blockpos2 = relativeFrom.getOrigin();
                                if (dir.getAxis() == Direction.Axis.X) {
                                    if (chunkCentre.getX() > blockpos2.getX()) {
                                        break label139;
                                    }
                                }
                                else if (chunkCentre.getX() < blockpos2.getX()) {
                                    break label139;
                                }
                                b0 = 0;
                                break label140;
                            }
                            b0 = 16;
                        }
                        byte b1;
                        label132:
                        {
                            label131:
                            {
                                if (dir.getAxis() == Direction.Axis.Y) {
                                    if (chunkCentre.getY() > blockpos2.getY()) {
                                        break label131;
                                    }
                                }
                                else if (chunkCentre.getY() < blockpos2.getY()) {
                                    break label131;
                                }

                                b1 = 0;
                                break label132;
                            }

                            b1 = 16;
                        }
                        byte b2;
                        label124:
                        {
                            label123:
                            {
                                if (dir.getAxis() == Direction.Axis.Z) {
                                    if (chunkCentre.getZ() > blockpos2.getZ()) {
                                        break label123;
                                    }
                                }
                                else if (chunkCentre.getZ() < blockpos2.getZ()) {
                                    break label123;
                                }
                                b2 = 0;
                                break label124;
                            }
                            b2 = 16;
                        }
                        //blockPos3 = blockPos2.offset(b0, b1, b2)
                        int blockpos3x = blockpos2.getX() + b0;
                        int blockpos3y = blockpos2.getY() + b1;
                        int blockpos3z = blockpos2.getZ() + b2;
                        double vec31x = blockpos3x;
                        double vec31y = blockpos3y;
                        double vec31z = blockpos3z;
                        //vec3 = viewVector.subtract(vec31)
                        double vec3x = viewVector.x - vec31x;
                        double vec3y = viewVector.y - vec31y;
                        double vec3z = viewVector.z - vec31z;
                        //vec3 = vec3.normalize()
                        double length = VectorUtil.length(vec3x, vec3y, vec3z);
                        vec3x = VectorUtil.normalizeComponent(vec3x, length);
                        vec3y = VectorUtil.normalizeComponent(vec3y, length);
                        vec3z = VectorUtil.normalizeComponent(vec3z, length);
                        //vec3 = vec3.scale(CEILED_SECTION_DIAGONAL)
                        vec3x *= CEILED_SECTION_DIAGONAL;
                        vec3y *= CEILED_SECTION_DIAGONAL;
                        vec3z *= CEILED_SECTION_DIAGONAL;
                        boolean flag2 = true;
                        while (VectorUtil.subtractLengthSqr(viewVector, vec31x, vec31y, vec31z) > 3_600) {
                            vec31x += vec3x;
                            vec31y += vec3y;
                            vec31z += vec3z;
                            if (vec31y > this.level.getMaxBuildHeight() || vec31y < this.level.getMinBuildHeight()) {
                                break;
                            }
                            //Replace new BlockPos with MutableBlockPos since it is only used to store x, y, z values
                            ChunkRenderDispatcher.RenderChunk renderChunkAt = this.viewArea.getRenderChunkAt(mutablePos.set(vec31x, vec31y, vec31z));
                            if (renderChunkAt == null || infoMap.get(renderChunkAt) == null) {
                                flag2 = false;
                                break;
                            }
                        }
                        if (!flag2) {
                            continue;
                        }
                    }
                    LevelRenderer.RenderChunkInfo renderChunkInfo1 = infoMap.get(relativeFrom);
                    if (renderChunkInfo1 != null) {
                        renderChunkInfo1.addSourceDirection(dir);
                    }
                    else if (!relativeFrom.hasAllNeighbors()) {
                        if (!this.closeToBorder(chunkPos, renderChunk)) {
                            this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                        }
                    }
                    else {
                        //noinspection ObjectAllocationInLoop
                        LevelRenderer.RenderChunkInfo renderChunkInfo2 = new LevelRenderer.RenderChunkInfo(relativeFrom, dir,
                                                                                                           renderChunkInfo.step + 1);
                        renderChunkInfo2.setDirections(renderChunkInfo.directions, dir);
                        infoQueue.add(renderChunkInfo2);
                        infoMap.put(relativeFrom, renderChunkInfo2);
                    }
                }
            }
        }
    }
}