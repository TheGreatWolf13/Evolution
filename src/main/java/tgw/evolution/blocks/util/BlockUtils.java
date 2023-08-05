package tgw.evolution.blocks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.blocks.IPhysics;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.patches.obj.IStateArgumentPredicate;
import tgw.evolution.patches.obj.IStatePredicate;
import tgw.evolution.util.collection.TriKey2BLinkedHashCache;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionToIntMap;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;
import java.util.function.ToIntFunction;

public final class BlockUtils {

    public static final IStatePredicate ALWAYS = BlockUtils::always;
    public static final IStatePredicate NEVER = BlockUtils::never;
    public static final IStateArgumentPredicate<EntityType<?>> ALWAYS_SPAWN = BlockUtils::always;
    public static final IStateArgumentPredicate<EntityType<?>> NEVER_SPAWN = BlockUtils::never;
    public static final ToIntFunction<BlockState> LIGHT_1 = BlockUtils::light1;
    public static final ToIntFunction<BlockState> LIGHT_2 = BlockUtils::light2;
    public static final ToIntFunction<BlockState> LIGHT_3 = BlockUtils::light3;
    public static final ToIntFunction<BlockState> LIGHT_4 = BlockUtils::light4;
    public static final ToIntFunction<BlockState> LIGHT_5 = BlockUtils::light5;
    public static final ToIntFunction<BlockState> LIGHT_7 = BlockUtils::light7;
    public static final ToIntFunction<BlockState> LIGHT_10 = BlockUtils::light10;
    public static final ToIntFunction<BlockState> LIGHT_11 = BlockUtils::light11;
    public static final ToIntFunction<BlockState> LIGHT_14 = BlockUtils::light14;
    public static final ToIntFunction<BlockState> LIGHT_15 = BlockUtils::light15;
    private static final ThreadLocal<TriKey2BLinkedHashCache<BlockState, BlockState, Direction>> OCCLUSION_CACHE = ThreadLocal.withInitial(
            () -> new TriKey2BLinkedHashCache<>(2_048, (byte) 127));

    private BlockUtils() {
    }

    private static boolean always(BlockState state, BlockGetter level, int x, int y, int z) {
        return true;
    }

    private static boolean always(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return true;
    }

    public static boolean canBeReplacedByFluid(BlockState state) {
        if (state.getBlock() instanceof IReplaceable) {
            if (!((IReplaceable) state.getBlock()).canBeReplacedByFluid(state)) {
                return false;
            }
        }
        return isReplaceable(state);
    }

    public static boolean canSupportCenter(BlockGetter level, int x, int y, int z, Direction side) {
        BlockState state = level.getBlockState_(x, y, z);
        return (side != Direction.DOWN || !state.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) &&
               state.isFaceSturdy_(level, x, y, z, side, SupportType.CENTER);
    }

    public static boolean canSupportRigidBlock(BlockGetter level, int x, int y, int z) {
        return level.getBlockState_(x, y, z).isFaceSturdy_(level, x, y, z, Direction.UP, SupportType.RIGID);
    }

    public static void dropItemStack(Level level, int x, int y, int z, ItemStack stack, double heightOffset) {
        if (level.isClientSide || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, x + 0.5, y + heightOffset, z + 0.5, stack);
        Vec3 motion = entity.getDeltaMovement();
        entity.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(entity);
    }

    public static void dropItemStack(Level world, int x, int y, int z, ItemStack stack) {
        dropItemStack(world, x, y, z, stack, 0);
    }

    public static void dropResources(BlockState state,
                                     Level level,
                                     int x,
                                     int y,
                                     int z,
                                     @Nullable BlockEntity tile,
                                     @Nullable Entity entity,
                                     ItemStack stack) {
        if (level instanceof ServerLevel l) {
            OList<ItemStack> drops = state.getDrops(l, x, y, z, stack, tile, entity, level.random);
            for (int i = 0, len = drops.size(); i < len; ++i) {
                popResource(level, x, y, z, drops.get(i));
            }
            state.spawnAfterBreak_(l, x, y, z, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState state, Level level, int x, int y, int z) {
        dropResources(state, level, x, y, z, null, null, ItemStack.EMPTY);
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

    public static @Nullable Axis getSmallestBeam(DirectionToIntMap beams) {
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

    public static boolean hasSolidFace(BlockGetter level, int x, int y, int z, Direction face) {
        BlockState state = level.getBlockState_(x, y, z);
        return state.isFaceSturdy_(level, x, y, z, face);
    }

    public static boolean hasSolidFaceAtSide(BlockGetter level, int x, int y, int z, Direction side) {
        switch (side) {
            case WEST -> --x;
            case EAST -> ++x;
            case DOWN -> --y;
            case UP -> ++y;
            case NORTH -> --z;
            case SOUTH -> ++z;
        }
        return hasSolidFace(level, x, y, z, side.getOpposite());
    }

    /**
     * Returns whether the blockstate is considered replaceable.
     */
    public static boolean isReplaceable(BlockState state) {
        return state.getMaterial().isReplaceable() || state.getBlock() instanceof IReplaceable replaceable && replaceable.isReplaceable(state);
    }

    private static int light1(BlockState state) {
        return 1;
    }

    private static int light10(BlockState state) {
        return 10;
    }

    private static int light11(BlockState state) {
        return 11;
    }

    private static int light14(BlockState state) {
        return 14;
    }

    private static int light15(BlockState state) {
        return 15;
    }

    private static int light2(BlockState state) {
        return 2;
    }

    private static int light3(BlockState state) {
        return 3;
    }

    private static int light4(BlockState state) {
        return 4;
    }

    private static int light5(BlockState state) {
        return 5;
    }

    private static int light7(BlockState state) {
        return 7;
    }

    private static boolean never(BlockState state, BlockGetter level, int x, int y, int z) {
        return false;
    }

    private static boolean never(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return false;
    }

    public static void popResource(Level level, int x, int y, int z, ItemStack stack) {
        if (!level.isClientSide && !stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            float height = EntityType.ITEM.getHeight() / 2.0F;
            double px = x + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
            double py = y + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25) - height;
            double pz = z + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
            ItemEntity itemEntity = new ItemEntity(level, px, py, pz, stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public static void scheduleBlockTick(LevelAccessor level, int x, int y, int z) {
        if (level.isClientSide()) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk == null) {
            return;
        }
        chunk.getChunkStorage().scheduleBlockTick(chunk, x, y, z);
    }

    public static void scheduleFluidTick(LevelAccessor level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return;
        }
        Fluid fluid = fluidState.getType();
        level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
    }

    public static void schedulePreciseBlockTick(LevelAccessor level, int x, int y, int z, int ticksInTheFuture) {
        if (level.isClientSide()) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk == null) {
            return;
        }
        chunk.getChunkStorage().schedulePreciseBlockTick(chunk, x, y, z, ticksInTheFuture);
    }

    public static boolean shouldRenderFace(BlockState state, BlockGetter level, int offX, int offY, int offZ, Direction face, int x, int y, int z) {
        BlockState adjacentState = level.getBlockState_(x, y, z);
        if (state.getBlock().shouldCull(level, state, offX, offY, offZ, adjacentState, x, y, z, face)) {
            return false;
        }
        if (adjacentState.canOcclude()) {
            TriKey2BLinkedHashCache<BlockState, BlockState, Direction> map = OCCLUSION_CACHE.get();
            byte b = map.getAndMoveToFirst(state, adjacentState, face);
            if (b != 127) {
                return b != 0;
            }
            VoxelShape shape = state.getFaceOcclusionShape_(level, offX, offY, offZ, face);
            if (shape.isEmpty()) {
                return true;
            }
            VoxelShape adjacentShape = adjacentState.getFaceOcclusionShape_(level, x, y, z, face.getOpposite());
            boolean flag = Shapes.joinIsNotEmpty(shape, adjacentShape, BooleanOp.ONLY_FIRST);
            map.putAndMoveToFirst(state, adjacentState, face, (byte) (flag ? 1 : 0));
            return flag;
        }
        return true;
    }

    public static void updateOrDestroy(BlockState state,
                                       BlockState updatedState,
                                       LevelAccessor level,
                                       int x,
                                       int y,
                                       int z,
                                       @BlockFlags int flags,
                                       int limit) {
        if (updatedState != state) {
            if (updatedState.isAir()) {
                if (!level.isClientSide()) {
                    level.destroyBlock_(x, y, z, (flags & BlockFlags.NO_NEIGHBOR_DROPS) == 0, null, limit);
                }
            }
            else {
                level.setBlock_(x, y, z, updatedState, flags & ~BlockFlags.NO_NEIGHBOR_DROPS, limit);
            }
        }
    }

    public static void updateSlopingBlocks(LevelAccessor level, int x, int y, int z) {
        if (!isReplaceable(level.getBlockState_(x, y, z))) {
            if (isReplaceable(level.getBlockState_(x, y + 1, z))) {
                int dirList = 0;
                for (Direction direction : DirectionUtil.HORIZ_NESW) {
                    BlockState state = level.getBlockStateAtSide(x, y + 1, z, direction);
                    if (state.getBlock() instanceof IPhysics physics && physics.slopes()) {
                        dirList = DirectionList.add(dirList, direction);
                    }
                }
                Random random = level.getRandom();
                while (!DirectionList.isEmpty(dirList)) {
                    int index = DirectionList.getRandom(dirList, random);
                    Direction direction = DirectionList.get(dirList, index);
                    dirList = DirectionList.remove(dirList, index);
                    scheduleBlockTick(level, x + direction.getStepX(), y + 1, z + direction.getStepZ());
                }
            }
        }
    }
}
