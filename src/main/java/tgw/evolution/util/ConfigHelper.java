package tgw.evolution.util;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSSyncServerConfig;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.FunctionMethodHandler;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigHelper {

    private static final FunctionMethodHandler<ModConfig, Void, CommentedConfig> MOD_CONFIG_SET_CONFIG_DATA =
            new FunctionMethodHandler<>(ModConfig.class,
                                        "setConfigData",
                                        CommentedConfig.class);
    private static final FieldHandler<ConfigTracker, ConcurrentHashMap<String, ModConfig>> CONFIG_MAP = new FieldHandler<>(ConfigTracker.class,
                                                                                                                           "fileMap");
    private static final FunctionMethodHandler<ModConfig, Void, IConfigEvent> MOD_CONFIG_FIRE_EVENT = new FunctionMethodHandler<>(ModConfig.class,
                                                                                                                                  "fireEvent",
                                                                                                                                  IConfigEvent.class);

    private ConfigHelper() {
    }

    public static void fireEvent(ModConfig config, IConfigEvent event) {
        MOD_CONFIG_FIRE_EVENT.call(config, event);
    }

    public static List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> gatherAllConfigValues(ModConfig config) {
        return gatherAllConfigValues(((ForgeConfigSpec) config.getSpec()).getValues(), (ForgeConfigSpec) config.getSpec());
    }

    public static List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> gatherAllConfigValues(UnmodifiableConfig config,
                                                                                                              ForgeConfigSpec spec) {
        List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> values = new ArrayList<>();
        gatherValuesFromConfig(config, spec, values);
        return ImmutableList.copyOf(values);
    }

    private static void gatherValuesFromConfig(UnmodifiableConfig config,
                                               ForgeConfigSpec spec,
                                               List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> values) {
        for (Object o : config.valueMap().values()) {
            if (o instanceof AbstractConfig) {
                gatherValuesFromConfig((UnmodifiableConfig) o, spec, values);
            }
            else if (o instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
                //noinspection ObjectAllocationInLoop
                values.add(Pair.of(configValue, valueSpec));
            }
        }
    }

    @Nullable
    public static ModConfig getModConfig(String fileName) {
        ConcurrentHashMap<String, ModConfig> configMap = CONFIG_MAP.get(ConfigTracker.INSTANCE);
        return configMap != null ? configMap.get(fileName) : null;
    }

    public static boolean isModified(ModConfig config) {
        for (Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec> pair : gatherAllConfigValues(config)) {
            if (!pair.getLeft().get().equals(pair.getRight().getDefault())) {
                return true;
            }
        }
        return false;
    }

    public static void resetCache(ModConfig config) {
        for (Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec> pair : gatherAllConfigValues(config)) {
            pair.getLeft().clearCache();
        }
    }

    public static void sendConfigDataToServer(ModConfig config) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (config.getType() == ModConfig.Type.SERVER && minecraft.player != null && minecraft.player.hasPermissions(2)) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                TomlFormat.instance().createWriter().write(config.getConfigData(), stream);
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSyncServerConfig(config.getFileName(), stream.toByteArray()));
                stream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setConfigData(ModConfig config, @Nullable CommentedConfig configData) {
        MOD_CONFIG_SET_CONFIG_DATA.call(config, configData);
        if (configData instanceof FileConfig) {
            config.save();
        }
    }
}
