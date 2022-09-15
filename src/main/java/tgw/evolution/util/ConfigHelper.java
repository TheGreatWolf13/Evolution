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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSSyncServerConfig;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigHelper {

    private static final Method FIRE_EVENT = ObfuscationReflectionHelper.findMethod(ModConfig.class, "fireEvent", ModConfig.class,
                                                                                    IConfigEvent.class);
    private static final Method SET_CONFIG_DATA = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", ModConfig.class,
                                                                                         CommentedConfig.class);

    private ConfigHelper() {
    }

    public static void fireEvent(ModConfig config, IConfigEvent event) {
        try {
            FIRE_EVENT.invoke(config, event);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke fireEvent method");
        }
    }

    public static List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> gatherAllConfigValues(ModConfig config) {
        return gatherAllConfigValues(((ForgeConfigSpec) config.getSpec()).getValues(), (ForgeConfigSpec) config.getSpec());
    }

    public static List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> gatherAllConfigValues(UnmodifiableConfig config,
                                                                                                              ForgeConfigSpec spec) {
        OList<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> values = new OArrayList<>();
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
        ConcurrentHashMap<String, ModConfig> configMap = ConfigTracker.INSTANCE.fileMap();
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
        try {
            SET_CONFIG_DATA.invoke(config, configData);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke setConfigData method");
        }
        if (configData instanceof FileConfig) {
            config.save();
        }
    }
}
