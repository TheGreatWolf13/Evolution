package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ISpecialAttack {

    @Nonnull
    BasicAttackType getBasicAttackType(ItemStack stack);

    @Nullable
    ChargeAttackType getChargeAttackType();

    boolean hasChargeAttack();

    enum BasicAttackType implements IAttackType {
        AXE_SWEEP(10),
        SPEAR_STAB(15),
        SWORD(10);

        private final int attackTime;

        BasicAttackType(int attackTime) {
            this.attackTime = attackTime;
        }

        @Override
        public int getAttackTime() {
            return this.attackTime;
        }

        @Override
        public boolean isCameraLocked(int tick) {
            return switch (this) {
                case AXE_SWEEP, SWORD -> false;
                case SPEAR_STAB -> tick >= 7;
            };
        }

        @Override
        public boolean isHitTick(int tick) {
            return switch (this) {
                case AXE_SWEEP, SWORD -> true;
                case SPEAR_STAB -> tick > 10;
            };
        }
    }

    enum ChargeAttackType implements IAttackType {
        ;

        @Override
        public int getAttackTime() {
            //TODO implementation
            return 0;
        }

        @Override
        public boolean isCameraLocked(int tick) {
            //TODO implementation
            return false;
        }

        @Override
        public boolean isHitTick(int tick) {
            //TODO implementation
            return false;
        }
    }

    enum StopReason {
        END,
        HIT_BLOCK,
        HIT_ENTITY,
        BLOCKED
    }

    interface IAttackType {

        int getAttackTime();

        boolean isCameraLocked(int tick);

        boolean isHitTick(int tick);
    }
}
