package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.patches.IClientChunkCachePatch;
import tgw.evolution.patches.IClientChunkCache_StoragePatch;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin extends ChunkSource implements IClientChunkCachePatch {

    @Shadow
    @Final
    static Logger LOGGER;
    @Shadow
    @Final
    public ClientLevel level;
    @Shadow
    volatile ClientChunkCache.Storage storage;
    @Shadow
    @Final
    private LevelChunk emptyChunk;

    @Shadow
    private static boolean isValidChunk(@Nullable LevelChunk chunk, int x, int z) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Handle when camera is not the player
     */
    @Overwrite
    public void drop(int x, int z) {
        if (this.storage.inRange(x, z)) {
            int index = this.storage.getIndex(x, z);
            LevelChunk chunk = this.storage.getChunk(index);
            if (isValidChunk(chunk, x, z)) {
                MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
                this.storage.replace(index, chunk, null);
            }
        }
        else if ((Object) this.storage instanceof IClientChunkCache_StoragePatch patch && patch.inCameraRange(x, z)) {
            int index = patch.getCameraIndex(x, z);
            LevelChunk chunk = patch.getCameraChunk(index);
            if (isValidChunk(chunk, x, z)) {
                MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
                patch.cameraReplace(index, chunk, null);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Handle when the camera is not the player
     */
    @Override
    @Overwrite
    public String gatherStats() {
        return (this.storage.chunks.length() + ((IClientChunkCache_StoragePatch) (Object) this.storage).getCameraChunksLength()) +
               ", " +
               this.getLoadedChunksCount();
    }

    /**
     * @author TheGreatWolf
     * @reason Handle when camera is not the player
     */
    @Override
    @Overwrite
    @Nullable
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean load) {
        if (this.storage.inRange(x, z)) {
            LevelChunk chunk = this.storage.getChunk(this.storage.getIndex(x, z));
            if (isValidChunk(chunk, x, z)) {
                return chunk;
            }
        }
        else if ((Object) this.storage instanceof IClientChunkCache_StoragePatch patch && patch.inCameraRange(x, z)) {
            LevelChunk chunk = patch.getCameraChunk(patch.getCameraIndex(x, z));
            if (isValidChunk(chunk, x, z)) {
                return chunk;
            }
        }
        return load ? this.emptyChunk : null;
    }

    @Inject(method = "updateViewRadius", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;" +
                                                                            "viewCenterX:I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUpdateViewRadius0(int viewDistance, CallbackInfo ci, int i, int j, ClientChunkCache.Storage storage) {
        IClientChunkCache_StoragePatch patch = (IClientChunkCache_StoragePatch) (Object) storage;
        IClientChunkCache_StoragePatch patch1 = (IClientChunkCache_StoragePatch) (Object) this.storage;
        patch.setCamViewCenter(patch1.getCamViewCenterX(), patch1.getCamViewCenterZ());
    }

    @Inject(method = "updateViewRadius", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache;" +
                                                                            "storage:Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;",
            ordinal = 5), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUpdateViewRadius1(int viewDistance,
                                     CallbackInfo ci,
                                     int i,
                                     int j,
                                     ClientChunkCache.Storage storage) {
        IClientChunkCache_StoragePatch thisPatch = (IClientChunkCache_StoragePatch) (Object) this.storage;
        IClientChunkCache_StoragePatch patch = (IClientChunkCache_StoragePatch) (Object) storage;
        for (int h = 0; h < thisPatch.getCameraChunksLength(); h++) {
            LevelChunk chunk = thisPatch.getCameraChunk(h);
            if (chunk != null) {
                ChunkPos pos = chunk.getPos();
                if (patch.inCameraRange(pos.x, pos.z)) {
                    patch.cameraReplace(patch.getCameraIndex(pos.x, pos.z), chunk);
                }
            }
        }
    }

    @Nullable
    public LevelChunk replaceWithPacketData(int x,
                                            int z,
                                            FriendlyByteBuf buf,
                                            CompoundTag tag,
                                            Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        if (this.storage.inRange(x, z)) {
            int index = this.storage.getIndex(x, z);
            LevelChunk chunk = this.storage.getChunk(index);
            ChunkPos pos = new ChunkPos(x, z);
            if (!isValidChunk(chunk, x, z)) {
                chunk = new LevelChunk(this.level, pos);
                chunk.replaceWithPacketData(buf, tag, consumer);
                this.storage.replace(index, chunk);
            }
            else {
                chunk.replaceWithPacketData(buf, tag, consumer);
            }
            this.level.onChunkLoaded(pos);
            MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
            return chunk;
        }
        if ((Object) this.storage instanceof IClientChunkCache_StoragePatch patch && patch.inCameraRange(x, z)) {
            int index = patch.getCameraIndex(x, z);
            LevelChunk chunk = patch.getCameraChunk(index);
            ChunkPos pos = new ChunkPos(x, z);
            if (!isValidChunk(chunk, x, z)) {
                chunk = new LevelChunk(this.level, pos);
                chunk.replaceWithPacketData(buf, tag, consumer);
                patch.cameraReplace(index, chunk);
            }
            else {
                chunk.replaceWithPacketData(buf, tag, consumer);
            }
            this.level.onChunkLoaded(pos);
            MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
            return chunk;
        }
        LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", x, z);
        return null;
    }

    @Override
    public void updateCameraViewCenter(int x, int z) {
        ((IClientChunkCache_StoragePatch) (Object) this.storage).setCamViewCenter(x, z);
    }
}
