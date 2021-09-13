package tgw.evolution.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.util.PlayerHelper;

import javax.annotation.Nonnull;
import java.util.Set;

public abstract class ItemGenericTool extends ItemTiered implements IDurability, IMelee, IMass {

    protected final float attackSpeed;
    protected final float efficiency;
    private final Set<Block> effectiveBlocks;
    private final Set<Material> effectiveMaterials;

    protected ItemGenericTool(float attackSpeed,
                              IItemTier tier,
                              Set<Block> effectiveBlocks,
                              Set<Material> effectiveMaterials,
                              Item.Properties builder,
                              ToolType tool) {
        super(tier, builder.addToolType(tool, tier.getLevel()));
        this.effectiveBlocks = effectiveBlocks;
        this.effectiveMaterials = effectiveMaterials;
        this.efficiency = tier.getSpeed();
        this.attackSpeed = attackSpeed;
    }

    public abstract float baseDamage();

    public abstract int blockDurabilityDamage();

    @Override
    public boolean canAttackBlock(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }

    public abstract int entityDurabilityDamage();

    @Override
    public double getAttackDamage() {
        return this.baseDamage() + this.getTier().getAttackDamageBonus();
    }

    @Override
    public double getAttackSpeed() {
        return this.attackSpeed - PlayerHelper.ATTACK_SPEED;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(this.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlotType.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                                              "Damage modifier",
                                              this.getAttackDamage(),
                                              AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", this.getAttackSpeed(), AttributeModifier.Operation.ADDITION));
            builder.put(ForgeMod.REACH_DISTANCE.get(),
                        new AttributeModifier(EvolutionAttributes.REACH_DISTANCE_MODIFIER,
                                              "Reach Modifier",
                                              this.getReach(),
                                              AttributeModifier.Operation.ADDITION));
            builder.put(EvolutionAttributes.MASS.get(),
                        new AttributeModifier(EvolutionAttributes.MASS_MODIFIER,
                                              "Mass Modifier",
                                              this.getMass(),
                                              AttributeModifier.Operation.ADDITION));
        }
        else if (slot == EquipmentSlotType.OFFHAND) {
            builder.put(EvolutionAttributes.MASS.get(),
                        new AttributeModifier(EvolutionAttributes.MASS_MODIFIER_OFFHAND,
                                              "Mass Modifier",
                                              this.getMass(),
                                              AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
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
        return this.reach() - PlayerHelper.REACH_DISTANCE;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(this.entityDurabilityDamage(), attacker, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        if (this.effectiveMaterials.contains(state.getMaterial())) {
            return this.getTier().getLevel() >= state.getHarvestLevel();
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, @Nonnull World world, BlockState state, BlockPos pos, LivingEntity livingEntity) {
        if (!world.isClientSide && state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(this.blockDurabilityDamage(), livingEntity, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
        }
        return true;
    }

    public abstract float reach();
}