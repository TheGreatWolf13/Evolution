package tgw.evolution.util.hitbox.hms;

import net.minecraft.world.entity.Entity;

public interface HMEntity<T extends Entity> {

    float attackTime();

    default void prepare(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {

    }

    boolean riding();

    void setAttackTime(float attackTime);

    void setRiding(boolean riding);

    void setYoung(boolean young);

    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    boolean young();
}
