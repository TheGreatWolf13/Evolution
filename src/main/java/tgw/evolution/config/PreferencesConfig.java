package tgw.evolution.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class PreferencesConfig {

    public static ForgeConfigSpec.IntValue torchTime;

    public static void init(ForgeConfigSpec.Builder server) {
        server.comment("Config");
        torchTime = server.comment("Define the time in game hours the torch will be lit for. 0 will disable unliting (Default = 36)").defineInRange("evolution:torch_time", 36, 0, 2000000);
    }
}
