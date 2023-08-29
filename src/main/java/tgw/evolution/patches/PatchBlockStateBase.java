package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

public interface PatchBlockStateBase {

    default void attack_(Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        throw new AbstractMethodError();
    }

    default boolean canBeReplaced_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }

    default boolean canSurvive_(LevelReader level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean emissiveRendering_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getBlockSupportShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getCollisionShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getCollisionShape_(BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default float getDestroyProgress_(Player player, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getDestroySpeed_() {
        throw new AbstractMethodError();
    }

    default OList<ItemStack> getDrops(ServerLevel level,
                                      int x,
                                      int y,
                                      int z,
                                      ItemStack tool,
                                      @Nullable BlockEntity tile,
                                      @Nullable Entity entity,
                                      Random random) {
        throw new AbstractMethodError();
    }

    default VoxelShape getFaceOcclusionShape_(BlockGetter level, int x, int y, int z, Direction face) {
        throw new AbstractMethodError();
    }

    default VoxelShape getInteractionShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getLightBlock_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getOcclusionShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default long getSeed_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getShadeBrightness_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getShape_(BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default VoxelShape getShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getVisualShape_(BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default boolean hasPostProcess_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isCollisionShapeFullBlock_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isFaceSturdy_(BlockGetter level, int x, int y, int z, Direction side, SupportType support) {
        throw new AbstractMethodError();
    }

    default boolean isFaceSturdy_(BlockGetter level, int x, int y, int z, Direction side) {
        throw new AbstractMethodError();
    }

    default boolean isRedstoneConductor_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isSolidRender_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isSuffocating_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isValidSpawn_(BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        throw new AbstractMethodError();
    }

    default boolean isViewBlocking_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void neighborChanged_(Level level, int x, int y, int z, Block block, int fromX, int fromY, int fromZ, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void onPlace_(Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default void onRemove_(Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        throw new AbstractMethodError();
    }

    default boolean propagatesSkylightDown_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void randomTick_(ServerLevel level, int x, int y, int z, Random random) {
        throw new AbstractMethodError();
    }

    default void spawnAfterBreak_(ServerLevel level, int x, int y, int z, ItemStack stack) {
        throw new AbstractMethodError();
    }

    default BlockState stateForParticles(Level level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void tick_(ServerLevel level, int x, int y, int z, Random random) {
        throw new AbstractMethodError();
    }

    default void updateIndirectNeighbourShapes_(LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        throw new AbstractMethodError();
    }

    default void updateNeighbourShapes_(LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        throw new AbstractMethodError();
    }

    default BlockState updateShape_(Direction from,
                                    BlockState fromState,
                                    LevelAccessor level,
                                    int x,
                                    int y,
                                    int z,
                                    int fromX,
                                    int fromY,
                                    int fromZ) {
        throw new AbstractMethodError();
    }
}
