package tgw.evolution.items;

import javax.annotation.Nonnull;

public interface ICustomAttack {

    @Nonnull
    AttackType getAttackType();

    enum AttackType {
        SWORD(10);

        private final int attackTime;

        AttackType(int attackTime) {
            this.attackTime = attackTime;
        }

        public int getAttackTime() {
            return this.attackTime;
        }
    }

    enum StopReason {
        END,
        HIT_BLOCK,
        HIT_ENTITY
    }
}
