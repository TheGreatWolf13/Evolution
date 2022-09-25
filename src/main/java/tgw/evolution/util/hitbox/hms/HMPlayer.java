package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.Evolution;

public interface HMPlayer<T extends LivingEntity> extends HMHumanoid<T> {

    HM cloak();

    HM ear();

    HM jacket();

    HM leftPants();

    HM leftSleeve();

    HM rightPants();

    HM rightSleeve();

    void setAllVisible(boolean visible);

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HMHumanoid.super.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.leftPants().copy(this.leftLeg());
        this.rightPants().copy(this.rightLeg());
        this.leftSleeve().copy(this.leftArm());
        this.rightSleeve().copy(this.rightArm());
        this.jacket().copy(this.body());
        if (entity instanceof Player player) {
            this.cloak().setRotationY(Mth.DEG_TO_RAD * 180);
            if (entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                if (this.crouching()) {
                    this.cloak().setPivotZ(3.125F - 0.5f);
                    this.cloak().setPivotY(24.0F - 1.85f);
                }
                else {
                    this.cloak().setPivotZ(3.125F);
                    this.cloak().setPivotY(24.0F);
                }
            }
            else if (this.crouching()) {
                this.cloak().setPivotZ(3.125f - 0.2F);
                this.cloak().setPivotY(24.0f - 0.8F);
            }
            else {
                this.cloak().setPivotZ(3.125F + 0.7f);
                this.cloak().setPivotY(24.0F + 0.85f);
            }
            float partialTicks = Evolution.PROXY.getPartialTicks();
            double dx = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, entity.xo, entity.getX());
            double dy = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, entity.yo, entity.getY());
            double dz = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, entity.zo, entity.getZ());
            float bodyRot = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
            double sinBodyRot = Mth.sin(bodyRot);
            double cosBodyRot = -Mth.cos(bodyRot);
            float f1 = (float) dy * 10.0F;
            f1 = Mth.clamp(f1, -6.0F, 32.0F);
            float f2 = (float) (dx * sinBodyRot + dz * cosBodyRot) * 100.0F;
            f2 = Mth.clamp(f2, 0.0F, 150.0F);
            if (f2 < 0.0F) {
                f2 = 0.0F;
            }
            float f4 = Mth.lerp(partialTicks, player.oBob, player.bob);
            f1 += Mth.sin(Mth.lerp(partialTicks, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * f4;
            if (entity.isCrouching()) {
                f1 += 25.0F;
            }
            this.cloak().setRotationX(Mth.DEG_TO_RAD * (6.0F + f2 / 2.0F + f1));
        }
    }
}
