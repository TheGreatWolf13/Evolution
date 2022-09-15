package tgw.evolution.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.hitbox.ColliderHitbox;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.math.AABBMutable;

public interface IMelee {

    IAttackType BARE_HAND_ATTACK = new IAttackType() {

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(0);
        }

        @Override
        public int getAttackTime() {
            return 6;
        }

        @Override
        public @Nullable ColliderHitbox getCollider(HumanoidArm arm) {
            return null;
        }

        @Override
        public boolean isCameraLocked(int tick) {
            return false;
        }

        @Override
        public boolean isHitTick(int tick) {
            return true;
        }

        @Override
        public boolean isMotionLocked(int tick) {
            return false;
        }
    };

    double getAttackDamage(ItemStack stack, IAttackType attackType);

    double getAttackSpeed(ItemStack stack);

    int getAutoAttackTime(ItemStack stack);

    BasicAttackType getBasicAttackType(ItemStack stack);

    @Nullable ChargeAttackType getChargeAttackType(ItemStack stack);

    EvolutionDamage.Type getDamageType(ItemStack stack, IMelee.IAttackType attackType);

    int getMinAttackTime(ItemStack stack);

    boolean isHoldable(ItemStack stack);

    boolean shouldPlaySheatheSound(ItemStack stack);

    enum BasicAttackType implements IAttackType {
        AXE_SWEEP(10),
        SPEAR_STAB(15),
        SWORD(10);

        private final int attackTime;

        BasicAttackType(int attackTime) {
            this.attackTime = attackTime;
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(1);
            buf.writeEnum(this);
        }

        @Override
        public int getAttackTime() {
            return this.attackTime;
        }

        @Override
        public ColliderHitbox getCollider(HumanoidArm arm) {
            return new ColliderHitbox(HitboxType.SPEAR, AABBMutable.block(0.25, -12.0, -1.25, -1.25, -20.0, 2.5));
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

        @Override
        public boolean isMotionLocked(int tick) {
            return switch (this) {
                case AXE_SWEEP, SWORD -> false;
                case SPEAR_STAB -> tick >= 7;
            };
        }
    }

    enum ChargeAttackType implements IAttackType {
        ;

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(2);
            buf.writeEnum(this);
        }

        @Override
        public int getAttackTime() {
            //TODO implementation
            return 0;
        }

        @Override
        public ColliderHitbox getCollider(HumanoidArm arm) {
            //TODO implementation
            return null;
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

        @Override
        public boolean isMotionLocked(int tick) {
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

        static IAttackType decode(FriendlyByteBuf buf) {
            byte type = buf.readByte();
            return switch (type) {
                case 0 -> BARE_HAND_ATTACK;
                case 1 -> buf.readEnum(BasicAttackType.class);
                case 2 -> buf.readEnum(ChargeAttackType.class);
                default -> throw new IllegalStateException("Received invalid AttackType");
            };
        }

        void encode(FriendlyByteBuf buf);

        int getAttackTime();

        @Nullable ColliderHitbox getCollider(HumanoidArm arm);

        boolean isCameraLocked(int tick);

        boolean isHitTick(int tick);

        boolean isMotionLocked(int tick);
    }
}
