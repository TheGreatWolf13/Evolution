package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.entities.misc.EntityFallingWeight;

/**
 * Represents a block that needs to be supported, or else it falls.
 */
public interface IFallable extends IPhysics {

    default void fall(Level level, int x, int y, int z) {
        level.destroyBlock_(x, y - 1, z, true, null);
        BlockState stateForPhysicsChange = this.getStateForPhysicsChange(level.getBlockState_(x, y, z));
        EntityFallingWeight entity = new EntityFallingWeight(level, x + 0.5, y, z + 0.5, stateForPhysicsChange, 500);
        level.removeBlock_(x, y, z, true);
        level.addFreshEntity(entity);
        SoundEvent soundEvent = this.fallingSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 0.125F, 1.0F);
        }
    }

    @Nullable SoundEvent fallingSound();
}
