package tgw.evolution.patches;

import tgw.evolution.items.ICustomAttack;

import javax.annotation.Nullable;

public interface ILivingEntityPatch {

    float getMainhandCustomAttackProgress(float partialTicks);

    int getMainhandCustomAttackTicks();

    @Nullable
    ICustomAttack.AttackType getMainhandCustomAttackType();

    float getOffhandCustomAttackProgress(float partialTicks);

    int getOffhandCustomAttackTicks();

    @Nullable
    ICustomAttack.AttackType getOffhandCustomAttackType();

    boolean isMainhandCustomAttacking();

    boolean isOffhandCustomAttacking();

    boolean renderMainhandCustomAttack();

    boolean renderOffhandCustomAttack();

    void startMainhandCustomAttack(ICustomAttack.AttackType type);

    void startOffhandCustomAttack(ICustomAttack.AttackType type);

    void stopMainhandCustomAttack(ICustomAttack.StopReason reason);

    void stopOffhandCustomAttack(ICustomAttack.StopReason reason);
}
