package tgw.evolution.client.gui.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.ConfigHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tgw.evolution.init.EvolutionStyles.CONFIG;

public class ScreenModConfigSelection extends ScreenListMenu {

    private final Map<ModConfig.Type, Set<ModConfig>> configMap;
    private final Component textModify = new TranslatableComponent("evolution.gui.config.modify");
    private final Component textNoPermission = new TranslatableComponent("evolution.gui.config.noPermission").withStyle(ChatFormatting.RED);
    private final Component textSelectWorld = new TranslatableComponent("evolution.gui.config.selectWorld");
    private final Component textTitleClient = new TranslatableComponent("evolution.gui.config.title.client").setStyle(CONFIG);
    private final Component textTitleCommon = new TranslatableComponent("evolution.gui.config.title.common").setStyle(CONFIG);
    private final Component textTitleServer = new TranslatableComponent("evolution.gui.config.title.server").setStyle(CONFIG);

    public ScreenModConfigSelection(Screen parent, String displayName, Map<ModConfig.Type, Set<ModConfig>> configMap) {
        super(parent, new TextComponent(displayName), 30);
        this.configMap = configMap;
    }

    private static MutableComponent createLabelFromModConfig(ModConfig config) {
        String fileName = config.getFileName();
        fileName = fileName.replace(config.getModId() + "-", "");
        fileName = fileName.substring(0, fileName.length() - ".toml".length());
        fileName = FilenameUtils.getName(fileName);
        return ScreenConfig.createReadableLabel(fileName);
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        Set<ModConfig> clientConfigs = this.configMap.get(ModConfig.Type.CLIENT);
        if (clientConfigs != null && !clientConfigs.isEmpty()) {
            entries.add(new TitleItem(this.textTitleClient));
            clientConfigs.forEach(config -> entries.add(new FileItem(config)));
        }
        Set<ModConfig> commonConfigs = this.configMap.get(ModConfig.Type.COMMON);
        if (commonConfigs != null && !commonConfigs.isEmpty()) {
            entries.add(new TitleItem(this.textTitleCommon));
            commonConfigs.forEach(config -> entries.add(new FileItem(config)));
        }
        Set<ModConfig> serverConfigs = this.configMap.get(ModConfig.Type.SERVER);
        if (serverConfigs != null && !serverConfigs.isEmpty()) {
            entries.add(new TitleItem(this.textTitleServer));
            serverConfigs.forEach(config -> entries.add(new FileItem(config)));
        }
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 75, this.height - 29, 150, 20, CommonComponents.GUI_BACK,
                                            button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    protected void updateTooltip(int mouseX, int mouseY) {
    }

    @OnlyIn(Dist.CLIENT)
    public class FileItem extends Item {
        protected final ModConfig config;
        protected final Component fileName;
        protected final Button modifyButton;
        @Nullable
        protected final Button restoreButton;
        protected final Component title;
        private final List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> allConfigValues;

        public FileItem(ModConfig config) {
            super(createLabelFromModConfig(config));
            this.config = config;
            this.title = createTrimmedFileName(createLabelFromModConfig(config));
            this.allConfigValues = ConfigHelper.gatherAllConfigValues(config);
            this.fileName = createTrimmedFileName(new TextComponent(config.getFileName())).withStyle(ChatFormatting.GRAY);
            this.modifyButton = this.createModifyButton(config);
            this.modifyButton.active = !ScreenConfig.isPlayingGame() ||
                                       this.config.getType() != ModConfig.Type.SERVER ||
                                       this.hasRequiredPermission();
            if (config.getType() != ModConfig.Type.SERVER || Minecraft.getInstance().player != null) {
                this.restoreButton = new ButtonIcon(0, 0, 0, EvolutionResources.ICON_12_12, onPress -> this.showRestoreScreen(),
                                                    (button, poseStack, mouseX, mouseY) -> {
                                                        if (button.isHoveredOrFocused()) {
                                                            if (this.hasRequiredPermission() && button.active) {
                                                                ScreenModConfigSelection.this.renderTooltip(poseStack,
                                                                                                            Minecraft.getInstance().font.split(
                                                                                                                    EvolutionTexts.GUI_CONFIG_RESTORE_DEFAULTS,
                                                                                                                    Math.max(
                                                                                                                            ScreenModConfigSelection.this.width /
                                                                                                                            2 - 60, 170)), mouseX,
                                                                                                            mouseY);
                                                            }
                                                            else if (!this.hasRequiredPermission()) {
                                                                ScreenModConfigSelection.this.renderTooltip(poseStack,
                                                                                                            Minecraft.getInstance().font.split(
                                                                                                                    ScreenModConfigSelection.this.textNoPermission,
                                                                                                                    Math.max(
                                                                                                                            ScreenModConfigSelection.this.width /
                                                                                                                            2 - 60, 170)), mouseX,
                                                                                                            mouseY);
                                                            }
                                                        }
                                                    });
                this.restoreButton.active = this.hasRequiredPermission();
                this.updateRestoreDefaultButton();
            }
            else {
                this.restoreButton = null;
            }
        }

        private static MutableComponent createTrimmedFileName(Component fileName) {
            MutableComponent trimmedFileName = fileName.copy();
            if (Minecraft.getInstance().font.width(fileName) > 150) {
                return new TextComponent(Minecraft.getInstance().font.substrByWidth(fileName, 140) + "...");
            }
            return trimmedFileName;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            if (this.restoreButton != null) {
                return ImmutableList.of(this.modifyButton, this.restoreButton);
            }
            return ImmutableList.of(this.modifyButton);
        }

        private Button createModifyButton(ModConfig config) {
            boolean serverConfig = config.getType() == ModConfig.Type.SERVER && Minecraft.getInstance().level == null;
            Component langKey = serverConfig ? ScreenModConfigSelection.this.textSelectWorld : ScreenModConfigSelection.this.textModify;
            return new Button(0, 0, serverConfig ? 100 : 80, 20, langKey, onPress -> {
                if (ScreenConfig.isPlayingGame() && this.config.getType() == ModConfig.Type.SERVER && !this.hasRequiredPermission()) {
                    return;
                }
                if (serverConfig) {
                    Minecraft.getInstance().setScreen(new ScreenWorldSelection(ScreenModConfigSelection.this, config, this.title));
                }
                else {
                    ModList.get()
                           .getModContainerById(config.getModId())
                           .ifPresent(container -> Minecraft.getInstance()
                                                            .setScreen(new ScreenConfig(ScreenModConfigSelection.this,
                                                                                        new TextComponent(container.getModInfo().getDisplayName()),
                                                                                        config)));
                }
            }, (button, poseStack, mouseX, mouseY) -> {
                if (button.isHoveredOrFocused()) {
                    if (!this.hasRequiredPermission()) {
                        ScreenModConfigSelection.this.renderTooltip(poseStack,
                                                                    Minecraft.getInstance().font.split(ScreenModConfigSelection.this.textNoPermission,
                                                                                                       Math.max(ScreenModConfigSelection.this.width /
                                                                                                                2 - 60, 170)), mouseX, mouseY);
                    }
                }
            });
        }

        private boolean hasRequiredPermission() {
            if (this.config.getType() == ModConfig.Type.SERVER && Minecraft.getInstance().player != null) {
                return Minecraft.getInstance().player.hasPermissions(2);
            }
            return true;
        }

        @Override
        public void render(PoseStack poseStack,
                           int x,
                           int top,
                           int left,
                           int width,
                           int p_230432_6_,
                           int mouseX,
                           int mouseY,
                           boolean p_230432_9_,
                           float partialTicks) {
            Screen.drawString(poseStack, Minecraft.getInstance().font, this.title, left + 28, top + 2, 0xFF_FFFF);
            Screen.drawString(poseStack, Minecraft.getInstance().font, this.fileName, left + 28, top + 12, 0xFF_FFFF);
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.modifyButton.x = left + width - 103;
            this.modifyButton.y = top;
            this.modifyButton.render(poseStack, mouseX, mouseY, partialTicks);
            if (this.restoreButton != null) {
                this.restoreButton.x = left + width - 21;
                this.restoreButton.y = top;
                this.restoreButton.render(poseStack, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public void resetFocus() {
        }

        private void showRestoreScreen() {
            ScreenConfirmation confirmScreen = new ScreenConfirmation(ScreenModConfigSelection.this, EvolutionTexts.GUI_CONFIG_RESTORE_MESSAGE,
                                                                      result -> {
                                                                          if (!result || this.allConfigValues == null) {
                                                                              return true;
                                                                          }
                                                                          // Resets all config values
                                                                          CommentedConfig newConfig = CommentedConfig.copy(
                                                                                  this.config.getConfigData());
                                                                          this.allConfigValues.forEach(pair -> {
                                                                              ForgeConfigSpec.ConfigValue<?> configValue = pair.getLeft();
                                                                              ForgeConfigSpec.ValueSpec valueSpec = pair.getRight();
                                                                              newConfig.set(configValue.getPath(), valueSpec.getDefault());
                                                                          });
                                                                          this.updateRestoreDefaultButton();
                                                                          this.config.getConfigData().putAll(newConfig);
                                                                          ConfigHelper.resetCache(this.config);
                                                                          // Post logic for server configs
                                                                          if (this.config.getType() == ModConfig.Type.SERVER) {
                                                                              ConfigHelper.sendConfigDataToServer(this.config);
                                                                          }
                                                                          return true;
                                                                      });
            confirmScreen.setPositiveText(EvolutionTexts.GUI_CONFIG_RESTORE_DEFAULTS);
            confirmScreen.setNegativeText(CommonComponents.GUI_CANCEL);
            Minecraft.getInstance().setScreen(confirmScreen);
        }

        @Override
        public void tick() {
        }

        private void updateRestoreDefaultButton() {
            if (this.config != null && this.restoreButton != null && this.hasRequiredPermission()) {
                this.restoreButton.active = ConfigHelper.isModified(this.config);
            }
        }
    }
}
