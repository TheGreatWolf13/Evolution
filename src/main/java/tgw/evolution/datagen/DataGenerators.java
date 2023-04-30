package tgw.evolution.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import tgw.evolution.Evolution;
import tgw.evolution.patches.obj.GatherDataEventPlus;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = Evolution.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        Collection<Path> existingPaths = event instanceof GatherDataEventPlus plus ? plus.getExistingPaths() : Collections.EMPTY_LIST;
        GenBundle bundle = new GenBundle(generator, existingFileHelper, existingPaths);
        generator.addProvider(new RecipeProvider(bundle));
        generator.addProvider(new LootTableProvider(bundle));
        generator.addProvider(new AdvancementProvider(bundle));
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(bundle);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new ItemTagsProvider(bundle, blockTagsProvider));
        generator.addProvider(new FluidTagsProvider(bundle));
    }
}
