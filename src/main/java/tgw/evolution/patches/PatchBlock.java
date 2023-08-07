package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.HarvestLevel;

public interface PatchBlock {

    default void destroy_(LevelAccessor level, int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, int x, int y, int z, Player player) {
        throw new AbstractMethodError();
    }

    /**
     * Common Values: <br>
     * 0.55 = Sand; <br>
     * 0.57 = Gravel; <br>
     * 0.60 = Clay, Glass, Grass; <br>
     * 0.61 = Dry Grass; <br>
     * 0.63 = Dirt; <br>
     * 0.70 = Wood; <br>
     * 0.73 = MAXIMUM WALK SPEED; <br>
     * 0.80 = Stone; <br>
     * 0.95 = MAXIMUM SPRINT SPEED; <br>
     */
    default float getFrictionCoefficient(BlockState state) {
        throw new AbstractMethodError();
    }

    default @HarvestLevel int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default @Nullable BlockState getStateForPlacement_(Level level,
                                                       int x,
                                                       int y,
                                                       int z,
                                                       Player player,
                                                       InteractionHand hand,
                                                       BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }

    default boolean isFireSource(BlockState state, LevelReader level, int x, int y, int z, Direction side) {
        return false;
    }

    default boolean isLadder(BlockState state, LevelReader level, int x, int y, int z, LivingEntity entity) {
        return state.is(BlockTags.CLIMBABLE);
    }

    default void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        throw new AbstractMethodError();
    }

    default void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        throw new AbstractMethodError();
    }

    default boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void setPlacedBy_(Level level, int x, int y, int z, BlockState stateAtPos, Player player, ItemStack stack) {
    }

    default boolean shouldCull(BlockGetter level,
                               BlockState state,
                               int x,
                               int y,
                               int z,
                               BlockState adjacentState,
                               int adjX,
                               int adjY,
                               int adjZ,
                               Direction face) {
        return state.skipRendering(adjacentState, face);
    }

    default void spawnDestroyParticles_(Level level, Player player, int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
        throw new AbstractMethodError();
    }
}
