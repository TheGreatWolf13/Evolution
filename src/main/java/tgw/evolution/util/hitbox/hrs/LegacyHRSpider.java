package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.monster.Spider;
import tgw.evolution.util.hitbox.hms.LegacyHMSpider;

public interface LegacyHRSpider<T extends Spider> extends HRMob<T, LegacyHMSpider<T>> {

    @Override
    default void setScale(T entity, HR hr, float partialTicks) {
        hr.translateHR(0, 0, 3 / 16.0f);
    }
}
