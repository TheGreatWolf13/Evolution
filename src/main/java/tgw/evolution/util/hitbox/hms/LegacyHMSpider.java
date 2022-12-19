package tgw.evolution.util.hitbox.hms;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public interface LegacyHMSpider<T extends Entity> extends HMHierarchical<T> {

    HM head();

    HM legFL();

    HM legFML();

    HM legFMR();

    HM legFR();

    HM legHL();

    HM legHML();

    HM legHMR();

    HM legHR();

    @Override
    default void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        HM head = this.head();
        HM legHR = this.legHR();
        HM legHL = this.legHL();
        HM legHMR = this.legHMR();
        HM legHML = this.legHML();
        HM legFMR = this.legFMR();
        HM legFML = this.legFML();
        HM legFR = this.legFR();
        HM legFL = this.legFL();
        head.setRotationY(netHeadYaw * Mth.DEG_TO_RAD);
        head.setRotationX(headPitch * Mth.DEG_TO_RAD);
        legHR.setRotationZ(-45 * Mth.DEG_TO_RAD);
        legHL.setRotationZ(45 * Mth.DEG_TO_RAD);
        legHMR.setRotationZ(-33.3f * Mth.DEG_TO_RAD);
        legHML.setRotationZ(33.3f * Mth.DEG_TO_RAD);
        legFMR.setRotationZ(-33.3f * Mth.DEG_TO_RAD);
        legFML.setRotationZ(33.3f * Mth.DEG_TO_RAD);
        legFR.setRotationZ(-45 * Mth.DEG_TO_RAD);
        legFL.setRotationZ(45 * Mth.DEG_TO_RAD);
        legHR.setRotationY(-45 * Mth.DEG_TO_RAD);
        legHL.setRotationY(45 * Mth.DEG_TO_RAD);
        legHMR.setRotationY(-22.5f * Mth.DEG_TO_RAD);
        legHML.setRotationY(22.5f * Mth.DEG_TO_RAD);
        legFMR.setRotationY(22.5f * Mth.DEG_TO_RAD);
        legFML.setRotationY(-22.5f * Mth.DEG_TO_RAD);
        legFR.setRotationY(45 * Mth.DEG_TO_RAD);
        legFL.setRotationY(-45 * Mth.DEG_TO_RAD);
        float ampl = 0.4f * limbSwingAmount;
        float limbSwingY = 0.666_2F * 2.0F * limbSwing;
        float hindYRot = -Mth.cos(limbSwingY) * ampl;
        float frontYRot = -Mth.cos(limbSwingY + Mth.PI * 1.5F) * ampl;
        float limbSwingZ = 0.666_2F * limbSwing;
        float hindZRot = Math.abs(Mth.sin(limbSwingZ)) * ampl;
        float frontZRot = Math.abs(Mth.sin(limbSwingZ + Mth.PI * 1.5F)) * ampl;
        legHR.addRotationY(hindYRot);
        legHL.addRotationY(-hindYRot);
        legHMR.addRotationY(-hindYRot);
        legHML.addRotationY(hindYRot);
        legFMR.addRotationY(-frontYRot);
        legFML.addRotationY(frontYRot);
        legFR.addRotationY(frontYRot);
        legFL.addRotationY(-frontYRot);
        legHR.addRotationZ(hindZRot);
        legHL.addRotationZ(-hindZRot);
        legHMR.addRotationZ(hindZRot);
        legHML.addRotationZ(-hindZRot);
        legFMR.addRotationZ(frontZRot);
        legFML.addRotationZ(-frontZRot);
        legFR.addRotationZ(frontZRot);
        legFL.addRotationZ(-frontZRot);
    }
}
