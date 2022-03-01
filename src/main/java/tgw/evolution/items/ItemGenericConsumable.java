package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.food.IHunger;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;

public abstract class ItemGenericConsumable extends ItemEv implements IConsumable {

    public ItemGenericConsumable(Item.Properties properties) {
        super(properties);
    }

    private static void applyEffects(LivingEntity entity, ItemStack stack, Level level) {
        Item item = stack.getItem();
        if (item instanceof IConsumable consumable) {
            for (Pair<MobEffectInstance, Float> pair : consumable.getEffects()) {
                if (!level.isClientSide && pair.getLeft() != null && level.random.nextFloat() < pair.getRight()) {
                    //noinspection ObjectAllocationInLoop
                    entity.addEffect(new MobEffectInstance(pair.getLeft()));
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return this instanceof IDrink || this instanceof IFood || this instanceof INutrient ? this.onItemConsume(entity, level, stack) : stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return this.getUseAnimation();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.getConsumeTime();
    }

    protected ItemStack onItemConsume(LivingEntity entity, Level level, ItemStack stack) {
        applyEffects(entity, stack, level);
        Player player = entity instanceof Player pl ? pl : null;
        if (stack.getItem() instanceof IFood food) {
            if (player instanceof ServerPlayer) {
                IHunger hunger = player.getCapability(CapabilityHunger.INSTANCE).orElseThrow(IllegalStateException::new);
                int amount = food.getHunger();
                hunger.increaseHungerLevel(amount);
                hunger.increaseSaturationLevel(amount);
            }
            level.playSound(null,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            entity.getEatingSound(stack),
                            SoundSource.NEUTRAL,
                            1.0F,
                            1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        }
        if (stack.getItem() instanceof IDrink drink) {
            if (player instanceof ServerPlayer) {
                IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
                int amount = drink.getThirst();
                thirst.increaseThirstLevel(amount);
                thirst.increaseHydrationLevel(amount);
            }
        }
        if (stack.getItem() instanceof INutrient) {
            //TODO increase nutrient
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (this instanceof IDrink || this instanceof IFood || this instanceof INutrient) {
            ItemStack heldStack = player.getItemInHand(hand);
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, heldStack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    }

    @Override
    public double useItemSlowDownRate() {
        return 0.3;
    }
}
