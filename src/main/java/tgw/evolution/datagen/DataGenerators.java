package tgw.evolution.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import tgw.evolution.Evolution;

@Mod.EventBusSubscriber(modid = Evolution.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(new EvRecipeProvider(generator));
        generator.addProvider(new EvLootTableProvider(generator));
        generator.addProvider(new EvAdvancementProvider(generator, existingFileHelper));
        EvBlockTagsProvider blockTagsProvider = new EvBlockTagsProvider(generator, existingFileHelper);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new EvItemTagsProvider(generator, blockTagsProvider, existingFileHelper));
        generator.addProvider(new EvFluidTagsProvider(generator, existingFileHelper));
    }
}
