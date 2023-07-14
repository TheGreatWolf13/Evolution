package tgw.evolution.capabilities.modular.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPartGuard;
import tgw.evolution.util.collection.lists.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public class PartGuard implements IPart<PartTypes.Guard, ItemPartGuard, PartGuard> {

    @Override
    public void appendText(EitherList<FormattedText, TooltipComponent> tooltip, int num) {
        //TODO implementation

    }

    @Override
    public void damage(int amount) {
        //TODO implementation

    }

    @Override
    public String getDescriptionId(CompoundTag tag) {
        //TODO implementation
        return "null";
    }

    @Override
    public int getDurabilityDmg(CompoundTag tag) {
        //TODO implementation
        return 0;
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
    public PartTypes.Guard getType() {
        //TODO implementation
        return PartTypes.Guard.NULL;
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartGuard part) {
        //TODO implementation
        return false;
    }

    @Override
    public void set(ItemStack stack, PartTypes.Guard type, EvolutionMaterials material) {
        //TODO implementation

    }

    @Override
    public void set(PartTypes.Guard type, MaterialInstance material) {
        //TODO implementation

    }
}
