package tgw.evolution.mixin;

import net.minecraft.server.Bootstrap;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.obj.GatherDataEventPlus;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@Mixin(DatagenModLoader.class)
public abstract class DatagenModLoaderMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private static boolean runningDataGen;

    @Shadow
    private static GatherDataEvent.DataGeneratorConfig dataGeneratorConfig;

    @Shadow
    private static ExistingFileHelper existingFileHelper;

    /**
     * @author TheGreatWolf
     * @reason Give me the paths to existing files since the ExistingFileHelper doesn't seem to help
     */
    @Overwrite
    public static void begin(final Set<String> mods,
                             final Path path,
                             final Collection<Path> inputs,
                             Collection<Path> existingPacks,
                             Set<String> existingMods,
                             final boolean serverGenerators,
                             final boolean clientGenerators,
                             final boolean devToolGenerators,
                             final boolean reportsGenerator,
                             final boolean structureValidator,
                             final boolean flat,
                             final String assetIndex,
                             final File assetsDir) {
        if (mods.contains("minecraft") && mods.size() == 1) {
            return;
        }
        LOGGER.info("Initializing Data Gatherer for mods {}", mods);
        runningDataGen = true;
        Bootstrap.bootStrap();
        dataGeneratorConfig = new GatherDataEvent.DataGeneratorConfig(mods, path, inputs, serverGenerators, clientGenerators, devToolGenerators,
                                                                      reportsGenerator, structureValidator, flat);
        ModLoader.get().gatherAndInitializeMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {});
        if (!mods.contains("forge")) {
            //If we aren't generating data for forge, automatically add forge as an existing so mods can access forge's data
            existingMods.add("forge");
        }
        existingFileHelper = new ExistingFileHelper(existingPacks, existingMods, structureValidator, assetIndex, assetsDir);
        ModLoader.get()
                 .runEventGenerator(mc -> new GatherDataEventPlus(mc, dataGeneratorConfig.makeGenerator(
                         p -> dataGeneratorConfig.isFlat() ? p : p.resolve(mc.getModId()), dataGeneratorConfig.getMods().contains(mc.getModId())),
                                                                  dataGeneratorConfig, existingFileHelper, existingPacks));
        dataGeneratorConfig.runAll();
    }
}
