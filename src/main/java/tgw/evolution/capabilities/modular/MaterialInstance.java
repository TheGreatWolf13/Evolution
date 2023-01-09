package tgw.evolution.capabilities.modular;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.init.Material;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

public class MaterialInstance {

    public static final MaterialInstance DUMMY = new MaterialInstance(Material.ANDESITE);
    private final Material material;
    private @Nullable CompoundTag tag;

    public MaterialInstance(Material material) {
        this.material = material;
    }

    @Contract(pure = true, value = "_ -> new")
    public static MaterialInstance read(CompoundTag nbt) {
        Material material = Material.byId(nbt.getByte("Material"));
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

    @HarvestLevel
    public int getHarvestLevel() {
        return this.material.getHarvestLevel();
    }

    public Material getMaterial() {
        return this.material;
    }

    public String getName() {
        return this.material.getName();
    }

    public int getResistance() {
        return this.material.getResistance();
    }

    public boolean isSimilar(MaterialInstance other) {
        return this.material == other.material;
    }

    public CompoundTag write() {
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
        this.tag.putByte("Material", this.material.getId());
        return this.tag;
    }
}
