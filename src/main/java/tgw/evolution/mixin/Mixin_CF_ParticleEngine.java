package tgw.evolution.mixin;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchParticleEngine;
import tgw.evolution.patches.PatchTerrainParticle;
import tgw.evolution.util.collection.OArrayLimitedQueue;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Mixin(ParticleEngine.class)
public abstract class Mixin_CF_ParticleEngine implements PreparableReloadListener, PatchParticleEngine {

    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
    @Unique private final OList<Particle> particlesToAdd_;
    @Unique private final R2OMap<ParticleRenderType, OArrayLimitedQueue<Particle>> particles_;
    @Unique private final I2OMap<ParticleProvider<?>> providers_;
    @Unique private final O2OMap<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets_;
    @Unique private final O2IMap<ParticleGroup> trackedParticleCounts_;
    @Unique private final OList<TrackingEmitter> trackingEmitters_;
    @Shadow protected @Nullable ClientLevel level;
    @Shadow @Final @DeleteField private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final @DeleteField private Queue<Particle> particlesToAdd;
    @Shadow @Final @DeleteField private Int2ObjectMap<ParticleProvider<?>> providers;
    @Mutable @Shadow @Final @RestoreFinal private Random random;
    @Unique private int renderedParticles;
    @Shadow @Final @DeleteField private Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets;
    @Mutable @Shadow @Final @RestoreFinal private TextureAtlas textureAtlas;
    @Mutable @Shadow @Final @RestoreFinal private TextureManager textureManager;
    @Shadow @Final @DeleteField private Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts;
    @Shadow @Final @DeleteField private Queue<TrackingEmitter> trackingEmitters;

    @ModifyConstructor
    public Mixin_CF_ParticleEngine(ClientLevel level, TextureManager textureManager) {
        this.particles_ = new R2OHashMap<>();
        this.trackingEmitters_ = new OArrayList<>();
        this.providers_ = new I2OHashMap();
        this.particlesToAdd_ = new OArrayList<>();
        this.spriteSets_ = new O2OHashMap<>();
        this.trackedParticleCounts_ = new O2IHashMap<>();
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        textureManager.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = level;
        this.textureManager = textureManager;
        this.registerProviders();
        this.random = new Random();
    }

    @Overwrite
    public void add(Particle particle) {
        Optional<ParticleGroup> optional = particle.getParticleGroup();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd_.add(particle);
                this.updateCount(optional.get(), 1);
            }
        }
        else {
            this.particlesToAdd_.add(particle);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Use proper loop
     */
    @Overwrite
    public String countParticles() {
        int count = 0;
        R2OMap<ParticleRenderType, OArrayLimitedQueue<Particle>> particles = this.particles_;
        for (R2OMap.Entry<ParticleRenderType, OArrayLimitedQueue<Particle>> e = particles.fastEntries(); e != null; e = particles.fastEntries()) {
            count += e.value().size();
        }
        return String.valueOf(count);
    }

    @Overwrite
    public void crack(BlockPos pos, Direction face) {
        Evolution.deprecatedMethod();
        this.crack_(pos.getX(), pos.getY(), pos.getZ(), face);
    }

    @Override
    public void crack_(int x, int y, int z, Direction face) {
        assert this.level != null;
        BlockState state = this.level.getBlockState_(x, y, z);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            VoxelShape shape = state.getShape_(this.level, x, y, z);
            double minX = shape.min(Direction.Axis.X);
            double maxX = shape.max(Direction.Axis.X);
            double minY = shape.min(Direction.Axis.Y);
            double maxY = shape.max(Direction.Axis.Y);
            double minZ = shape.min(Direction.Axis.Z);
            double maxZ = shape.max(Direction.Axis.Z);
            double px = x + this.random.nextDouble() * (maxX - minX - 0.2) + 0.1 + minX;
            double py = y + this.random.nextDouble() * (maxY - minY - 0.2) + 0.1 + minY;
            double pz = z + this.random.nextDouble() * (maxZ - minZ - 0.2) + 0.1 + minZ;
            switch (face) {
                case DOWN -> py = y + minY - 0.1;
                case UP -> py = y + maxY + 0.1;
                case NORTH -> pz = z + minZ - 0.1;
                case SOUTH -> pz = z + maxZ + 0.1;
                case WEST -> px = x + minX - 0.1;
                case EAST -> px = x + maxX + 0.1;
            }
            this.add(PatchTerrainParticle.create(this.level, px, py, pz, 0, 0, 0, state, x, y, z).setPower(0.2F).scale(0.6F));
        }
    }

    @Overwrite
    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions) {
        assert this.level != null;
        this.trackingEmitters_.add(new TrackingEmitter(this.level, entity, particleOptions));
    }

    @Overwrite
    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions, int i) {
        assert this.level != null;
        this.trackingEmitters_.add(new TrackingEmitter(this.level, entity, particleOptions, i));
    }

    @Overwrite
    public void destroy(BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.destroy_(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void destroy_(int x, int y, int z, BlockState state) {
        assert this.level != null;
        if (!state.isAir()) {
            VoxelShape shape = state.getShape_(this.level, x, y, z);
            OList<AABB> aabbs = shape.cachedBoxes();
            for (int b = 0, len = aabbs.size(); b < len; ++b) {
                AABB bb = aabbs.get(b);
                double x0 = bb.minX;
                double x1 = bb.maxX;
                double y0 = bb.minY;
                double y1 = bb.maxY;
                double z0 = bb.minZ;
                double z1 = bb.maxZ;
                double sx = Math.min(1, x1 - x0);
                double sy = Math.min(1, y1 - y0);
                double sz = Math.min(1, z1 - z0);
                int dx = Math.max(2, Mth.ceil(sx / 0.25));
                int dy = Math.max(2, Mth.ceil(sy / 0.25));
                int dz = Math.max(2, Mth.ceil(sz / 0.25));
                for (int i = 0; i < dx; ++i) {
                    for (int j = 0; j < dy; ++j) {
                        for (int k = 0; k < dz; ++k) {
                            double vx = (i + 0.5) / dx;
                            double vy = (j + 0.5) / dy;
                            double vz = (k + 0.5) / dz;
                            //noinspection ObjectAllocationInLoop
                            this.add(PatchTerrainParticle.create(this.level, x + vx * sx + x0, y + vy * sy + y0, z + vz * sz + z0,
                                                                 vx - 0.5, vy - 0.5, vz - 0.5, state, x, y, z));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getRenderedParticles() {
        return this.renderedParticles;
    }

    @Overwrite
    private boolean hasSpaceInParticleLimit(ParticleGroup group) {
        return this.trackedParticleCounts_.getInt(group) < group.getLimit();
    }

    @Overwrite
    private void loadParticleDescription(ResourceManager manager, ResourceLocation resLoc, Map<ResourceLocation, List<ResourceLocation>> map) {
        ResourceLocation jsonLoc = new ResourceLocation(resLoc.getNamespace(), "particles/" + resLoc.getPath() + ".json");
        try {
            Resource resource = manager.getResource(jsonLoc);
            try {
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                try {
                    ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
                    List<ResourceLocation> list = particleDescription.getTextures();
                    boolean bl = this.spriteSets_.containsKey(resLoc);
                    if (list == null) {
                        if (bl) {
                            throw new IllegalStateException("Missing texture list for particle " + resLoc);
                        }
                    }
                    else {
                        if (!bl) {
                            throw new IllegalStateException("Redundant texture list for particle " + resLoc);
                        }
                        OList<ResourceLocation> descList = new OArrayList<>(list.size());
                        for (int i = 0, len = list.size(); i < len; ++i) {
                            ResourceLocation rl = list.get(i);
                            //noinspection ObjectAllocationInLoop
                            descList.add(new ResourceLocation(rl.getNamespace(), "particle/" + rl.getPath()));
                        }
                        map.put(resLoc, descList);
                    }
                }
                catch (Throwable var12) {
                    try {
                        reader.close();
                    }
                    catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                    throw var12;
                }
                reader.close();
            }
            catch (Throwable var13) {
                if (resource != null) {
                    try {
                        resource.close();
                    }
                    catch (Throwable var10) {
                        var13.addSuppressed(var10);
                    }
                }
                throw var13;
            }
            resource.close();
        }
        catch (IOException var14) {
            throw new IllegalStateException("Failed to load description for particle " + resLoc, var14);
        }
    }

    @Overwrite
    private @Nullable <T extends ParticleOptions> Particle makeParticle(T particleOptions,
                                                                        double d,
                                                                        double e,
                                                                        double f,
                                                                        double g,
                                                                        double h,
                                                                        double i) {
        ParticleProvider<T> provider = (ParticleProvider<T>) this.providers_.get(Registry.PARTICLE_TYPE.getId(particleOptions.getType()));
        if (provider == null) {
            return null;
        }
        assert this.level != null;
        return provider.createParticle(particleOptions, this.level, d, e, f, g, h, i);
    }

    @Overwrite
    private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider<T> provider) {
        this.providers_.put(Registry.PARTICLE_TYPE.getId(particleType), provider);
    }

    @Overwrite
    private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> registration) {
        ParticleEngine.MutableSpriteSet mutableSpriteSet = new ParticleEngine.MutableSpriteSet();
        this.spriteSets_.put(Registry.PARTICLE_TYPE.getKey(particleType), mutableSpriteSet);
        this.providers_.put(Registry.PARTICLE_TYPE.getId(particleType), registration.create(mutableSpriteSet));
    }

    @Shadow
    protected abstract void registerProviders();

    @Override
    @Overwrite
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier,
                                          ResourceManager manager,
                                          ProfilerFiller prof1,
                                          ProfilerFiller prof2,
                                          Executor exec1,
                                          Executor exec2) {
        Map<ResourceLocation, List<ResourceLocation>> map = Maps.newConcurrentMap();
        CompletableFuture<?>[] futures = new CompletableFuture[Registry.PARTICLE_TYPE.size()];
        int i = 0;
        for (ResourceLocation resLoc : Registry.PARTICLE_TYPE.keySet()) {
            //noinspection ObjectAllocationInLoop
            futures[i++] = CompletableFuture.runAsync(() -> this.loadParticleDescription(manager, resLoc, map));
        }
        CompletableFuture<TextureAtlas.Preparations> stitching = CompletableFuture.allOf(futures).thenApplyAsync(v -> {
            prof1.startTick();
            prof1.push("stitching");
            TextureAtlas.Preparations preparations = this.textureAtlas.prepareToStitch(manager,
                                                                                       map.values().stream().flatMap(Collection::stream),
                                                                                       prof1, 0);
            prof1.pop();
            prof1.endTick();
            return preparations;
        }, exec1);
        Objects.requireNonNull(barrier);
        return stitching.thenCompose(barrier::wait).thenAcceptAsync(preparations -> {
            this.particles_.clear();
            prof2.startTick();
            prof2.push("upload");
            this.textureAtlas.reload(preparations);
            prof2.popPush("bindSpriteSets");
            TextureAtlasSprite sprite = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
            map.forEach((resourceLocation, list) -> {
                ImmutableList iList;
                if (list.isEmpty()) {
                    iList = ImmutableList.of(sprite);
                }
                else {
                    Stream<ResourceLocation> stream = list.stream();
                    TextureAtlas atlas = this.textureAtlas;
                    iList = stream.map(atlas::getSprite).collect(ImmutableList.toImmutableList());
                }
                ImmutableList<TextureAtlasSprite> immutableList = iList;
                this.spriteSets_.get(resourceLocation).rebind(immutableList);
            });
            prof2.pop();
            prof2.endTick();
        }, exec2);
    }

    /**
     * @author TheGreatWolf
     * @reason Add occlusion culling
     */
    @Overwrite
    public void render(PoseStack matrices, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera camera, float partialTicks) {
        lightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        EvLevelRenderer levelRenderer = Minecraft.getInstance().lvlRenderer();
        this.renderedParticles = 0;
        for (int i = 0, len = RENDER_ORDER.size(); i < len; i++) {
            ParticleRenderType type = RENDER_ORDER.get(i);
            OArrayLimitedQueue<Particle> queue = this.particles_.get(type);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            boolean began = false;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            OList<Particle> list = queue.internalList();
            for (int j = 0, len2 = list.size(); j < len2; ++j) {
                Particle particle = list.get(j);
                AABB bb = particle.getBoundingBox();
                if (!levelRenderer.visibleFrustumCulling(bb)) {
                    continue;
                }
                double cx = (bb.minX + bb.maxX) * 0.5;
                double cy = (bb.minY + bb.maxY) * 0.5;
                double cz = (bb.minZ + bb.maxZ) * 0.5;
                if (!levelRenderer.visibleOcclusionCulling(cx, cy, cz)) {
                    continue;
                }
                //noinspection ConstantConditions
                if (!began) {
                    began = true;
                    AccessorRenderSystem.setShader(GameRenderer.getParticleShader());
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    type.begin(builder, this.textureManager);
                }
                try {
                    ++this.renderedParticles;
                    particle.render(builder, camera, partialTicks);
                }
                catch (Throwable t) {
                    CrashReport crash = CrashReport.forThrowable(t, "Rendering Particle");
                    CrashReportCategory category = crash.addCategory("Particle being rendered");
                    //noinspection ObjectAllocationInLoop
                    category.setDetail("Particle", particle::toString);
                    //noinspection ObjectAllocationInLoop
                    category.setDetail("Particle Type", type::toString);
                    throw new ReportedException(crash);
                }
            }
            //noinspection ConstantConditions
            if (began) {
                type.end(tesselator);
            }
        }
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }

    @Overwrite
    public void setLevel(@Nullable ClientLevel level) {
        this.level = level;
        this.particles_.clear();
        this.trackingEmitters_.clear();
        this.trackedParticleCounts_.clear();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations.
     */
    @Overwrite
    public void tick() {
        assert this.level != null;
        R2OMap<ParticleRenderType, OArrayLimitedQueue<Particle>> particles = this.particles_;
        for (R2OMap.Entry<ParticleRenderType, OArrayLimitedQueue<Particle>> e = particles.fastEntries(); e != null; e = particles.fastEntries()) {
            this.level.getProfiler().push(e.key().toString());
            this.tickParticleList(e.value());
            this.level.getProfiler().pop();
        }
        OList<TrackingEmitter> trackingEmitters = this.trackingEmitters_;
        if (!trackingEmitters.isEmpty()) {
            for (int i = 0; i < trackingEmitters.size(); ++i) {
                TrackingEmitter emitter = trackingEmitters.get(i);
                emitter.tick();
                if (!emitter.isAlive()) {
                    trackingEmitters.remove(i--);
                }
            }
        }
        OList<Particle> particlesToAdd = this.particlesToAdd_;
        if (!particlesToAdd.isEmpty()) {
            for (int i = 0, len = particlesToAdd.size(); i < len; ++i) {
                Particle particle = particlesToAdd.get(i);
                OArrayLimitedQueue<Particle> queue = particles.get(particle.getRenderType());
                if (queue == null) {
                    queue = new OArrayLimitedQueue<>(16_384);
                    particles.put(particle.getRenderType(), queue);
                }
                queue.add(particle);
            }
            particlesToAdd.clear();
        }
    }

    @Shadow
    protected abstract void tickParticle(Particle pParticle);

    @Unique
    private void tickParticleList(OArrayLimitedQueue<Particle> particles) {
        if (!particles.isEmpty()) {
            OList<Particle> list = particles.internalList();
            for (int i = 0; i < list.size(); ++i) {
                Particle particle = list.get(i);
                this.tickParticle(particle);
                if (!particle.isAlive()) {
                    Optional<ParticleGroup> particleGroup = particle.getParticleGroup();
                    if (particleGroup.isPresent()) {
                        this.updateCount(particleGroup.get(), -1);
                    }
                    list.remove(i--);
                }
            }
        }
    }

    @Overwrite
    @DeleteMethod
    private void tickParticleList(Collection<Particle> particles) {
        throw new AbstractMethodError();
    }

    @Overwrite
    private void updateCount(ParticleGroup group, int i) {
        this.trackedParticleCounts_.addTo(group, i);
    }
}
