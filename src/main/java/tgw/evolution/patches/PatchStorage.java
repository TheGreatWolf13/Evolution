package tgw.evolution.patches;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public interface PatchStorage {

    @CanIgnoreReturnValue
    default LevelChunk cameraReplace(int index, LevelChunk oldChunk, @Nullable LevelChunk newChunk) {
        throw new AbstractMethodError();
    }

    default void cameraReplace(int index, LevelChunk chunk) {
        throw new AbstractMethodError();
    }

    default int getCamViewCenterX() {
        throw new AbstractMethodError();
    }

    default int getCamViewCenterZ() {
        throw new AbstractMethodError();
    }

    default @Nullable LevelChunk getCameraChunk(int index) {
        throw new AbstractMethodError();
    }

    default int getCameraChunksLength() {
        throw new AbstractMethodError();
    }

    default int getCameraIndex(int x, int z) {
        throw new AbstractMethodError();
    }

    default boolean inCameraRange(int x, int z) {
        throw new AbstractMethodError();
    }

    default void setCamViewCenter(int x, int z) {
        throw new AbstractMethodError();
    }
}
