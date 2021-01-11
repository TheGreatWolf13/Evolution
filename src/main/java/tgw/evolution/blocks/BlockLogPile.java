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
import tgw.evolution.util.EnumWoodNames;
import tgw.evolution.util.EnumWoodVariant;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LOG_COUNT;

public class BlockLogPile extends BlockMass implements IReplaceable {

    public final EnumWoodNames name;
    public EnumWoodVariant variant;

    public BlockLogPile(EnumWoodNames name) {
        super(Block.Properties.create(Material.WOOD)
                              .hardnessAndResistance(1_000.0F, 2.0F)
                              .sound(SoundType.WOOD)
                              .harvestLevel(HarvestLevel.UNBREAKABLE), name.getMass());
        this.setDefaultState(this.getDefaultState().with(LOG_COUNT, 1).with(DIRECTION_HORIZONTAL, Direction.NORTH));
        this.name = name;
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
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LOG_COUNT, DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this.variant.getLog(), state.get(LOG_COUNT));
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
    public int getMass(BlockState state) {
        return state.get(LOG_COUNT) * this.getBaseMass() / 16;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(this.variant.getLog());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        int logCount = state.get(LOG_COUNT);
        if (logCount == 16) {
            return VoxelShapes.fullCube();
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
                                MathHelper.rotateShape(Direction.NORTH, state.get(DIRECTION_HORIZONTAL), EvolutionHitBoxes.LOG_PILE[logCount]));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        return state.getBlock() == this ?
               state.with(LOG_COUNT, Math.min(16, state.get(LOG_COUNT) + 1)) :
               this.getDefaultState().with(DIRECTION_HORIZONTAL, context.getPlacementHorizontalFacing());
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
                (useContext.getFace() == Direction.UP || useContext.getFace() == state.get(DIRECTION_HORIZONTAL).rotateY()) &&
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
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return (this.isSolid(state) || BlockUtils.isReplaceable(world.getBlockState(pos.up()))) &&
               Block.hasSolidSide(world.getBlockState(pos.down()), world, pos.down(), Direction.UP);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                spawnDrops(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world.isRemote) {
            return;
        }
        if (Math.abs(pos.getX() + 0.5 - player.posX) < 1.5 &&
            Math.abs(pos.getY() - player.posY) < 1.5 &&
            Math.abs(pos.getZ() + 0.5 - player.posZ) < 1.5) {
            ItemStack stack = new ItemStack(this.variant.getLog());
            if (!player.inventory.addItemStackToInventory(stack)) {
                BlockUtils.dropItemStack(world, pos, stack);
            }
            else {
                world.playSound(null,
                                pos.getX() + 0.5f,
                                pos.getY() + 0.5f,
                                pos.getZ() + 0.5f,
                                SoundEvents.ENTITY_ITEM_PICKUP,
                                SoundCategory.PLAYERS,
                                0.2f,
                                ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7f + 1) * 2);
            }
            if (state.get(LOG_COUNT) == 1) {
                world.removeBlock(pos, false);
                return;
            }
            world.setBlockState(pos, state.with(LOG_COUNT, state.get(LOG_COUNT) - 1));
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(DIRECTION_HORIZONTAL, rot.rotate(state.get(DIRECTION_HORIZONTAL)));
    }
}
