package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

/**
 * Represents a block that slopes, that is, can "fall" sideways.
 */
public interface ISloppable extends IFallable {

    ThreadLocal<DirectionList> DIRECTION_LIST = ThreadLocal.withInitial(DirectionList::new);
    ThreadLocal<AABBMutable> TEST_BB = ThreadLocal.withInitial(AABBMutable::new);

    boolean canSlope(BlockGetter level, BlockPos pos);

    boolean canSlopeFail();

    default void slope(Level level, BlockPos pos, Direction offset) {
        EntityFallingWeight entity = new EntityFallingWeight(level, pos.getX() + offset.getStepX() + 0.5, pos.getY(),
                                                             pos.getZ() + offset.getStepZ() + 0.5,
                                                             this.getStateForPhysicsChange(level.getBlockState(pos)), pos);
        level.removeBlock(pos, true);
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
    default boolean slopeLogic(Level level, BlockPos pos) {
        if (!this.canSlope(level, pos)) {
            BlockUtils.scheduleBlockTick(level, pos.getX(), pos.getY() - 1, pos.getZ());
            return true;
        }
        if (level.random.nextFloat() < this.slopeChance()) {
            DirectionList slopePossibility = DIRECTION_LIST.get();
            slopePossibility.clear();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            for (Direction slopeDirection : DirectionUtil.HORIZ_NESW) {
                if (BlockUtils.isReplaceable(BlockUtils.getBlockStateAtSide(level, x, y, z, slopeDirection))) {
                    if (BlockUtils.isReplaceable(BlockUtils.getBlockStateAtSide(level, x, y - 1, z, slopeDirection))) {
                        slopePossibility.add(slopeDirection);
                    }
                }
            }
            AABBMutable testBB = TEST_BB.get();
            while (!slopePossibility.isEmpty()) {
                Direction slopeDirection = slopePossibility.getRandomAndRemove(level.random);
                int x0 = x + slopeDirection.getStepX();
                int z0 = z + slopeDirection.getStepZ();
                if (level.getEntitiesOfClass(EntityFallingWeight.class, testBB.setUnchecked(x0, y, z0, x0 + 1, y + 1, z0 + 1)).isEmpty()) {
                    this.slope(level, pos, slopeDirection);
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
