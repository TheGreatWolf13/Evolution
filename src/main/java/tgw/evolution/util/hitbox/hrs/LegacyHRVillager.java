package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.npc.Villager;
import tgw.evolution.util.hitbox.hms.LegacyHMVillager;

public interface LegacyHRVillager extends HRMob<Villager, LegacyHMVillager<Villager>> {

    @Override
    default void setScale(Villager entity, HR hr, float partialTicks) {
        float f = 0.937_5F;
        if (entity.isBaby()) {
            f *= 0.5F;
            this.setShadowRadius(0.25F);
        }
        else {
            this.setShadowRadius(0.5F);
        }
        hr.scaleHR(f, f, f);
    }
}
