package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.init.EvolutionHitBoxes;

public class BlockPlaceableRock extends BlockPlaceableItem {

    public BlockPlaceableRock() {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return EvolutionHitBoxes.GROUND_ROCK.move(offset.x, offset.y, offset.z);
    }
}
