package tgw.evolution.util.hitbox.hrs;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;
import tgw.evolution.util.hitbox.hms.HMCreeper;

public interface HRCreeper extends HRMob<Creeper, HMCreeper<Creeper>> {

    @Override
    default void setScale(Creeper entity, HR hr, float partialTicks) {
        float swelling = entity.getSwelling(partialTicks);
        float f1 = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float scaleHor = (1.0F + swelling * 0.4F) * f1;
        float scaleY = (1.0F + swelling * 0.1F) / f1;
        hr.scaleHR(scaleHor, scaleY, scaleHor);
    }
}
