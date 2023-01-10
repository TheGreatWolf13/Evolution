package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Range;
import tgw.evolution.init.EvolutionBStates;

public class BlockAtm extends Block implements IAir {

    public BlockAtm() {
        super(Properties.of(Material.GLASS).noCollission().noDrops().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(EvolutionBStates.ATM, 0));
    }

    @Override
    public boolean allowsFrom(BlockState state, Direction from) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EvolutionBStates.ATM);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
        return 1;
    }
}
