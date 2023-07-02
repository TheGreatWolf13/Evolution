package tgw.evolution.capabilities.modular;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.INBTSerializable;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.collection.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public interface IModular extends INBTSerializable<CompoundTag> {

    IModular NULL = new Impl();

    void appendPartTooltip(EitherList<FormattedText, TooltipComponent> tooltip);

    void damage(ItemModular.DamageCause cause, @HarvestLevel int harvestLevel);

    String getDescriptionId();

    ReferenceSet<Material> getEffectiveMaterials();

    @HarvestLevel
    int getHarvestLevel();

    double getMass();

    int getTotalDurabilityDmg();

    int getTotalMaxDurability();

    boolean isAxe();

    boolean isBroken();

    boolean isHammer();

    boolean isShovel();

    boolean isSimilar(IModular modular);

    boolean isSword();

    boolean isTwoHanded();

    final class Impl implements IModular {

        private final CompoundTag tag = new CompoundTag();

        private Impl() {
        }

        @Override
        public void appendPartTooltip(EitherList<FormattedText, TooltipComponent> tooltip) {
        }

        @Override
        public void damage(ItemModular.DamageCause cause, @HarvestLevel int harvestLevel) {
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
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
        public boolean isAxe() {
            return false;
        }

        @Override
        public boolean isBroken() {
            return false;
        }

        @Override
        public boolean isHammer() {
            return false;
        }

        @Override
        public boolean isShovel() {
            return false;
        }

        @Override
        public boolean isSimilar(IModular modular) {
            return false;
        }

        @Override
        public boolean isSword() {
            return false;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.tag;
        }
    }
}
