package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.INBTSerializable;
import org.intellij.lang.annotations.MagicConstant;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.ItemModular;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public interface IModular extends INBTSerializable<CompoundTag> {

    IModular NULL = new Impl();

    void appendTooltip(List<Either<FormattedText, TooltipComponent>> tooltip);

    void damage(ItemModular.DamageCause cause);

    double getAttackDamage();

    EvolutionDamage.Type getDamageType();

    String getDescriptionId();

    ReferenceSet<Material> getEffectiveMaterials();

    @MagicConstant(valuesFromClass = HarvestLevel.class)
    int getHarvestLevel();

    double getMass();

    int getTotalDurabilityDmg();

    int getTotalMaxDurability();

    boolean isBroken();

    boolean isInit();

    boolean isTwoHanded();

    void setDurabilityDmg(int damage);

    final class Impl implements IModular {

        private Impl() {
        }

        @Override
        public void appendTooltip(List<Either<FormattedText, TooltipComponent>> tooltip) {
        }

        @Override
        public void damage(ItemModular.DamageCause cause) {
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }

        @Override
        public double getAttackDamage() {
            return 0;
        }

        @Override
        public EvolutionDamage.Type getDamageType() {
            return EvolutionDamage.Type.GENERIC;
        }

        @Override
        public String getDescriptionId() {
            return "null";
        }

        @Override
        public ReferenceSet<Material> getEffectiveMaterials() {
            return ReferenceSet.of();
        }

        @Override
        public int getHarvestLevel() {
            return HarvestLevel.HAND;
        }

        @Override
        public double getMass() {
            return 0;
        }

        @Override
        public int getTotalDurabilityDmg() {
            return 0;
        }

        @Override
        public int getTotalMaxDurability() {
            return 0;
        }

        @Override
        public boolean isBroken() {
            return false;
        }

        @Override
        public boolean isInit() {
            return false;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void setDurabilityDmg(int damage) {
        }
    }
}
