package tgw.evolution.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.NewConstructor;
import tgw.evolution.patches.PatchChunkEntities;
import tgw.evolution.util.collection.lists.OList;

import java.util.List;
import java.util.stream.Stream;

@Mixin(ChunkEntities.class)
public abstract class Mixin_CF_ChunkEntities<T> implements PatchChunkEntities<T> {

    @Unique private final long chunkPos;
    @Shadow @Final @DeleteField private List<T> entities;
    @Unique private final OList<T> entities_;
    @Shadow @Final @DeleteField private ChunkPos pos;

    @NewConstructor
    public Mixin_CF_ChunkEntities(long chunkPos, OList<T> entities) {
        this.chunkPos = chunkPos;
        this.entities_ = entities;
    }

    @ModifyConstructor
    public Mixin_CF_ChunkEntities(ChunkPos pos, List<T> list) {
        throw new RuntimeException("Invalid constructor");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Stream<T> getEntities() {
        Evolution.deprecatedMethod();
        return this.entities_.stream();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ChunkPos getPos() {
        Evolution.deprecatedMethod();
        return new ChunkPos(this.chunkPos);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean isEmpty() {
        return this.entities_.isEmpty();
    }
}
