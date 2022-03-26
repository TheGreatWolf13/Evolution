package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import tgw.evolution.util.math.DirectionToIntMap;
import tgw.evolution.util.math.DirectionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockUtils {

    private BlockUtils() {
    }

    public static boolean canBeReplacedByFluid(BlockState state) {
        if (state.getBlock() instanceof IReplaceable) {
            if (!((IReplaceable) state.getBlock()).canBeReplacedByFluid(state)) {
                return false;
            }
        }
        return isReplaceable(state);
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

    public static void destroyBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block.dropResources(state, level, pos);
        level.removeBlock(pos, false);
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
    }

    public static void dropItemStack(Level level, BlockPos pos, @Nonnull ItemStack stack, double heightOffset) {
        if (level.isClientSide || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + heightOffset, pos.getZ() + 0.5, stack);
        Vec3 motion = entity.getDeltaMovement();
        entity.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(entity);
    }

    public static void dropItemStack(Level world, BlockPos pos, @Nonnull ItemStack stack) {
        dropItemStack(world, pos, stack, 0);
    }

    /**
     * @param state The BlockState of the ladder
     * @return The ladder up movement speed, in m/t.
     */
    public static double getLadderUpSpeed(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IClimbable climbable) {
            return climbable.getUpSpeed();
        }
        return 0.1;
    }

    @Nullable
    public static Axis getSmallestBeam(DirectionToIntMap beams) {
        int x = beams.getBeamSize(Axis.X);
        int z = beams.getBeamSize(Axis.Z);
        if (x == 0) {
            if (z == 0) {
                return null;
            }
            return Axis.Z;
        }
        if (z == 0) {
            return Axis.X;
        }
        return x < z ? Axis.X : Axis.Z;
    }

    public static boolean hasMass(BlockState state) {
        return state.getBlock() instanceof BlockMass;
    }

    public static boolean hasSolidSide(BlockGetter world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        return state.isFaceSturdy(world, pos, side);
    }

    /**
     * Returns whether the blockstate is considered replaceable.
     */
    public static boolean isReplaceable(BlockState state) {
        return state.getMaterial().isReplaceable() || state.getBlock() instanceof IReplaceable replaceable && replaceable.isReplaceable(state);
    }

    public static boolean isTouchingWater(BlockGetter level, BlockPos pos) {
        MutableBlockPos mutablePos = new MutableBlockPos();
        for (Direction direction : DirectionUtil.ALL) {
            mutablePos.set(pos).move(direction);
            BlockState stateAtPos = level.getBlockState(mutablePos);
            if (stateAtPos.getFluidState().is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the tree trunk is supported.
     */
//    public static boolean isTrunkSustained(World world, OriginMutableBlockPos pos) {
//        BlockState state = world.getBlockState(pos.down().getPos());
//        if (!isReplaceable(state)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().north().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().south().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().west().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().east().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().north().east().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().north().west().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().west().south().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().down().east().south().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().north().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().south().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().east().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().west().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().north().west().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().south().west().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().north().east().getPos());
//        if (state.getBlock() instanceof BlockLog && state.getValue(TREE)) {
//            return true;
//        }
//        state = world.getBlockState(pos.reset().south().east().getPos());
//        return state.getBlock() instanceof BlockLog && state.getValue(TREE);
//    }
    public static void scheduleBlockTick(Level level, BlockPos pos, int tickrate) {
        level.scheduleTick(pos, level.getBlockState(pos).getBlock(), tickrate);
    }

    public static void scheduleFluidTick(LevelAccessor level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return;
        }
        Fluid fluid = fluidState.getType();
        level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
    }
}
