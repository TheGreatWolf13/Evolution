package tgw.evolution.mixin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlock;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;

@Mixin(Block.class)
public abstract class Mixin_FS_Block extends BlockBehaviour implements PatchBlock, ItemLike {

    @Mutable @Shadow @Final @RestoreFinal public static IdMapper<BlockState> BLOCK_STATE_REGISTRY;
    @Shadow @Final @DeleteField private static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE;
    @Mutable @Shadow @Final @RestoreFinal private static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal private static LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE;
    @Shadow @Final protected StateDefinition<Block, BlockState> stateDefinition;

    public Mixin_FS_Block(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static boolean canSupportCenter(LevelReader level, BlockPos pos, Direction side) {
        Evolution.deprecatedMethod();
        return BlockUtils.canSupportCenter(level, pos.getX(), pos.getY(), pos.getZ(), side);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static boolean canSupportRigidBlock(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return BlockUtils.canSupportRigidBlock(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void dropResources(BlockState state, LootContext.Builder builder) {
        ServerLevel level = builder.getLevel();
        BlockPos pos = new BlockPos(builder.getParameter(LootContextParams.ORIGIN));
        List<ItemStack> drops = state.getDrops(builder);
        for (int i = 0, l = drops.size(); i < l; i++) {
            BlockUtils.popResource(level, pos.getX(), pos.getY(), pos.getZ(), drops.get(i));
        }
        state.spawnAfterBreak(level, pos, ItemStack.EMPTY);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void dropResources(BlockState state, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, null);
            for (int i = 0, l = drops.size(); i < l; i++) {
                BlockUtils.popResource(level, pos.getX(), pos.getY(), pos.getZ(), drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void dropResources(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity te) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, te);
            for (int i = 0, l = drops.size(); i < l; i++) {
                BlockUtils.popResource(serverLevel, pos.getX(), pos.getY(), pos.getZ(), drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid lambda allocation
     */
    @Overwrite
    public static void dropResources(BlockState state,
                                     Level level,
                                     BlockPos pos,
                                     @Nullable BlockEntity te,
                                     Entity entity,
                                     ItemStack tool) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, te, entity, tool);
            for (int i = 0, len = drops.size(); i < len; i++) {
                BlockUtils.popResource(level, pos.getX(), pos.getY(), pos.getZ(), drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, tool);
        }
    }

    @Shadow
    public static List<ItemStack> getDrops(BlockState pState,
                                           ServerLevel pLevel,
                                           BlockPos pPos,
                                           @Nullable BlockEntity pBlockEntity) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static List<ItemStack> getDrops(BlockState pState,
                                           ServerLevel pLevel,
                                           BlockPos pPos,
                                           @Nullable BlockEntity pBlockEntity,
                                           @Nullable Entity pEntity,
                                           ItemStack pTool) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static int getId(@Nullable BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static boolean isShapeFullBlock(VoxelShape shape) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void popResource(Level level, BlockPos pos, ItemStack stack) {
        Evolution.deprecatedMethod();
        BlockUtils.popResource(level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    /**
     * @author TheGreatWolf
     * @reason Improve culling.
     */
    @Overwrite
    public static boolean shouldRenderFace(BlockState state, BlockGetter level, BlockPos offset, Direction face, BlockPos pos) {
        Evolution.deprecatedMethod();
        return BlockUtils.shouldRenderFace(state, level, offset.getX(), offset.getY(), offset.getZ(), face, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static BlockState updateFromNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return BlockUtils.updateFromNeighbourShapes(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void updateOrDestroy(BlockState state,
                                       BlockState updatedState,
                                       LevelAccessor level,
                                       BlockPos pos,
                                       @BlockFlags int flags,
                                       int limit) {
        Evolution.deprecatedMethod();
        BlockUtils.updateOrDestroy(state, updatedState, level, pos.getX(), pos.getY(), pos.getZ(), flags, limit);
    }

    @ModifyStatic
    @Unique
    private static void clinit() {
        LOGGER = LogUtils.getLogger();
        BLOCK_STATE_REGISTRY = new IdMapper<>();
        SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<>() {
            @Override
            public Boolean load(VoxelShape shape) {
                return !Shapes.joinIsNotEmpty(Shapes.block(), shape, BooleanOp.NOT_SAME);
            }
        });
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.animateTick_(state, level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
    }

    @Shadow
    public abstract BlockState defaultBlockState();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.destroy_(level, pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void destroy_(LevelAccessor level, int x, int y, int z, BlockState state) {
    }

    @Override
    public void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        if (this.properties.drops == BuiltInLootTables.EMPTY) {
            return;
        }
        ItemStack stack = new ItemStack(this);
        if (!stack.isEmpty()) {
            consumer.accept(stack);
        }
        else {
            Evolution.warn("{} dropped nothing!", state);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, int x, int y, int z, Player player) {
        return new ItemStack(this);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8f;
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, int x, int y, int z) {
        return HarvestLevel.HAND;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> function) {
        ImmutableMap.Builder<BlockState, VoxelShape> builder = new ImmutableMap.Builder<>();
        OList<BlockState> possibleStates = this.stateDefinition.getPossibleStates_();
        for (int i = 0, len = possibleStates.size(); i < len; ++i) {
            BlockState state = possibleStates.get(i);
            builder.put(state, function.apply(state));
        }
        return builder.build();
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        return this.defaultBlockState();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        Evolution.deprecatedMethod();
        this.playerDestroy_(level, player, pos.getX(), pos.getY(), pos.getZ(), state, te, stack);
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get((Block) (Object) this));
        BlockUtils.dropResources(state, level, x, y, z, te, player, stack);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    //ok
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        Evolution.deprecatedMethod();
        this.playerWillDestroy_(level, pos.getX(), pos.getY(), pos.getZ(), state, player);
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        this.spawnDestroyParticles_(level, player, x, y, z, state.stateForParticles(level, x, y, z));
        if (state.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(player, false);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.propagatesSkylightDown_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return !isShapeFullBlock(state.getShape_(level, x, y, z)) && state.getFluidState().isEmpty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.spawnDestroyParticles_(level, player, pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void spawnDestroyParticles_(Level level, Player player, int x, int y, int z, BlockState state) {
        level.levelEvent_(player, LevelEvent.PARTICLES_DESTROY_BLOCK, x, y, z, getId(state));
    }

    @Override
    public BlockState stateForParticles(BlockState state, Level level, int x, int y, int z) {
        return state;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        Evolution.deprecatedMethod();
        this.stepOn_(level, pos.getX(), pos.getY(), pos.getZ(), state, entity);
    }

    @Override
    public void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x, 0, velocity.z);
    }
}
