package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

public interface LegacyHMVillager<T extends Entity> extends HMHierarchical<T> {

    HM head();

    HM legL();

    HM legR();

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HM head = this.head();
        HM legR = this.legR();
        HM legL = this.legL();
        boolean unhappy = false;
        if (entity instanceof AbstractVillager villager) {
            unhappy = villager.getUnhappyCounter() > 0;
        }
        head.setRotationY(netHeadYaw * Mth.DEG_TO_RAD);
        head.setRotationX(headPitch * Mth.DEG_TO_RAD);
        if (unhappy) {
            head.setRotationZ(0.3F * Mth.sin(0.45F * ageInTicks));
            head.setRotationX(-0.4F);
        }
        else {
            head.setRotationZ(0.0F);
        }
        float legRot = 0.5F * limbSwingAmount * Mth.cos(limbSwing * 0.5F);
        legR.setRotationX(legRot);
        legL.setRotationX(-legRot);
        legR.setRotationY(0.0F);
        legL.setRotationY(0.0F);
    }
}
