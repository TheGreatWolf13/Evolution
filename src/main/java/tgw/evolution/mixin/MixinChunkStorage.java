package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchChunkStorage;

import java.io.IOException;

@Mixin(ChunkStorage.class)
public abstract class MixinChunkStorage implements AutoCloseable, PatchChunkStorage {

    @Shadow private @Nullable LegacyStructureDataHandler legacyStructureHandler;
    @Shadow @Final private IOWorker worker;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable CompoundTag read(ChunkPos chunkPos) throws IOException {
        Evolution.deprecatedMethod();
        return this.read_(chunkPos.toLong());
    }

    @Override
    public @Nullable CompoundTag read_(long chunkPos) throws IOException {
        return this.worker.load_(chunkPos);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
        Evolution.deprecatedMethod();
        this.write_(chunkPos.toLong(), compoundTag);
    }

    @Override
    public void write_(long chunkPos, CompoundTag compoundTag) {
        this.worker.store_(chunkPos, compoundTag);
        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkPos);
        }
    }
}
