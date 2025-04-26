package tgw.evolution.mixin;

import net.minecraft.world.level.entity.ChunkEntities;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.NewConstructor;
import tgw.evolution.patches.mixin.ObjectFactories;
import tgw.evolution.util.collection.lists.OList;

@Mixin(ObjectFactories.class)
public abstract class Mixin_M_ObjectFactories {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Contract("_, _ -> new")
    @Overwrite
    @NewConstructor
    public static <T> ChunkEntities<T> newChunkEntities(long chunkPos, OList<T> entities) {
        //noinspection Contract
        throw new AbstractMethodError();
    }
}
