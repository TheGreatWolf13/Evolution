package tgw.evolution.patches.replace.net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
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
import tgw.evolution.client.renderer.ambient.LightTexture;
import tgw.evolution.client.renderer.chunk.LevelRenderer;
import tgw.evolution.mixin.AccessorRenderSystem;
import tgw.evolution.patches.PatchTerrainParticle;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.queues.OArrayLimitedQueue;
import tgw.evolution.util.collection.queues.OQueue;
import tgw.evolution.util.math.FastRandom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.random.RandomGenerator;

@Environment(EnvType.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
    private static final int MAX_PARTICLES_PER_LAYER = 16_384;
    private static final OList<ParticleRenderType> RENDER_ORDER = OList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
    protected @Nullable ClientLevel level;
    private final R2OMap<ParticleRenderType, OQueue<Particle>> particles = new R2OHashMap<>();
    private final OList<Particle> particlesToAdd = new OArrayList<>();
    private final I2OMap<ParticleProvider<?>> providers = new I2OHashMap<>();
    private final RandomGenerator random = new FastRandom();
    private int renderedParticles;
    private final O2OMap<ResourceLocation, MutableSpriteSet> spriteSets = new O2OHashMap<>();
    private final TextureAtlas textureAtlas;
    private final TextureManager textureManager;
    private final O2IMap<ParticleGroup> trackedParticleCounts = new O2IHashMap<>();
    private final OList<TrackingEmitter> trackingEmitters = new OArrayList<>();

    public ParticleEngine(ClientLevel level, TextureManager textureManager) {
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        textureManager.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = level;
        this.textureManager = textureManager;
        this.registerProviders();
    }

    private static void tickParticle(Particle particle) {
        try {
            particle.tick();
        }
        catch (Throwable throwable) {
            CrashReport report = CrashReport.forThrowable(throwable, "Ticking Particle");
            CrashReportCategory category = report.addCategory("Particle being ticked");
            category.setDetail("Particle", particle::toString);
            category.setDetail("Particle Type", particle.getRenderType()::toString);
            throw new ReportedException(report);
        }
    }

    public void add(Particle particle) {
        Optional<ParticleGroup> optional = particle.getParticleGroup();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd.add(particle);
                this.updateCount(optional.get(), 1);
            }
        }
        else {
            this.particlesToAdd.add(particle);
        }
    }

    public void close() {
        this.textureAtlas.clearTextureData();
    }

    public String countParticles() {
        int count = 0;
        R2OMap<ParticleRenderType, OQueue<Particle>> particles = this.particles;
        for (long it = particles.beginIteration(); particles.hasNextIteration(it); it = particles.nextEntry(it)) {
            count += particles.getIterationValue(it).size();
        }
        return String.valueOf(count);
    }

    public void crack(int x, int y, int z, Direction face, double hitX, double hitY, double hitZ) {
        assert this.level != null;
        BlockState stateAtPos = this.level.getBlockState_(x, y, z).getDestroyingState(this.level, x, y, z, face, hitX, hitY, hitZ);
        BlockState stateForParticles = stateAtPos.stateForParticles(this.level, x, y, z);
        if (stateForParticles.getRenderShape() != RenderShape.INVISIBLE) {
            VoxelShape shape = stateAtPos.getShape_(this.level, x, y, z);
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
            this.add(PatchTerrainParticle.create(this.level, px, py, pz, 0, 0, 0, stateForParticles, x, y, z).setPower(0.2F).scale(0.6F));
        }
    }

    public @Nullable Particle createParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
        Particle particle = this.makeParticle(particleOptions, d, e, f, g, h, i);
        if (particle != null) {
            this.add(particle);
            return particle;
        }
        return null;
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions) {
        assert this.level != null;
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions));
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions, int i) {
        assert this.level != null;
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions, i));
    }

    public void destroy(int x, int y, int z, BlockState state) {
        assert this.level != null;
        if (state.isAir()) {
            return;
        }
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
                        this.add(PatchTerrainParticle.create(this.level, x + vx * sx + x0, y + vy * sy + y0, z + vz * sz + z0, vx - 0.5, vy - 0.5, vz - 0.5, state, x, y, z));
                    }
                }
            }
        }
    }

    public int getRenderedParticles() {
        return this.renderedParticles;
    }

    private boolean hasSpaceInParticleLimit(ParticleGroup group) {
        return this.trackedParticleCounts.getInt(group) < group.getLimit();
    }

    private void loadParticleDescription(ResourceManager manager, ResourceLocation resLoc, Map<ResourceLocation, List<ResourceLocation>> map) {
        ResourceLocation jsonLoc = new ResourceLocation(resLoc.getNamespace(), "particles/" + resLoc.getPath() + ".json");
        try {
            Resource resource = manager.getResource(jsonLoc);
            try {
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                try {
                    ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
                    List<ResourceLocation> list = particleDescription.getTextures();
                    boolean bl = this.spriteSets.containsKey(resLoc);
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
                catch (Throwable e) {
                    try {
                        reader.close();
                    }
                    catch (Throwable t) {
                        e.addSuppressed(t);
                    }
                    throw e;
                }
                reader.close();
            }
            catch (Throwable e) {
                //noinspection ConstantValue
                if (resource != null) {
                    try {
                        resource.close();
                    }
                    catch (Throwable t) {
                        e.addSuppressed(t);
                    }
                }
                throw e;
            }
            resource.close();
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to load description for particle " + resLoc, e);
        }
    }

    private @Nullable <T extends ParticleOptions> Particle makeParticle(T options, double d, double e, double f, double g, double h, double i) {
        ParticleProvider<T> provider = (ParticleProvider<T>) this.providers.get(Registry.PARTICLE_TYPE.getId(options.getType()));
        if (provider == null) {
            return null;
        }
        assert this.level != null;
        return provider.createParticle(options, this.level, d, e, f, g, h, i);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
        this.providers.put(Registry.PARTICLE_TYPE.getId(type), provider);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
        MutableSpriteSet mutableSpriteSet = new MutableSpriteSet();
        this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(type), mutableSpriteSet);
        this.providers.put(Registry.PARTICLE_TYPE.getId(type), registration.create(mutableSpriteSet));
    }

    private void registerProviders() {
        this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
        this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
        this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
        this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
        this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
        this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
        this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
        this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
        this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
        this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
        this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
        this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
        this.register(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
        this.register(ParticleTypes.DUST, DustParticle.Provider::new);
        this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
        this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
        this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
        this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
        this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
        this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
        this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
        this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
        this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
        this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
        this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
        this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
        this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
        this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
        this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
        this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
        this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
        this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
        this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
        this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
        this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
        this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
        this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
        this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
        this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
        this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle.SporeBlossomFallProvider::new);
        this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.ObsidianTearHangProvider::new);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.ObsidianTearFallProvider::new);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.ObsidianTearLandProvider::new);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle.DripstoneWaterHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle.DripstoneWaterFallProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaFallProvider::new);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
        this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
        this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
        this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
        this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prof1, ProfilerFiller prof2, Executor exec1, Executor exec2) {
        Map<ResourceLocation, List<ResourceLocation>> map = Maps.newConcurrentMap();
        CompletableFuture<?>[] futures = new CompletableFuture[Registry.PARTICLE_TYPE.size()];
        int i = 0;
        for (long it = Registry.PARTICLE_TYPE.beginIteration(); Registry.PARTICLE_TYPE.hasNextIteration(it); it = Registry.PARTICLE_TYPE.nextEntry(it)) {
            ResourceLocation resLoc = Registry.PARTICLE_TYPE.getIterationLocation(it);
            //noinspection ObjectAllocationInLoop
            futures[i++] = CompletableFuture.runAsync(() -> this.loadParticleDescription(manager, resLoc, map));
        }
        CompletableFuture<TextureAtlas.Preparations> stitching = CompletableFuture.allOf(futures).thenApplyAsync(v -> {
            prof1.startTick();
            prof1.push("stitching");
            TextureAtlas.Preparations preparations = this.textureAtlas.prepareToStitch(manager, map.values().stream().flatMap(Collection::stream), prof1, 0);
            prof1.pop();
            prof1.endTick();
            return preparations;
        }, exec1);
        return stitching.thenCompose(barrier::wait).thenAcceptAsync(preparations -> {
            this.particles.clear();
            prof2.startTick();
            prof2.push("upload");
            this.textureAtlas.reload(preparations);
            prof2.popPush("bindSpriteSets");
            TextureAtlasSprite sprite = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
            map.forEach((resourceLocation, list) -> {
                OList<TextureAtlasSprite> iList;
                if (list.isEmpty()) {
                    iList = OList.of(sprite);
                }
                else {
                    int len = list.size();
                    iList = new OArrayList<>(len);
                    for (int j = 0; j < len; ++j) {
                        iList.add(this.textureAtlas.getSprite(list.get(j)));
                    }
                    iList = iList.view();
                }
                this.spriteSets.get(resourceLocation).rebind(iList);
            });
            prof2.pop();
            prof2.endTick();
        }, exec2);
    }

    public void render(PoseStack matrices, LightTexture lightTexture, Camera camera, float partialTicks) {
        lightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer();
        this.renderedParticles = 0;
        for (int i = 0, len = RENDER_ORDER.size(); i < len; i++) {
            ParticleRenderType type = RENDER_ORDER.get(i);
            OQueue<Particle> queue = this.particles.get(type);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            boolean began = false;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            for (long it = queue.beginIteration(); queue.hasNextIteration(it); it = queue.nextEntry(it)) {
                Particle particle = queue.getIteration(it);
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

    public void setLevel(@Nullable ClientLevel level) {
        this.level = level;
        this.particles.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    public void tick() {
        assert this.level != null;
        R2OMap<ParticleRenderType, OQueue<Particle>> particles = this.particles;
        for (long it = particles.beginIteration(); particles.hasNextIteration(it); it = particles.nextEntry(it)) {
            //noinspection DataFlowIssue
            this.level.getProfiler().push(particles.getIterationKey(it).toString());
            this.tickParticleList(particles.getIterationValue(it));
            this.level.getProfiler().pop();
        }
        OList<TrackingEmitter> trackingEmitters = this.trackingEmitters;
        if (!trackingEmitters.isEmpty()) {
            for (int i = 0; i < trackingEmitters.size(); ++i) {
                TrackingEmitter emitter = trackingEmitters.get(i);
                emitter.tick();
                if (!emitter.isAlive()) {
                    trackingEmitters.remove(i--);
                }
            }
        }
        OList<Particle> particlesToAdd = this.particlesToAdd;
        if (!particlesToAdd.isEmpty()) {
            for (int i = 0, len = particlesToAdd.size(); i < len; ++i) {
                Particle particle = particlesToAdd.get(i);
                OQueue<Particle> queue = particles.get(particle.getRenderType());
                if (queue == null) {
                    queue = new OArrayLimitedQueue<>(MAX_PARTICLES_PER_LAYER);
                    particles.put(particle.getRenderType(), queue);
                }
                queue.enqueue(particle);
            }
            particlesToAdd.clear();
        }
    }

    private void tickParticleList(OQueue<Particle> particles) {
        for (long it = particles.beginIteration(); particles.hasNextIteration(it); it = particles.nextEntry(it)) {
            Particle particle = particles.getIteration(it);
            tickParticle(particle);
            if (!particle.isAlive()) {
                Optional<ParticleGroup> particleGroup = particle.getParticleGroup();
                if (particleGroup.isPresent()) {
                    this.updateCount(particleGroup.get(), -1);
                }
                it = particles.removeIteration(it);
            }
        }
    }

    private void updateCount(ParticleGroup group, int i) {
        this.trackedParticleCounts.put(group, this.trackedParticleCounts.getInt(group) + i);
    }

    @Environment(value = EnvType.CLIENT)
    private static class MutableSpriteSet implements SpriteSet {

        private OList<TextureAtlasSprite> sprites;

        @Override
        public TextureAtlasSprite get(int i, int j) {
            return this.sprites.get(i * (this.sprites.size() - 1) / j);
        }

        @Override
        public TextureAtlasSprite get(Random random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }

        public void rebind(OList<TextureAtlasSprite> sprites) {
            this.sprites = sprites;
        }
    }

    @FunctionalInterface
    @Environment(value = EnvType.CLIENT)
    private interface SpriteParticleRegistration<T extends ParticleOptions> {
        ParticleProvider<T> create(SpriteSet var1);
    }
}
