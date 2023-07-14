package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.misc.EntityFallingWeight;

/**
 * Represents a block that needs to be supported, or else it falls.
 */
public interface IFallable extends IPhysics {

    default void fall(Level level, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                                             this.getStateForPhysicsChange(level.getBlockState(pos)), pos);
        level.removeBlock(pos, true);
        level.addFreshEntity(entity);
        Evolution.info("Spawned entity for {}", pos);
        SoundEvent soundEvent = this.fallingSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 0.125F, 1.0F);
        }
        BlockPos up = pos.above();
//        BlockUtils.scheduleBlockTick(level, up, 2);
//        for (Direction dir : DirectionUtil.HORIZ_NESW) {
//            BlockUtils.scheduleBlockTick(level, up.relative(dir), 2);
//        }
    }

    @Override
    default boolean fallLogic(Level level, BlockPos pos) {
        Evolution.info("Fall Logic called at {}", pos);
        if (BlockUtils.isReplaceable(level.getBlockState_(pos.getX(), pos.getY() - 1, pos.getZ()))) {
            Evolution.info("Block below is replaceable, falling");
            this.fall(level, pos);
            return true;
        }
        return false;
    }

    @Override
    default boolean fallable() {
        return true;
    }
}
