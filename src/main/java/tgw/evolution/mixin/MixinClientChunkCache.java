package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchClientChunkCache;
import tgw.evolution.patches.obj.IBlockEntityTagOutput;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.physics.EarthHelper;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache extends ChunkSource implements PatchClientChunkCache {

    @Shadow @Final private LevelChunk emptyChunk;
    @Shadow @Final public ClientLevel level;
    @Shadow volatile ClientChunkCache.Storage storage;

    @Shadow
    private static int calculateStorageRange(int i) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static boolean isValidChunk(@Nullable LevelChunk chunk, int x, int z) {
        if (chunk == null) {
            return false;
        }
        ChunkPos chunkPos = chunk.getPos();
        return chunkPos.x == EarthHelper.wrapChunkCoordinate(x) && chunkPos.z == EarthHelper.wrapChunkCoordinate(z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void drop(int x, int z) {
        ClientChunkCache.Storage storage = this.storage;
        if (storage.inRange(x, z)) {
            int index = storage.getIndex(x, z);
            LevelChunk chunk = storage.getChunk(index);
            if (isValidChunk(chunk, x, z)) {
                storage.drop(index, chunk);
            }
        }
        else if (storage.inCameraRange(x, z)) {
            int index = storage.getCameraIndex(x, z);
            LevelChunk chunk = storage.getCameraChunk(index);
            if (isValidChunk(chunk, x, z)) {
                //Unload event
                storage.cameraDrop(index, chunk);
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public String gatherStats() {
        return (this.storage.chunks.length() + this.storage.getCameraChunksLength()) + ", " + this.getLoadedChunksCount();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public @Nullable LevelChunk getChunk(int x, int z, ChunkStatus status, boolean load) {
        ClientChunkCache.Storage storage = this.storage;
        if (storage.inRange(x, z)) {
            int index = storage.getIndex(x, z);
            LevelChunk chunk = storage.getChunk(index);
            if (isValidChunk(chunk, x, z)) {
                return chunk;
            }
        }
        else {
            if (storage.inCameraRange(x, z)) {
                LevelChunk chunk = storage.getCameraChunk(storage.getCameraIndex(x, z));
                if (isValidChunk(chunk, x, z)) {
                    return chunk;
                }
            }
        }
        return load ? this.emptyChunk : null;
    }

    @Override
    public LHashSet getLoadedEmptySections() {
        return this.storage.getLoadedEmptySections();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void onLightUpdate(LightLayer type, SectionPos pos) {
        Evolution.deprecatedMethod();
        this.onLightUpdate_(type, pos.x(), pos.y(), pos.z());
    }

    @Override
    public void onLightUpdate_(LightLayer lightLayer, int secX, int secY, int secZ) {
        Minecraft.getInstance().lvlRenderer().setSectionDirty(secX, secY, secZ);
    }

    @Override
    public void onSectionEmptinessChanged(int secX, int secY, int secZ, boolean empty) {
        this.storage.onSectionEmptinessChanged(secX, secY, secZ, empty);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public @Nullable LevelChunk replaceWithPacketData(int x, int z, FriendlyByteBuf buf, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        Evolution.deprecatedMethod();
        return null;
    }

    /**
     * Runs on the main thread. Called by {@link ClientboundLevelChunkPacketData}
     */
    @Override
    public @Nullable LevelChunk replaceWithPacketData_(int x, int z, FriendlyByteBuf buf, CompoundTag tag, Consumer<IBlockEntityTagOutput> consumer) {
        ClientChunkCache.Storage storage = this.storage;
        if (storage.inRange(x, z)) {
            int index = storage.getIndex(x, z);
            LevelChunk chunk = storage.getChunk(index);
            ChunkPos pos = new ChunkPos(x, z);
            if (!isValidChunk(chunk, x, z)) {
                chunk = new LevelChunk(this.level, pos);
                chunk.replaceWithPacketData_(buf, tag, consumer);
                storage.replace(index, chunk);
            }
            else {
                chunk.replaceWithPacketData_(buf, tag, consumer);
            }
            this.level.onChunkLoaded(pos);
            return chunk;
        }
        if (storage.inCameraRange(x, z)) {
            int index = storage.getCameraIndex(x, z);
            LevelChunk chunk = storage.getCameraChunk(index);
            ChunkPos pos = new ChunkPos(x, z);
            if (!isValidChunk(chunk, x, z)) {
                chunk = new LevelChunk(this.level, pos);
                chunk.replaceWithPacketData_(buf, tag, consumer);
                storage.cameraReplace(index, chunk);
            }
            else {
                chunk.replaceWithPacketData_(buf, tag, consumer);
            }
            this.level.onChunkLoaded(pos);
            return chunk;
        }
        return null;
    }

    @Override
    public void updateCameraViewCenter(int x, int z) {
        this.storage.setCamViewCenter(x, z);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void updateViewCenter(int x, int z) {
        this.storage.viewCenterX = x;
        this.storage.viewCenterZ = z;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void updateViewRadius(int radius) {
        ClientChunkCache.Storage oldStorage = this.storage;
        int oldRadius = oldStorage.chunkRadius;
        int newRadius = calculateStorageRange(radius);
        if (oldRadius != newRadius) {
            ClientChunkCache.Storage newStorage = ((ClientChunkCache) (Object) this).new Storage(newRadius);
            newStorage.setCamViewCenter(oldStorage.getCamViewCenterX(), oldStorage.getCamViewCenterZ());
            newStorage.viewCenterX = oldStorage.viewCenterX;
            newStorage.viewCenterZ = oldStorage.viewCenterZ;
            AtomicReferenceArray<LevelChunk> chunks = oldStorage.chunks;
            for (int i = 0, len = chunks.length(); i < len; ++i) {
                LevelChunk chunk = chunks.get(i);
                if (chunk != null) {
                    ChunkPos chunkPos = chunk.getPos();
                    if (newStorage.inRange(chunkPos.x, chunkPos.z)) {
                        newStorage.replace(newStorage.getIndex(chunkPos.x, chunkPos.z), chunk);
                    }
                }
            }
            for (int i = 0, len = oldStorage.getCameraChunksLength(); i < len; ++i) {
                LevelChunk chunk = oldStorage.getCameraChunk(i);
                if (chunk != null) {
                    ChunkPos pos = chunk.getPos();
                    if (newStorage.inCameraRange(pos.x, pos.z)) {
                        newStorage.cameraReplace(newStorage.getCameraIndex(pos.x, pos.z), chunk);
                    }
                }
            }
            this.storage = newStorage;
        }
    }
}
