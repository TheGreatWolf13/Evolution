package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import tgw.evolution.init.EvolutionHitBoxes;

public class BlockPlaceableRock extends BlockPlaceableItem {

    public BlockPlaceableRock() {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Vector3d offset = state.getOffset(world, pos);
        return EvolutionHitBoxes.GROUND_ROCK.move(offset.x, offset.y, offset.z);
    }
}
