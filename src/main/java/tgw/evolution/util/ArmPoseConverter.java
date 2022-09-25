package tgw.evolution.util;

import net.minecraft.client.model.HumanoidModel;

public final class ArmPoseConverter {

    private ArmPoseConverter() {
    }

    public static ArmPose fromVanilla(HumanoidModel.ArmPose pose) {
        return switch (pose) {
            case ITEM -> ArmPose.ITEM;
            case BLOCK -> ArmPose.BLOCK;
            case EMPTY -> ArmPose.EMPTY;
            case SPYGLASS -> ArmPose.SPYGLASS;
            case THROW_SPEAR -> ArmPose.THROW_SPEAR;
            case BOW_AND_ARROW -> ArmPose.BOW_AND_ARROW;
            case CROSSBOW_HOLD -> ArmPose.CROSSBOW_HOLD;
            case CROSSBOW_CHARGE -> ArmPose.CROSSBOW_CHARGE;
        };
    }

    public static HumanoidModel.ArmPose toVanilla(ArmPose pose) {
        return switch (pose) {
            case CROSSBOW_CHARGE -> HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            case CROSSBOW_HOLD -> HumanoidModel.ArmPose.CROSSBOW_HOLD;
            case BOW_AND_ARROW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
            case THROW_SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
            case SPYGLASS -> HumanoidModel.ArmPose.SPYGLASS;
            case EMPTY -> HumanoidModel.ArmPose.EMPTY;
            case BLOCK -> HumanoidModel.ArmPose.BLOCK;
            case ITEM -> HumanoidModel.ArmPose.ITEM;
        };
    }
}
