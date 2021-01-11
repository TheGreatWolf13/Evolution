package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import tgw.evolution.init.EvolutionHitBoxes;

public class BlockPlaceableRock extends BlockPlaceableItem {

    public BlockPlaceableRock() {
        super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Vec3d vec3d = state.getOffset(world, pos);
        return EvolutionHitBoxes.GROUND_ROCK.withOffset(vec3d.x, vec3d.y, vec3d.z);
    }
}
