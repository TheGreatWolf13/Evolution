package tgw.evolution.blocks.util;

import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.patches.obj.IStateArgumentPredicate;
import tgw.evolution.patches.obj.IStatePredicate;
import tgw.evolution.util.collection.TriKey2BLinkedHashCache;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.physics.SI;

import java.util.function.Consumer;
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
    public static final ToIntFunction<BlockState> LIGHT_YELLOW_15 = BlockUtils::lightYellow15;
    private static final ThreadLocal<TriKey2BLinkedHashCache<BlockState, BlockState, Direction>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> new TriKey2BLinkedHashCache<>(2_048, (byte) 127));
    private static final ItemDropper DROPPER = new ItemDropper();

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

    public static void dropResources(BlockState state, LevelAccessor level, int x, int y, int z, @Nullable BlockEntity tile, @Nullable Entity entity, ItemStack tool) {
        if (level instanceof ServerLevel l) {
            try (ItemDropper dropper = DROPPER.setup(level, x, y, z)) {
                state.dropLoot(l, x, y, z, tool, tile, entity, l.random, dropper);
            }
            state.spawnAfterBreak_(l, x, y, z, tool);
        }
    }

    public static void dropResources(BlockState state, LevelAccessor level, int x, int y, int z) {
        dropResources(state, level, x, y, z, level.getBlockEntity_(x, y, z), null, ItemStack.EMPTY);
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

    public static boolean isSmokeyPos(LevelAccessor level, int x, int y, int z) {
        for (int i = 1; i <= 5; ++i) {
            BlockState stateBelow = level.getBlockState_(x, y - i, z);
            if (CampfireBlock.isLitCampfire(stateBelow)) {
                return true;
            }
            boolean empty = Shapes.joinIsNotEmpty(CampfireBlock.VIRTUAL_FENCE_POST, stateBelow.getCollisionShape_(level, x, y, z), BooleanOp.AND);
            if (empty) {
                BlockState state = level.getBlockState_(x, y - i - 1, z);
                return CampfireBlock.isLitCampfire(state);
            }
        }
        return false;
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

    private static int lightYellow15(BlockState state) {
        return 0b1_1111_1_1111;
    }

    private static boolean never(BlockState state, BlockGetter level, int x, int y, int z) {
        return false;
    }

    private static boolean never(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return false;
    }

    public static void popResource(LevelAccessor level, int x, int y, int z, ItemStack stack) {
        if (!level.isClientSide() && !stack.isEmpty()) {
            ServerLevel l = (ServerLevel) level;
            if (l.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
                float height = EntityType.ITEM.getHeight() / 2.0F;
                double px = x + 0.5 + Mth.nextDouble(l.random, -0.25, 0.25);
                double py = y + 0.5 + Mth.nextDouble(l.random, -0.25, 0.25) - height;
                double pz = z + 0.5 + Mth.nextDouble(l.random, -0.25, 0.25);
                ItemEntity itemEntity = new ItemEntity(l, px, py, pz, stack, l.random.nextDouble() * 0.2 - 0.1, 1.5 * SI.METER / SI.SECOND, l.random.nextDouble() * 0.2 - 0.1);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }

    public static void preventCreativeDropFromBottomPart(Level level, int x, int y, int z, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if (half == DoubleBlockHalf.UPPER) {
            BlockState stateBelow = level.getBlockState_(x, y - 1, z);
            if (stateBelow.is(state.getBlock()) && stateBelow.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                BlockState stateToBecome = stateBelow.hasProperty(BlockStateProperties.WATERLOGGED) && stateBelow.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock_(x, y - 1, z, stateToBecome, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.NO_NEIGHBOR_DROPS);
                level.levelEvent_(player, LevelEvent.PARTICLES_DESTROY_BLOCK, x, y - 1, z, Block.getId(stateBelow));
            }
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

    public static BlockState updateFromNeighbourShapes(BlockState state, LevelAccessor level, int x, int y, int z) {
        BlockState updatedState = state;
        updatedState = updatedState.updateShape_(Direction.WEST, level.getBlockState_(x - 1, y, z), level, x, y, z, x - 1, y, z);
        updatedState = updatedState.updateShape_(Direction.EAST, level.getBlockState_(x + 1, y, z), level, x, y, z, x + 1, y, z);
        updatedState = updatedState.updateShape_(Direction.NORTH, level.getBlockState_(x, y, z - 1), level, x, y, z, x, y, z - 1);
        updatedState = updatedState.updateShape_(Direction.SOUTH, level.getBlockState_(x, y, z + 1), level, x, y, z, x, y, z + 1);
        updatedState = updatedState.updateShape_(Direction.DOWN, level.getBlockState_(x, y - 1, z), level, x, y, z, x, y - 1, z);
        updatedState = updatedState.updateShape_(Direction.UP, level.getBlockState_(x, y + 1, z), level, x, y, z, x, y + 1, z);
        return updatedState;
    }

    public static void updateOrDestroy(BlockState state, BlockState updatedState, LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
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

    public static final class ItemDropper implements Consumer<ItemStack>, AutoCloseable {

        private @Nullable LevelAccessor level;
        private int x;
        private int y;
        private int z;

        @Override
        public void accept(ItemStack stack) {
            assert this.level != null;
            popResource(this.level, this.x, this.y, this.z, stack);
        }

        @Override
        public void close() {
            this.level = null;
        }

        public ItemDropper setup(LevelAccessor level, int x, int y, int z) {
            assert this.level == null;
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }
}
