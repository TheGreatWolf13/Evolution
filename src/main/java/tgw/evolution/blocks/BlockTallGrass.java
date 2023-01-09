package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IForgeShearable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionShapes;

public class BlockTallGrass extends BlockBush implements IForgeShearable {

    public BlockTallGrass() {
        super(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XYZ;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EvolutionShapes.GRASS;
    }
}
