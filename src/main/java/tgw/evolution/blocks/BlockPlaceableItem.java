package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.init.EvolutionHitBoxes;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;

public class BlockPlaceableItem extends BlockMass implements IReplaceable, IFluidLoggable, EntityBlock {

    public BlockPlaceableItem(Properties properties) {
        super(properties.dynamicShape(), 0);
        this.registerDefaultState(this.defaultBlockState().setValue(FLUID_LOGGED, false));
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canFlowThrough(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLUID_LOGGED);
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this));
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        return 100_000;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return 0;
    }

    @Override
    public int getMass(Level level, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.getValue(FLUID_LOGGED)) {
            Fluid fluid = this.getFluid(level, pos);
            if (fluid instanceof FluidGeneric fluidGeneric) {
                int amount = this.getCurrentAmount(level, pos, state);
                int layers = Mth.ceil(amount / 12_500.0);
                mass = layers * fluidGeneric.getMass() / 8;
            }
        }
        return mass + this.getBaseMass();
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return EvolutionHitBoxes.GROUND_ITEM.move(offset.x, offset.y, offset.z);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TELoggable(pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(FLUID_LOGGED)) {
            BlockUtils.scheduleFluidTick(level, currentPos);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
