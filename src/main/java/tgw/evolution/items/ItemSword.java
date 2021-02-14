package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemSword extends ItemGenericTool implements IOffhandAttackable, ISweepAttack, IParry, ILunge, IBeltWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet();
    private final double mass;

    public ItemSword(float attackSpeed, IItemTier tier, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder, ToolTypeEv.SWORD);
        this.mass = mass;
    }

    @Override
    public float baseDamage() {
        return 6.0f;
    }

    @Override
    public int blockDurabilityDamage() {
        return 2;
    }

    @Override
    public int entityDurabilityDamage() {
        return 1;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.SLASHING;
    }

    @Override
    public float getEfficiency() {
        return 0;
    }

    @Override
    public int getFullLungeTime() {
        return 24;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public int getMinLungeTime() {
        return 4;
    }

    @Override
    public float getParryPercentage(ItemStack stack) {
        return 0.1F;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public float getSweepRatio() {
        return 0.1f;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public float reach() {
        return 4.0f;
    }

    @Override
    public double useItemSlowDownRate() {
        return 0.7;
    }
}
