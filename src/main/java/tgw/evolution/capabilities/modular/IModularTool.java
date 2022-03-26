package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.HandlePart;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.HarvestLevels;

import java.util.List;

public interface IModularTool extends IModular {

    IModularTool NULL = new Impl();

    static IModularTool get(ItemStack stack) {
        return stack.getCapability(CapabilityModular.TOOL).orElse(NULL);
    }

    double getAttackSpeed();

    int getBackPriority();

    HandlePart getHandle();

    HeadPart getHead();

    float getMiningSpeed();

    double getMoment();

    boolean isSharpened();

    void setHandle(PartTypes.Handle handleType, MaterialInstance material);

    void setHead(PartTypes.Head headType, MaterialInstance material);

    void sharp();

    final class Impl implements IModularTool {
        private final HandlePart handle = new HandlePart();
        private final HeadPart head = new HeadPart();
        private final CompoundTag tag = new CompoundTag();

        private Impl() {
        }

        @Override
        public void appendTooltip(List<Either<FormattedText, TooltipComponent>> tooltip) {
        }

        @Override
        public void damage(ItemModular.DamageCause cause, @HarvestLevel int harvestLevel) {
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }

        @Override
        public double getAttackDamage() {
            return 0;
        }

        @Override
        public double getAttackSpeed() {
            return 0;
        }

        @Override
        public int getBackPriority() {
            return -1;
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
        public HandlePart getHandle() {
            return this.handle;
        }

        @Override
        public int getHarvestLevel() {
            return HarvestLevels.HAND;
        }

        @Override
        public HeadPart getHead() {
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
        public boolean isSharpened() {
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
