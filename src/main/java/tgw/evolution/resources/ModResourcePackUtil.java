package tgw.evolution.resources;

import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.level.DataPackConfig;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ModResourcePackUtil {

    private static final Pattern COMPILE = Pattern.compile("\"");

    private ModResourcePackUtil() {
    }

    /**
     * Appends mod resource packs to the given list.
     *
     * @param packs   the resource pack list to append
     * @param type    the type of resource
     * @param subPath the resource pack sub path directory in mods, may be {@code null}
     */
    public static void appendModResourcePacks(List<IModResourcePack> packs, PackType type, @Nullable String subPath) {
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            if ("builtin".equals(container.getMetadata().getType())) {
                continue;
            }
            IModResourcePack pack = ModPackResources.create(getName(container.getMetadata()), container, subPath, type,
                                                            PackActivationType.ALWAYS_ENABLED);
            if (pack != null) {
                packs.add(pack);
            }
        }
    }

    public static boolean containsDefault(String filename) {
        return "pack.mcmeta".equals(filename);
    }

    /**
     * Creates the default data pack settings that replaces
     * {@code DataPackSettings.SAFE_MODE} used in vanilla.
     *
     * @return the default data pack settings
     */
    public static DataPackConfig createDefaultDataPackSettings() {
        ModdedPackSource modResourcePackCreator = new ModdedPackSource(PackType.SERVER_DATA);
        List<Pack> moddedResourcePacks = new ArrayList<>();
        modResourcePackCreator.register(moddedResourcePacks::add);
        List<String> enabled = new ArrayList<>(DataPackConfig.DEFAULT.getEnabled());
        List<String> disabled = new ArrayList<>(DataPackConfig.DEFAULT.getDisabled());
        // This ensures that any built-in registered data packs by mods which needs to be enabled by default are
        // as the data pack screen automatically put any data pack as disabled except the Default data pack.
        for (Pack profile : moddedResourcePacks) {
            PackResources pack = profile.open();
            if (pack instanceof ModPackResources && ((ModPackResources) pack).getActivationType().isEnabledByDefault()) {
                enabled.add(profile.getId());
            }
            else {
                disabled.add(profile.getId());
            }
        }
        return new DataPackConfig(enabled, disabled);
    }

    public static String getName(ModMetadata info) {
        if (info.getName() != null) {
            return info.getName();
        }
        return "Mod \"" + info.getId() + "\"";
    }

    public static void loadDynamicRegistry(RegistryAccess.Writable dynamicRegistryManager) {
        try (PackRepository resourcePackManager = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(),
                                                                     new ModdedPackSource(PackType.SERVER_DATA))) {
            try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA,
                                                                                         resourcePackManager.openAllSelected())) {
                RegistryOps.createAndLoad(JsonOps.INSTANCE, dynamicRegistryManager, resourceManager);
            }
        }
    }

    public static @Nullable InputStream openDefault(ModMetadata info, PackType type, String filename) {
        if ("pack.mcmeta".equals(filename)) {
            String description = info.getName();
            if (description == null) {
                description = "";
            }
            else {
                description = COMPILE.matcher(description).replaceAll("\\\"");
            }
            String pack = String.format(
                    "{\"pack\":{\"pack_format\":" + type.getVersion(SharedConstants.getCurrentVersion()) + ",\"description\":\"%s\"}}", description);
            return IOUtils.toInputStream(pack, Charsets.UTF_8);
        }
        return null;
    }
}
