package tgw.evolution.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;

public class ItemClimbingHook extends ItemEv implements IThrowable {

    public ItemClimbingHook() {
        super(EvolutionItems.propMisc().stacksTo(1));
    }

    @Override
    public int getAutoAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    public BasicAttackType getBasicAttackType(ItemStack stack) {
        //TODO implementation
        return null;
    }

    @Override
    public SoundEvent getBlockHitSound(ItemStack stack) {
        //TODO implementation
        return null;
    }

    @Override
    public @Nullable ChargeAttackType getChargeAttackType(ItemStack stack) {
        return null;
    }

    @Override
    public int getCooldown(ItemStack stack) {
        //TODO implementation
        return 0;
    }

    @Override
    public double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type) {
        return 1;
    }

    @Override
    public int getMinAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public boolean isDamageProportionalToMomentum() {
        return true;
    }

    @Override
    public boolean isHoldable(ItemStack stack) {
        return true;
    }

    @Override
    public float precision() {
        return 0.8f;
    }

    @Override
    public EvolutionDamage.Type projectileDamageType() {
        return EvolutionDamage.Type.PIERCING;
    }

    @Override
    public double projectileSpeed() {
        return 0.5;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (living instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge < 0) {
                return;
            }
            float strength = MathHelper.getRelativeChargeStrength(charge);
            if (strength < 0.1) {
                return;
            }
            if (!level.isClientSide) {
                EntityHook hook = new EntityHook(level, player);
                hook.shoot(player, player.getXRot(), player.getYRot(), this);
                hook.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                level.addFreshEntity(hook);
                level.playSound(null, hook, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                Evolution.usingPlaceholder(player, "sound");
                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            this.addStat(player);
        }
    }

    @Override
    public boolean shouldPlaySheatheSound(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        if (player.getOffhandItem().getItem() != EvolutionItems.rope.get()) {
            player.displayClientMessage(EvolutionTexts.ACTION_HOOK, true);
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
