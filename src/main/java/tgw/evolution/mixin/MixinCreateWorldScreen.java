package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.resources.ModResourcePackUtil;
import tgw.evolution.resources.ModdedPackSource;
import tgw.evolution.util.math.FastRandom;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen extends Screen {

    @Shadow protected DataPackConfig dataPacks;
    @Shadow private @Nullable PackRepository tempDataPackRepository;

    public MixinCreateWorldScreen(Component component) {
        super(component);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static CreateWorldScreen createFresh(@Nullable Screen screen) {
        RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
        ModResourcePackUtil.loadDynamicRegistry(writable);
        RegistryAccess.Frozen frozen = writable.freeze();
        WorldPreset preset = WorldPreset.FLAT;
        return new CreateWorldScreen(screen, ModResourcePackUtil.createDefaultDataPackSettings(), new WorldGenSettingsComponent(frozen, preset.create(frozen, new FastRandom().nextLong(), true, false), Optional.of(preset), OptionalLong.empty()));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static CreateWorldScreen createFromExisting(@Nullable Screen screen, WorldStem worldStem, @Nullable Path path) {
        WorldData worldData = worldStem.worldData();
        LevelSettings levelSettings = worldData.getLevelSettings();
        WorldGenSettings worldGenSettings = worldData.worldGenSettings();
        RegistryAccess.Frozen registryAccess = worldStem.registryAccess();
        CreateWorldScreen createWorldScreen = new CreateWorldScreen(screen, ModResourcePackUtil.createDefaultDataPackSettings(), new WorldGenSettingsComponent(registryAccess, worldGenSettings, WorldPreset.of(worldGenSettings), OptionalLong.of(worldGenSettings.seed())));
        createWorldScreen.initName = levelSettings.levelName();
        createWorldScreen.commands = levelSettings.allowCommands();
        createWorldScreen.commandsChanged = true;
        createWorldScreen.difficulty = levelSettings.difficulty();
        createWorldScreen.gameRules.assignFrom(levelSettings.gameRules(), null);
        if (levelSettings.hardcore()) {
            createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
        }
        else if (levelSettings.gameType().isSurvival()) {
            createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
        }
        else if (levelSettings.gameType().isCreative()) {
            createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
        }
        createWorldScreen.tempDataPackDir = path;
        return createWorldScreen;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private @Nullable Pair<File, PackRepository> getDataPackSelectionSettings() {
        Path path = this.getTempDataPackDir();
        if (path != null) {
            File file = path.toFile();
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(file, PackSource.DEFAULT));
                this.tempDataPackRepository.sources.add(new ModdedPackSource(PackType.SERVER_DATA));
                this.tempDataPackRepository.reload();
            }
            this.tempDataPackRepository.setSelected(this.dataPacks.getEnabled());
            return Pair.of(file, this.tempDataPackRepository);
        }
        return null;
    }

    @Shadow
    protected abstract @Nullable Path getTempDataPackDir();
}
