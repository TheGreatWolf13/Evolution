package tgw.evolution.util.hitbox;

public final class MixinTempHelper {

    private MixinTempHelper() {
    }

    public static boolean condition() {
        return true;
    }

//    private static ModelPart getArm(HumanoidModel model, HumanoidArm side) {
//        return side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
//    }

//    public static void setup(HitboxPlayer hitbox, LivingEntity entity) {
//        ISpecialAttack.IAttackType type = ((ILivingEntityPatch) entity).getMainhandSpecialAttackType();
//        if (type instanceof ISpecialAttack.BasicAttackType basic) {
//            switch (basic) {
//                case AXE_SWEEP -> {
//                    HumanoidArm attackingSide = entity.getMainArm();
//                    HitboxGroup attackingArm = hitbox.getArmForSide(attackingSide);
//                    float progress = ((ILivingEntityPatch) entity).getMainhandSpecialAttackProgress(1.0f);
//                    attackingArm.setRotationX(-xRot(progress));
//                    attackingArm.setRotationY(yRot(progress, hitbox.head.rotationX));
//                    attackingArm.setRotationZ(zRot(progress));
//                }
//                case SPEAR_STAB -> spear(hitbox, entity);
//            }
//        }
//    }

//    public static void setup(HumanoidModel model, LivingEntity player) {
//        ISpecialAttack.IAttackType type = ((ILivingEntityPatch) player).getMainhandSpecialAttackType();
//        if (type instanceof ISpecialAttack.BasicAttackType basic) {
//            switch (basic) {
//                case AXE_SWEEP -> {
//                    HumanoidArm attackingSide = player.getMainArm();
//                    ModelPart attackingArm = getArm(model, attackingSide);
//                    float progress = ((ILivingEntityPatch) player).getMainhandSpecialAttackProgress(1.0f);
//                    attackingArm.xRot = xRot(progress);
//                    attackingArm.yRot = yRot(progress, model.head.xRot);
//                    attackingArm.zRot = zRot(progress);
//                }
//                case SPEAR_STAB -> spear(model, player);
//            }
//        }
//    }

//    private static void spear(HumanoidModel model, LivingEntity entity) {
//        HumanoidArm attackingSide = entity.getMainArm();
//        ModelPart attackingArm = getArm(model, attackingSide);
//        float progress = ((ILivingEntityPatch) entity).getMainhandSpecialAttackProgress(Minecraft.getInstance().getFrameTime());
////        if (progress < 0.5f) {
////            model.body.yRot = -MathHelper.sin(MathHelper.sqrt(progress) * MathHelper.TAU) * 0.2F;
////        }
////        else {
////            model.body.yRot = MathHelper.sin(MathHelper.sqrt(progress) * MathHelper.TAU) * 0.4F;
////        }
//        if (progress < 0.5f) {
//            //From -PI/10 to PI/4
//            attackingArm.xRot = progress * 2 * 7 / 20 * MathHelper.PI - MathHelper.PI / 10;
//        }
//        else if (progress < 0.75f) {
//            progress -= 0.5f;
//            float targetRot = model.head.xRot * 2 / 3 - MathHelper.PI_OVER_2;
//            //From PI/4 to targetRot
//            attackingArm.xRot = progress * 4 * (targetRot - MathHelper.PI / 4) + MathHelper.PI / 4;
//        }
//        else {
//            attackingArm.xRot = model.head.xRot * 2 / 3 - MathHelper.PI_OVER_2;
//        }
//    }

//    private static void spear(HitboxPlayer hitbox, LivingEntity entity) {
//        HumanoidArm attackingSide = entity.getMainArm();
//        HitboxGroup attackingArm = hitbox.getArmForSide(attackingSide);
//        float progress = ((ILivingEntityPatch) entity).getMainhandSpecialAttackProgress(1.0f);
////        if (progress < 0.5f) {
////            hitbox.body.rotationY = -MathHelper.sin(MathHelper.sqrt(progress) * MathHelper.TAU) * 0.2F;
////        }
////        else {
////            hitbox.body.rotationY = MathHelper.sin(MathHelper.sqrt(progress) * MathHelper.TAU) * 0.4F;
////        }
//    }
}
