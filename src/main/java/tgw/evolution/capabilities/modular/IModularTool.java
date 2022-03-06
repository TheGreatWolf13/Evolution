package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.part.GrabPart;
import tgw.evolution.capabilities.modular.part.HandlePart;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public interface IModularTool extends IModular {

    IModularTool NULL = new Impl();

    double getAttackSpeed();

    int getBackPriority();

    GrabPart<PartTypes.Handle> getHandle();

    ItemMaterial getHandleMaterial();

    PartTypes.Handle getHandleType();

    HeadPart getHead();

    ItemMaterial getHeadMaterial();

    PartTypes.Head getHeadType();

    float getMiningSpeed();

    double getMoment();

    double getReach();

    boolean isSharpened();

    void setHandle(PartTypes.Handle handleType, MaterialInstance material);

    void setHead(PartTypes.Head headType, MaterialInstance material);

    void sharp();

    final class Impl implements IModularTool {
        private final HandlePart handle = new HandlePart();
        private final HeadPart head = new HeadPart();

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
        public GrabPart<PartTypes.Handle> getHandle() {
            return this.handle;
        }

        @Override
        public ItemMaterial getHandleMaterial() {
            return this.handle.getMaterial().getMaterial();
        }

        @Override
        public PartTypes.Handle getHandleType() {
            return this.handle.getType();
        }

        @Override
        public int getHarvestLevel() {
            return HarvestLevel.HAND;
        }

        @Override
        public HeadPart getHead() {
            return this.head;
        }

        @Override
        public ItemMaterial getHeadMaterial() {
            return this.head.getMaterial().getMaterial();
        }

        @Override
        public PartTypes.Head getHeadType() {
            return this.head.getType();
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
        public double getReach() {
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
            return new CompoundTag();
        }

        @Override
        public void setDurabilityDmg(int damage) {
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
