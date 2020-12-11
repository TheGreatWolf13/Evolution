package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionBlockStateProperties;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.EnumWoodNames;
import tgw.evolution.util.EnumWoodVariant;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

public class BlockLogPile extends BlockMass implements IReplaceable {

    public static final IntegerProperty LOG_COUNT = EvolutionBlockStateProperties.LOG_COUNT;
    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape[] SHAPES = {VoxelShapes.empty(),
                                                EvolutionHitBoxes.SINGLE_LOG_1,
                                                EvolutionHitBoxes.DOUBLE_LOG_1,
                                                EvolutionHitBoxes.TRIPLE_LOG_1,
                                                EvolutionHitBoxes.QUARTER_SLAB_LOWER_1,
                                                EvolutionHitBoxes.SINGLE_LOG_2,
                                                EvolutionHitBoxes.DOUBLE_LOG_2,
                                                EvolutionHitBoxes.TRIPLE_LOG_2,
                                                EvolutionHitBoxes.SLAB_LOWER,
                                                EvolutionHitBoxes.SINGLE_LOG_3,
                                                EvolutionHitBoxes.DOUBLE_LOG_3,
                                                EvolutionHitBoxes.TRIPLE_LOG_3,
                                                EvolutionHitBoxes.QUARTER_SLAB_LOWER_3,
                                                EvolutionHitBoxes.SINGLE_LOG_4,
                                                EvolutionHitBoxes.DOUBLE_LOG_4,
                                                EvolutionHitBoxes.TRIPLE_LOG_4,
                                                VoxelShapes.fullCube()};
    public final EnumWoodNames name;
    public EnumWoodVariant variant;

    public BlockLogPile(EnumWoodNames name) {
        super(Block.Properties.create(Material.WOOD)
                              .hardnessAndResistance(1_000.0F, 2.0F)
                              .sound(SoundType.WOOD)
                              .harvestLevel(HarvestLevel.UNBREAKABLE), name.getMass() * 16);
        this.setDefaultState(this.getDefaultState().with(LOG_COUNT, 1).with(DIRECTION, Direction.NORTH));
        this.name = name;
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LOG_COUNT, DIRECTION);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this.variant.getLog(), state.get(LOG_COUNT));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(this.variant.getLog());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = VoxelShapes.empty();
        if (state.get(LOG_COUNT) >= 4) {
            shape = MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION), SHAPES[4]);
        }
        if (state.get(LOG_COUNT) >= 8) {
            shape = VoxelShapes.combine(shape, MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION), SHAPES[8]), IBooleanFunction.OR);
        }
        if (state.get(LOG_COUNT) >= 12) {
            shape = VoxelShapes.combine(shape, MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION), SHAPES[12]), IBooleanFunction.OR);
        }
        if (state.get(LOG_COUNT) == 16) {
            shape = VoxelShapes.combine(shape, MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION), SHAPES[16]), IBooleanFunction.OR);
        }
        return VoxelShapes.combine(shape,
                                   MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION), SHAPES[state.get(LOG_COUNT)]),
                                   IBooleanFunction.OR);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        return state.getBlock() == this ?
               state.with(LOG_COUNT, Math.min(16, state.get(LOG_COUNT) + 1)) :
               this.getDefaultState().with(DIRECTION, context.getPlacementHorizontalFacing());
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.get(LOG_COUNT) < 13;
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        return useContext.getItem().getItem() == this.variant.getLog() &&
               state.get(LOG_COUNT) < 16 &&
               (!useContext.replacingClickedOnBlock() ||
                (useContext.getFace() == Direction.UP || useContext.getFace() == state.get(DIRECTION).rotateY()) &&
                useContext.getHitVec().y - useContext.getPos().getY() < 1 &&
                useContext.getHitVec().x - useContext.getPos().getX() < 1 &&
                useContext.getHitVec().z - useContext.getPos().getZ() < 1 &&
                useContext.getHitVec().x - useContext.getPos().getX() > 0 &&
                useContext.getHitVec().z - useContext.getPos().getZ() > 0);
    }

    @Override
    public boolean isSolid(BlockState state) {
        return state.get(LOG_COUNT) == 16;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return (this.isSolid(state) || BlockUtils.isReplaceable(worldIn.getBlockState(pos.up()))) &&
               Block.hasSolidSide(worldIn.getBlockState(pos.down()), worldIn, pos.down(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isRemote) {
            return;
        }
        if (Math.abs(pos.getX() + 0.5 - player.posX) < 1.5 &&
            Math.abs(pos.getY() - player.posY) < 1.5 &&
            Math.abs(pos.getZ() + 0.5 - player.posZ) < 1.5) {
            ItemStack stack = new ItemStack(this.variant.getLog());
            if (!player.inventory.addItemStackToInventory(stack)) {
                BlockUtils.dropItemStack(worldIn, pos, stack);
            }
            else {
                worldIn.playSound(null,
                                  pos.getX() + 0.5f,
                                  pos.getY() + 0.5f,
                                  pos.getZ() + 0.5f,
                                  SoundEvents.ENTITY_ITEM_PICKUP,
                                  SoundCategory.PLAYERS,
                                  0.2f,
                                  ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.7f + 1) * 2);
            }
            if (state.get(LOG_COUNT) == 1) {
                worldIn.removeBlock(pos, false);
                return;
            }
            worldIn.setBlockState(pos, state.with(LOG_COUNT, state.get(LOG_COUNT) - 1));
        }
    }
}
