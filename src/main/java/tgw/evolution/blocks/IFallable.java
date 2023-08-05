package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.misc.EntityFallingWeight;

/**
 * Represents a block that needs to be supported, or else it falls.
 */
public interface IFallable extends IPhysics {

    default void fall(Level level, int x, int y, int z) {
        BlockState stateForPhysicsChange = this.getStateForPhysicsChange(level.getBlockState_(x, y, z));
        EntityFallingWeight entity = new EntityFallingWeight(level, x + 0.5, y, z + 0.5,
                                                             stateForPhysicsChange,
                                                             stateForPhysicsChange.getBlock() instanceof IPhysics physics ?
                                                             physics.getMass(level, x, y, z, stateForPhysicsChange) :
                                                             500);
        level.removeBlock(new BlockPos(x, y, z), true);
        level.addFreshEntity(entity);
        Evolution.info("Spawned entity for [{}, {}, {}]", x, y, z);
        SoundEvent soundEvent = this.fallingSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 0.125F, 1.0F);
        }
//        BlockPos up = pos.above();
//        BlockUtils.scheduleBlockTick(level, up, 2);
//        for (Direction dir : DirectionUtil.HORIZ_NESW) {
//            BlockUtils.scheduleBlockTick(level, up.relative(dir), 2);
//        }
    }

    @Override
    default boolean fallLogic(Level level, int x, int y, int z) {
        Evolution.info("Fall Logic called at [{}, {}, {}]", x, y, z);
        if (BlockUtils.isReplaceable(level.getBlockState_(x, y - 1, z))) {
            Evolution.info("Block below is replaceable, falling");
            this.fall(level, x, y, z);
            return true;
        }
        return false;
    }

    @Override
    default boolean fallable() {
        return true;
    }
}
