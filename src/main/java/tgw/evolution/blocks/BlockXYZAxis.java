package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static tgw.evolution.init.EvolutionBStates.AXIS;

public abstract class BlockXYZAxis extends BlockPhysics {

    public BlockXYZAxis(Properties builder) {
        super(builder);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.defaultBlockState().setValue(AXIS, hitResult.getDirection().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                case Y -> state;
            };
            case NONE, CLOCKWISE_180 -> state;
        };
    }
}