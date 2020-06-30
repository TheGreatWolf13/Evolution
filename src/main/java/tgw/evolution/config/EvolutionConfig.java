package tgw.evolution.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import tgw.evolution.Evolution;

import java.io.File;

@Mod.EventBusSubscriber
public class EvolutionConfig {

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;

    static {
        PreferencesConfig.init(COMMON_BUILDER);
        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        Evolution.LOGGER.info("Loading config: " + path);
        CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        Evolution.LOGGER.info("Built config: " + path);
        file.load();
        Evolution.LOGGER.info("Loaded config: " + path);
        config.setConfig(file);
    }
}
