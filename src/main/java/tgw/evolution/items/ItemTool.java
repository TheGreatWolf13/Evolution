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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.entities.EvolutionAttributes;

import java.util.Set;

public abstract class ItemTool extends ItemTiered implements IDurability {

    protected final float efficiency;
    protected final float attackSpeed;
    private final Set<Block> effectiveBlocks;
    private final Set<Material> effectiveMaterials;

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
            multimap.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(EvolutionAttributes.REACH_DISTANCE_MODIFIER, "Reach Modifier", this.getReach(), AttributeModifier.Operation.ADDITION));
            multimap.put(EvolutionAttributes.MASS.getName(), new AttributeModifier(EvolutionAttributes.MASS_MODIFIER, "Mass Modifier", 5, AttributeModifier.Operation.ADDITION));
        }
        else if (equipmentSlot == EquipmentSlotType.OFFHAND) {
            multimap.put(EvolutionAttributes.MASS.getName(), new AttributeModifier(EvolutionAttributes.MASS_MODIFIER_OFFHAND, "Mass Modifier", 3, AttributeModifier.Operation.ADDITION));
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
}