package tgw.evolution.items;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.tooltip.*;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.hitbox.ColliderHitbox;
import tgw.evolution.util.hitbox.HitboxType;

import java.util.List;

public interface IMelee {

    IAttackType BARE_HAND_ATTACK = new IAttackType() {
        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(0);
        }

        @Override
        public int getAttackTime() {
            return 10;
        }

        @Override
        public @Nullable ColliderHitbox getCollider(HumanoidArm arm) {
            return null;
        }

        @Override
        public EvolutionDamage.Type getDamageType() {
            return EvolutionDamage.Type.CRUSHING;
        }

        @Override
        public double getDmgMultiplier(IMelee melee, ItemStack stack) {
            return 1;
        }

        @Override
        public int getFollowUps() {
            return 9;
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
        public boolean isLateralMotionLocked(int tick) {
            return false;
        }

        @Override
        public boolean isLongitudinalMotionLocked(int tick) {
            return false;
        }

        @Override
        public boolean isVerticalMotionLocked(int tick) {
            return false;
        }
    };

    int getAutoAttackTime(ItemStack stack);

    BasicAttackType getBasicAttackType(ItemStack stack);

    SoundEvent getBlockHitSound(ItemStack stack);

    @Nullable ChargeAttackType getChargeAttackType(ItemStack stack);

    int getCooldown(ItemStack stack);

    double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type);

    int getMinAttackTime(ItemStack stack);

    boolean isHoldable(ItemStack stack);

    default void makeTooltip(Player player, List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack) {
        tooltip.add(EvolutionTexts.basicAttack());
        IMelee.BasicAttackType basicAttack = this.getBasicAttackType(stack);
        int followUps = basicAttack.getFollowUps();
        if (followUps > 0) {
            tooltip.add(TooltipFollowUp.followUp(followUps));
        }
        double mult = basicAttack.getDmgMultiplier(this, stack);
        tooltip.add(TooltipDmgMultiplier.basic(mult));
        double dmg = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        tooltip.add(TooltipDamage.basic(basicAttack.getDamageType(), mult * dmg));
        int cooldown = this.getCooldown(stack);
        tooltip.add(TooltipCooldown.cooldown(cooldown));
        IMelee.ChargeAttackType chargeAttack = this.getChargeAttackType(stack);
        if (chargeAttack != null) {
            tooltip.add(EvolutionTexts.EITHER_EMPTY);
            tooltip.add(EvolutionTexts.chargeAttack());
            mult = chargeAttack.getDmgMultiplier(this, stack);
            tooltip.add(TooltipDmgMultiplier.charge(mult));
            tooltip.add(TooltipDamage.charge(chargeAttack.getDamageType(), mult * dmg));
            tooltip.add(TooltipCooldown.cooldown(cooldown));
        }
        if (stack.getItem() instanceof IThrowable throwable && throwable.isThrowable(stack)) {
            tooltip.add(EvolutionTexts.EITHER_EMPTY);
            tooltip.add(EvolutionTexts.throwAttack());
            tooltip.add(TooltipThrowSpeed.throwSpeed(throwable.projectileSpeed()));
            tooltip.add(TooltipPrecision.precision(throwable.precision()));
            tooltip.add(TooltipDmgMultiplier.thrown(mult));
            EvolutionDamage.Type type = throwable.projectileDamageType();
            tooltip.add(TooltipDamage.thrown(type, dmg * this.getDmgMultiplier(stack, type)));
            if (throwable.isDamageProportionalToMomentum()) {
                tooltip.add(TooltipInfo.info(EvolutionTexts.TOOLTIP_DAMAGE_PROPORTIONAL));
            }
        }
    }

    boolean shouldPlaySheatheSound(ItemStack stack);

    enum BasicAttackType implements IAttackType {
        AXE_STRIKE_1(12, new ColliderHitbox(HitboxType.NONE, -0.75 * 0.85, 0, -5 * 0.85 * Mth.SQRT_OF_TWO, 0.75 * 0.85, -5 * 0.85 * Mth.SQRT_OF_TWO,
                                            -9 * 0.85 * Mth.SQRT_OF_TWO)),
        HAMMER_STRIKE_1(12, null),
        HOE_STRIKE_1(12, new ColliderHitbox(HitboxType.NONE, -1.25 * 0.85, 0, -6 * 0.85 * Mth.SQRT_OF_TWO, 1.25 * 0.85, -5 * 0.85 * Mth.SQRT_OF_TWO,
                                            -9 * 0.85 * Mth.SQRT_OF_TWO)),
        JAVELIN_THRUST(15, new ColliderHitbox(HitboxType.NONE, -0.5 * 0.85, -1.5 * 0.85 * Mth.SQRT_OF_TWO, -14 * 0.85 * Mth.SQRT_OF_TWO, 0.5 * 0.85,
                                              1.5 * 0.85 * Mth.SQRT_OF_TWO, -8 * 0.85 * Mth.SQRT_OF_TWO)),
        MACE_STRIKE_1(12, null),
        PICKAXE_STRIKE_1(12, new ColliderHitbox(HitboxType.NONE, -1 * 0.85, -4 * 0.85 * Mth.SQRT_OF_TWO, -4 * 0.85 * Mth.SQRT_OF_TWO, 1 * 0.85,
                                                4 * 0.85 * Mth.SQRT_OF_TWO, -7 * 0.85 * Mth.SQRT_OF_TWO)),
        SHOVEL_STRIKE_1(12, new ColliderHitbox(HitboxType.NONE, 0, -2 * 0.85 * Mth.SQRT_OF_TWO, -6 * 0.85 * Mth.SQRT_OF_TWO, -1.5 * 0.85,
                                               2 * 0.85 * Mth.SQRT_OF_TWO, -10 * 0.85 * Mth.SQRT_OF_TWO, false)),
        TORCH_SWEEP(10, null);

        private final int attackTime;
        private final @Nullable ColliderHitbox collider;

        BasicAttackType(int attackTime, @Nullable ColliderHitbox collider) {
            this.attackTime = attackTime;
            this.collider = collider;
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
        public @Nullable ColliderHitbox getCollider(HumanoidArm arm) {
            if (this.collider != null) {
                return this.collider.adjust(arm);
            }
            return null;
        }

        @Override
        public EvolutionDamage.Type getDamageType() {
            return switch (this) {
                case AXE_STRIKE_1, HOE_STRIKE_1 -> EvolutionDamage.Type.SLASHING;
                case HAMMER_STRIKE_1, MACE_STRIKE_1, SHOVEL_STRIKE_1, TORCH_SWEEP -> EvolutionDamage.Type.CRUSHING;
                case JAVELIN_THRUST, PICKAXE_STRIKE_1 -> EvolutionDamage.Type.PIERCING;
            };
        }

        @Override
        public double getDmgMultiplier(IMelee melee, ItemStack stack) {
            return melee.getDmgMultiplier(stack, this.getDamageType());
        }

        @Override
        public int getFollowUps() {
            return switch (this) {
                case AXE_STRIKE_1, MACE_STRIKE_1 -> 2;
                case JAVELIN_THRUST, HAMMER_STRIKE_1, HOE_STRIKE_1 -> 1;
                case PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> 0;
                case TORCH_SWEEP -> 4;
            };
        }

        @Override
        public boolean isCameraLocked(int tick) {
            return switch (this) {
                case AXE_STRIKE_1, HAMMER_STRIKE_1, HOE_STRIKE_1, MACE_STRIKE_1, PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> tick >= 6;
                case JAVELIN_THRUST -> tick >= 8;
                case TORCH_SWEEP -> false;
            };
        }

        @Override
        public boolean isHitTick(int tick) {
            return switch (this) {
                case AXE_STRIKE_1, HAMMER_STRIKE_1, HOE_STRIKE_1, MACE_STRIKE_1, PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> tick > 6;
                case JAVELIN_THRUST -> tick > 7;
                case TORCH_SWEEP -> true;
            };
        }

        @Override
        public boolean isLateralMotionLocked(int tick) {
            return switch (this) {
                case AXE_STRIKE_1, HAMMER_STRIKE_1, HOE_STRIKE_1, MACE_STRIKE_1, PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> tick > 6;
                case JAVELIN_THRUST -> tick >= 7;
                case TORCH_SWEEP -> false;
            };
        }

        @Override
        public boolean isLongitudinalMotionLocked(int tick) {
            return switch (this) {
                case AXE_STRIKE_1, JAVELIN_THRUST, TORCH_SWEEP -> false;
                case HAMMER_STRIKE_1, HOE_STRIKE_1, MACE_STRIKE_1, PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> tick > 8;
            };
        }

        @Override
        public boolean isVerticalMotionLocked(int tick) {
            return switch (this) {
                case AXE_STRIKE_1, HAMMER_STRIKE_1, HOE_STRIKE_1, MACE_STRIKE_1, PICKAXE_STRIKE_1, SHOVEL_STRIKE_1 -> tick > 6;
                case JAVELIN_THRUST -> tick >= 7;
                case TORCH_SWEEP -> false;
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
        public EvolutionDamage.Type getDamageType() {
            //TODO implementation
            return null;
        }

        @Override
        public double getDmgMultiplier(IMelee melee, ItemStack stack) {
            //TODO implementation
            return 0;
        }

        @Override
        public int getFollowUps() {
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

        @Override
        public boolean isLateralMotionLocked(int tick) {
            //TODO implementation
            return false;
        }

        @Override
        public boolean isLongitudinalMotionLocked(int tick) {
            //TODO implementation
            return false;
        }

        @Override
        public boolean isVerticalMotionLocked(int tick) {
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

        EvolutionDamage.Type getDamageType();

        double getDmgMultiplier(IMelee melee, ItemStack stack);

        int getFollowUps();

        boolean isCameraLocked(int tick);

        boolean isHitTick(int tick);

        boolean isLateralMotionLocked(int tick);

        boolean isLongitudinalMotionLocked(int tick);

        boolean isVerticalMotionLocked(int tick);
    }
}
