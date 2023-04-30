package tgw.evolution.datagen;

import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionFluids;

import java.util.Collections;

public class FluidTagsProvider extends TagsProvider<Fluid> {

    public FluidTagsProvider(GenBundle bundle) {
        super(bundle, Registry.FLUID, Evolution.MODID, Collections.EMPTY_LIST);
    }

    @Override
    protected void addTags() {
        this.tag(FluidTags.WATER).add(EvolutionFluids.FRESH_WATER.get(), EvolutionFluids.SALT_WATER.get());
    }

    @Override
    public String getName() {
        return "Evolution Fluid Tags";
    }

    @Override
    protected String tagType() {
        return "Fluid";
    }
}
