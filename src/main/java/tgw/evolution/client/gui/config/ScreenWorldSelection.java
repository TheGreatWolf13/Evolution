package tgw.evolution.client.gui.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FileUtils;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.util.ConfigHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ScreenWorldSelection extends ScreenListMenu {
    private final ModConfig config;
    private final ResourceLocation resMissingIcon = new ResourceLocation("textures/misc/unknown_server.png");
    private final LevelResource serverConfigFolder = new LevelResource("serverconfig");
    private final Component textSelect = new TranslatableComponent("evolution.gui.config.select");

    public ScreenWorldSelection(Screen parent, ModConfig config, Component title) {
        super(parent, new TranslatableComponent("evolution.gui.config.editWorldConfig", title.plainCopy().withStyle(ChatFormatting.YELLOW)), 30);
        this.config = config;
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        try {
            LevelStorageSource source = Minecraft.getInstance().getLevelSource();
            List<LevelSummary> levelList = source.getLevelList();
            Collections.sort(levelList);
            for (LevelSummary summary : levelList) {
                //noinspection ObjectAllocationInLoop
                entries.add(new WorldItem(summary));
            }
        }
        catch (LevelStorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 75,
                                            this.height - 29,
                                            150,
                                            20,
                                            CommonComponents.GUI_BACK,
                                            button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        for (Item item : this.entries) {
            if (item instanceof WorldItem worldItem) {
                worldItem.disposeIcon();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void updateTooltip(int mouseX, int mouseY) {
    }

    public class WorldItem extends Item {
        private final Component folderName;
        private final File iconFile;
        private final ResourceLocation iconId;
        private final Button modifyButton;
        private final DynamicTexture texture;
        private final Component worldName;

        public WorldItem(LevelSummary summary) {
            super(summary.getLevelName());
            this.worldName = new TextComponent(summary.getLevelName());
            this.folderName = new TextComponent(summary.getLevelId()).withStyle(ChatFormatting.GRAY);
            this.iconId = new ResourceLocation("minecraft",
                                               "worlds/" +
                                               Util.sanitizeName(summary.getLevelId(), ResourceLocation::validPathChar) +
                                               "/" +
                                               Hashing.sha1().hashUnencodedChars(summary.getLevelId()) +
                                               "/icon");
            this.iconFile = summary.getIcon().isFile() ? summary.getIcon() : null;
            this.texture = this.loadWorldIcon();
            this.modifyButton = new Button(0, 0, 60, 20,
                                           ScreenWorldSelection.this.textSelect,
                                           onPress -> this.loadServerConfig(summary.getLevelId(), summary.getLevelName()));
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.modifyButton);
        }

        public void disposeIcon() {
            if (this.texture != null) {
                this.texture.close();
            }
        }

        private void loadServerConfig(String worldFileName, String worldName) {
            try (LevelStorageSource.LevelStorageAccess storageAccess = Minecraft.getInstance().getLevelSource().createAccess(worldFileName)) {
                Path serverConfigPath = storageAccess.getLevelPath(ScreenWorldSelection.this.serverConfigFolder);
                FileUtils.getOrCreateDirectory(serverConfigPath, "serverconfig");
                final CommentedFileConfig data = ScreenWorldSelection.this.config.getHandler()
                                                                                 .reader(serverConfigPath)
                                                                                 .apply(ScreenWorldSelection.this.config);
                ConfigHelper.setConfigData(ScreenWorldSelection.this.config, data);
                if (ModList.get().getModContainerById(ScreenWorldSelection.this.config.getModId()).isPresent()) {
                    ScreenWorldSelection.this.minecraft.setScreen(new ScreenConfig(ScreenWorldSelection.this.parent,
                                                                                   new TextComponent(worldName),
                                                                                   ScreenWorldSelection.this.config));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Nullable
        private DynamicTexture loadWorldIcon() {
            if (this.iconFile == null) {
                return null;
            }
            try (InputStream is = new FileInputStream(this.iconFile); NativeImage image = NativeImage.read(is)) {
                if (image.getWidth() != 64 || image.getHeight() != 64) {
                    return null;
                }
                DynamicTexture texture = new DynamicTexture(image);
                ScreenWorldSelection.this.minecraft.getTextureManager().register(this.iconId, texture);
                return texture;
            }
            catch (IOException ignored) {
            }
            return null;
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
            //noinspection VariableNotUsedInsideIf
            RenderSystem.setShaderTexture(0, this.texture != null ? this.iconId : ScreenWorldSelection.this.resMissingIcon);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            blit(poseStack, left + 4, top, 22, 22, 0, 0, 64, 64, 64, 64);
            Screen.drawString(poseStack, ScreenWorldSelection.this.minecraft.font, this.worldName, left + 32, top + 2, 0xFF_FFFF);
            Screen.drawString(poseStack, ScreenWorldSelection.this.minecraft.font, this.folderName, left + 32, top + 12, 0xFF_FFFF);
            this.modifyButton.x = left + width - 51;
            this.modifyButton.y = top;
            this.modifyButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }
}
