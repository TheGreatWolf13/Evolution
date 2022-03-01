package tgw.evolution.capabilities.modular.part;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.intellij.lang.annotations.MagicConstant;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.items.IDurability;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public interface IPart<T extends IPartType<T>> extends IDurability {

    void appendText(List<Either<FormattedText, TooltipComponent>> tooltip, int num);

    void damage(int amount);

    @Override
    default int getDmg(ItemStack stack) {
        return this.getDurabilityDmg();
    }

    int getDurabilityDmg();

    @MagicConstant(valuesFromClass = HarvestLevel.class)
    int getHarvestLevel();

    default double getMass() {
        return 1.378_615e-6 * this.getType().getVolume() * this.getMaterial().getDensity();
    }

    MaterialInstance getMaterial();

    @Override
    default int getMaxDmg(ItemStack stack) {
        return this.getMaxDurability();
    }

    int getMaxDurability();

    T getType();

    CompoundTag write();
}
