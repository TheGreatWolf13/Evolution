package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.renderer.DimensionOverworld;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClientLevel;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.O2OArrayMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;
import tgw.evolution.util.math.FastRandom;

import java.util.*;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

@Mixin(ClientLevel.class)
public abstract class Mixin_CFM_ClientLevel extends Level implements PatchClientLevel {

    @Unique private final RandomGenerator animRandom;
    @Mutable @Shadow @Final @RestoreFinal private ClientChunkCache chunkSource;
    @Mutable @Shadow @Final @RestoreFinal private ClientLevel.ClientLevelData clientLevelData;
    @Mutable @Shadow @Final @RestoreFinal private ClientPacketListener connection;
    @Mutable @Shadow @Final @RestoreFinal private DimensionSpecialEffects effects;
    @Mutable @Shadow @Final @RestoreFinal private TransientEntitySectionManager<Entity> entityStorage;
    @DeleteField @Shadow @Final private LevelRenderer levelRenderer;
    @Mutable @Shadow @Final @RestoreFinal private Deque<Runnable> lightUpdateQueue;
    @DeleteField @Shadow @Final private Map<String, MapItemSavedData> mapData;
    @Unique private final O2OMap<String, MapItemSavedData> mapData_;
    @Mutable @Shadow @Final @RestoreFinal private Minecraft minecraft;
    @Mutable @Shadow @Final @RestoreFinal List<AbstractClientPlayer> players;
    @Shadow private Scoreboard scoreboard;
    @Shadow private int serverSimulationDistance;
    @Mutable @Shadow @Final @RestoreFinal EntityTickList tickingEntities;
    @DeleteField @Shadow @Final private Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches;
    @Unique private final O2OArrayMap<ColorResolver, BlockTintCache> tintCaches_;

    @ModifyConstructor
    public Mixin_CFM_ClientLevel(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, int renderDistance, int simDistance, Supplier<ProfilerFiller> supplier, LevelRenderer levelRenderer, boolean bl, long l) {
        super(clientLevelData, resourceKey, holder, supplier, true, bl, l);
        this.animRandom = new FastRandom();
        this.tickingEntities = new EntityTickList();
        this.entityStorage = new TransientEntitySectionManager<>(Entity.class, ((ClientLevel) (Object) this).new EntityCallbacks());
        this.minecraft = Minecraft.getInstance();
        this.players = new OArrayList<>();
        this.scoreboard = new Scoreboard();
        this.mapData_ = new O2OHashMap<>();
        this.tintCaches_ = new O2OArrayMap<>(3);
        //noinspection DataFlowIssue
        BlockTintCache grassColor = new BlockTintCache(null);
        grassColor.setSource(this::calculateGrassColor);
        this.tintCaches_.put(BiomeColors.GRASS_COLOR_RESOLVER, grassColor);
        //noinspection DataFlowIssue
        BlockTintCache foliageColor = new BlockTintCache(null);
        foliageColor.setSource(this::calculateFoliageColor);
        this.tintCaches_.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, foliageColor);
        //noinspection DataFlowIssue
        BlockTintCache waterColor = new BlockTintCache(null);
        foliageColor.setSource(this::calculateWaterColor);
        this.tintCaches_.put(BiomeColors.WATER_COLOR_RESOLVER, waterColor);
        this.lightUpdateQueue = new ArrayDeque<>();
        this.connection = clientPacketListener;
        this.chunkSource = new ClientChunkCache((ClientLevel) (Object) this, renderDistance);
        this.clientLevelData = clientLevelData;
        this.effects = DimensionSpecialEffects.forType(holder.value());
        this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
        this.serverSimulationDistance = simDistance;
        this.updateSkyBrightness();
        this.prepareWeather();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addAlwaysVisibleParticle(ParticleOptions particleData, double x, double y, double z, double velX, double velY, double velZ) {
        this.minecraft.lvlRenderer().listener().addParticle(particleData, false, true, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addAlwaysVisibleParticle(ParticleOptions particleData, boolean ignoreRange, double x, double y, double z, double velX, double velY, double velZ) {
        this.minecraft.lvlRenderer().listener().addParticle(particleData, particleData.getType().getOverrideLimiter() || ignoreRange, true, x, y, z, velX, velY, velZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void addDestroyBlockEffect_(int x, int y, int z, BlockState state) {
        this.minecraft.particleEngine.destroy_(x, y, z, state);
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities
     */
    @Overwrite
    private void addEntity(int i, Entity entity) {
        this.removeEntity(i, Entity.RemovalReason.DISCARDED);
        this.entityStorage.addEntity(entity);
        entity.onAddedToWorld();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void addMapData(Map<String, MapItemSavedData> map) {
        this.mapData_.putAll(map);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double velX, double velY, double velZ) {
        this.minecraft.lvlRenderer().listener().addParticle(particleData, particleData.getType().getOverrideLimiter(), false, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addParticle(ParticleOptions particleData, boolean force, double x, double y, double z, double velX, double velY, double velZ) {
        this.minecraft.lvlRenderer().listener().addParticle(particleData, particleData.getType().getOverrideLimiter() || force, false, x, y, z, velX, velY, velZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void animateTick(int x, int y, int z) {
        Block block = this.getMarkerParticleTarget();
        for (int i = 0; i < 667; ++i) {
            this.doAnimateTick(x, y, z, 16, this.animRandom, block);
            this.doAnimateTick(x, y, z, 32, this.animRandom, block);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public int calculateBlockTint(BlockPos pos, ColorResolver colorResolver) {
        Evolution.deprecatedMethod();
        return this.calculateBlockTint_(pos.getX(), pos.getY(), pos.getZ(), colorResolver);
    }

    @Override
    public int calculateBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        int blend = Minecraft.getInstance().options.biomeBlendRadius;
        if (blend == 0) {
            return colorResolver.getColor(this.getBiome_(x, y, z).value(), x, z);
        }
        int r = 0;
        int g = 0;
        int b = 0;
        int total = 0;
        int x0 = x - blend;
        int x1 = x + blend;
        int z0 = z - blend;
        int z1 = z + blend;
        for (int dx = x0; dx <= x1; ++dx) {
            for (int dz = z0; dz <= z1; ++dz) {
                ++total;
                int color = colorResolver.getColor(this.getBiome_(dx, y, dz).value(), dx, dz);
                r += color >> 16 & 0xff;
                g += color >> 8 & 0xff;
                b += color & 0xff;
            }
        }
        return (r / total & 255) << 16 | (g / total & 255) << 8 | b / total & 255;
    }

    @Unique
    private int calculateFoliageColor(int x, int y, int z) {
        return this.calculateBlockTint_(x, y, z, BiomeColors.FOLIAGE_COLOR_RESOLVER);
    }

    @Unique
    private int calculateGrassColor(int x, int y, int z) {
        return this.calculateBlockTint_(x, y, z, BiomeColors.GRASS_COLOR_RESOLVER);
    }

    @Unique
    private int calculateWaterColor(int x, int y, int z) {
        return this.calculateBlockTint_(x, y, z, BiomeColors.WATER_COLOR_RESOLVER);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void clearTintCaches() {
        for (long it = this.tintCaches_.beginIteration(); this.tintCaches_.hasNextIteration(it); it = this.tintCaches_.nextEntry(it)) {
            this.tintCaches_.getIterationValue(it).invalidateAll();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void destroyBlockProgress(int breakerId, long pos, int progress, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        this.minecraft.lvlRenderer().destroyBlockProgress(breakerId, pos, progress, face, hitX, hitY, hitZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    @DeleteMethod
    public void doAnimateTick(int posX, int posY, int posZ, int range, Random random, @Nullable Block block, BlockPos.MutableBlockPos pos) {
        throw new AbstractMethodError();
    }

    @Unique
    private void doAnimateTick(int posX, int posY, int posZ, int range, RandomGenerator random, @Nullable Block particleBlock) {
        int x = posX + this.random.nextInt(range) - this.random.nextInt(range);
        int y = posY + this.random.nextInt(range) - this.random.nextInt(range);
        int z = posZ + this.random.nextInt(range) - this.random.nextInt(range);
        BlockState state = this.getBlockState_(x, y, z);
        Block block = state.getBlock();
        block.animateTick_(state, this, x, y, z, random);
        FluidState fluidState = this.getFluidState_(x, y, z);
        if (!fluidState.isEmpty()) {
            fluidState.animateTick_(this, x, y, z, random);
            ParticleOptions dripParticle = fluidState.getDripParticle();
            if (dripParticle != null && this.random.nextInt(10) == 0) {
                this.trySpawnDripParticles(x, y - 1, z, dripParticle, state.isFaceSturdy_(this, x, y, z, Direction.DOWN));
            }
        }
        if (particleBlock == block) {
            this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state), x + 0.5, y + 0.5, z + 0.5, 0, 0, 0);
        }
        if (!state.isCollisionShapeFullBlock_(this, x, y, z)) {
            Optional<AmbientParticleSettings> ambientParticle = this.getBiome_(x, y, z).value().getAmbientParticle();
            if (ambientParticle.isPresent()) {
                AmbientParticleSettings settings = ambientParticle.get();
                if (settings.canSpawn(this.random)) {
                    this.addParticle(settings.getOptions(), x + this.random.nextDouble(), y + this.random.nextDouble(), z + this.random.nextDouble(), 0, 0, 0);
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public Map<String, MapItemSavedData> getAllMapData() {
        return this.mapData_.view();
    }

    @Override
    public final @Nullable ChunkAccess getAnyChunkImmediately(int chunkX, int chunkZ) {
        return this.getChunkSource().getChunk(chunkX, chunkZ, false);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        Evolution.deprecatedMethod();
        return this.getBlockTint_(pos.getX(), pos.getY(), pos.getZ(), colorResolver);
    }

    @Override
    public int getBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        //noinspection DataFlowIssue
        return this.tintCaches_.get(colorResolver).getColor_(x, y, z);
    }

    @Override
    public final @Nullable LevelChunk getChunkAtImmediately(int chunkX, int chunkZ) {
        return this.getChunkSource().getChunk(chunkX, chunkZ, false);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public @Nullable MapItemSavedData getMapData(String name) {
        return this.mapData_.get(name);
    }

    @Shadow
    protected abstract @Nullable Block getMarkerParticleTarget();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BlockPos getSharedSpawnPos() {
        int x = this.levelData.getXSpawn();
        int y = this.levelData.getYSpawn();
        int z = this.levelData.getZSpawn();
        WorldBorder worldBorder = this.getWorldBorder();
        if (!worldBorder.isWithinBounds_(x, z)) {
            x = Mth.floor(worldBorder.getCenterX());
            z = Mth.floor(worldBorder.getCenterZ());
            y = this.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        }
        return new BlockPos(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Use Evolution dimension
     */
    @Overwrite
    public float getStarBrightness(float partialTicks) {
        DimensionOverworld dimension = EvolutionClient.getDimension();
        if (dimension != null) {
            return dimension.getSkyBrightness(partialTicks);
        }
        float timeOfDay = this.getTimeOfDay(partialTicks);
        float f = 1.0F - (Mth.cos(timeOfDay * Mth.TWO_PI) * 2.0F + 0.25F);
        f = Mth.clamp(f, 0, 1);
        return f * f * 0.5F;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void globalLevelEvent(int id, BlockPos pos, int data) {
        throw new AbstractMethodError();
    }

    @Override
    public void globalLevelEvent_(@LvlEvent int event, int x, int y, int z, int data) {
        this.minecraft.lvlRenderer().globalLevelEvent(event, x, y, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    public void levelEvent(@Nullable Player player, @LvlEvent int event, BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        this.levelEvent_(player, event, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    @Override
    public void levelEvent_(@Nullable Player player, int event, int x, int y, int z, int data) {
        try {
            this.minecraft.lvlRenderer().levelEvent(event, x, y, z, data);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Playing level event");
            CrashReportCategory category = crash.addCategory("Level event being played");
            category.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, x, y, z));
            //noinspection ConstantConditions
            category.setDetail("Event source", player);
            category.setDetail("Event type", event);
            category.setDetail("Event data", data);
            throw new ReportedException(crash);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void onChunkLoaded(ChunkPos pos) {
        Evolution.deprecatedMethod();
        this.onChunkLoaded(pos.x, pos.z);
    }

    @Override
    public void onChunkLoaded(int chunkX, int chunkZ) {
        for (long it = this.tintCaches_.beginIteration(); this.tintCaches_.hasNextIteration(it); it = this.tintCaches_.nextEntry(it)) {
            this.tintCaches_.getIterationValue(it).invalidateForChunk(chunkX, chunkZ);
        }
        this.entityStorage.startTicking(chunkX, chunkZ);
        this.minecraft.lvlRenderer().onChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public void onSectionBecomingNonEmpty(long secPos) {
        this.minecraft.lvlRenderer().onSectionBecomingNonEmpty(secPos);
    }

    @Shadow
    public abstract void removeEntity(int i, Entity.RemovalReason removalReason);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        Evolution.deprecatedMethod();
        this.sendBlockUpdated_(pos.getX(), pos.getY(), pos.getZ(), oldState, newState, flags);
    }

    @Override
    public void sendBlockUpdated_(int x, int y, int z, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        this.minecraft.lvlRenderer().blockChanged(x, y, z, oldState, newState, flags);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void setBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState) {
        throw new AbstractMethodError();
    }

    @Override
    public void setBlocksDirty_(int x, int y, int z, BlockState oldState, BlockState newState) {
        this.minecraft.lvlRenderer().setBlockDirty(x, y, z, oldState, newState);
    }

    @Shadow
    public abstract void setDefaultSpawnPos(BlockPos blockPos, float f);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void setKnownState(BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.setKnownState_(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void setKnownState_(int x, int y, int z, BlockState state) {
        this.setBlock_(x, y, z, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.UPDATE_NEIGHBORS);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void setMapData(String name, MapItemSavedData data) {
        this.mapData_.put(name, data);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ) {
        this.minecraft.lvlRenderer().setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
    }

    @Shadow
    protected abstract void spawnFluidParticle(double d, double e, double f, double g, double h, ParticleOptions particleOptions);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void spawnParticle(BlockPos blockPos, ParticleOptions particleOptions, VoxelShape voxelShape, double d) {
        throw new AbstractMethodError();
    }

    @Unique
    private void spawnParticle(int x, double y, int z, ParticleOptions particle, VoxelShape shape) {
        this.spawnFluidParticle(x + shape.min(Direction.Axis.X), x + shape.max(Direction.Axis.X), z + shape.min(Direction.Axis.Z), z + shape.max(Direction.Axis.Z), y, particle);
    }

    @Unique
    private void trySpawnDripParticles(int x, int y, int z, ParticleOptions particle, boolean downFaceRigid) {
        if (this.getFluidState_(x, y, z).isEmpty()) {
            BlockState state = this.getBlockState_(x, y, z);
            VoxelShape shape = state.getCollisionShape_(this, x, y, z);
            if (shape.max(Direction.Axis.Y) < 1) {
                if (downFaceRigid) {
                    this.spawnFluidParticle(x, x + 1, z, z + 1, y + 0.95, particle);
                }
            }
            else if (!state.is(BlockTags.IMPERMEABLE)) {
                double minY = shape.min(Direction.Axis.Y);
                if (minY > 0) {
                    this.spawnParticle(x, y + minY - 0.05, z, particle, shape);
                }
                else {
                    BlockState stateBelow = this.getBlockState_(x, y - 1, z);
                    VoxelShape shapeBelow = stateBelow.getCollisionShape_(this, x, y - 1, z);
                    if (shapeBelow.max(Direction.Axis.Y) < 1 && this.getFluidState_(x, y - 1, z).isEmpty()) {
                        this.spawnParticle(x, y - 0.05, z, particle, shape);
                    }
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void trySpawnDripParticles(BlockPos blockPos, BlockState blockState, ParticleOptions particleOptions, boolean bl) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void unload(LevelChunk chunk) {
        chunk.clearAllBlockEntities();
        this.entityStorage.stopTicking(chunk.getPos());
    }
}
