package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.WoodVariant;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LOG_COUNT;

public class BlockLogPile extends BlockMass implements IReplaceable {

    public final WoodVariant variant;

    public BlockLogPile(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(1_000.0F, 2.0F).sound(SoundType.WOOD).harvestLevel(HarvestLevel.UNBREAKABLE), variant.getMass());
        this.registerDefaultState(this.defaultBlockState().setValue(LOG_COUNT, 1).setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
        this.variant = variant;
    }

    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world.isClientSide) {
            return;
        }
        if (Math.abs(pos.getX() + 0.5 - player.getX()) < 1.5 &&
            Math.abs(pos.getY() - player.getY()) < 1.5 &&
            Math.abs(pos.getZ() + 0.5 - player.getZ()) < 1.5) {
            ItemStack stack = new ItemStack(this.variant.getLogItem());
            if (!player.inventory.add(stack)) {
                BlockUtils.dropItemStack(world, pos, stack);
            }
            else {
                world.playSound(null,
                                pos.getX() + 0.5f,
                                pos.getY() + 0.5f,
                                pos.getZ() + 0.5f,
                                SoundEvents.ITEM_PICKUP,
                                SoundCategory.PLAYERS,
                                0.2f,
                                ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1) * 2);
            }
            if (state.getValue(LOG_COUNT) == 1) {
                world.removeBlock(pos, false);
                return;
            }
            world.setBlockAndUpdate(pos, state.setValue(LOG_COUNT, state.getValue(LOG_COUNT) - 1));
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
        return useContext.getItemInHand().getItem() == this.variant.getLogItem() &&
               state.getValue(LOG_COUNT) < 16 &&
               (!useContext.replacingClickedOnBlock() ||
                (useContext.getClickedFace() == Direction.UP || useContext.getClickedFace() == state.getValue(DIRECTION_HORIZONTAL).getClockWise()) &&
                useContext.getClickLocation().y - useContext.getClickedPos().getY() < 1 &&
                useContext.getClickLocation().x - useContext.getClickedPos().getX() < 1 &&
                useContext.getClickLocation().z - useContext.getClickedPos().getZ() < 1 &&
                useContext.getClickLocation().x - useContext.getClickedPos().getX() > 0 &&
                useContext.getClickLocation().z - useContext.getClickedPos().getZ() > 0);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return (state.getValue(LOG_COUNT) == 16 || BlockUtils.isReplaceable(world.getBlockState(pos.above()))) &&
               BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LOG_COUNT, DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this.variant.getLogItem(), state.getValue(LOG_COUNT));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.62f;
    }

    @Override
    public int getMass(BlockState state) {
        return state.getValue(LOG_COUNT) * this.getBaseMass() / 16;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(this.variant.getLogItem());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        int logCount = state.getValue(LOG_COUNT);
        if (logCount == 16) {
            return VoxelShapes.block();
        }
        VoxelShape shape = VoxelShapes.empty();
        if (logCount >= 12) {
            shape = EvolutionHitBoxes.LOG_PILE[12];
        }
        else if (logCount >= 8) {
            shape = EvolutionHitBoxes.LOG_PILE[8];
        }
        else if (logCount >= 4) {
            shape = EvolutionHitBoxes.LOG_PILE[4];
        }
        return MathHelper.union(shape,
                                MathHelper.rotateShape(Direction.NORTH, state.getValue(DIRECTION_HORIZONTAL), EvolutionHitBoxes.LOG_PILE[logCount]));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        return state.getBlock() == this ?
               state.setValue(LOG_COUNT, Math.min(16, state.getValue(LOG_COUNT) + 1)) :
               this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, context.getHorizontalDirection());
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(LOG_COUNT) < 13;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }
}
