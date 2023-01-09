package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionToIntMap;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

public final class BlockUtils {

    private static final ThreadLocal<MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(MutableBlockPos::new);
    private static final DirectionList DIRECTION_LIST = new DirectionList();

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

    public static void dropItemStack(Level level, BlockPos pos, ItemStack stack, double heightOffset) {
        if (level.isClientSide || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + heightOffset, pos.getZ() + 0.5, stack);
        Vec3 motion = entity.getDeltaMovement();
        entity.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(entity);
    }

    public static void dropItemStack(Level world, BlockPos pos, ItemStack stack) {
        dropItemStack(world, pos, stack, 0);
    }

    public static BlockState getBlockState(BlockGetter level, double x, double y, double z) {
        return getBlockState(level, Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static BlockState getBlockState(BlockGetter level, int x, int y, int z) {
        return level.getBlockState(MUTABLE_POS.get().set(x, y, z));
    }

    public static BlockState getBlockStateAtSide(BlockGetter level, int x, int y, int z, Direction side) {
        return getBlockState(level, x + side.getStepX(), y + side.getStepY(), z + side.getStepZ());
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
        return state.getBlock() instanceof BlockPhysics;
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

    public static void scheduleBlockTick(LevelAccessor level, int x, int y, int z) {
        if (level.isClientSide()) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk == null) {
            return;
        }
        IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
        chunkStorage.scheduleBlockTick(chunk, BlockPos.asLong(x, y, z));
    }

    public static void scheduleFluidTick(LevelAccessor level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return;
        }
        Fluid fluid = fluidState.getType();
        level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
    }

    public static void updateSlopingBlocks(LevelAccessor level, BlockPos pos) {
        if (!isReplaceable(level.getBlockState(pos))) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            if (isReplaceable(getBlockState(level, x, y + 1, z))) {
                DIRECTION_LIST.clear();
                for (Direction direction : DirectionUtil.HORIZ_NESW) {
                    BlockState state = getBlockStateAtSide(level, x, y + 1, z, direction);
                    if (state.getBlock() instanceof IPhysics physics && physics.slopes()) {
                        DIRECTION_LIST.add(direction);
                    }
                }
                Random random = level.getRandom();
                while (!DIRECTION_LIST.isEmpty()) {
                    Direction direction = DIRECTION_LIST.getRandomAndRemove(random);
                    scheduleBlockTick(level, x + direction.getStepX(), y + 1, z + direction.getStepZ());
                }
            }
        }
    }
}
