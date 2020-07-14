package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import tgw.evolution.util.DirectionToIntMap;
import tgw.evolution.util.OriginMutableBlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BlockUtils {

    private static final BlockPos.MutableBlockPos AUX_POS = new BlockPos.MutableBlockPos();

    public static boolean hasSolidSide(World world, BlockPos pos, Direction side) {
        return Block.hasSolidSide(world.getBlockState(pos), world, pos, side);
    }

    public static boolean canSustainSapling(BlockState state, IPlantable plantable) {
        return plantable instanceof BlockBush && BlockBush.isValidGround(state);
    }

    public static boolean compareVanillaBlockStates(BlockState vanilla, BlockState evolution) {
        if (vanilla.getBlock() == Blocks.GRASS_BLOCK) {
            return evolution.getBlock() instanceof BlockGrass;
        }
        return false;
    }

    public static boolean isTouchingWater(IWorld world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutablePos.setPos(pos).move(direction);
            BlockState stateAtPos = world.getBlockState(mutablePos);
            if (stateAtPos.getFluidState().isTagged(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    public static boolean willFluidAllowGap(World world, BlockPos pos, Direction direction, Fluid fluid, int currentLevel) {
        if (currentLevel == 1) {
            if (!world.getFluidState(AUX_POS.setPos(pos).move(Direction.DOWN)).getFluid().isEquivalentTo(fluid)) {
                return true;
            }
            BlockState fluidState = world.getBlockState(AUX_POS.move(direction));
            if (BlockUtils.canBeReplacedByWater(fluidState) && fluidState.getFluidState().getLevel() != 8) {
                return false;
            }
        }
        AUX_POS.setPos(pos).move(direction);
        if (!willFluidAllowGap(world, AUX_POS, fluid, currentLevel)) {
            return false;
        }
        Direction sideOffset = Direction.byHorizontalIndex(direction.getHorizontalIndex() + 1);
        AUX_POS.setPos(pos).move(sideOffset);
        if (!willFluidAllowGap(world, AUX_POS, fluid, currentLevel)) {
            return false;
        }
        AUX_POS.setPos(pos).move(sideOffset.getOpposite());
        return willFluidAllowGap(world, AUX_POS, fluid, currentLevel);
    }

    private static boolean willFluidAllowGap(World world, BlockPos pos, Fluid fluid, int currentLevel) {
        BlockState stateAtPos = world.getBlockState(pos);
        if (BlockUtils.isReplaceable(stateAtPos)) {
            IFluidState fluidStateAtPos = stateAtPos.getFluidState();
            if (fluid.isEquivalentTo(fluidStateAtPos.getFluid())) {
                int levelAtPos = fluidStateAtPos.getLevel();
                return levelAtPos == currentLevel;
            }
        }
        return true;
    }

    @Nullable
    public static Axis getSmallestBeam(DirectionToIntMap map) {
        if (map.containsKey(Direction.NORTH)) {
            if (map.containsKey(Direction.SOUTH)) {
                if (map.containsKey(Direction.WEST)) {
                    if (map.containsKey(Direction.EAST)) {
                        if (map.get(Direction.NORTH) + map.get(Direction.SOUTH) < map.get(Direction.EAST) + map.get(Direction.WEST)) {
                            return Axis.Z;
                        }
                        return Axis.X;
                    }
                }
                return Axis.Z;
            }
        }
        if (map.containsKey(Direction.WEST)) {
            if (map.containsKey(Direction.EAST)) {
                return Axis.X;
            }
        }
        return null;
    }

    /**
     * Returns whether the tree trunk is supported.
     */
    public static boolean isTrunkSustained(World worldIn, OriginMutableBlockPos pos) {
        BlockState state = worldIn.getBlockState(pos.down().getPos());
        if (!isReplaceable(state)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().north().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().south().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().west().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().east().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().north().east().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().north().west().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().west().south().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().down().east().south().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().north().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().south().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().east().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().west().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().north().west().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().south().west().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().north().east().getPos());
        if (state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE)) {
            return true;
        }
        state = worldIn.getBlockState(pos.reset().south().east().getPos());
        return state.getBlock() instanceof BlockLog && state.get(BlockLog.TREE);
    }

    /**
     * Returns whether the blockstate is considered replaceable.
     */
    public static boolean isReplaceable(BlockState state) {
        return state.getMaterial().isReplaceable() || state.getBlock() instanceof IReplaceable && ((IReplaceable) state.getBlock()).isReplaceable(
                state);
    }

    public static boolean canBeReplacedByWater(BlockState state) {
        if (state.getBlock() instanceof IReplaceable) {
            if (!((IReplaceable) state.getBlock()).canBeReplacedByLiquid(state)) {
                return false;
            }
        }
        return isReplaceable(state);
    }

    public static boolean hasMass(BlockState state) {
        return state.getBlock() instanceof BlockMass;
    }

    public static void dropItemStack(World world, BlockPos pos, @Nonnull ItemStack stack) {
        if (world.isRemote || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, stack);
        Vec3d motion = entity.getMotion();
        entity.addVelocity(-motion.x, -motion.y, -motion.z);
        world.addEntity(entity);
    }

    public static void scheduleBlockTick(World world, BlockPos pos, int tickrate) {
        world.getPendingBlockTicks().scheduleTick(pos, world.getBlockState(pos).getBlock(), tickrate);
    }
}
