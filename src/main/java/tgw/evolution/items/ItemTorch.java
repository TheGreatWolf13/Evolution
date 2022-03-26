package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntityTorch;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTorch extends ItemWallOrFloor implements IFireAspect, IThrowable {

    public ItemTorch(Properties properties) {
        super(EvolutionBlocks.TORCH.get(), EvolutionBlocks.WALL_TORCH.get(), properties);
    }

    public static ItemStack createStack(Level level, int count) {
        return createStack(level.getDayTime(), count);
    }

    public static ItemStack createStack(long timeCreated, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("TimeCreated", Time.roundToLastFullHour(timeCreated));
        ItemStack stack = new ItemStack(EvolutionItems.torch.get(), count);
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack getDroppedStack(TETorch tile) {
        if (getRemainingTime(tile) <= 0) {
            return new ItemStack(EvolutionItems.torch_unlit.get());
        }
        return createStack(tile.getTimePlaced(), 1);
    }

    public static int getRemainingTime(long timeNow, long timePlaced) {
        int torchTime = EvolutionConfig.SERVER.torchTime.get();
        long timeNowRounded = Time.roundToLastFullHour(timeNow);
        long timeCreated = Time.roundToLastFullHour(timePlaced);
        int deltaTime = (int) (timeNowRounded - timeCreated) / 1_000;
        return Math.max(torchTime - deltaTime, 0);
    }

    public static int getRemainingTime(Level level, ItemStack stack) {
        return getRemainingTime(level.getDayTime(), stack.getTag().getLong("TimeCreated"));
    }

    public static int getRemainingTime(TETorch tile) {
        return getRemainingTime(tile.getLevel().getDayTime(), tile.getTimePlaced());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!stack.hasTag()) {
            return;
        }
        int remainingTime = getRemainingTime(level, stack);
        tooltip.add(EvolutionTexts.torch(remainingTime));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            items.add(this.getDefaultInstance());
        }
    }

    @Override
    public float getChance() {
        return 0.2f;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    @Override
    public int getLevel() {
        return 2;
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        long ticks = level.getDayTime();
        if (ticks % 10 == 0) {
            if (!stack.hasTag()) {
                return;
            }
            if (getRemainingTime(level, stack) <= 0) {
                //TODO
//                entity.setSlot(slot, new ItemStack(EvolutionItems.torch_unlit.get(), stack.getCount()));
            }
        }
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
                long timeCreated;
                if (stack.hasTag()) {
                    timeCreated = stack.getTag().getLong("TimeCreated");
                }
                else {
                    timeCreated = level.getDayTime();
                }
                EntityTorch torch = new EntityTorch(level, player, timeCreated);
                torch.shoot(player, player.getXRot(), player.getYRot(), 0.6f * strength, 1.0F);
                torch.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                level.addFreshEntity(torch);
                level.playSound(null, torch, SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                Evolution.usingPlaceholder(player, "sound");
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            this.addStat(player);
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (!state.getValue(EvolutionBStates.LIT)) {
            level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TETorch teTorch) {
            if (stack.hasTag()) {
                teTorch.setTimePlaced(stack.getTag().getLong("TimeCreated"));
            }
            else {
                teTorch.setTimePlaced(level.getDayTime());
            }
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
