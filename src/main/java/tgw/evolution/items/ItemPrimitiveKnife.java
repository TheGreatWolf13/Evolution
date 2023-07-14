package tgw.evolution.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.init.EvolutionSounds;

public class ItemPrimitiveKnife extends ItemEv implements IMelee {

    private final EvolutionMaterials material;

    public ItemPrimitiveKnife(EvolutionMaterials material, Properties properties) {
        super(properties);
        this.material = material;
    }

    @Override
    public int getAutoAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    public BasicAttackType getBasicAttackType(ItemStack stack) {
        return BasicAttackType.PRIMITIVE_KNIFE_STRIKE;
    }

    @Override
    public SoundEvent getBlockHitSound(ItemStack stack) {
        return EvolutionSounds.STONE_WEAPON_HIT_BLOCK;
    }

    @Override
    public @Nullable ChargeAttackType getChargeAttackType(ItemStack stack) {
        return null;
    }

    @Override
    public int getCooldown(ItemStack stack) {
        //TODO implementation
        return 20;
    }

    @Override
    public double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type) {
        //TODO implementation
        double mult = 0.87 * (0.65 * this.material.getModulusOfElasticity() / 3.5 + 0.35 * 12.5);
//        return this.head.getDmgMultiplierInternal() * mult / PlayerHelper.ATTACK_DAMAGE;
        return 2;
    }

    @Override
    public int getMinAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    public boolean isHoldable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean shouldPlaySheatheSound(ItemStack stack) {
        return false;
    }
}
