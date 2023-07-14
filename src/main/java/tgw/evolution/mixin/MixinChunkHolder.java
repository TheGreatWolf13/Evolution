package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchEither;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    /**
     * @reason Deprecated
     */
    @Overwrite
    public @Nullable LevelChunk getTickingChunk() {
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> future = this.getTickingChunkFuture();
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = future.getNow(null);
        return either == null ? null : ((PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) either).leftOrNull();
    }

    @Shadow
    public abstract CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture();
}
