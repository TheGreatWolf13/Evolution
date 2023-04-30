package tgw.evolution.patches.obj;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.nio.file.Path;
import java.util.Collection;

public class GatherDataEventPlus extends GatherDataEvent {

    private final Collection<Path> existingPaths;

    public GatherDataEventPlus(ModContainer mc,
                               DataGenerator dataGenerator,
                               DataGeneratorConfig dataGeneratorConfig, ExistingFileHelper existingFileHelper, Collection<Path> existingPaths) {
        super(mc, dataGenerator, dataGeneratorConfig, existingFileHelper);
        this.existingPaths = existingPaths;
    }

    public Collection<Path> getExistingPaths() {
        return this.existingPaths;
    }
}
