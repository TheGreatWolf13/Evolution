package tgw.evolution.util.hitbox.hms;

import net.minecraft.world.entity.monster.Monster;

public interface HMAbstractZombie<T extends Monster> extends HMHumanoid<T> {

    boolean aggresive(T entity);

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HMHumanoid.super.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AnimationUtils.animateZombieArms(this.armL(), this.armR(), this.aggresive(entity), this.attackTime(), ageInTicks);
    }
}
