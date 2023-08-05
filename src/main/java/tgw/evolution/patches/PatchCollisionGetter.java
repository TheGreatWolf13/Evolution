package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface PatchCollisionGetter {

    default boolean isUnobstructed_(BlockState state, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }
}
