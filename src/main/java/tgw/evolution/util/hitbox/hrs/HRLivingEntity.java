package tgw.evolution.util.hitbox.hrs;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.util.hitbox.hms.HMEntity;

public interface HRLivingEntity<T extends LivingEntity, M extends HMEntity<T>> extends HREntity<T> {

    private static float sleepDirectionToRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    default float attackAnim(T entity, float partialTicks) {
        return entity.getAttackAnim(partialTicks);
    }

    default float bob(T entity, float partialTicks) {
        return entity.tickCount + partialTicks;
    }

    default float flipDegrees(T entity) {
        return 90.0f;
    }

    M model();

    default void renderOrInit(T entity, HR hr, float partialTicks) {
        this.model().setAttackTime(this.attackAnim(entity, partialTicks));
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        this.model().setRiding(shouldSit);
        this.model().setYoung(entity.isBaby());
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity livingVehicle) {
            bodyYaw = Mth.rotLerp(partialTicks, livingVehicle.yBodyRotO, livingVehicle.yBodyRot);
            netHeadYaw = headYaw - bodyYaw;
            float v = Mth.wrapDegrees(netHeadYaw);
            if (v < -85.0F) {
                v = -85.0F;
            }
            if (v >= 85.0F) {
                v = 85.0F;
            }
            bodyYaw = headYaw - v;
            if (v * v > 2_500.0F) {
                bodyYaw += v * 0.2F;
            }
            netHeadYaw = headYaw - bodyYaw;
        }
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                hr.translateHR(-direction.getStepX() * f4, 0.0f, -direction.getStepZ() * f4);
            }
        }
        float ageInTicks = this.bob(entity, partialTicks);
        this.rotations(entity, hr, ageInTicks, bodyYaw, partialTicks);
        this.setScale(entity, hr, partialTicks);
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = Mth.lerp(partialTicks, entity.animationSpeedOld, entity.animationSpeed);
            limbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTicks);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }
            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        this.setLimbSwing(limbSwing);
        this.setLimbSwingAmount(limbSwingAmount);
        this.setAgeInTicks(ageInTicks);
        this.setNetHeadYaw(-netHeadYaw);
        this.setHeadPitch(-headPitch);
        this.model().prepare(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model().setup(entity, limbSwing, limbSwingAmount, ageInTicks, -netHeadYaw, -headPitch);
    }

    default void rotations(T entity, HR hr, float ageInTicks, float rotationYaw, float partialTicks) {
        if (this.shaking(entity)) {
            rotationYaw += Mth.cos(entity.tickCount * 3.25f) * Mth.PI * 0.4F;
        }
        Pose pose = entity.getPose();
        if (pose != Pose.SLEEPING) {
            hr.rotateYHR(180.0F - rotationYaw);
        }
        if (entity.deathTime > 0) {
            float f = (entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            hr.rotateZHR(f * this.flipDegrees(entity));
        }
        else if (entity.isAutoSpinAttack()) {
            hr.rotateXHR(-90.0F - entity.getXRot());
            hr.rotateYHR((entity.tickCount + partialTicks) * -75.0F);
        }
        else if (pose == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            float f1 = direction != null ? sleepDirectionToRotation(direction) : rotationYaw;
            hr.rotateYHR(f1);
            hr.rotateZHR(this.flipDegrees(entity));
            hr.rotateYHR(270.0F);
        }
    }

    void setAgeInTicks(float ageInTicks);

    default void setHeadPitch(float headPitch) {
    }

    default void setLimbSwing(float limbSwing) {
    }

    default void setLimbSwingAmount(float limbSwingAmount) {
    }

    default void setNetHeadYaw(float netHeadYaw) {
    }

    default void setScale(T entity, HR hr, float partialTicks) {
    }

    default boolean shaking(T entity) {
        return entity.isFullyFrozen() || entity.hasEffect(EvolutionEffects.SHIVERING.get());
    }
}
