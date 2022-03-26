package tgw.evolution.items.modular;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.*;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.constants.HarvestLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemModularTool extends ItemModular implements IThrowable, ITwoHanded, IBackWeapon, IMelee, ISpecialAttack {

    public ItemModularTool(Properties builder) {
        super(builder);
    }

    public static ItemStack createNew(PartTypes.Head headType,
                                      ItemMaterial headMaterial,
                                      PartTypes.Handle handleType,
                                      ItemMaterial handleMaterial,
                                      boolean sharp) {
        if (!headMaterial.isAllowedBy(headType)) {
            throw new RuntimeException("Invalid material for " + headType.getName() + ": " + headMaterial.getName());
        }
        if (!handleMaterial.isAllowedBy(handleType)) {
            throw new RuntimeException("Invalid material for " + handleType.getName() + ": " + handleMaterial.getName());
        }
        ItemStack stack = new ItemStack(EvolutionItems.MODULAR_TOOL.get());
        IModularTool tool = IModularTool.get(stack);
        tool.setHead(headType, new MaterialInstance(headMaterial));
        tool.setHandle(handleType, new MaterialInstance(handleMaterial));
        if (sharp) {
            tool.sharp();
        }
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            for (PartTypes.Head head : PartTypes.Head.VALUES) {
                for (ItemMaterial material : ItemMaterial.VALUES) {
                    if (material.isAllowedBy(head)) {
                        items.add(createNew(head, material, PartTypes.Handle.ONE_HANDED, ItemMaterial.WOOD, true));
                    }
                }
            }
        }
    }

    @Override
    public double getAttackDamage(ItemStack stack) {
        return IModularTool.get(stack).getAttackDamage();
    }

    @Override
    public double getAttackSpeed(ItemStack stack) {
        return IModularTool.get(stack).getAttackSpeed();
    }

    @Nonnull
    @Override
    public BasicAttackType getBasicAttackType(ItemStack stack) {
        IModularTool tool = IModularTool.get(stack);
        if (tool.getHead().getType() == PartTypes.Head.SPEAR) {
            return BasicAttackType.SPEAR_STAB;
        }
        return BasicAttackType.AXE_SWEEP;
    }

    @Nullable
    @Override
    public ChargeAttackType getChargeAttackType() {
        //TODO implementation
        return null;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType(ItemStack stack) {
        return IModularTool.get(stack).getDamageType();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IModularTool tool = IModularTool.get(stack);
        if (tool.getEffectiveMaterials().contains(state.getMaterial())) {
            return tool.getMiningSpeed();
        }
        return 1.0F;
    }

    public float getMiningSpeed(ItemStack stack) {
        return IModularTool.get(stack).getMiningSpeed();
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModularTool.get(stack);
    }

    @Override
    public int getPriority(ItemStack stack) {
        return IModularTool.get(stack).getBackPriority();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (IModularTool.get(stack).getHead().getType() == PartTypes.Head.SPEAR) {
            return UseAnim.SPEAR;
        }
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public boolean hasChargeAttack() {
        //TODO implementation
        return false;
    }

    @Override
    public <T extends LivingEntity> void hurtAndBreak(ItemStack stack,
                                                      DamageCause cause,
                                                      T entity,
                                                      Consumer<T> onBroken,
                                                      @HarvestLevel int harvestLevel) {
        if (!entity.level.isClientSide && (!(entity instanceof Player player) || !player.getAbilities().instabuild)) {
            if (stack.isDamageableItem()) {
                this.damage(stack, cause, harvestLevel);
                if (this.isBroken(stack)) {
                    onBroken.accept(entity);
                    if (entity instanceof Player player) {
                        player.awardStat(Stats.ITEM_BROKEN.get(this));
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        this.hurtAndBreak(stack, DamageCause.HIT_ENTITY, attacker, e -> e.broadcastBreakEvent(e.getUsedItemHand()));
        return true;
    }

    @Override
    public boolean isBroken(ItemStack stack) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isCancelable(ItemStack stack) {
        return this.isThrowable(stack);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        IModularTool tool = IModularTool.get(stack);
        if (tool.getEffectiveMaterials().contains(state.getMaterial())) {
            return tool.getHarvestLevel() >= ((IBlockPatch) state.getBlock()).getHarvestLevel(state);
        }
        return false;
    }

    @Override
    public boolean isThrowable(ItemStack stack) {
        return IModularTool.get(stack).getHead().getType() == PartTypes.Head.SPEAR;
    }

    @Override
    public boolean isTwoHanded(ItemStack stack) {
        return IModularTool.get(stack).isTwoHanded();
    }

    @Override
    public boolean mineBlock(ItemStack stack, @Nonnull Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            IModularTool tool = IModularTool.get(stack);
            this.hurtAndBreak(stack,
                              tool.getEffectiveMaterials().contains(state.getMaterial()) ? DamageCause.BREAK_BLOCK : DamageCause.BREAK_BAD_BLOCK,
                              entity, e -> e.broadcastBreakEvent(e.getUsedItemHand()));
        }
        return true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && this.isThrowable(stack)) {
            int i = this.getUseDuration(stack) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    EntitySpear spear = new EntitySpear(level, player, stack);
                    spear.shoot(player, player.getXRot(), player.getYRot(), 0.825f, 2.5F);
                    if (player.getAbilities().instabuild) {
                        spear.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                    }
                    level.addFreshEntity(spear);
                    IModularTool tool = IModularTool.get(stack);
                    level.playSound(null, spear, tool.getHead().getMaterial().getMaterial().isStone() ?
                                                 EvolutionSounds.STONE_SPEAR_THROW.get() :
                                                 EvolutionSounds.METAL_SPEAR_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!player.getAbilities().instabuild) {
                        player.getInventory().removeItem(stack);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                this.addStat(player);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isBroken(stack) || this.isTwoHanded(stack) && hand == InteractionHand.OFF_HAND) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        if (!((ILivingEntityPatch) player).renderMainhandSpecialAttack() && this.isThrowable(stack)) {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }
}
