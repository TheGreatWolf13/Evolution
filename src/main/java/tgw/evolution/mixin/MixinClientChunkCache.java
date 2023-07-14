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
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.client.renderer.chunk.EvModelDataManager;
import tgw.evolution.patches.PatchClientChunkCache;
import tgw.evolution.patches.PatchMinecraft;
import tgw.evolution.patches.PatchStorage;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache extends ChunkSource implements PatchClientChunkCache {

    @Shadow @Final static Logger LOGGER;
    @Shadow @Final public ClientLevel level;
    @Shadow volatile ClientChunkCache.Storage storage;
    @Shadow @Final private LevelChunk emptyChunk;

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
                EvModelDataManager.onChunkUnload(chunk);
                this.storage.replace(index, chunk, null);
            }
        }
        else //noinspection ConstantConditions
            if ((Object) this.storage instanceof PatchStorage patch && patch.inCameraRange(x, z)) {
                int index = patch.getCameraIndex(x, z);
                LevelChunk chunk = patch.getCameraChunk(index);
                if (isValidChunk(chunk, x, z)) {
                    EvModelDataManager.onChunkUnload(chunk);
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
        return (this.storage.chunks.length() + ((PatchStorage) (Object) this.storage).getCameraChunksLength()) +
               ", " +
               this.getLoadedChunksCount();
    }

    /**
     * @author TheGreatWolf
     * @reason Handle when camera is not the player
     */
    @Override
    @Overwrite
    public @Nullable LevelChunk getChunk(int x, int z, ChunkStatus status, boolean load) {
        ClientChunkCache.Storage storage = this.storage;
        if (storage.inRange(x, z)) {
            LevelChunk chunk = storage.getChunk(storage.getIndex(x, z));
            if (isValidChunk(chunk, x, z)) {
                return chunk;
            }
        }
        else {
            PatchStorage patch = (PatchStorage) (Object) storage;
            if (patch.inCameraRange(x, z)) {
                LevelChunk chunk = patch.getCameraChunk(patch.getCameraIndex(x, z));
                if (isValidChunk(chunk, x, z)) {
                    return chunk;
                }
            }
        }
        return load ? this.emptyChunk : null;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void onLightUpdate(LightLayer type, SectionPos pos) {
        ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().setSectionDirty(pos.x(), pos.y(), pos.z());
    }

    @Inject(method = "updateViewRadius", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;" +
                                                                            "viewCenterX:I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUpdateViewRadius0(int viewDistance, CallbackInfo ci, int i, int j, ClientChunkCache.Storage storage) {
        PatchStorage patch = (PatchStorage) (Object) storage;
        PatchStorage patch1 = (PatchStorage) (Object) this.storage;
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
        PatchStorage thisPatch = (PatchStorage) (Object) this.storage;
        PatchStorage patch = (PatchStorage) (Object) storage;
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

    /**
     * @author TheGreatWolf
     * @reason Handle when camera is not the player
     */
    @Overwrite
    public @Nullable LevelChunk replaceWithPacketData(int x,
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
            return chunk;
        }
        //noinspection ConstantConditions
        if ((Object) this.storage instanceof PatchStorage patch && patch.inCameraRange(x, z)) {
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
            return chunk;
        }
        LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", x, z);
        return null;
    }

    @Override
    public void updateCameraViewCenter(int x, int z) {
        ((PatchStorage) (Object) this.storage).setCamViewCenter(x, z);
    }
}
