package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;
import java.util.function.Consumer;

public interface PatchBlockBehaviour {

    default InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        throw new AbstractMethodError();
    }

    default boolean canBeReplaced_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }

    default boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        throw new AbstractMethodError();
    }

    default VoxelShape getBlockSupportShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default float getDestroyProgress_(BlockState state, Player player, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default BlockState getDestroyingState(BlockState state, Level level, int x, int y, int z, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        throw new AbstractMethodError();
    }

    default int getEmissiveLightColor(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getLightBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getOcclusionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default long getSeed_(BlockState state, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getShadeBrightness_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default int getSignal_(BlockState state, BlockGetter level, int x, int y, int z, Direction dir) {
        throw new AbstractMethodError();
    }

    default VoxelShape getVisualShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default boolean isCollisionShapeFullBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape moveShapeByOffset(VoxelShape shape, int x, int z) {
        throw new AbstractMethodError();
    }

    default void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        throw new AbstractMethodError();
    }

    default void spawnAfterBreak_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        throw new AbstractMethodError();
    }

    default void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        throw new AbstractMethodError();
    }

    default void translateByOffset(PoseStack matrices, int x, int z) {
        throw new AbstractMethodError();
    }

    default void updateIndirectNeighbourShapes_(BlockState state, LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        throw new AbstractMethodError();
    }

    default BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        throw new AbstractMethodError();
    }

    default boolean updatesSelf(BlockState state, Level level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }
}
