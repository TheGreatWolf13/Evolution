package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPartPommel;
import tgw.evolution.util.collection.lists.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public class PartPommel implements IPartHit<PartTypes.Pommel, ItemPartPommel, PartPommel> {

    @Override
    public void appendText(EitherList<FormattedText, TooltipComponent> tooltip, int num) {
        //TODO implementation

    }

    @Override
    public boolean canBeSharpened() {
        //TODO implementation
        return false;
    }

    @Override
    public void damage(int amount) {
        //TODO implementation

    }

    @Override
    public SoundEvent getBlockHitSound() {
        //TODO implementation
        return null;
    }

    @Override
    public EvolutionDamage.Type getDamageType() {
        //TODO implementation
        return null;
    }

    @Override
    public String getDescriptionId(CompoundTag tag) {
        //TODO implementation
        return "null";
    }

    @Override
    public double getDmgMultiplierInternal() {
        //TODO implementation
        return 0;
    }

    @Override
    public int getDurabilityDmg(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public ReferenceSet<Material> getEffectiveMaterials() {
        //TODO implementation
        return null;
    }

    @Override
    public @HarvestLevel int getHarvestLevel() {
        //TODO implementation
        return 0;
    }

    @Override
    public MaterialInstance getMaterialInstance(CompoundTag tag) {
        //TODO implementation
        return new MaterialInstance();
    }

    @Override
    public int getMaxDamage(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public int getMaxDurability() {
        //TODO implementation
        return 0;
    }

    @Override
    public float getMiningSpeed() {
        //TODO implementation
        return 0;
    }

    @Override
    public int getSharpAmount() {
        //TODO implementation
        return 0;
    }

    @Override
    public PartTypes.Pommel getType() {
        //TODO implementation
        return PartTypes.Pommel.NULL;
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartPommel part) {
        //TODO implementation
        return false;
    }

    @Override
    public void loseSharp(int amount) {
        //TODO implementation

    }

    @Override
    public void set(ItemStack stack, PartTypes.Pommel type, EvolutionMaterials material) {
        //TODO implementation

    }

    @Override
    public void set(PartTypes.Pommel type, MaterialInstance material) {
        //TODO implementation

    }

    @Override
    public void sharp() {
        //TODO implementation

    }
}
