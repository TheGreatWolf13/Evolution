package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.IPacket;
import tgw.evolution.network.PacketSCBlockDestruction;
import tgw.evolution.patches.ILevelPatch;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ILevelPatch {

    private final BlockPos.MutableBlockPos randomPosInChunkCachedPos = new BlockPos.MutableBlockPos();
    @Shadow @Final private MinecraftServer server;

    public ServerLevelMixin(WritableLevelData pLevelData,
                            ResourceKey<Level> pDimension,
                            Holder<DimensionType> pDimensionTypeRegistration,
                            Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    @Override
    public void destroyBlockProgress(int breakerId, long pos, int progress) {
        IPacket packet = null;
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            if (player != null && player.level == this && player.getId() != breakerId) {
                double dx = BlockPos.getX(pos) - player.getX();
                double dy = BlockPos.getY(pos) - player.getY();
                double dz = BlockPos.getZ(pos) - player.getZ();
                if (dx * dx + dy * dy + dz * dz < 1_024.0) {
                    if (packet == null) {
                        packet = new PacketSCBlockDestruction(breakerId, pos, progress);
                    }
                    EvolutionNetwork.send(player, packet);
                }
            }
        }
    }

    @Shadow
    protected abstract BlockPos findLightningTargetAround(BlockPos pPos);

    /**
     * @author TheGreatWolf
     * @reason Remove allocations, handle evolution pending ticking
     */
    @Overwrite
    public void tickChunk(LevelChunk chunk, int randomTickSpeed) {
        ChunkPos chunkPos = chunk.getPos();
        boolean raining = this.isRaining();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        ProfilerFiller profiler = this.getProfiler();
        profiler.push("thunder");
        if (raining && this.isThundering() && this.random.nextInt(100_000) == 0) {
            BlockPos lightningPos = this.findLightningTargetAround(this.getRandomPosInChunk(minX, 0, minZ, 15, this.randomPosInChunkCachedPos));
            if (this.isRainingAt(lightningPos)) {
                DifficultyInstance difficulty = this.getCurrentDifficultyAt(lightningPos);
                boolean shouldSpawnSkeletonTrap = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) &&
                                                  this.random.nextDouble() < difficulty.getEffectiveDifficulty() * 0.01 &&
                                                  !this.getBlockState(lightningPos.below()).is(Blocks.LIGHTNING_ROD);
                if (shouldSpawnSkeletonTrap) {
                    SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this);
                    assert skeletonHorse != null;
                    skeletonHorse.setTrap(true);
                    skeletonHorse.setAge(0);
                    skeletonHorse.setPos(lightningPos.getX(), lightningPos.getY(), lightningPos.getZ());
                    this.addFreshEntity(skeletonHorse);
                }
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this);
                assert lightningBolt != null;
                lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
                lightningBolt.setVisualOnly(shouldSpawnSkeletonTrap);
                this.addFreshEntity(lightningBolt);
            }
        }
        profiler.popPush("iceandsnow");
        if (this.random.nextInt(16) == 0) {
            BlockPos topPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                                                   this.getRandomPosInChunk(minX, 0, minZ, 15, this.randomPosInChunkCachedPos));
            BlockPos belowTopPos = topPos.below();
            Biome biome = this.getBiome(topPos).value();
            // Forge: check area to avoid loading neighbors in unloaded chunks
            if (this.isAreaLoaded(topPos, 1)) {
                if (biome.shouldFreeze(this, belowTopPos)) {
                    this.setBlockAndUpdate(belowTopPos, Blocks.ICE.defaultBlockState());
                }
            }
            if (raining) {
                if (biome.shouldSnow(this, topPos)) {
                    this.setBlockAndUpdate(topPos, Blocks.SNOW.defaultBlockState());
                }
                BlockState stateBelowTopPos = this.getBlockState(belowTopPos);
                Biome.Precipitation precipitation = biome.getPrecipitation();
                if (precipitation == Biome.Precipitation.RAIN && biome.coldEnoughToSnow(belowTopPos)) {
                    precipitation = Biome.Precipitation.SNOW;
                }
                stateBelowTopPos.getBlock().handlePrecipitation(stateBelowTopPos, this, belowTopPos, precipitation);
            }
        }
        profiler.popPush("tickBlocks");
        IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
        chunkStorage.tick(chunk);
        if (randomTickSpeed > 0) {
            for (LevelChunkSection section : chunk.getSections()) {
                if (section.isRandomlyTicking()) {
                    int minY = section.bottomBlockY();
                    for (int k = 0; k < randomTickSpeed; ++k) {
                        BlockPos randomPos = this.getRandomPosInChunk(minX, minY, minZ, 15, this.randomPosInChunkCachedPos);
                        profiler.push("randomTick");
                        BlockState randomState = section.getBlockState(randomPos.getX() - minX, randomPos.getY() - minY, randomPos.getZ() - minZ);
                        if (randomState.isRandomlyTicking()) {
                            randomState.randomTick((ServerLevel) (Object) this, randomPos.immutable(), this.random);
                        }
                        FluidState randomFluidState = randomState.getFluidState();
                        if (randomFluidState.isRandomlyTicking()) {
                            randomFluidState.randomTick(this, randomPos.immutable(), this.random);
                        }
                        profiler.pop();
                    }
                }
            }
        }
        profiler.pop();
    }
}
