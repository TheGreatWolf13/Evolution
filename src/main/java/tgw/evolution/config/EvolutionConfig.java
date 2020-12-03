package tgw.evolution.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.util.Time;

public final class EvolutionConfig {

    public static final Common COMMON;
    public static final Client CLIENT;
    private static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

    private EvolutionConfig() {
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue torchTime;

        Common(final ForgeConfigSpec.Builder builder) {
            builder.comment("Common config settings").push("Common");
            this.torchTime = builder.comment("Define the time in game hours the torch will be lit for. 0 will disable unliting.", "Default: 10")
                                    .defineInRange("Torch Time", 10, 0, Time.YEAR_IN_TICKS / Time.HOUR_IN_TICKS);
            builder.pop();
        }
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue crazyMode;

        Client(final ForgeConfigSpec.Builder builder) {
            builder.comment("Client-only settings").push("Client");
            this.crazyMode = builder.comment("I wouldn't use this if I were you... Might have some strange effects.", "Default: false")
                                    .define("Crazy Mode", false);
            builder.pop();
        }
    }
}
