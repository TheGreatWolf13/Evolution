package tgw.evolution.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionFluids;

public class EvFluidTagsProvider extends FluidTagsProvider {

    public EvFluidTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Evolution.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(FluidTags.WATER).add(EvolutionFluids.FRESH_WATER.get(), EvolutionFluids.SALT_WATER.get());
    }
}
