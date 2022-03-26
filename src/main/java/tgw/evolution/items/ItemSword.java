package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.ItemMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class ItemSword extends ItemGenericTool implements IOffhandAttackable, ISweepAttack, IParry, ILunge, IBeltWeapon, ISpecialAttack {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet();
    private final double mass;

    public ItemSword(float attackSpeed, ItemMaterial tier, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder, ToolType.SWORD);
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
    public BasicAttackType getBasicAttackType(ItemStack stack) {
        return BasicAttackType.SWORD;
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
    public double getMass(ItemStack stack) {
        return this.mass;
    }

    @Override
    public int getMinLungeTime() {
        return 4;
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModular.NULL;
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
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
    public float reach() {
        return 4.0f;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }

    @Override
    public boolean useItemPreventsSprinting() {
        return true;
    }

    @Override
    public double useItemSlowDownRate() {
        return 0.7;
    }
}
