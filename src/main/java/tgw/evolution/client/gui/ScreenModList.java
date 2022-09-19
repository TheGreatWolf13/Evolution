package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.common.util.Size2i;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.PathResourcePack;
import net.minecraftforge.resource.ResourcePackLoader;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.config.ScreenUtil;
import tgw.evolution.client.gui.widgets.AdvCheckBox;
import tgw.evolution.client.gui.widgets.AdvEditBox;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.client.gui.widgets.Label;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScreenModList extends Screen {
    private final Object2ObjectMap<String, ItemStack> itemCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, Pair<ResourceLocation, Size2i>> logoCache = new Object2ObjectOpenHashMap<>();
    private final Comparator<ModEntry> sort = Comparator.comparing(o -> o.getInfo().getDisplayName());
    //Text Components are not static to be able to be freed from memory
    private final FormattedText textButtonOpenModsFolder = new TranslatableComponent("fml.button.open.mods.folder");
    private final FormattedText textFilterUpdates = new TranslatableComponent("evolution.gui.modsList.filterUpdates");
    private final Component textMenuModsConfig = new TranslatableComponent("fml.menu.mods.config");
    private final Component textMenuModsTitle = new TranslatableComponent("fml.menu.mods.title").withStyle(ChatFormatting.BOLD)
                                                                                                .withStyle(ChatFormatting.WHITE);
    private final Component textReportBugs = new TranslatableComponent("evolution.gui.modsList.reportBugs");
    private final Component textWebsite = new TranslatableComponent("evolution.gui.modsList.website");
    private final ResourceLocation versionCheckIcons = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");
    private @Nullable List<? extends FormattedCharSequence> activeTooltip;
    private Label authors;
    private Button configButton;
    private Label credits;
    private StringList descriptionList;
    private boolean hasAuthors;
    private boolean hasCredits;
    private Button issueButton;
    private Label license;
    private Component modId;
    private int modIdWidth;
    private ModList modList;
    private AdvEditBox searchEditBox;
    private @Nullable ModInfo selectedModInfo;
    private int tooltipYOffset;
    private AdvCheckBox updatesButton;
    private Label version;
    private Button websiteButton;

    public ScreenModList() {
        super(TextComponent.EMPTY);
    }

    private static int getLabelCount(ModInfo selectedModInfo) {
        int count = 1;
        if (selectedModInfo.getConfigElement("credits").isPresent()) {
            count++;
        }
        if (selectedModInfo.getConfigElement("authors").isPresent()) {
            count++;
        }
        return count;
    }

    private DynamicTexture createLogoTexture(NativeImage image, boolean smooth) {
        return new DynamicTexture(image) {
            @Override
            public void upload() {
                this.bind();
                image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), smooth, false, false, false);
            }
        };
    }

    private void drawLogo(PoseStack poseStack, int contentWidth, int x, int y, int maxWidth, int maxHeight) {
        if (this.selectedModInfo != null) {
            if (this.logoCache.containsKey(this.selectedModInfo.getModId())) {
                Pair<ResourceLocation, Size2i> logoInfo = this.logoCache.get(this.selectedModInfo.getModId());
                if (logoInfo.getLeft() != null) {
                    ResourceLocation logoResource = logoInfo.getLeft();
                    Size2i size = logoInfo.getRight();
                    RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
                    RenderSystem.setShaderTexture(0, logoResource);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    int width = size.width;
                    int height = size.height;
                    if (size.width > maxWidth) {
                        width = maxWidth;
                        height = width * size.height / size.width;
                    }
                    if (height > maxHeight) {
                        height = maxHeight;
                        width = height * size.width / size.height;
                    }
                    x += (contentWidth - width) / 2;
                    y += (maxHeight - height) / 2;
                    Screen.blit(poseStack, x, y, width, height, 0.0F, 0.0F, size.width, size.height, size.width, size.height);
                }
            }
        }
    }

    private void drawModInfo(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.vLine(matrices, this.modList.getRight() + 11, -1, this.height, 0xFF70_7070);
        fill(matrices, this.modList.getRight() + 12, 0, this.width, this.height, 0x6600_0000);
        this.descriptionList.render(matrices, mouseX, mouseY, partialTicks);
        int contentLeft = this.modList.getRight() + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;
        if (this.selectedModInfo != null) {
            this.drawLogo(matrices, contentWidth, contentLeft, 10, this.width - (this.modList.getRight() + 12 + 10) - 10, 50);
            matrices.pushPose();
            matrices.translate(contentLeft, 70, 0);
            matrices.scale(2.0F, 2.0F, 2.0F);
            drawString(matrices, this.font, this.selectedModInfo.getDisplayName(), 0, 0, 0xFF_FFFF);
            matrices.popPose();
            drawString(matrices, this.font, this.modId, contentLeft + contentWidth - this.modIdWidth, 92, 0xFF_FFFF);
            this.version.render(this.font, matrices, contentLeft, 92, contentWidth, mouseX, mouseY);
            VersionChecker.CheckResult result = VersionChecker.getResult(this.selectedModInfo);
            if (result.status().shouldDraw() && result.url() != null) {
                String version = ForgeI18n.parseMessage("fml.menu.mods.info.version", this.selectedModInfo.getVersion().toString());
                int versionWidth = this.font.width(version);
                RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
                RenderSystem.setShaderTexture(0, this.versionCheckIcons);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int vOffset = result.status().isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                Screen.blit(matrices, contentLeft + versionWidth + 5, 92, result.status().getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
                if (MathHelper.isMouseInRange(mouseX, mouseY, contentLeft + versionWidth + 5, 92, contentLeft + versionWidth + 5 + 8, 92 + 8)) {
                    this.setActiveTooltip(new TranslatableComponent("fml.menu.mods.info.updateavailable", result.url()));
                }
            }
            int labelOffset = this.height - 20;
            this.license.render(this.font, matrices, contentLeft, labelOffset, contentWidth, mouseX, mouseY);
            labelOffset -= 15;
            if (this.hasCredits) {
                this.credits.render(this.font, matrices, contentLeft, labelOffset, contentWidth, mouseX, mouseY);
                labelOffset -= 15;
            }
            if (this.hasAuthors) {
                this.authors.render(this.font, matrices, contentLeft, labelOffset, contentWidth, mouseX, mouseY);
            }
        }
    }

    private void drawModList(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
        RenderSystem.setShaderTexture(0, this.versionCheckIcons);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(matrices, this.modList.getRight() - 24, 8, 24, 0, 8, 8, 64, 16);
        this.modList.render(matrices, mouseX, mouseY, partialTicks);
        drawString(matrices, this.font, this.textMenuModsTitle, 70, 10, 0xFF_FFFF);
        this.searchEditBox.render(matrices, mouseX, mouseY, partialTicks);
        if (MathHelper.isMouseInRange(mouseX, mouseY, this.modList.getRight() - 14, 7, this.modList.getRight(), 7 + 14)) {
            this.setActiveTooltip(this.textFilterUpdates);
            this.tooltipYOffset = 10;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.searchEditBox = new AdvEditBox(this.font, 11, 25, 148, 20, EvolutionTexts.GUI_GENERAL_SEARCH);
        this.searchEditBox.setResponder(s -> {
            this.modList.filterAndUpdateList(s);
            this.updateSelectedModList();
        });
        this.addWidget(this.searchEditBox);
        this.modList = new ModList();
        this.modList.setLeftPos(10);
        this.modList.setRenderTopAndBottom(false);
        this.addWidget(this.modList);
        this.addRenderableWidget(
                new Button(10, this.modList.getBottom() + 8, 127, 20, CommonComponents.GUI_BACK, onPress -> this.getMinecraft().setScreen(null)));
        this.addRenderableWidget(new ButtonIcon(140, this.modList.getBottom() + 8, 12 * 2, EvolutionResources.ICON_12_12,
                                                onPress -> Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile()),
                                                (button, matrices, mouseX, mouseY) -> this.setActiveTooltip(this.textButtonOpenModsFolder)));
        int padding = 10;
        int contentLeft = this.modList.getRight() + 12 + padding;
        int contentWidth = this.width - contentLeft - padding;
        int buttonWidth = (contentWidth - padding) / 3;
        this.configButton = this.addRenderableWidget(new Button(contentLeft, 105, buttonWidth, 20, this.textMenuModsConfig, onPress -> {
            if (this.selectedModInfo != null) {
                Optional<BiFunction<Minecraft, Screen, Screen>> factoryFor = ConfigGuiHandler.getGuiFactoryFor(this.selectedModInfo);
                if (factoryFor.isPresent()) {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(factoryFor.get().apply(this.minecraft, this));
                }
            }
        }));
        this.configButton.visible = false;
        this.websiteButton = this.addRenderableWidget(new Button(contentLeft + buttonWidth + 5, 105, buttonWidth, 20, this.textWebsite,
                                                                 onPress -> this.openLink("displayURL", this.selectedModInfo)));
        this.websiteButton.visible = false;
        this.issueButton = this.addRenderableWidget(
                new Button(contentLeft + buttonWidth + buttonWidth + 10, 105, buttonWidth, 20, this.textReportBugs,
                           onPress -> this.openLink("issueTrackerURL", this.selectedModInfo != null ? this.selectedModInfo.getOwningFile() : null)));
        this.issueButton.visible = false;
        this.descriptionList = new StringList(contentWidth, this.height - 135 - 55, contentLeft, 130);
        this.descriptionList.setRenderTopAndBottom(false);
        this.descriptionList.setRenderBackground(false);
        this.addWidget(this.descriptionList);
        this.updatesButton = this.addRenderableWidget(new AdvCheckBox(this.modList.getRight() - 14, 7, EvolutionTexts.EMPTY, false, b -> {
            this.modList.filterAndUpdateList(this.searchEditBox.getValue());
            this.updateSelectedModList();
        }));
        this.modList.filterAndUpdateList(this.searchEditBox.getValue());
        if (this.selectedModInfo != null) {
            this.setSelectedModInfo(this.selectedModInfo);
        }
    }

    private void loadAndCacheLogo(IModInfo info) {
        if (this.logoCache.containsKey(info.getModId())) {
            return;
        }
        outer:
        if (info.getLogoFile().isPresent()) {
            String s = info.getLogoFile().get();
            if (s.isEmpty()) {
                break outer;
            }
            if (s.contains("/") || s.contains("\\")) {
                Evolution.warn("Skipped loading logo file from {}. The file name '{}' contained illegal characters '/' or '\\'",
                               info.getDisplayName(), s);
                break outer;
            }
            Optional<PathResourcePack> packFor = ResourcePackLoader.getPackFor(info.getModId());
            if (packFor.isPresent()) {
                PathResourcePack resourcePack = packFor.get();
                try (InputStream is = resourcePack.getRootResource(s); NativeImage logo = is != null ? NativeImage.read(is) : null) {
                    if (logo != null) {
                        TextureManager textureManager = this.getMinecraft().getTextureManager();
                        this.logoCache.put(info.getModId(),
                                           Pair.of(textureManager.register("modlogo", this.createLogoTexture(logo, info.getLogoBlur())),
                                                   new Size2i(logo.getWidth(), logo.getHeight())));
                        return;
                    }
                }
                catch (IOException ignored) {
                }
            }
        }
        this.logoCache.put(info.getModId(), Pair.of(null, new Size2i(0, 0)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.selectedModInfo != null) {
            int contentLeft = this.modList.getRight() + 12 + 10;
            String version = ForgeI18n.parseMessage("fml.menu.mods.info.version", this.selectedModInfo.getVersion().toString());
            int versionWidth = this.font.width(version);
            if (MathHelper.isMouseInRange(mouseX, mouseY, contentLeft + versionWidth + 5, 92, contentLeft + versionWidth + 5 + 8, 92 + 8)) {
                VersionChecker.CheckResult result = VersionChecker.getResult(this.selectedModInfo);
                if (result.status().shouldDraw() && result.url() != null) {
                    Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url()));
                    this.handleComponentClicked(style);
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openLink(String key, @Nullable IConfigurable configurable) {
        if (configurable != null) {
            Optional<Object> optional = configurable.getConfigElement(key);
            if (optional.isPresent()) {
                this.openLink(optional.get().toString());
            }
        }
    }

    private void openLink(String url) {
        Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        this.handleComponentClicked(style);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.renderBackground(matrices);
        this.drawModList(matrices, mouseX, mouseY, partialTicks);
        this.drawModInfo(matrices, mouseX, mouseY, partialTicks);
        super.render(matrices, mouseX, mouseY, partialTicks);
        if (this.activeTooltip != null) {
            this.renderTooltip(matrices, this.activeTooltip, mouseX, mouseY + this.tooltipYOffset, this.font);
            this.tooltipYOffset = 0;
        }
    }

    private void setActiveTooltip(FormattedText tooltip) {
        this.activeTooltip = this.font.split(tooltip, Math.min(200, this.width));
        this.tooltipYOffset = 0;
    }

    private void setSelectedModInfo(ModInfo selectedModInfo) {
        this.selectedModInfo = selectedModInfo;
        this.loadAndCacheLogo(selectedModInfo);
        this.configButton.visible = true;
        this.websiteButton.visible = true;
        this.issueButton.visible = true;
        this.configButton.active = ConfigGuiHandler.getGuiFactoryFor(selectedModInfo).isPresent();
        this.websiteButton.active = selectedModInfo.getConfigElement("displayURL").isPresent();
        this.issueButton.active = selectedModInfo.getOwningFile().getConfigElement("issueTrackerURL").isPresent();
        int contentLeft = this.modList.getRight() + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;
        int labelCount = getLabelCount(selectedModInfo);
        this.descriptionList.updateSize(contentWidth, this.height - 135 - 10 - labelCount * 15, 130, this.height - 10 - labelCount * 15);
        this.descriptionList.setLeftPos(contentLeft);
        this.descriptionList.setTextFromInfo(selectedModInfo);
        this.descriptionList.setScrollAmount(0);
        this.updateSelectedModList();
        ModEntry entry = this.modList.getEntryFromInfo(this.selectedModInfo);
        if (entry != null) {
            this.modList.centerScrollOn(entry);
        }
        this.updateMod();
    }

    @Override
    public void tick() {
        this.searchEditBox.tick();
    }

    public void updateMod() {
        assert this.selectedModInfo != null;
        this.modId = new TextComponent("Mod ID: " + this.selectedModInfo.getModId()).withStyle(ChatFormatting.DARK_GRAY);
        this.modIdWidth = this.font.width(this.modId);
        this.version = new Label(new TranslatableComponent("evolution.gui.modsList.version"),
                                 new TextComponent(this.selectedModInfo.getVersion().toString()), l -> this.setActiveTooltip(l.getTooltip()));
        this.license = new Label(new TranslatableComponent("evolution.gui.modsList.license"),
                                 new TextComponent(this.selectedModInfo.getOwningFile().getLicense()), l -> this.setActiveTooltip(l.getTooltip()));
        Optional<Object> creditsOp = this.selectedModInfo.getConfigElement("credits");
        this.hasCredits = creditsOp.isPresent();
        if (this.hasCredits) {
            this.credits = new Label(new TranslatableComponent("evolution.gui.modsList.credits"), new TextComponent(creditsOp.get().toString()),
                                     l -> this.setActiveTooltip(l.getTooltip()));
        }
        Optional<Object> authorsOp = this.selectedModInfo.getConfigElement("authors");
        this.hasAuthors = authorsOp.isPresent();
        if (this.hasAuthors) {
            this.authors = new Label(new TranslatableComponent("evolution.gui.modsList.authors"), new TextComponent(authorsOp.get().toString()),
                                     l -> this.setActiveTooltip(l.getTooltip()));
        }
    }

    private void updateSelectedModList() {
        ModEntry selectedEntry = this.modList.getEntryFromInfo(this.selectedModInfo);
        if (selectedEntry != null) {
            this.modList.setSelected(selectedEntry);
        }
    }

    private class ModList extends AbstractSelectionList<ModEntry> {
        public ModList() {
            //noinspection ConstantConditions
            super(ScreenModList.this.minecraft, 150, ScreenModList.this.height, 46, ScreenModList.this.height - 35, 26);
        }

        @Override
        public void centerScrollOn(ModEntry entry) {
            super.centerScrollOn(entry);
        }

        public void filterAndUpdateList(String text) {
            this.replaceEntries(net.minecraftforge.fml.ModList.get()
                                                              .getMods()
                                                              .stream()
                                                              .filter(info -> info.getDisplayName().toLowerCase().contains(text.toLowerCase()))
                                                              .filter(info -> !ScreenModList.this.updatesButton.isChecked() ||
                                                                              VersionChecker.getResult(info).status().shouldDraw())
                                                              .map(info -> new ModEntry((ModInfo) info, this))
                                                              .sorted(ScreenModList.this.sort)
                                                              .collect(Collectors.toList()));
            this.setScrollAmount(0);
        }

        @Nullable
        public ModEntry getEntryFromInfo(@Nullable IModInfo info) {
            if (info == null) {
                return null;
            }
            for (int i = 0, l = this.children().size(); i < l; i++) {
                ModEntry modEntry = this.children().get(i);
                if (modEntry.info == info) {
                    return modEntry;
                }
            }
            return null;
        }

        @Override
        public int getRowLeft() {
            return super.getLeft();
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getLeft() + this.width - 6;
        }

        @Override
        public boolean keyPressed(@Key int key, int scanCode, @Modifiers int modifiers) {
            if (key == GLFW.GLFW_KEY_ENTER && this.getSelected() != null) {
                ScreenModList.this.setSelectedModInfo(this.getSelected().info);
                SoundManager handler = Minecraft.getInstance().getSoundManager();
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return super.keyPressed(key, scanCode, modifiers);
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            ScreenUtil.scissor(this.getRowLeft(), this.getTop(), this.getWidth(), this.getBottom() - this.getTop());
            super.render(poseStack, mouseX, mouseY, partialTicks);
            RenderSystem.disableScissor();
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }

    private class ModEntry extends AbstractSelectionList.Entry<ModEntry> {
        private final ModInfo info;
        private final ModList list;
        private @Nullable MutableComponent modName;
        private @Nullable MutableComponent version;

        public ModEntry(ModInfo info, ModList list) {
            this.info = info;
            this.list = list;
        }

        private Component getFormattedModName() {
            if (this.modName == null) {
                String name = this.info.getDisplayName();
                int width = this.list.getRowWidth() - (this.list.getMaxScroll() > 0 ? 30 : 24);
                if (ScreenModList.this.font.width(name) > width) {
                    name = ScreenModList.this.font.plainSubstrByWidth(name, width - 10) + "...";
                }
                this.modName = new TextComponent(name);
                if ("forge".equals(this.info.getModId()) || "minecraft".equals(this.info.getModId())) {
                    this.modName.withStyle(ChatFormatting.DARK_GRAY);
                }
            }
            return this.modName;
        }

        public IModInfo getInfo() {
            return this.info;
        }

        private ItemStack getItemIcon() {
            String modId = this.info.getModId();
            if (ScreenModList.this.itemCache.containsKey(modId)) {
                return ScreenModList.this.itemCache.get(modId);
            }
            if ("forge".equals(modId)) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                ScreenModList.this.itemCache.put("forge", stack);
                return stack;
            }
            if ("minecraft".equals(modId)) {
                ItemStack stack = new ItemStack(Items.GRASS_BLOCK);
                ScreenModList.this.itemCache.put("minecraft", stack);
                return stack;
            }
            Optional<String> itemIcon = this.info.getConfigElement("itemIcon");
            if (itemIcon.isPresent() && !itemIcon.get().isEmpty()) {
                try {
                    ItemParser parser = new ItemParser(new StringReader(itemIcon.get()), false).parse();
                    ItemStack stack = new ItemStack(parser.getItem(), 1, parser.getNbt());
                    ScreenModList.this.itemCache.put(modId, stack);
                    return stack;
                }
                catch (CommandSyntaxException ignored) {
                }
            }
            for (Item value : ForgeRegistries.ITEMS.getValues()) {
                //noinspection ConstantConditions
                if (value.getRegistryName().getNamespace().equals(modId)) {
                    ItemStack stack = new ItemStack(value);
                    ScreenModList.this.itemCache.put(modId, stack);
                    return stack;
                }
            }
            ItemStack stack = new ItemStack(Items.GRASS_BLOCK);
            ScreenModList.this.itemCache.put(modId, stack);
            return stack;
        }

        private Component getVersion() {
            if (this.version == null) {
                this.version = new TextComponent(this.info.getVersion().toString()).withStyle(ChatFormatting.GRAY);
            }
            return this.version;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
            ScreenModList.this.setSelectedModInfo(this.info);
            this.list.setSelected(this);
            return false;
        }

        @Override
        public void render(PoseStack matrices,
                           int index,
                           int top,
                           int left,
                           int rowWidth,
                           int rowHeight,
                           int mouseX,
                           int mouseY,
                           boolean hovered,
                           float partialTicks) {
            drawString(matrices, ScreenModList.this.font, this.getFormattedModName(), left + 24, top + 2, 0xFF_FFFF);
            drawString(matrices, ScreenModList.this.font, this.getVersion(), left + 24, top + 12, 0xFF_FFFF);
            ScreenModList.this.getMinecraft().getItemRenderer().renderGuiItem(this.getItemIcon(), left + 4, top + 2);
            VersionChecker.CheckResult result = VersionChecker.getResult(this.info);
            if (result.status().shouldDraw()) {
                RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
                RenderSystem.setShaderTexture(0, ScreenModList.this.versionCheckIcons);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int vOffset = result.status().isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                Screen.blit(matrices, left + rowWidth - 8 - 10, top + 6, result.status().getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
            }
        }
    }

    private class StringList extends AbstractSelectionList<StringEntry> {
        public StringList(int width, int height, int left, int top) {
            //noinspection ConstantConditions
            super(ScreenModList.this.minecraft, width, ScreenModList.this.height, top, top + height, 10);
            this.setLeftPos(left);
        }

        @Override
        public int getRowLeft() {
            return this.getLeft();
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getLeft() + this.width - 7;
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
            ScreenUtil.scissor(this.getRowLeft(), this.getTop(), this.getWidth(), this.getBottom() - this.getTop());
            super.render(matrices, mouseX, mouseY, partialTicks);
            RenderSystem.disableScissor();
        }

        @Override
        public void setSelected(@Nullable StringEntry entry) {
        }

        public void setTextFromInfo(IModInfo info) {
            this.clearEntries();
            for (FormattedText text : ScreenModList.this.font.getSplitter()
                                                             .splitLines(info.getDescription().trim(), this.getRowWidth(), Style.EMPTY)) {
                //noinspection ObjectAllocationInLoop
                this.addEntry(new StringEntry(text.getString().replace("\n", "").replace("\r", "").trim()));
            }
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }

    private class StringEntry extends AbstractSelectionList.Entry<StringEntry> {
        private final String line;

        public StringEntry(String line) {
            this.line = line;
        }

        @Override
        public void render(PoseStack matrices,
                           int index,
                           int top,
                           int left,
                           int rowWidth,
                           int rowHeight,
                           int mouseX,
                           int mouseY,
                           boolean hovered,
                           float partialTicks) {
            drawString(matrices, ScreenModList.this.font, this.line, left, top, 0xFF_FFFF);
        }
    }
}
