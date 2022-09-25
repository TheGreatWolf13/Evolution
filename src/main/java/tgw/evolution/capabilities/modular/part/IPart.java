package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.IDurability;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public interface IPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>>
        extends IDurability, INBTSerializable<CompoundTag> {

    void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num);

    void damage(int amount);

    String getDescriptionId();

    @Override
    default int getDmg(ItemStack stack) {
        return this.getDurabilityDmg();
    }

    int getDurabilityDmg();

    @HarvestLevel
    int getHarvestLevel();

    default double getMass() {
        return 1.378_615e-6 * this.getType().getVolume(this.getMaterialInstance().getMaterial()) * this.getMaterialInstance().getDensity();
    }

    MaterialInstance getMaterialInstance();

    @Override
    default int getMaxDmg(ItemStack stack) {
        return this.getMaxDurability();
    }

    int getMaxDurability();

    T getType();

    void init(T type, ItemMaterial material);

    boolean isBroken();

    boolean isSimilar(P part);

    void set(T type, MaterialInstance material);
}
