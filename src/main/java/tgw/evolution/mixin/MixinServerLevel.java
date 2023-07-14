package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.network.PacketSCBlockDestruction;
import tgw.evolution.patches.PatchLevel;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements PatchLevel {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final BlockPos.MutableBlockPos randomPosInChunkCachedPos = new BlockPos.MutableBlockPos();
    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;
    @Shadow @Final private MinecraftServer server;

    public MixinServerLevel(WritableLevelData pLevelData,
                            ResourceKey<Level> pDimension,
                            Holder<DimensionType> pDimensionTypeRegistration,
                            Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    private boolean addEntity(Entity entity) {
        if (entity.isRemoved()) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getKey(entity.getType()));
            return false;
        }
        if (this.entityManager.addNewEntity(entity)) {
            entity.onAddedToWorld();
            return true;
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    private void addPlayer(ServerPlayer player) {
        Entity entity = this.getEntities().get(player.getUUID());
        if (entity != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", player.getUUID().toString());
            entity.unRide();
            this.removePlayerImmediately((ServerPlayer) entity, Entity.RemovalReason.DISCARDED);
        }
        this.entityManager.addNewEntity(player);
        player.onAddedToWorld();
    }

    @Override
    public void destroyBlockProgress(int breakerId, long pos, int progress) {
        Packet<ClientGamePacketListener> packet = null;
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            if (player != null && player.level == this && player.getId() != breakerId) {
                double dx = BlockPos.getX(pos) - player.getX();
                double dy = BlockPos.getY(pos) - player.getY();
                double dz = BlockPos.getZ(pos) - player.getZ();
                if (dx * dx + dy * dy + dz * dz < 1_024.0) {
                    if (packet == null) {
                        packet = new PacketSCBlockDestruction(breakerId, pos, progress);
                    }
                    player.connection.send(packet);
                }
            }
        }
    }

    @Shadow
    public abstract void removePlayerImmediately(ServerPlayer serverPlayer, Entity.RemovalReason removalReason);

    /**
     * @author TheGreatWolf
     * @reason Remove allocations, handle evolution pending ticking
     */
    @Overwrite
    public void tickChunk(LevelChunk chunk, int randomTickSpeed) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        ProfilerFiller profiler = this.getProfiler();
        profiler.push("tickBlocks");
        chunk.getChunkStorage().tick(chunk);
        if (randomTickSpeed > 0) {
            for (LevelChunkSection section : chunk.getSections()) {
                if (section.isRandomlyTicking()) {
                    int minY = section.bottomBlockY();
                    for (int k = 0; k < randomTickSpeed; ++k) {
                        profiler.push("randomTick");
                        BlockPos randomPos = this.getRandomPosInChunk(minX, minY, minZ, 15, this.randomPosInChunkCachedPos);
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
