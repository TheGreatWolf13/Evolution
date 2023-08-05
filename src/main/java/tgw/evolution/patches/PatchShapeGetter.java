package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface PatchShapeGetter {

    default VoxelShape get_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }
}
