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
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.util.PlayerHelper;

import javax.annotation.Nonnull;
import java.util.Set;

public abstract class ItemGenericTool extends ItemTiered implements IDurability, IMelee, IMass {

    protected final float efficiency;
    protected final float attackSpeed;
    private final Set<Block> effectiveBlocks;
    private final Set<Material> effectiveMaterials;

    protected ItemGenericTool(float attackSpeed,
                              IItemTier tier,
                              Set<Block> effectiveBlocks,
                              Set<Material> effectiveMaterials,
                              Item.Properties builder) {
        super(tier, builder);
        this.effectiveBlocks = effectiveBlocks;
        this.effectiveMaterials = effectiveMaterials;
        this.efficiency = tier.getEfficiency();
        this.attackSpeed = attackSpeed;
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
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }

    @Override
    public double getAttackDamage() {
        return this.setBaseDamage() + this.getTier().getAttackDamage();
    }

    @Override
    public double getAttackSpeed() {
        return this.attackSpeed - PlayerHelper.ATTACK_SPEED;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot);
        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                         new AttributeModifier(ATTACK_DAMAGE_MODIFIER,
                                               "Tool modifier",
                                               this.getAttackDamage(),
                                               AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                         new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.getAttackSpeed(), AttributeModifier.Operation.ADDITION));
            multimap.put(PlayerEntity.REACH_DISTANCE.getName(),
                         new AttributeModifier(EvolutionAttributes.REACH_DISTANCE_MODIFIER,
                                               "Reach Modifier",
                                               this.getReach(),
                                               AttributeModifier.Operation.ADDITION));
            multimap.put(EvolutionAttributes.MASS.getName(),
                         new AttributeModifier(EvolutionAttributes.MASS_MODIFIER,
                                               "Mass Modifier",
                                               this.getMass(),
                                               AttributeModifier.Operation.ADDITION));
        }
        else if (slot == EquipmentSlotType.OFFHAND) {
            multimap.put(EvolutionAttributes.MASS.getName(),
                         new AttributeModifier(EvolutionAttributes.MASS_MODIFIER_OFFHAND,
                                               "Mass Modifier",
                                               this.getMass(),
                                               AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (this.effectiveMaterials.contains(state.getMaterial())) {
            return this.efficiency;
        }
        return this.effectiveBlocks.contains(state.getBlock()) ? this.efficiency : 1.0F;
    }

    public float getEfficiency() {
        return this.efficiency;
    }

    @Override
    public double getReach() {
        return this.setReach() - PlayerHelper.REACH_DISTANCE;
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damageItem(2, attacker, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, @Nonnull World world, BlockState state, BlockPos pos, LivingEntity livingEntity) {
        if (!world.isRemote && state.getBlockHardness(world, pos) != 0.0F) {
            stack.damageItem(1, livingEntity, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
        }
        return true;
    }

    public abstract float setBaseDamage();

    public abstract float setReach();
}