package tgw.evolution.items;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.UUID;

public abstract class ItemTool extends TieredItem implements IDurability {

    public static final UUID REACH_DISTANCE_MODIFIER = UUID.fromString("449b8c5d-47b0-4c67-a90e-758b956f2d3c");
    private final Set<Block> effectiveBlocks;
    private final Set<Material> effectiveMaterials;
    protected final float efficiency;
    protected final float attackSpeed;

    protected ItemTool(float attackSpeedIn, IItemTier tier, Set<Block> effectiveBlocksIn, Set<Material> effectiveMaterials, Item.Properties builder) {
        super(tier, builder);
        this.effectiveBlocks = effectiveBlocksIn;
        this.effectiveMaterials = effectiveMaterials;
        this.efficiency = tier.getEfficiency();
        this.attackSpeed = attackSpeedIn;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.effectiveMaterials.contains(state.getMaterial())) {
            return this.efficiency;
        }
        return this.effectiveBlocks.contains(state.getBlock()) ? this.efficiency : 1.0F;
    }

    @Override
    public boolean canHarvestBlock(BlockState state) {
        int i = this.getTier().getHarvestLevel();
        if (this.effectiveMaterials.contains(state.getMaterial())) {
            return i >= state.getHarvestLevel();
        }
        return false;
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damageItem(2, attacker, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isRemote && state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.damageItem(1, entityLiving, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
        }
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this.getAttackDamage(), AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.getAttackSpeed(), AttributeModifier.Operation.ADDITION));
            multimap.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(REACH_DISTANCE_MODIFIER, "Reach Modifier", this.getReach(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected abstract float setReach();

    protected abstract float setBaseDamage();

    public double getReach() {
        return this.setReach() - 5F;
    }

    public float getEfficiency() {
        return this.efficiency;
    }

    public float getAttackDamage() {
        return this.setBaseDamage() + this.getTier().getAttackDamage();
    }

    public float getAttackSpeed() {
        return this.attackSpeed - 4;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}