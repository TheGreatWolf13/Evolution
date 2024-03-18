package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.patches.PatchBlockBehaviour;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(BlockBehaviour.class)
public abstract class MixinBlockBehaviour implements PatchBlockBehaviour {

    @Shadow @Final protected boolean hasCollision;
    @Shadow @Final protected Material material;

    @Shadow
    public abstract Item asItem();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        Evolution.deprecatedMethod();
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeReplaced_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (this.material.isReplaceable()) {
            ItemStack stack = player.getItemInHand(hand);
            return stack.isEmpty() || !stack.is(this.asItem());
        }
        return false;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.canSurvive_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return true;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockSupportShape_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getBlockSupportShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return this.getCollisionShape_(state, level, x, y, z, null);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getCollisionShape_(state, level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.hasCollision ? state.getShape_(level, x, y, z) : Shapes.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getDestroyProgress_(state, player, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getDestroyProgress_(BlockState state, Player player, BlockGetter level, int x, int y, int z) {
        float speed = state.getDestroySpeed_();
        if (speed < 0) {
            return 0.0F;
        }
        int mult = player.hasCorrectToolForDrops(state) ? 30 : 100;
        return player.getDestroySpeed(state, x, y, z) / speed / mult;
    }

    @Override
    public int getEmissiveLightColor(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        return DynamicLights.FULL_LIGHTMAP;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getInteractionShape_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Shapes.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getLightBlock_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getLightBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        if (state.isSolidRender_(level, x, y, z)) {
            return level.getMaxLightLevel();
        }
        return state.propagatesSkylightDown_(level, x, y, z) ? 0 : 1;
    }

    @Shadow
    public abstract float getMaxHorizontalOffset();

    @Shadow
    public abstract float getMaxVerticalOffset();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getOcclusionShape_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getOcclusionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return state.getShape_(level, x, y, z);
    }

    @Shadow
    public abstract BlockBehaviour.OffsetType getOffsetType();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public long getSeed(BlockState state, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getSeed_(state, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public long getSeed_(BlockState state, int x, int y, int z) {
        return Mth.getSeed(x, y, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShadeBrightness_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getShadeBrightness_(BlockState state, BlockGetter level, int x, int y, int z) {
        return state.isCollisionShapeFullBlock_(level, x, y, z) ? 0.2F : 1.0F;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getShape_(state, level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return Shapes.block();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Deprecated
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        Evolution.deprecatedMethod();
        return this.getSignal_(state, level, pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    @Override
    public int getSignal_(BlockState state, BlockGetter level, int x, int y, int z, Direction dir) {
        return 0;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.getVisualShape_(state, level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    public VoxelShape getVisualShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.getCollisionShape_(state, level, x, y, z, entity);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isCollisionShapeFullBlock_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isCollisionShapeFullBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Block.isShapeFullBlock(state.getCollisionShape_(level, x, y, z));
    }

    @Override
    public VoxelShape moveShapeByOffset(VoxelShape shape, int x, int z) {
        BlockBehaviour.OffsetType offsetType = this.getOffsetType();
        if (offsetType == BlockBehaviour.OffsetType.NONE) {
            return shape;
        }
        long seed = Mth.getSeed(x, 0, z);
        float horizOffset = this.getMaxHorizontalOffset();
        double d = Mth.clamp(((seed & 15L) / 15.0F - 0.5) * 0.5, -horizOffset, horizOffset);
        double e = offsetType == BlockBehaviour.OffsetType.XYZ ? ((seed >> 4 & 15L) / 15.0F - 1.0) * this.getMaxVerticalOffset() : 0;
        double g = Mth.clamp(((seed >> 8 & 15L) / 15.0F - 0.5) * 0.5, -horizOffset, horizOffset);
        return shape.move(d, e, g);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block oldBlock, BlockPos fromPos, boolean bl) {
        Evolution.deprecatedMethod();
        this.neighborChanged_(state, level, pos.getX(), pos.getY(), pos.getZ(), oldBlock, fromPos.getX(), fromPos.getY(), fromPos.getZ(), bl);
    }

    @Override
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        Evolution.deprecatedMethod();
        this.onPlace_(state, level, pos.getX(), pos.getY(), pos.getZ(), oldState, isMoving);
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Evolution.deprecatedMethod();
        this.onRemove_(state, level, pos.getX(), pos.getY(), pos.getZ(), newState, isMoving);
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            level.removeBlockEntity_(x, y, z);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.randomTick_(state, level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        this.tick_(state, level, x, y, z, random);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack) {
        Evolution.deprecatedMethod();
        this.spawnAfterBreak_(state, level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public void spawnAfterBreak_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        Evolution.deprecatedMethod();
        this.tick_(state, level, pos.getX(), pos.getY(), pos.getZ(), random);
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
    }

    @Override
    public void translateByOffset(PoseStack matrices, int x, int z) {
        BlockBehaviour.OffsetType offsetType = this.getOffsetType();
        if (offsetType == BlockBehaviour.OffsetType.NONE) {
            return;
        }
        long seed = Mth.getSeed(x, 0, z);
        float horizOffset = this.getMaxHorizontalOffset();
        double d = Mth.clamp(((seed & 15L) / 15.0F - 0.5) * 0.5, -horizOffset, horizOffset);
        double e = offsetType == BlockBehaviour.OffsetType.XYZ ? ((seed >> 4 & 15L) / 15.0F - 1.0) * this.getMaxVerticalOffset() : 0;
        double g = Mth.clamp(((seed >> 8 & 15L) / 15.0F - 0.5) * 0.5, -horizOffset, horizOffset);
        matrices.translate(d, e, g);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, @BlockFlags int flags, int limit) {
        Evolution.deprecatedMethod();
        this.updateIndirectNeighbourShapes_(state, level, pos.getX(), pos.getY(), pos.getZ(), flags, limit);
    }

    @Override
    public void updateIndirectNeighbourShapes_(BlockState state, LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    //ok
    public BlockState updateShape(BlockState state, Direction from, BlockState fromState, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
        Evolution.deprecatedMethod();
        return this.updateShape_(state, from, fromState, level, pos.getX(), pos.getY(), pos.getZ(), fromPos.getX(), fromPos.getY(), fromPos.getZ());
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        return state;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Evolution.deprecatedMethod();
        return this.use_(state, level, pos.getX(), pos.getY(), pos.getZ(), player, hand, hitResult);
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }
}
