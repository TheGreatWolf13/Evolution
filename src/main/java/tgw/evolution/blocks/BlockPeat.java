package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.entities.misc.EntityFallingPeat;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.FLUIDLOGGED;
import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;

public class BlockPeat extends BlockMass implements IReplaceable, IFluidLoggable {

    public BlockPeat() {
        super(Block.Properties.create(Material.EARTH).hardnessAndResistance(2.0f, 0.5f).sound(SoundType.GROUND), 1_156);
        this.setDefaultState(this.getDefaultState().with(LAYERS_1_4, 1).with(FLUIDLOGGED, false));
    }

    private static boolean canFallThrough(BlockState state, World world, BlockPos pos) {
        if (!BlockGravity.canFallThrough(state)) {
            return false;
        }
        return state.getCollisionShape(world, pos).isEmpty();
    }

    public static void checkFallable(World world, BlockPos pos, BlockState state) {
        BlockPos posDown = pos.down();
        BlockState stateDown = world.getBlockState(posDown);
        int layers = 0;
        if (stateDown.getBlock() == EvolutionBlocks.PEAT.get()) {
            layers = stateDown.get(LAYERS_1_4);
        }
        if (layers == 4) {
            return;
        }
        if (!world.isAirBlock(posDown)) {
            if (!canFallThrough(stateDown, world, posDown)) {
                return;
            }
        }
        if (pos.getY() < 0) {
            return;
        }
        world.removeBlock(pos, true);
        EntityFallingPeat entity = new EntityFallingPeat(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state.get(LAYERS_1_4));
        world.addEntity(entity);
        entity.playSound(EvolutionSounds.SOIL_COLLAPSE.get(), 0.25F, 1.0F);
        for (Direction dir : MathHelper.DIRECTIONS_EXCEPT_DOWN) {
            BlockUtils.scheduleBlockTick(world, pos.offset(dir), 2);
        }
    }

    public static void placeLayersOn(World world, BlockPos pos, int layers) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == EvolutionBlocks.PEAT.get()) {
            for (int i = 1; i <= 4; i++) {
                if (state.get(LAYERS_1_4) == i) {
                    if (i + layers > 4) {
                        int remain = i + layers - 4;
                        world.setBlockState(pos, state.with(LAYERS_1_4, 4), BlockFlags.NOTIFY_AND_UPDATE);
                        world.setBlockState(pos.up(),
                                            EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, remain),
                                            BlockFlags.NOTIFY_AND_UPDATE);
                        return;
                    }
                    world.setBlockState(pos, state.with(LAYERS_1_4, i + layers), BlockFlags.NOTIFY_AND_UPDATE);
                    return;
                }
            }
        }
        if (state.getBlock() instanceof IReplaceable) {
            world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, layers), BlockFlags.NOTIFY_AND_UPDATE);
            ((IReplaceable) state.getBlock()).onReplaced(state, world, pos);
            return;
        }
        if (state.getMaterial().isReplaceable()) {
            world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS_1_4, layers), BlockFlags.NOTIFY_AND_UPDATE);
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return state.get(LAYERS_1_4) < 4;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canFlowThrough(BlockState state, Direction direction) {
        if (state.get(LAYERS_1_4) == 4) {
            return direction == Direction.UP;
        }
        return direction != Direction.DOWN;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TELoggable();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_4, FLUIDLOGGED);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.peat.get(), state.get(LAYERS_1_4));
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        int missingLayers = 4 - state.get(LAYERS_1_4);
        return missingLayers * 25_000;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.55f;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return state.get(LAYERS_1_4) * 25_000;
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.get(FLUIDLOGGED)) {
            Fluid fluid = this.getFluid(world, pos);
            if (fluid instanceof FluidGeneric) {
                int amount = this.getCurrentAmount(world, pos, state);
                int layers = MathHelper.ceil(amount / 12_500.0);
                mass = layers * ((FluidGeneric) fluid).getMass() / 8;
            }
        }
        return mass + this.getMass(state);
    }

    @Override
    public int getMass(BlockState state) {
        return state.get(LAYERS_1_4) * this.getBaseMass() / 4;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.PEAT[state.get(LAYERS_1_4)];
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getBlock() == this) {
            int layers = state.get(LAYERS_1_4);
            return state.with(LAYERS_1_4, MathHelper.clampMax(4, layers + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(FLUIDLOGGED);
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        if (useContext.getItem().getItem() == this.asItem() && state.get(LAYERS_1_4) < 4) {
            if (useContext.replacingClickedOnBlock()) {
                return useContext.getFace() == Direction.UP;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.get(LAYERS_1_4) != 4;
    }

    @Override
    public boolean isSolid(BlockState state) {
        return state.get(LAYERS_1_4) == 4;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockUtils.scheduleBlockTick(world, pos.up(), 2);
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        Block block = state.getBlock();
        Block newBlock = newState.getBlock();
        if (block == newBlock) {
            if (state.get(FLUIDLOGGED) && newState.get(FLUIDLOGGED)) {
                int layers = state.get(LAYERS_1_4);
                int newLayers = newState.get(LAYERS_1_4);
                if (newLayers > layers) {
                    Block fluidBlock = this.getFluidState(world, pos, state).getBlockState().getBlock();
                    fluidBlock.onReplaced(state, world, pos, newState, isMoving);
                }
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        BlockUtils.scheduleBlockTick(world, pos.up(), 2);
        if (player.isCreative() || state.get(LAYERS_1_4) == 1) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        world.setBlockState(pos, state.with(LAYERS_1_4, state.get(LAYERS_1_4) - 1));
        return true;
    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(FLUIDLOGGED)) {
            BlockUtils.scheduleFluidTick(world, pos);
        }
        if (!world.isRemote) {
            checkFallable(world, pos, state);
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (state.get(FLUIDLOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return !state.isValidPosition(world, currentPos) ?
               Blocks.AIR.getDefaultState() :
               super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }
}
