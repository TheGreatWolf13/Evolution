package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tgw.evolution.entities.EntityFallingPeat;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionSounds;

import java.util.Random;

public class BlockPeat extends BlockMass implements IReplaceable {

    public static final IntegerProperty LAYERS = EvolutionBlockStateProperties.LAYERS_1_4;
    protected static final VoxelShape[] SHAPES = {VoxelShapes.empty(),
                                                  EvolutionHitBoxes.QUARTER_SLAB_LOWER_1,
                                                  EvolutionHitBoxes.QUARTER_SLAB_LOWER_2,
                                                  EvolutionHitBoxes.QUARTER_SLAB_LOWER_3,
                                                  VoxelShapes.fullCube()};

    public BlockPeat() {
        super(Block.Properties.create(Material.EARTH).hardnessAndResistance(2.0f, 0.5f).sound(SoundType.GROUND), 1156);
        this.setDefaultState(this.getDefaultState().with(LAYERS, 1));
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        worldIn.getPendingBlockTicks().scheduleTick(pos.up(), worldIn.getBlockState(pos.up()).getBlock(), 2);
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public boolean isSolid(BlockState state) {
        return state.get(LAYERS) == 4;
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        if (useContext.getItem().getItem() == this.asItem() && state.get(LAYERS) < 4) {
            if (useContext.replacingClickedOnBlock()) {
                return useContext.getFace() == Direction.UP;
            }
            return true;
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.get(LAYERS)];

    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if (!world.isRemote) {
            world.getPendingBlockTicks().scheduleTick(pos.up(), world.getBlockState(pos.up()).getBlock(), 2);
        }
        if (player.isCreative() || state.get(LAYERS) == 1) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        world.setBlockState(pos, state.with(LAYERS, state.get(LAYERS) - 1));
        return true;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            checkFallable(worldIn, pos, state);
        }
    }

    public static void checkFallable(World worldIn, BlockPos pos, BlockState state) {
        BlockPos posDown = pos.down();
        if (!worldIn.isAirBlock(posDown) && worldIn.getBlockState(posDown) != EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, 1) && worldIn.getBlockState(posDown) != EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, 2) && worldIn.getBlockState(posDown) != EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, 3)) {
            if (!canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) {
                return;
            }
        }
        if (pos.getY() < 0) {
            return;
        }
        if (worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
            if (worldIn.isRemote) {
                return;
            }
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            EntityFallingPeat entity = new EntityFallingPeat(worldIn, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state.get(LAYERS));
            worldIn.addEntity(entity);
            entity.playSound(EvolutionSounds.SOIL_COLLAPSE.get(), 0.25F, 1.0F);
            worldIn.getPendingBlockTicks().scheduleTick(pos.up(), worldIn.getBlockState(pos.up()).getBlock(), 2);
            worldIn.getPendingBlockTicks().scheduleTick(pos.north(), worldIn.getBlockState(pos.north()).getBlock(), 2);
            worldIn.getPendingBlockTicks().scheduleTick(pos.south(), worldIn.getBlockState(pos.south()).getBlock(), 2);
            worldIn.getPendingBlockTicks().scheduleTick(pos.east(), worldIn.getBlockState(pos.east()).getBlock(), 2);
            worldIn.getPendingBlockTicks().scheduleTick(pos.west(), worldIn.getBlockState(pos.west()).getBlock(), 2);
            return;
        }
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        BlockPos blockpos = pos.down();
        while ((worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos), worldIn, blockpos)) && blockpos.getY() > 0) {
            blockpos = blockpos.down();
        }
        if (blockpos.getY() <= 0) {
            return;
        }
        worldIn.setBlockState(blockpos.up(), state);
    }

    private static boolean canFallThrough(BlockState state, World worldIn, BlockPos pos) {
        if (!BlockGravity.canFallThrough(state)) {
            return false;
        }
        return state.getCollisionShape(worldIn, pos) == null;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getBlock() == this) {
            int i = state.get(LAYERS);
            return state.with(LAYERS, Math.min(4, i + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    public static void placeLayersOn(World world, BlockPos pos, int layers) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == EvolutionBlocks.PEAT.get()) {
            for (int i = 1; i <= 4; i++) {
                if (state == EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, i)) {
                    if (i + layers > 4) {
                        int remain = i + layers - 4;
                        world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, 4), 3);
                        world.setBlockState(pos.up(), EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, remain), 3);
                        return;
                    }
                    world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, i + layers), 3);
                    return;
                }
            }
        }
        if (state.getMaterial().isReplaceable()) {
            world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, layers), 3);
            return;
        }
        if (state.getBlock() instanceof IReplaceable) {
            world.setBlockState(pos, EvolutionBlocks.PEAT.get().getDefaultState().with(LAYERS, layers), 3);
        }
    }

    @Override
    public int getMass(BlockState state) {
        return super.getMass(state) / 4 * state.get(LAYERS);
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(EvolutionItems.peat.get(), state.get(LAYERS));
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.get(LAYERS) != 4;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }
}
