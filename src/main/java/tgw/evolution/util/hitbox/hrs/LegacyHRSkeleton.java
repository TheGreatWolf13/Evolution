package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import tgw.evolution.util.hitbox.hms.LegacyHMSkeleton;

public interface LegacyHRSkeleton extends HRHumanoid<AbstractSkeleton, LegacyHMSkeleton<AbstractSkeleton>> {

    @Override
    default boolean shaking(AbstractSkeleton entity) {
        return entity.isShaking();
    }
}
