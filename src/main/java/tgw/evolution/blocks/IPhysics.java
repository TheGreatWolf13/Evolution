package tgw.evolution.blocks;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Represents any block that has physics, from reacting to gravity, to popping, to structural integrity
 */
public interface IPhysics {

    /**
     * @return Whether the main physics branch should return.
     */
    default boolean fallLogic(Level level, BlockPos pos) {
        return false;
    }

    default boolean fallable() {
        return false;
    }

    @Nullable SoundEvent fallingSound();

    double getMass(Level level, BlockPos pos, BlockState state);

    default BlockState getStateForPhysicsChange(BlockState state) {
        return state;
    }

    /**
     * @return Whether the main physics branch should return.
     */
    default boolean popLogic(Level level, BlockPos pos) {
        return false;
    }

    default boolean pops() {
        return false;
    }

    /**
     * @return Whether the main physics branch should return.
     */
    @CanIgnoreReturnValue
    default boolean slopeLogic(Level level, BlockPos pos) {
        return false;
    }

    default boolean slopes() {
        return false;
    }
}
