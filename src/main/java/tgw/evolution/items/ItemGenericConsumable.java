package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.potion.InfiniteEffectInstance;

public abstract class ItemGenericConsumable extends ItemEv implements IConsumable {

    public ItemGenericConsumable(Properties properties) {
        super(properties);
    }

    private static void applyEffects(LivingEntity entity, ItemStack stack, World world) {
        Item item = stack.getItem();
        if (item instanceof IConsumable) {
            for (Pair<EffectInstance, Float> pair : ((IConsumable) item).getEffects()) {
                if (!world.isRemote && pair.getLeft() != null && world.rand.nextFloat() < pair.getRight()) {
                    //noinspection ObjectAllocationInLoop
                    entity.addPotionEffect(new InfiniteEffectInstance(pair.getLeft()));
                }
            }
        }

    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return this.getUseAnimation();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.getConsumeTime();
    }

    private ItemStack onItemConsume(LivingEntity entity, World world, ItemStack stack) {
        applyEffects(entity, stack, world);
        PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        if (stack.getItem() instanceof IFood) {
            //TODO increase hunger
            world.playSound(null,
                            entity.posX,
                            entity.posY,
                            entity.posZ,
                            entity.getEatSound(stack),
                            SoundCategory.NEUTRAL,
                            1.0F,
                            1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
        }
        if (stack.getItem() instanceof IDrink) {
            if (player instanceof ServerPlayerEntity) {
                IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
                int amount = ((IDrink) stack.getItem()).getThirst();
                thirst.increaseThirstLevel(amount);
                thirst.increaseHydrationLevel(amount);
            }
        }
        if (stack.getItem() instanceof INutrient) {
            //TODO increase nutrient
        }
        if (player instanceof ServerPlayerEntity) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
        }
        if (player != null) {
            player.addStat(Stats.ITEM_USED.get(this));
        }
        if (player == null || !player.abilities.isCreativeMode) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (this instanceof IDrink || this instanceof IFood || this instanceof INutrient) {
            ItemStack heldStack = player.getHeldItem(hand);
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, heldStack);
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entity) {
        return this instanceof IDrink || this instanceof IFood || this instanceof INutrient ? this.onItemConsume(entity, world, stack) : stack;
    }

    @Override
    public double useItemSlowDownRate() {
        return 0.3;
    }
}