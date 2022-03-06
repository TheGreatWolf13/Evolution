package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.intellij.lang.annotations.MagicConstant;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public class MaterialInstance {

    public static final MaterialInstance DUMMY = new MaterialInstance(ItemMaterial.STONE_ANDESITE);
    private final ItemMaterial material;

    public MaterialInstance(ItemMaterial material) {
        this.material = material;
    }

    public static MaterialInstance fromNBT(CompoundTag nbt) {
        ItemMaterial material = ItemMaterial.valueOf(nbt.getString("Material"));
        return new MaterialInstance(material);
    }

    public static MaterialInstance read(CompoundTag nbt) {
        ItemMaterial material = ItemMaterial.byName(nbt.getString("Material"));
        return new MaterialInstance(material);
    }

    public void appendText(List<Either<FormattedText, TooltipComponent>> tooltip) {
        tooltip.add(Either.left(EvolutionTexts.material(this.material)));
    }

    public double getDensity() {
        return this.material.getDensity();
    }

    public int getElasticModulus() {
        return this.material.getModulusOfElasticity();
    }

    public int getHardness() {
        return this.material.getHardness();
    }

    @MagicConstant(valuesFromClass = HarvestLevel.class)
    public int getHarvestLevel() {
        return this.material.getHarvestLevel();
    }

    public ItemMaterial getMaterial() {
        return this.material;
    }

    public String getName() {
        return this.material.getName();
    }

    public int getResistance() {
        return this.material.getResistance();
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Material", this.material.getName());
        return tag;
    }
}
