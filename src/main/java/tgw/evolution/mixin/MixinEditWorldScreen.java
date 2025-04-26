package tgw.evolution.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

@Mixin(EditWorldScreen.class)
public abstract class MixinEditWorldScreen extends Screen {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Gson WORLD_GEN_SETTINGS_GSON;
    @Shadow @Final private BooleanConsumer callback;
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;
    @Shadow private EditBox nameEdit;
    @Shadow private Button renameButton;

    public MixinEditWorldScreen(Component component) {
        super(component);
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public void init() {
        assert this.minecraft != null;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button resetIconBtn = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.resetIcon"), b -> {
            Optional<Path> iconFileOpt = this.levelAccess.getIconFile();
            if (iconFileOpt.isPresent()) {
                FileUtils.deleteQuietly(iconFileOpt.get().toFile());
            }
            b.active = false;
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.openFolder"), b -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backup"), b -> this.callback.accept(!makeBackupAndShowToast(this.levelAccess))));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backupFolder"), b -> {
            LevelStorageSource levelSource = this.minecraft.getLevelSource();
            Path backupPath = levelSource.getBackupPath();
            try {
                Files.createDirectories(Files.exists(backupPath) ? backupPath.toRealPath() : backupPath);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            Util.getPlatform().openFile(backupPath.toFile());
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.optimize"), b -> {}));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.export_worldgen_settings"), b -> {
            DataResult<String> worldGenSettingsStr;
            try {
                WorldStem worldStem = this.minecraft.makeWorldStem(this.levelAccess, false);
                try {
                    DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, worldStem.registryAccess());
                    DataResult<JsonElement> dataResult = WorldGenSettings.CODEC.encodeStart(dynamicOps, worldStem.worldData().worldGenSettings());
                    worldGenSettingsStr = dataResult.flatMap(jsonElement -> {
                        Path path = this.levelAccess.getLevelPath(LevelResource.ROOT).resolve("worldgen_settings_export.json");
                        try {
                            JsonWriter jsonWriter = WORLD_GEN_SETTINGS_GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
                            try {
                                WORLD_GEN_SETTINGS_GSON.toJson(jsonElement, jsonWriter);
                            }
                            catch (Throwable e) {
                                try {
                                    jsonWriter.close();
                                }
                                catch (Throwable suppressed) {
                                    e.addSuppressed(suppressed);
                                }
                                throw e;
                            }
                            jsonWriter.close();
                        }
                        catch (JsonIOException | IOException e) {
                            return DataResult.error("Error writing file: " + e.getMessage());
                        }
                        return DataResult.success(path.toString());
                    });
                }
                catch (Throwable e) {
                    try {
                        worldStem.close();
                    }
                    catch (Throwable suppressed) {
                        e.addSuppressed(suppressed);
                    }
                    throw e;
                }
                worldStem.close();
            }
            catch (Exception e) {
                LOGGER.warn("Could not parse level data", e);
                worldGenSettingsStr = DataResult.error("Could not parse level data: " + e.getMessage());
            }
            Component component = new TextComponent(worldGenSettingsStr.get().map(Function.identity(), DataResult.PartialResult::message));
            Component component2 = new TranslatableComponent(worldGenSettingsStr.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure");
            worldGenSettingsStr.error().ifPresent(partialResult -> LOGGER.error("Error exporting world settings: {}", partialResult));
            this.minecraft.getToasts().addToast(SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component2, component));
        }));
        this.renameButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableComponent("selectWorld.edit.save"), b -> this.onRename()));
        this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, CommonComponents.GUI_CANCEL, b -> this.callback.accept(false)));
        resetIconBtn.active = this.levelAccess.getIconFile().filter(Files::isRegularFile).isPresent();
        LevelSummary summary = this.levelAccess.getSummary();
        String levelName = summary == null ? "" : summary.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, new TranslatableComponent("selectWorld.enterName"));
        this.nameEdit.setValue(levelName);
        this.nameEdit.setResponder(s -> this.renameButton.active = !s.trim().isEmpty());
        this.addWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);
    }

    @Shadow
    protected abstract void onRename();
}
