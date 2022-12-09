package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.Evolution;
import tgw.evolution.items.ItemUtils;

public interface HMPlayer<T extends LivingEntity> extends HMHumanoid<T> {

    HM cape();

    HM clothesArmL();

    HM clothesArmR();

    HM clothesBody();

    HM clothesForearmL();

    HM clothesForearmR();

    HM clothesForelegL();

    HM clothesForelegR();

    HM clothesLegL();

    HM clothesLegR();

    boolean isSlim();

    void setAllVisible(boolean visible);

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HMHumanoid.super.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (!this.isSlim()) {
            if (!ItemUtils.usesModularRendering(entity.getMainHandItem())) {
                if (entity.getMainArm() == HumanoidArm.RIGHT) {
                    this.itemR().translateX(-1);
                }
                else {
                    this.itemL().translateX(1);
                }
            }
            if (!ItemUtils.usesModularRendering(entity.getOffhandItem())) {
                if (entity.getMainArm() == HumanoidArm.RIGHT) {
                    this.itemL().translateX(1);
                }
                else {
                    this.itemR().translateX(-1);
                }
            }
        }
        else {
            if (ItemUtils.usesModularRendering(entity.getMainHandItem())) {
                if (entity.getMainArm() == HumanoidArm.RIGHT) {
                    this.itemR().translateX(1);
                }
                else {
                    this.itemL().translateX(-1);
                }
            }
            if (ItemUtils.usesModularRendering(entity.getOffhandItem())) {
                if (entity.getMainArm() == HumanoidArm.RIGHT) {
                    this.itemL().translateX(-1);
                }
                else {
                    this.itemR().translateX(1);
                }
            }
        }
        this.clothesLegL().copy(this.legL());
        this.clothesLegR().copy(this.legR());
        this.clothesArmL().copy(this.armL());
        this.clothesArmR().copy(this.armR());
        this.clothesForelegL().copy(this.forelegL());
        this.clothesForelegR().copy(this.forelegR());
        this.clothesForearmL().copy(this.forearmL());
        this.clothesForearmR().copy(this.forearmR());
        this.clothesBody().copy(this.body());
        if (entity instanceof Player player) {
            this.cape().setRotationY(Mth.DEG_TO_RAD * 180);
            if (entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                if (this.crouching()) {
                    this.cape().setPivotZ(3.125F - 0.5f);
                    this.cape().setPivotY(24.0F - 1.85f);
                }
                else {
                    this.cape().setPivotZ(3.125F);
                    this.cape().setPivotY(24.0F);
                }
            }
            else if (this.crouching()) {
                this.cape().setPivotZ(3.125f - 0.2F);
                this.cape().setPivotY(24.0f - 0.8F);
            }
            else {
                this.cape().setPivotZ(3.125F + 0.7f);
                this.cape().setPivotY(24.0F + 0.85f);
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
            this.cape().setRotationX(Mth.DEG_TO_RAD * (6.0F + f2 / 2.0F + f1));
        }
    }
}
