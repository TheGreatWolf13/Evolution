package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCUpdateCameraViewCenter;
import tgw.evolution.patches.IServerPlayerPatch;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin extends ChunkStorage {

    private final LongSet chunksLoaded = new LongOpenHashSet();
    private final LongSet chunksToLoad = new LongOpenHashSet();
    private final LongSet chunksToUnload = new LongOpenHashSet();
    @Shadow
    public int viewDistance;
    @Shadow
    @Final
    ServerLevel level;
    @Shadow
    @Final
    private ChunkMap.DistanceManager distanceManager;
    @Shadow
    @Final
    private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Shadow
    @Final
    private ThreadedLevelLightEngine lightEngine;
    @Shadow
    @Final
    private PlayerMap playerMap;
    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

    public ChunkMapMixin(Path pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
        super(pRegionFolder, pFixerUpper, pSync);
    }

    @Shadow
    public static boolean isChunkInRange(int p_200879_, int p_200880_, int p_200881_, int p_200882_, int p_200883_) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean isChunkOnRangeBorder(int p_183829_, int p_183830_, int p_183831_, int p_183832_, int p_183833_) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Handle when the camera is not the player
     */
    @Overwrite
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean boundaryOnly) {
        Set<ServerPlayer> set = this.playerMap.getPlayers(pos.toLong());
        ImmutableList.Builder<ServerPlayer> builder = ImmutableList.builder();
        for (ServerPlayer player : set) {
            SectionPos lastSectionPos = player.getLastSectionPos();
            if (boundaryOnly && isChunkOnRangeBorder(pos.x, pos.z, lastSectionPos.x(), lastSectionPos.z(), this.viewDistance) ||
                !boundaryOnly && isChunkInRange(pos.x, pos.z, lastSectionPos.x(), lastSectionPos.z(), this.viewDistance)) {
                builder.add(player);
                continue;
            }
            SectionPos lastCameraSectionPos = ((IServerPlayerPatch) player).getLastCameraSectionPos();
            if (lastCameraSectionPos != null) {
                if (boundaryOnly && isChunkOnRangeBorder(pos.x, pos.z, lastCameraSectionPos.x(), lastCameraSectionPos.z(), this.viewDistance) ||
                    !boundaryOnly && isChunkInRange(pos.x, pos.z, lastCameraSectionPos.x(), lastCameraSectionPos.z(), this.viewDistance)) {
                    builder.add(player);
                }
            }
        }
        return builder.build();
    }

    @Shadow
    @Nullable
    protected abstract ChunkHolder getVisibleChunkIfPresent(long p_140328_);

    /**
     * @author TheGreatWolf
     * @reason Avoid a few allocations.
     */
    @Overwrite
    public void move(ServerPlayer player) {
        for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
            if (trackedEntity.entity == player) {
                trackedEntity.updatePlayers(this.level.players());
            }
            else {
                trackedEntity.updatePlayer(player);
            }
        }
        SectionPos lastSectionPos = player.getLastSectionPos();
        SectionPos currentSectionPos = SectionPos.of(player);
        long lastChunk = ChunkPos.asLong(lastSectionPos.x(), lastSectionPos.z());
        long currentChunk = ChunkPos.asLong(currentSectionPos.x(), currentSectionPos.z());
        boolean ignored = this.playerMap.ignored(player);
        boolean skip = this.skipPlayer(player);
        boolean diffSections = lastSectionPos.asLong() != currentSectionPos.asLong();
        boolean shouldRemovePlayerTicket = false;
        boolean shouldAddPlayerTicket = false;
        if (diffSections || ignored != skip) {
            this.updatePlayerPos(player);
            SectionPos pos = SectionPos.of(player.getCamera());
            EvolutionNetwork.send(player, new PacketSCUpdateCameraViewCenter(pos.x(), pos.z()));
            if (!ignored) {
                shouldRemovePlayerTicket = true;
            }
            if (!skip) {
                shouldAddPlayerTicket = true;
            }
            if (!ignored && skip) {
                this.playerMap.ignorePlayer(player);
            }
            if (ignored && !skip) {
                this.playerMap.unIgnorePlayer(player);
            }
            if (lastChunk != currentChunk) {
                this.playerMap.updatePlayer(lastChunk, currentChunk, player);
            }
        }
        this.chunksToLoad.clear();
        this.chunksToUnload.clear();
        this.chunksLoaded.clear();
        int sectionX = SectionPos.blockToSectionCoord(player.getBlockX());
        int sectionZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        int lastX = lastSectionPos.x();
        int lastZ = lastSectionPos.z();
        if (Math.abs(lastX - sectionX) <= this.viewDistance * 2 && Math.abs(lastZ - sectionZ) <= this.viewDistance * 2) {
            int minX = Math.min(sectionX, lastX) - this.viewDistance - 1;
            int minZ = Math.min(sectionZ, lastZ) - this.viewDistance - 1;
            int maxX = Math.max(sectionX, lastX) + this.viewDistance + 1;
            int maxZ = Math.max(sectionZ, lastZ) + this.viewDistance + 1;
            for (int dx = minX; dx <= maxX; ++dx) {
                for (int dz = minZ; dz <= maxZ; ++dz) {
                    boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                    boolean load = isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance);
                    if (load != wasLoaded) {
                        if (load) {
                            this.chunksToLoad.add(ChunkPos.asLong(dx, dz));
                        }
                        else {
                            this.chunksToUnload.add(ChunkPos.asLong(dx, dz));
                        }
                    }
                    else if (load && wasLoaded) {
                        this.chunksLoaded.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
        }
        else {
            //Unloads chunks
            for (int dx = lastX - this.viewDistance - 1; dx <= lastX + this.viewDistance + 1; ++dx) {
                for (int dz = lastZ - this.viewDistance - 1; dz <= lastZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance)) {
                        this.chunksToUnload.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
            //Loads chunks
            for (int dx = sectionX - this.viewDistance - 1; dx <= sectionX + this.viewDistance + 1; ++dx) {
                for (int dz = sectionZ - this.viewDistance - 1; dz <= sectionZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance)) {
                        this.chunksToLoad.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
        }
        //Handle camera chunks
        boolean shouldRemoveCameraTicket = false;
        boolean shouldAddCameraTicket = false;
        boolean shouldUnloadAll = false;
        boolean shouldCameraLoad = true;
        IServerPlayerPatch patch = (IServerPlayerPatch) player;
        SectionPos lastCameraSectionPos = patch.getLastCameraSectionPos();
        SectionPos currentCameraSectionPos = null;
        if (player.getCamera() == player) {
            if (!patch.getCameraUnload()) {
                shouldCameraLoad = false;
            }
            else {
                shouldUnloadAll = true;
                shouldRemoveCameraTicket = true;
            }
        }
        if (shouldCameraLoad) {
            patch.setCameraUnload(!shouldUnloadAll);
            boolean firstTick = false;
            if (lastCameraSectionPos == null) {
                lastCameraSectionPos = lastSectionPos;
                firstTick = true;
            }
            currentCameraSectionPos = SectionPos.of(player.getCamera());
            sectionX = currentCameraSectionPos.getX();
            sectionZ = currentCameraSectionPos.getZ();
            if (firstTick || lastCameraSectionPos.asLong() != currentCameraSectionPos.asLong()) {
                shouldAddCameraTicket = true;
                shouldRemoveCameraTicket = !firstTick;
                EvolutionNetwork.send(player, new PacketSCUpdateCameraViewCenter(sectionX, sectionZ));
            }
            patch.setLastCameraSectionPos(shouldUnloadAll ? null : currentCameraSectionPos);
            lastX = lastCameraSectionPos.x();
            lastZ = lastCameraSectionPos.z();
            if (!shouldUnloadAll && Math.abs(lastX - sectionX) <= this.viewDistance * 2 && Math.abs(lastZ - sectionZ) <= this.viewDistance * 2) {
                int minX = Math.min(sectionX, lastX) - this.viewDistance - 1;
                int minZ = Math.min(sectionZ, lastZ) - this.viewDistance - 1;
                int maxX = Math.max(sectionX, lastX) + this.viewDistance + 1;
                int maxZ = Math.max(sectionZ, lastZ) + this.viewDistance + 1;
                for (int dx = minX; dx <= maxX; ++dx) {
                    for (int dz = minZ; dz <= maxZ; ++dz) {
                        boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                        boolean load = isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance);
                        long pos = ChunkPos.asLong(dx, dz);
                        if (load) {
                            if (!this.chunksLoaded.contains(pos)) {
                                if (wasLoaded) {
                                    if (this.chunksToUnload.remove(pos)) {
                                        this.chunksToLoad.add(pos);
                                    }
                                }
                                else if (this.chunksToLoad.add(pos)) {
                                    this.chunksToUnload.remove(pos);
                                }
                            }
                        }
                        else if (wasLoaded) {
                            if (!this.chunksLoaded.contains(pos) && !this.chunksToLoad.contains(pos)) {
                                this.chunksToUnload.add(pos);
                            }
                        }
                    }
                }
            }
            else {
                //Unloads chunks
                for (int dx = lastX - this.viewDistance - 1; dx <= lastX + this.viewDistance + 1; ++dx) {
                    for (int dz = lastZ - this.viewDistance - 1; dz <= lastZ + this.viewDistance + 1; ++dz) {
                        if (isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance)) {
                            long pos = ChunkPos.asLong(dx, dz);
                            if (!this.chunksLoaded.contains(pos) && !this.chunksToLoad.contains(pos)) {
                                this.chunksToUnload.add(pos);
                            }
                        }
                    }
                }
                //Loads chunks
                if (!shouldUnloadAll) {
                    for (int dx = sectionX - this.viewDistance - 1; dx <= sectionX + this.viewDistance + 1; ++dx) {
                        for (int dz = sectionZ - this.viewDistance - 1; dz <= sectionZ + this.viewDistance + 1; ++dz) {
                            if (isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance)) {
                                long pos = ChunkPos.asLong(dx, dz);
                                if (!this.chunksLoaded.contains(pos)) {
                                    if (this.chunksToLoad.add(pos)) {
                                        this.chunksToUnload.remove(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Tickets
        if (shouldRemovePlayerTicket) {
            if (shouldCameraLoad && !shouldUnloadAll && lastSectionPos.asLong() == currentCameraSectionPos.asLong()) {
                shouldRemovePlayerTicket = false;
            }
        }
        if (shouldRemoveCameraTicket) {
            if (lastCameraSectionPos.asLong() == currentSectionPos.asLong()) {
                shouldRemoveCameraTicket = false;
            }
        }
        if (shouldRemovePlayerTicket) {
            this.distanceManager.removePlayer(lastSectionPos, player);
        }
        if (shouldRemoveCameraTicket) {
            this.distanceManager.removePlayer(lastCameraSectionPos, player);
        }
        if (shouldAddPlayerTicket) {
            this.distanceManager.addPlayer(currentSectionPos, player);
        }
        if (shouldAddCameraTicket) {
            this.distanceManager.addPlayer(currentCameraSectionPos, player);
        }
        LongIterator it = this.chunksToUnload.longIterator();
        while (it.hasNext()) {
            long pos = it.nextLong();
            //noinspection ObjectAllocationInLoop
            ChunkPos chunkPos = new ChunkPos(pos);
            this.updateChunkTrackingNoPkt(player, chunkPos, true, false);

        }
        it = this.chunksToLoad.longIterator();
        while (it.hasNext()) {
            long pos = it.nextLong();
            //noinspection ObjectAllocationInLoop
            ChunkPos chunkPos = new ChunkPos(pos);
            this.updateChunkTrackingNoPkt(player, chunkPos, false, true);
        }
    }

    private void playerLoadedChunkNoPkt(ServerPlayer player, LevelChunk chunk) {
        player.trackChunk(chunk.getPos(), new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, null, null, true));
        DebugPackets.sendPoiPacketsForChunk(this.level, chunk.getPos());
        OList<Mob> leashes = null;
        OList<Entity> passengers = null;
        for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
            Entity entity = trackedEntity.entity;
            if (entity != player && entity.chunkPosition().equals(chunk.getPos())) {
                trackedEntity.updatePlayer(player);
                if (entity instanceof Mob mob && mob.getLeashHolder() != null) {
                    if (leashes == null) {
                        leashes = new OArrayList<>();
                    }
                    leashes.add(mob);
                }
                if (!entity.getPassengers().isEmpty()) {
                    if (passengers == null) {
                        passengers = new OArrayList<>();
                    }
                    passengers.add(entity);
                }
            }
        }
        if (leashes != null) {
            for (Mob mob : leashes) {
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
            }
        }
        if (passengers != null) {
            for (Entity passenger : passengers) {
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetPassengersPacket(passenger));
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid (most) allocations, handle when the camera is not the player
     */
    @Overwrite
    protected void setViewDistance(int viewDistance) {
        int clamppedViewDist = Mth.clamp(viewDistance + 1, 3, 33);
        if (clamppedViewDist != this.viewDistance) {
            int oldViewDist = this.viewDistance;
            this.viewDistance = clamppedViewDist;
            this.distanceManager.updatePlayerTickets(this.viewDistance + 1);
            for (ChunkHolder chunkHolder : this.updatingChunkMap.values()) {
                ChunkPos pos = chunkHolder.getPos();
                //noinspection ObjectAllocationInLoop
                MutableObject<ClientboundLevelChunkWithLightPacket> packetHolder = new MutableObject<>();
                for (ServerPlayer player : this.getPlayers(pos, false)) {
                    SectionPos sectionPos = player.getLastSectionPos();
                    boolean wasLoaded = isChunkInRange(pos.x, pos.z, sectionPos.x(), sectionPos.z(), oldViewDist);
                    boolean load = isChunkInRange(pos.x, pos.z, sectionPos.x(), sectionPos.z(), this.viewDistance);
                    this.updateChunkTracking(player, pos, packetHolder, wasLoaded, load);
                }
            }
        }
    }

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer pPlayer);

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer pPlayer,
                                                ChunkPos pChunkPos,
                                                MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache,
                                                boolean pWasLoaded,
                                                boolean pLoad);

    protected void updateChunkTrackingNoPkt(ServerPlayer player, ChunkPos pos, boolean wasLoaded, boolean load) {
        if (player.level == this.level) {
            ForgeEventFactory.fireChunkWatch(wasLoaded, load, player, pos, this.level);
            if (load && !wasLoaded) {
                ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pos.toLong());
                if (chunkholder != null) {
                    LevelChunk levelchunk = chunkholder.getTickingChunk();
                    if (levelchunk != null) {
                        this.playerLoadedChunkNoPkt(player, levelchunk);
                    }
                    DebugPackets.sendPoiPacketsForChunk(this.level, pos);
                }
            }
            else if (!load && wasLoaded) {
                player.untrackChunk(pos);
            }
        }
    }

    @Shadow
    protected abstract SectionPos updatePlayerPos(ServerPlayer p_140374_);
}
