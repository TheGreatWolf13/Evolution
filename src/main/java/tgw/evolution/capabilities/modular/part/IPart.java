package tgw.evolution.capabilities.modular.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.collection.lists.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public interface IPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> {

    void appendText(EitherList<FormattedText, TooltipComponent> tooltip, int num);

    void damage(int amount);

    String getDescriptionId(CompoundTag tag);

    int getDurabilityDmg(CompoundTag tag);

    @HarvestLevel
    int getHarvestLevel();

    default double getMass(CompoundTag tag) {
        return 1.378_615e-6 * this.getType().getVolume(this.getMaterialInstance(tag).getMaterial()) * this.getMaterialInstance(tag).getDensity();
    }

    MaterialInstance getMaterialInstance(CompoundTag tag);

    int getMaxDamage(CompoundTag tag);

    int getMaxDurability();

    T getType();

    boolean isBroken();

    boolean isSimilar(P part);

    void set(ItemStack stack, T type, EvolutionMaterials material);

    void set(T type, MaterialInstance material);
}
