package tgw.evolution.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeMod;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.PlayerHelper;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ItemGenericTool extends ItemModular implements IDurability, IMelee, IMass {

    protected final float attackSpeed;
    protected final float efficiency;
    private final Set<Block> effectiveBlocks;
    private final Set<Material> effectiveMaterials;
    private final ToolType tool;

    protected ItemGenericTool(float attackSpeed,
                              ItemMaterial tier,
                              Set<Block> effectiveBlocks,
                              Set<Material> effectiveMaterials,
                              Item.Properties builder,
                              ToolType tool) {
        super(builder);
        this.effectiveBlocks = effectiveBlocks;
        this.effectiveMaterials = effectiveMaterials;
        this.efficiency = 0 /*tier.getSpeed()*/;
        this.attackSpeed = attackSpeed;
        this.tool = tool;
    }

    public abstract float baseDamage();

    public abstract int blockDurabilityDamage();

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public abstract int entityDurabilityDamage();

    @Override
    public double getAttackDamage(ItemStack stack) {
        return this.baseDamage()/* + this.getTier().getAttackDamageBonus()*/;
    }

    @Override
    public double getAttackSpeed(ItemStack stack) {
        return this.attackSpeed - PlayerHelper.ATTACK_SPEED;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        switch (slot) {
            case MAINHAND -> {
                builder.put(Attributes.ATTACK_DAMAGE, EvolutionAttributes.attackDamageModifier(this.getAttackDamage(stack), SlotType.MAINHAND));
                builder.put(Attributes.ATTACK_SPEED, EvolutionAttributes.attackSpeedModifier(this.getAttackSpeed(stack), SlotType.MAINHAND));
                builder.put(ForgeMod.REACH_DISTANCE.get(), EvolutionAttributes.reachModifier(this.getReach(stack), SlotType.MAINHAND));
                builder.put(EvolutionAttributes.MASS.get(), EvolutionAttributes.massModifier(this.getMass(stack), SlotType.MAINHAND));
            }
            case OFFHAND -> builder.put(EvolutionAttributes.MASS.get(), EvolutionAttributes.massModifier(this.getMass(stack), SlotType.OFFHAND));
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
    public double getReach(ItemStack stack) {
        return this.reach() - PlayerHelper.REACH_DISTANCE;
    }

    @Override
    public <T extends LivingEntity> void hurtAndBreak(ItemStack stack, DamageCause cause, T entity, Consumer<T> onBroken) {
        //TODO implementation

    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(this.entityDurabilityDamage(), attacker, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
        return true;
    }

    @Override
    public boolean isBroken(ItemStack stack) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        if (this.effectiveMaterials.contains(state.getMaterial())) {
//            return this.getTier().getLevel() >= ((IBlockPatch) state.getBlock()).getHarvestLevel(state);
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, @Nonnull Level level, BlockState state, BlockPos pos, LivingEntity livingEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(this.blockDurabilityDamage(), livingEntity, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
        }
        return true;
    }

    public abstract float reach();
}