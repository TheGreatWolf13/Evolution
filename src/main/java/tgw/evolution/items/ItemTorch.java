package tgw.evolution.items;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntityTorch;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Time;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTorch extends ItemWallOrFloor implements IFireAspect, IThrowable {

    public ItemTorch(Properties properties) {
        super(EvolutionBlocks.TORCH.get(), EvolutionBlocks.WALL_TORCH.get(), properties);
    }

    public static ItemStack createStack(World world, int count) {
        return createStack(world.getDayTime(), count);
    }

    public static ItemStack createStack(long timeCreated, int count) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("TimeCreated", Time.roundToLastFullHour(timeCreated));
        ItemStack stack = new ItemStack(EvolutionItems.torch.get(), count);
        stack.setTag(nbt);
        return stack;
    }

    public static ItemStack getDroppedStack(TETorch tile) {
        if (getRemainingTime(tile) <= 0) {
            return new ItemStack(EvolutionItems.torch_unlit.get());
        }
        return createStack(tile.getTimePlaced(), 1);
    }

    public static int getRemainingTime(long timeNow, long timePlaced) {
        int torchTime = EvolutionConfig.COMMON.torchTime.get();
        long timeNowRounded = Time.roundToLastFullHour(timeNow);
        long timeCreated = Time.roundToLastFullHour(timePlaced);
        int deltaTime = (int) (timeNowRounded - timeCreated) / 1_000;
        return MathHelper.clampMin(torchTime - deltaTime, 0);
    }

    public static int getRemainingTime(World world, ItemStack stack) {
        return getRemainingTime(world.getDayTime(), stack.getTag().getLong("TimeCreated"));
    }

    public static int getRemainingTime(TETorch tile) {
        return getRemainingTime(tile.getLevel().getDayTime(), tile.getTimePlaced());
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (!stack.hasTag()) {
            return;
        }
        int remainingTime = getRemainingTime(world, stack);
        tooltip.add(EvolutionTexts.torch(remainingTime));
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
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
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        long ticks = world.getDayTime();
        if (ticks % 10 == 0) {
            if (!stack.hasTag()) {
                return;
            }
            if (getRemainingTime(world, stack) <= 0) {
                entity.setSlot(slot, new ItemStack(EvolutionItems.torch_unlit.get(), stack.getCount()));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) livingEntity;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge < 0) {
                return;
            }
            float strength = MathHelper.getRelativeChargeStrength(charge);
            if (strength < 0.1) {
                return;
            }
            if (!world.isClientSide) {
                long timeCreated;
                if (stack.hasTag()) {
                    timeCreated = stack.getTag().getLong("TimeCreated");
                }
                else {
                    timeCreated = world.getDayTime();
                }
                EntityTorch torch = new EntityTorch(world, player, timeCreated);
                torch.shoot(player, player.xRot, player.yRot, 0.6f * strength, 1.0F);
                torch.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                world.addFreshEntity(torch);
                world.playSound(null, torch, SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Evolution.usingPlaceholder(player, "sound");
                if (!player.abilities.instabuild) {
                    stack.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            this.addStat(player);
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (!state.getValue(EvolutionBStates.LIT)) {
            world.levelEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TETorch) {
            if (stack.hasTag()) {
                ((TETorch) tile).setTimePlaced(stack.getTag().getLong("TimeCreated"));
            }
            else {
                ((TETorch) tile).setTimePlaced(world.getDayTime());
            }
        }
        return super.updateCustomBlockEntityTag(pos, world, player, stack, state);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        player.startUsingItem(hand);
        return new ActionResult<>(ActionResultType.CONSUME, stack);
    }
}
