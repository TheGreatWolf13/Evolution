package tgw.evolution.capabilities.modular;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.PartHandle;
import tgw.evolution.capabilities.modular.part.PartHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.collection.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public interface IModularTool extends IModular {

    IModularTool NULL = new Impl();

    static IModularTool get(ItemStack stack) {
        return EvolutionCapabilities.getCapability(stack, CapabilityModular.TOOL, NULL);
    }

    int getBackPriority();

    int getCooldown();

    double getDmgMultiplier(EvolutionDamage.Type type);

    PartHandle getHandle();

    PartHead getHead();

    float getMiningSpeed();

    double getMoment();

    boolean isSharpened();

    void setHandle(PartTypes.Handle handleType, MaterialInstance material);

    void setHead(PartTypes.Head headType, MaterialInstance material);

    void sharp();

    final class Impl implements IModularTool {
        private final PartHandle handle = new PartHandle();
        private final PartHead head = new PartHead();
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
        public int getBackPriority() {
            return -1;
        }

        @Override
        public int getCooldown() {
            return 0;
        }

        @Override
        public String getDescriptionId() {
            return "null";
        }

        @Override
        public double getDmgMultiplier(EvolutionDamage.Type type) {
            return 0;
        }

        @Override
        public ReferenceSet<Material> getEffectiveMaterials() {
            return ReferenceSet.of();
        }

        @Override
        public PartHandle getHandle() {
            return this.handle;
        }

        @Override
        public int getHarvestLevel() {
            return HarvestLevel.HAND;
        }

        @Override
        public PartHead getHead() {
            return this.head;
        }

        @Override
        public double getMass() {
            return 0;
        }

        @Override
        public float getMiningSpeed() {
            return 0;
        }

        @Override
        public double getMoment() {
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
        public boolean isSharpened() {
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

        @Override
        public void setHandle(PartTypes.Handle handleType, MaterialInstance material) {
        }

        @Override
        public void setHead(PartTypes.Head headType, MaterialInstance material) {
        }

        @Override
        public void sharp() {
        }
    }
}
