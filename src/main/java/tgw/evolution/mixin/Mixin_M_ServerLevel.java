package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.network.PacketSCBlockDestruction;
import tgw.evolution.network.PacketSCLevelEvent;
import tgw.evolution.patches.PatchServerLevel;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;

import java.util.Set;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class Mixin_M_ServerLevel extends Level implements WorldGenLevel, PatchServerLevel {

    @Shadow @Final private static Logger LOGGER;
    @Shadow public volatile boolean isUpdatingNavigations;
    @Mutable @Shadow @Final public Set<Mob> navigatingMobs;
    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;
    @Shadow @Final private MinecraftServer server;

    public Mixin_M_ServerLevel(WritableLevelData pLevelData,
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
    @Overwrite
    @DeleteMethod
    public void blockUpdated(BlockPos blockPos, Block block) {
        throw new AbstractMethodError();
    }

    @Override
    public void blockUpdated_(int x, int y, int z, Block block) {
        if (!this.isDebug()) {
            this.updateNeighborsAt_(x, y, z, block);
        }
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

    @Override
    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Shadow
    public abstract PoiManager getPoiManager();

    @Override
    @Shadow
    public abstract @NotNull MinecraftServer getServer();

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

    @Override
    @Overwrite
    @DeleteMethod
    public void globalLevelEvent(int i, BlockPos blockPos, int j) {
        throw new AbstractMethodError();
    }

    @Override
    public void globalLevelEvent_(@LvlEvent int event, int x, int y, int z, int data) {
        this.server.getPlayerList().broadcastAll(new PacketSCLevelEvent(event, BlockPos.asLong(x, y, z), data, true));
    }

    @Override
    @Overwrite
    public void levelEvent(@Nullable Player player, @LvlEvent int event, BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        this.levelEvent_(player, event, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    @Override
    public void levelEvent_(@Nullable Player player, int event, int x, int y, int z, int data) {
        this.server.getPlayerList()
                   .broadcast(player, x, y, z, 64, this.dimension(), new PacketSCLevelEvent(event, BlockPos.asLong(x, y, z), data, false));
    }

    @Override
    @Overwrite
    public boolean mayInteract(Player player, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.mayInteract_(player, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean mayInteract_(Player player, int x, int y, int z) {
        return !this.server.isUnderSpawnProtection_((ServerLevel) (Object) this, x, y, z, player) && this.getWorldBorder().isWithinBounds_(x, z);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        throw new AbstractMethodError();
    }

    @Override
    public void onBlockStateChange_(int x, int y, int z, BlockState oldState, BlockState newState) {
        PoiType oldPoi = PoiType.TYPE_BY_STATE.get(oldState);
        PoiType newPoi = PoiType.TYPE_BY_STATE.get(newState);
        if (oldPoi != newPoi) {
            //noinspection VariableNotUsedInsideIf
            if (oldPoi != null) {
                this.getServer().execute(() -> this.getPoiManager().remove_(x, y, z));
            }
            if (newPoi != null) {
                this.getServer().execute(() -> this.getPoiManager().add_(x, y, z, newPoi));
            }
        }
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;navigatingMobs:Ljava/util/Set;",
            opcode = Opcodes.PUTFIELD))
    private void onInit(ServerLevel instance, Set<Mob> value) {
        this.navigatingMobs = new OHashSet<>();
    }

    @Shadow
    public abstract void removePlayerImmediately(ServerPlayer serverPlayer, Entity.RemovalReason removalReason);

    @Override
    @Overwrite
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        Evolution.deprecatedMethod();
        this.sendBlockUpdated_(pos.getX(), pos.getY(), pos.getZ(), oldState, newState, flags);
    }

    @Override
    public void sendBlockUpdated_(int x, int y, int z, BlockState oldState, BlockState newState, @BlockFlags int flags) {
        if (this.isUpdatingNavigations) {
            Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
        }
        this.getChunkSource().blockChanged_(x, y, z);
        VoxelShape oldShape = oldState.getCollisionShape_(this, x, y, z);
        VoxelShape newShape = newState.getCollisionShape_(this, x, y, z);
        if (oldShape != newShape && Shapes.joinIsNotEmpty(oldShape, newShape, BooleanOp.NOT_SAME)) {
            OList<PathNavigation> list = null;
            OSet<Mob> navigatingMobs = (OSet<Mob>) this.navigatingMobs;
            for (Mob e = navigatingMobs.fastEntries(); e != null; e = navigatingMobs.fastEntries()) {
                PathNavigation pathNavigation = e.getNavigation();
                if (pathNavigation.shouldRecomputePath_(x, y, z)) {
                    if (list == null) {
                        list = new OArrayList<>();
                    }
                    list.add(pathNavigation);
                }
            }
            try {
                this.isUpdatingNavigations = true;
                if (list != null) {
                    for (int i = 0, len = list.size(); i < len; ++i) {
                        list.get(i).recomputePath();
                    }
                }
            }
            finally {
                this.isUpdatingNavigations = false;
            }
        }
    }

    @Overwrite
    private void tickBlock(BlockPos pos, Block block) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockState state = this.getBlockState_(x, y, z);
        if (state.is(block)) {
            state.tick_((ServerLevel) (Object) this, x, y, z, this.random);
        }
    }

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
                        this.randValue = this.randValue * 3 + 0x3c6e_f35f;
                        int rand = this.randValue >> 2;
                        int dx = rand & 15;
                        int dy = rand >> 16 & 15;
                        int dz = rand >> 8 & 15;
                        BlockState randomState = section.getBlockState(dx, dy, dz);
                        if (randomState.isRandomlyTicking()) {
                            randomState.randomTick_((ServerLevel) (Object) this, minX + dx, minY + dy, minZ + dz, this.random);
                        }
                        FluidState randomFluidState = randomState.getFluidState();
                        if (randomFluidState.isRandomlyTicking()) {
                            randomFluidState.randomTick_(this, minX + dx, minY + dy, minZ + dz, this.random);
                        }
                        profiler.pop();
                    }
                }
            }
        }
        profiler.pop();
    }

    @Overwrite
    private void tickFluid(BlockPos pos, Fluid fluid) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        FluidState state = this.getFluidState_(x, y, z);
        if (state.is(fluid)) {
            state.tick_(this, x, y, z);
        }
    }
}
