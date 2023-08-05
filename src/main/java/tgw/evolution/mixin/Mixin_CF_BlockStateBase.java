package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockStateBase;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.List;
import java.util.Random;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class Mixin_CF_BlockStateBase extends StateHolder<Block, BlockState> implements PatchBlockStateBase {

    @Shadow protected @Nullable BlockBehaviour.BlockStateBase.Cache cache;
    @Mutable @Shadow @Final @RestoreFinal private boolean canOcclude;
    @Mutable @Shadow @Final @RestoreFinal private float destroySpeed;
    @Shadow @Final @DeleteField private BlockBehaviour.StatePredicate emissiveRendering;
    @Shadow @Final @DeleteField private BlockBehaviour.StatePredicate hasPostProcess;
    @Mutable @Shadow @Final @RestoreFinal private boolean isAir;
    @Shadow @Final @DeleteField private BlockBehaviour.StatePredicate isRedstoneConductor;
    @Shadow @Final @DeleteField private BlockBehaviour.StatePredicate isSuffocating;
    @Shadow @Final @DeleteField private BlockBehaviour.StatePredicate isViewBlocking;
    @Mutable @Shadow @Final @RestoreFinal private int lightEmission;
    @Mutable @Shadow @Final @RestoreFinal private Material material;
    @Mutable @Shadow @Final @RestoreFinal private MaterialColor materialColor;
    @Mutable @Shadow @Final @RestoreFinal private boolean requiresCorrectToolForDrops;
    @Mutable @Shadow @Final @RestoreFinal private boolean useShapeForLightOcclusion;

    @ModifyConstructor
    protected Mixin_CF_BlockStateBase(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
        super(block, immutableMap, mapCodec);
        BlockBehaviour.Properties properties = block.properties;
        BlockState state = this.asState();
        this.lightEmission = properties.lightEmission.applyAsInt(state);
        this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(state);
        this.isAir = properties.isAir;
        this.material = properties.material;
        this.materialColor = properties.materialColor.apply(state);
        this.destroySpeed = properties.destroyTime;
        this.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops;
        this.canOcclude = properties.canOcclude;
    }

    @Shadow
    protected abstract BlockState asState();

    @Overwrite
    public void attack(Level level, BlockPos pos, Player player) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void attack_(Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        this.getBlock().attack_(this.asState(), level, x, y, z, face, hitX, hitY, hitZ, player);
    }

    @Override
    public boolean canBeReplaced_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.getBlock().canBeReplaced_(this.asState(), level, x, y, z, player, hand, hitResult);
    }

    @Overwrite
    public boolean canSurvive(LevelReader level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.canSurvive_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean canSurvive_(LevelReader level, int x, int y, int z) {
        return this.getBlock().canSurvive_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public boolean emissiveRendering(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.emissiveRendering_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean emissiveRendering_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().properties.emissiveRendering_().test(this.asState(), level, x, y, z);
    }

    @Shadow
    public abstract Block getBlock();

    @Overwrite
    public VoxelShape getBlockSupportShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockSupportShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getBlockSupportShape_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().getBlockSupportShape_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public VoxelShape getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getCollisionShape_(level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext e ? e.getEntity() : null);
    }

    @Overwrite
    public VoxelShape getCollisionShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getCollisionShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getCollisionShape_(BlockGetter level,
                                         int x,
                                         int y,
                                         int z,
                                         @Nullable Entity entity) {
        return this.getBlock().getCollisionShape_(this.asState(), level, x, y, z, entity);
    }

    @Override
    public VoxelShape getCollisionShape_(BlockGetter level, int x, int y, int z) {
        return this.cache != null ? this.cache.collisionShape : this.getCollisionShape_(level, x, y, z, null);
    }

    @Overwrite
    public float getDestroyProgress(Player player, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getDestroyProgress_(player, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getDestroyProgress_(Player player, BlockGetter level, int x, int y, int z) {
        return this.getBlock().getDestroyProgress_(this.asState(), player, level, x, y, z);
    }

    @Overwrite
    public float getDestroySpeed(BlockGetter blockGetter, BlockPos blockPos) {
        Evolution.deprecatedMethod();
        return this.destroySpeed;
    }

    @Override
    public float getDestroySpeed_() {
        return this.destroySpeed;
    }

    @Overwrite
    public List<ItemStack> getDrops(LootContext.Builder builder) {
        Evolution.deprecatedMethod();
        Vec3 origin = builder.getParameter(LootContextParams.ORIGIN);
        ServerLevel level = builder.getLevel();
        return this.getDrops(level, Mth.floor(origin.x), Mth.floor(origin.y), Mth.floor(origin.z),
                             builder.getParameter(LootContextParams.TOOL), builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY),
                             builder.getOptionalParameter(LootContextParams.THIS_ENTITY), level.random);
    }

    @Override
    public OList<ItemStack> getDrops(ServerLevel level,
                                     int x,
                                     int y,
                                     int z,
                                     ItemStack tool,
                                     @Nullable BlockEntity tile,
                                     @Nullable Entity entity,
                                     Random random) {
        return this.getBlock().getDrops(this.asState(), level, x, y, z, tool, tile, entity, random);
    }

    @Overwrite
    public VoxelShape getFaceOcclusionShape(BlockGetter level, BlockPos pos, Direction face) {
        Evolution.deprecatedMethod();
        return this.getFaceOcclusionShape_(level, pos.getX(), pos.getY(), pos.getZ(), face);
    }

    @Override
    public VoxelShape getFaceOcclusionShape_(BlockGetter level, int x, int y, int z, Direction face) {
        return this.cache != null && this.cache.occlusionShapes != null ?
               this.cache.occlusionShapes[face.ordinal()] :
               Shapes.getFaceShape(this.getOcclusionShape_(level, x, y, z), face);
    }

    @Overwrite
    public VoxelShape getInteractionShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getInteractionShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getInteractionShape_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().getInteractionShape_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public int getLightBlock(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getLightBlock_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getLightBlock_(BlockGetter level, int x, int y, int z) {
        return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public VoxelShape getOcclusionShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getOcclusionShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getOcclusionShape_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().getOcclusionShape_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public float getShadeBrightness(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShadeBrightness_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getShadeBrightness_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().getShadeBrightness_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public VoxelShape getShape(BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getShape_(level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Overwrite
    public VoxelShape getShape(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShape_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getShape_(BlockGetter level, int x, int y, int z) {
        return this.getShape_(level, x, y, z, null);
    }

    @Override
    public VoxelShape getShape_(BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.getBlock().getShape_(this.asState(), level, x, y, z, entity);
    }

    @Overwrite
    public VoxelShape getVisualShape(BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getVisualShape_(level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    public VoxelShape getVisualShape_(BlockGetter level,
                                      int x,
                                      int y,
                                      int z,
                                      @Nullable Entity entity) {
        return this.getBlock().getVisualShape_(this.asState(), level, x, y, z, entity);
    }

    @Overwrite
    public boolean hasLargeCollisionShape() {
        return this.cache == null || this.cache.largeCollisionShape();
    }

    @Overwrite
    public boolean hasPostProcess(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.hasPostProcess_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean hasPostProcess_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().properties.hasPostProcess_().test(this.asState(), level, x, y, z);
    }

    @Overwrite
    public boolean isCollisionShapeFullBlock(BlockGetter level, BlockPos pos) {
        return this.isCollisionShapeFullBlock_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isCollisionShapeFullBlock_(BlockGetter level, int x, int y, int z) {
        return this.cache != null ?
               this.cache.isCollisionShapeFullBlock() :
               this.getBlock().isCollisionShapeFullBlock_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public boolean isFaceSturdy(BlockGetter level, BlockPos pos, Direction side, SupportType support) {
        Evolution.deprecatedMethod();
        return this.isFaceSturdy_(level, pos.getX(), pos.getY(), pos.getZ(), side, support);
    }

    @Overwrite
    public boolean isFaceSturdy(BlockGetter level, BlockPos pos, Direction side) {
        Evolution.deprecatedMethod();
        return this.isFaceSturdy_(level, pos.getX(), pos.getY(), pos.getZ(), side);
    }

    @Override
    public boolean isFaceSturdy_(BlockGetter level, int x, int y, int z, Direction side, SupportType support) {
        return this.cache != null ? this.cache.isFaceSturdy(side, support) : support.isSupporting_(this.asState(), level, x, y, z, side);
    }

    @Override
    public boolean isFaceSturdy_(BlockGetter level, int x, int y, int z, Direction side) {
        return this.isFaceSturdy_(level, x, y, z, side, SupportType.FULL);
    }

    @Overwrite
    public boolean isRedstoneConductor(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isRedstoneConductor_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isRedstoneConductor_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().properties.isRedstoneConductor_().test(this.asState(), level, x, y, z);
    }

    @Overwrite
    public boolean isSolidRender(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isSolidRender_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isSolidRender_(BlockGetter level, int x, int y, int z) {
        if (this.cache != null) {
            return this.cache.solidRender();
        }
        BlockState state = this.asState();
        return state.canOcclude() && Block.isShapeFullBlock(state.getOcclusionShape_(level, x, y, z));
    }

    @Overwrite
    public boolean isSuffocating(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isSuffocating_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isSuffocating_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().properties.isSuffocating_().test(this.asState(), level, x, y, z);
    }

    @Overwrite
    public boolean isValidSpawn(BlockGetter level, BlockPos pos, EntityType<?> entity) {
        Evolution.deprecatedMethod();
        return this.isValidSpawn_(level, pos.getX(), pos.getY(), pos.getZ(), entity);
    }

    @Override
    public boolean isValidSpawn_(BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return this.getBlock().properties.isValidSpawn_().test(this.asState(), level, x, y, z, entity);
    }

    @Overwrite
    public boolean isViewBlocking(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isViewBlocking_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isViewBlocking_(BlockGetter level, int x, int y, int z) {
        return this.getBlock().properties.isViewBlocking_().test(this.asState(), level, x, y, z);
    }

    @Overwrite
    public void neighborChanged(Level level, BlockPos pos, Block block, BlockPos fromPos, boolean bl) {
        Evolution.deprecatedMethod();
        this.neighborChanged_(level, pos.getX(), pos.getY(), pos.getZ(), block, fromPos.getX(), fromPos.getY(), fromPos.getZ(), bl);
    }

    @Override
    public void neighborChanged_(Level level, int x, int y, int z, Block block, int fromX, int fromY, int fromZ, boolean isMoving) {
        this.getBlock().neighborChanged_(this.asState(), level, x, y, z, block, fromX, fromY, fromZ, isMoving);
    }

    @Overwrite
    public void onPlace(Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        Evolution.deprecatedMethod();
        this.onPlace_(level, pos.getX(), pos.getY(), pos.getZ(), oldState, isMoving);
    }

    @Override
    public void onPlace_(Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        this.getBlock().onPlace_(this.asState(), level, x, y, z, oldState, isMoving);
    }

    @Overwrite
    public void onRemove(Level level, BlockPos pos, BlockState state, boolean isMoving) {
        Evolution.deprecatedMethod();
        this.onRemove_(level, pos.getX(), pos.getY(), pos.getZ(), state, isMoving);
    }

    @Override
    public void onRemove_(Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        this.getBlock().onRemove_(this.asState(), level, x, y, z, newState, isMoving);
    }

    @Overwrite
    public boolean propagatesSkylightDown(BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.propagatesSkylightDown_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean propagatesSkylightDown_(BlockGetter level, int x, int y, int z) {
        return this.cache != null ? this.cache.propagatesSkylightDown() : this.getBlock().propagatesSkylightDown_(this.asState(), level, x, y, z);
    }

    @Overwrite
    public void randomTick(ServerLevel level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.randomTick_(level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void randomTick_(ServerLevel level, int x, int y, int z, Random random) {
        this.getBlock().randomTick_(this.asState(), level, x, y, z, random);
    }

    @Overwrite
    public void spawnAfterBreak(ServerLevel level, BlockPos pos, ItemStack stack) {
        Evolution.deprecatedMethod();
        this.spawnAfterBreak_(level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public void spawnAfterBreak_(ServerLevel level, int x, int y, int z, ItemStack stack) {
        this.getBlock().spawnAfterBreak_(this.asState(), level, x, y, z, stack);
    }

    @Overwrite
    public void tick(ServerLevel level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.tick_(level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void tick_(ServerLevel level, int x, int y, int z, Random random) {
        this.getBlock().tick_(this.asState(), level, x, y, z, random);
    }

    @Overwrite
    public void updateIndirectNeighbourShapes(LevelAccessor level, BlockPos pos, @BlockFlags int flags, int limit) {
        Evolution.deprecatedMethod();
        this.updateIndirectNeighbourShapes_(level, pos.getX(), pos.getY(), pos.getZ(), flags, limit);
    }

    @Override
    public void updateIndirectNeighbourShapes_(LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        this.getBlock().updateIndirectNeighbourShapes_(this.asState(), level, x, y, z, flags, limit);
    }

    @Overwrite
    public final void updateNeighbourShapes(LevelAccessor level, BlockPos pos, int flags, int limit) {
        Evolution.deprecatedMethod();
        this.updateNeighbourShapes_(level, pos.getX(), pos.getY(), pos.getZ(), flags, limit);
    }

    @Override
    public void updateNeighbourShapes_(LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        for (Direction dir : DirectionUtil.UPDATE_ORDER) {
            int offX = x + dir.getStepX();
            int offY = y + dir.getStepY();
            int offZ = z + dir.getStepZ();
            BlockState stateAtSide = level.getBlockState_(offX, offY, offZ);
            BlockState updatedAtSide = stateAtSide.updateShape_(dir.getOpposite(), this.asState(), level, offX, offY, offZ, x, y, z);
            BlockUtils.updateOrDestroy(stateAtSide, updatedAtSide, level, offX, offY, offZ, flags, limit);
        }
    }

    @Overwrite
    public BlockState updateShape(Direction from, BlockState fromState, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
        Evolution.deprecatedMethod();
        return this.updateShape_(from, fromState, level, pos.getX(), pos.getY(), pos.getZ(), fromPos.getX(), fromPos.getY(), fromPos.getZ());
    }

    @Override
    public BlockState updateShape_(Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        return this.getBlock().updateShape_(this.asState(), from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Overwrite
    public InteractionResult use(Level level, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.getBlock().use_(this.asState(), level, hitResult.posX(), hitResult.posY(), hitResult.posZ(), player, hand, hitResult);
    }
}
