package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import tgw.evolution.util.hitbox.hms.HMSkeleton;

public interface HRSkeleton extends HRHumanoid<AbstractSkeleton, HMSkeleton<AbstractSkeleton>> {

    @Override
    default boolean shaking(AbstractSkeleton entity) {
        return entity.isShaking();
    }
}
