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
            builder.push("Common");
            this.torchTime = builder.translation("evolution.config.torchTime")
                                    .defineInRange("torchTime", 10, 0, Time.YEAR_IN_TICKS / Time.HOUR_IN_TICKS);
            builder.pop();
        }
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue crazyMode;
        public final ForgeConfigSpec.BooleanValue firstPersonRenderer;
        public final ForgeConfigSpec.BooleanValue hitmarkers;
        public final ForgeConfigSpec.BooleanValue limitTimeUnitsToHour;

        Client(final ForgeConfigSpec.Builder builder) {
            builder.push("Client");
            this.crazyMode = builder.translation("evolution.config.crazyMode").define("crazyMode", false);
            this.limitTimeUnitsToHour = builder.translation("evolution.config.limitTimeUnitsToHour").define("limitTimeUnitsToHour", false);
            this.hitmarkers = builder.translation("evolution.config.hitmarkers").define("hitmarkers", true);
            this.firstPersonRenderer = builder.translation("evolution.config.firstPersonRenderer").define("firstPersonRenderer", true);
            builder.pop();
        }
    }
}
