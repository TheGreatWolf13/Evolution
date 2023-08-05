package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.blocks.util.BlockUtils;
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

    boolean canSlopeFail();

    default void slope(Level level, int x, int y, int z, Direction offset) {
        BlockState stateForPhysicsChange = this.getStateForPhysicsChange(level.getBlockState_(x, y, z));
        EntityFallingWeight entity = new EntityFallingWeight(level, x + offset.getStepX() + 0.5, y, z + offset.getStepZ() + 0.5,
                                                             stateForPhysicsChange,
                                                             stateForPhysicsChange.getBlock() instanceof IPhysics physics ?
                                                             physics.getMass(level, x, y, z, stateForPhysicsChange) :
                                                             500);
        level.removeBlock(new BlockPos(x, y, z), true);
        level.addFreshEntity(entity);
        SoundEvent soundEvent = this.fallingSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 0.125F, 1.0F);
        }
//        BlockUtils.scheduleBlockTick(level, pos.below(), 2);
//        BlockPos up = pos.above();
//        BlockUtils.scheduleBlockTick(level, up, 2);
//        for (Direction dir : DirectionUtil.HORIZ_NESW) {
//            BlockUtils.scheduleBlockTick(level, up.relative(dir), 2);
//        }
    }

    float slopeChance();

    @Override
    default boolean slopeLogic(Level level, int x, int y, int z) {
        if (!this.canSlope(level, x, y, z)) {
            BlockUtils.scheduleBlockTick(level, x, y - 1, z);
            return true;
        }
        if (level.random.nextFloat() < this.slopeChance()) {
            int slopePossibility = 0;
            for (Direction slopeDirection : DirectionUtil.HORIZ_NESW) {
                if (BlockUtils.isReplaceable(level.getBlockStateAtSide(x, y, z, slopeDirection))) {
                    if (BlockUtils.isReplaceable(level.getBlockStateAtSide(x, y - 1, z, slopeDirection))) {
                        slopePossibility = DirectionList.add(slopePossibility, slopeDirection);
                    }
                }
            }
            AABBMutable testBB = TEST_BB.get();
            while (!DirectionList.isEmpty(slopePossibility)) {
                int index = DirectionList.getRandom(slopePossibility, level.random);
                Direction slopeDirection = DirectionList.get(slopePossibility, index);
                slopePossibility = DirectionList.remove(slopePossibility, index);
                int x0 = x + slopeDirection.getStepX();
                int z0 = z + slopeDirection.getStepZ();
                //TODO this whole piece of code should probably run on the update itself to avoid this check below
                if (level.getEntitiesOfClass(EntityFallingWeight.class, testBB.setUnchecked(x0, y, z0, x0 + 1, y + 1, z0 + 1)).isEmpty()) {
                    this.slope(level, x, y, z, slopeDirection);
                    return true;
                }
                if (this.canSlopeFail()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    default boolean slopes() {
        return true;
    }
}
