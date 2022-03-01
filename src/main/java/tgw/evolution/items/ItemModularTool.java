package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.GrabPart;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.patches.IBlockPatch;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ItemModularTool extends ItemModular implements ITwoHanded, IBackWeapon, IMelee {

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
        ItemStack stack = new ItemStack(EvolutionItems.modular_tool.get());
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        tool.setHead(new HeadPart(headType, new MaterialInstance(headMaterial)));
        tool.setHandle(new GrabPart<>(handleType, new MaterialInstance(handleMaterial)));
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
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getAttackDamage();
    }

    @Override
    public double getAttackSpeed(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getAttackSpeed();
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getDamageType();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        if (tool.getEffectiveMaterials().contains(state.getMaterial())) {
            return tool.getMiningSpeed();
        }
        return 1.0F;
    }

    public float getMiningSpeed(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getMiningSpeed();
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
    }

    @Override
    public int getPriority(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getBackPriority();
    }

    @Override
    public double getReach(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.getReach();
    }

    @Override
    public <T extends LivingEntity> void hurtAndBreak(ItemStack stack, DamageCause cause, T entity, Consumer<T> onBroken) {
        if (!entity.level.isClientSide && (!(entity instanceof Player player) || !player.getAbilities().instabuild)) {
            if (stack.isDamageableItem()) {
                this.damage(stack, cause);
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
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        if (tool.getEffectiveMaterials().contains(state.getMaterial())) {
            return tool.getHarvestLevel() >= ((IBlockPatch) state.getBlock()).getHarvestLevel(state);
        }
        return false;
    }

    @Override
    public boolean isTwoHanded(ItemStack stack) {
        IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
        return tool.isTwoHanded();
    }

    @Override
    public boolean mineBlock(ItemStack stack, @Nonnull Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            IModularTool tool = stack.getCapability(CapabilityModular.TOOL).orElse(IModularTool.NULL);
            this.hurtAndBreak(stack,
                              tool.getEffectiveMaterials().contains(state.getMaterial()) ? DamageCause.BREAK_BLOCK : DamageCause.BREAK_BAD_BLOCK,
                              entity,
                              e -> e.broadcastBreakEvent(e.getUsedItemHand()));
        }
        return true;
    }
}
