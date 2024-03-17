package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

/**
 * Represents a block that slopes, that is, can "fall" sideways.
 */
public interface ISloppable extends IFallable {

    ThreadLocal<AABBMutable> TEST_BB = ThreadLocal.withInitial(AABBMutable::new);

    boolean canSlope(BlockGetter level, int x, int y, int z);

    default void slope(Level level, int x, int y, int z, Direction offset) {
        BlockState stateForPhysicsChange = this.getStateForPhysicsChange(level.getBlockState_(x, y, z));
        EntityFallingWeight entity = new EntityFallingWeight(level, x + offset.getStepX() + 0.5, y, z + offset.getStepZ() + 0.5, stateForPhysicsChange, 500);
        level.removeBlock_(x, y, z, true);
        level.addFreshEntity(entity);
        SoundEvent soundEvent = this.fallingSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 0.125F, 1.0F);
        }
    }

    float slopeChance();

    /**
     * Returns {@code true} whether the block slopped, {@code false} otherwise.
     */
    default boolean slopeLogic(Level level, int x, int y, int z) {
        if (!this.canSlope(level, x, y, z)) {
            return false;
        }
        if (level.random.nextFloat() < this.slopeChance()) {
            int slopePossibility = 0;
            for (Direction slopeDirection : DirectionUtil.HORIZ_NESW) {
                if (!(level.getBlockStateAtSide(x, y, z, slopeDirection).getBlock() instanceof IFillable)) {
                    if (!(level.getBlockStateAtSide(x, y - 1, z, slopeDirection).getBlock() instanceof IFillable)) {
                        slopePossibility = DirectionList.add(slopePossibility, slopeDirection);
                    }
                }
            }
            AABBMutable testBB = TEST_BB.get();
            boolean tried = false;
            while (!DirectionList.isEmpty(slopePossibility)) {
                tried = true;
                int index = DirectionList.getRandom(slopePossibility, level.random);
                Direction slopeDirection = DirectionList.get(slopePossibility, index);
                slopePossibility = DirectionList.remove(slopePossibility, index);
                int x0 = x + slopeDirection.getStepX();
                int z0 = z + slopeDirection.getStepZ();
                //This appears to be the most performant way to do this. I tried using a Phantom Block, but it was a bit finicky and was worst for performance.
                if (level.getEntitiesOfClass(EntityFallingWeight.class, testBB.setUnchecked(x0, y, z0, x0 + 1, y + 1, z0 + 1)).isEmpty()) {
                    this.slope(level, x, y, z, slopeDirection);
                    return true;
                }
            }
            if (tried) {
                LevelChunk chunk = level.getChunkAt_(x, z);
                chunk.getChunkStorage().scheduleIntegrityTick(chunk, x & 15, y, z & 15, true);
            }
        }
        return false;
    }
}
