package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.monster.CaveSpider;

public interface LegacyHRCaveSpider extends LegacyHRSpider<CaveSpider> {

    @Override
    default void setScale(CaveSpider entity, HR hr, float partialTicks) {
        hr.scaleHR(0.7F, 0.7F, 0.7F);
        LegacyHRSpider.super.setScale(entity, hr, partialTicks);
    }
}
