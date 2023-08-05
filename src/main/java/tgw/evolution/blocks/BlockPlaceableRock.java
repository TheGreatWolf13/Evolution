package tgw.evolution.blocks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionShapes;

public class BlockPlaceableRock extends BlockPlaceableItem {

    public BlockPlaceableRock() {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion());
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.moveShapeByOffset(EvolutionShapes.GROUND_ROCK, x, z);
    }
}
