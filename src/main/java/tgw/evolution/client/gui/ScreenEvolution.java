package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.gui.config.BooleanChanger;
import tgw.evolution.client.gui.config.ConfigPath;
import tgw.evolution.client.gui.config.EnumChanger;
import tgw.evolution.client.gui.config.IntChanger;
import tgw.evolution.client.gui.widgets.*;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.config.*;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

public class ScreenEvolution extends Screen {

    private final Button backButton;
    private final ScrollableArea configArea;
    private final ConfigPath configPath;
    private final ButtonGroup group;
    private final ButtonSelectable[] groupButtons;
    private final ScrollableArea panelArea;
    private final Area[] panels;
    private final Screen parentScreen;
    private final Button restoreButton;
    private final Button saveButton;
    private final Component textConfig = new TranslatableComponent("evolution.gui.evolution.config");
    private final Component textCredits = new TranslatableComponent("evolution.gui.evolution.credits");
    private final Component textTipsAndTricks = new TranslatableComponent("evolution.gui.evolution.tips");
    private final Component textUnsaved = new TranslatableComponent("evolution.gui.evolution.config.unsaved");
    private final Runnable updateButtons;
    private @Nullable AdvEditBox editBox;
    private ConfigFolder folder = EvolutionConfig.ROOT;
    private boolean initialized;
    private int panelWidth;

    public ScreenEvolution(Screen parentScreen, boolean isFirst) {
        super(new TranslatableComponent("evolution.gui.evolution"));
        EvolutionConfig.load();
        this.parentScreen = parentScreen;
        this.group = new ButtonGroup();
        this.groupButtons = new ButtonSelectable[5];
        this.panels = new Area[4];
        this.group.setSelected(0);
        this.panelArea = new ScrollableArea(0, 0, this.panelWidth, this.height - 20, 5);
        this.configArea = new ScrollableArea(0, 0, this.width, this.height, 15);
        this.configPath = new ConfigPath(0, 0, 0, 10);
        this.saveButton = new Button(0, 0, 0, 20, new TranslatableComponent("evolution.gui.evolution.config.save"), b -> {
            EvolutionConfig.save();
            b.active = false;
        });
        this.restoreButton = new Button(0, 0, 0, 20, new TranslatableComponent("evolution.gui.evolution.config.restore"), b -> {
            EvolutionConfig.restore();
            this.setFolder(this.folder);
            b.active = false;
        });
        this.backButton = new Button(0, 0, 0, 20, EvolutionTexts.GUI_GENERAL_BACK, b -> this.goBack());
        this.updateButtons = () -> {
            this.saveButton.active = EvolutionConfig.isDirty();
            this.restoreButton.active = EvolutionConfig.needsRestoration();
        };
        for (int i = 0, len = this.panels.length; i < len; ++i) {
            if (i == 2) {
                //noinspection ObjectAllocationInLoop
                this.panels[i] = new GlueArea(0, 32, this.width, this.height, 15, this.configArea);
            }
            else {
                //noinspection ObjectAllocationInLoop
                this.panels[i] = new ScrollableArea(0, 32, this.width, this.height, 15);
            }
        }
        //noinspection OptionalGetWithoutIsPresent
        this.panelArea.addBeginning(new TextArea(5, 0, 0, new TextComponent("Evolution " + FabricLoader.getInstance().getModContainer(Evolution.MODID).get().getMetadata().getVersion().getFriendlyString()), 0xff_ffff, true));
        this.groupButtons[0] = this.panelArea.addBeginning(new ButtonDirt(5, 5, 20, 20, this.getTitle(), b -> this.setSelected(0), this.group));
        this.groupButtons[1] = this.panelArea.addBeginning(new ButtonDirt(5, 30, 20, 20, this.textTipsAndTricks, b -> this.setSelected(1), this.group));
        this.groupButtons[2] = this.panelArea.addBeginning(new ButtonDirt(5, 55, 20, 20, this.textConfig, b -> this.setSelected(2), this.group));
        this.groupButtons[3] = this.panelArea.addBeginning(new ButtonDirt(5, 55, 20, 20, this.textCredits, b -> this.setSelected(3), this.group));
        this.groupButtons[4] = this.panelArea.addEnd(new ButtonDirt(5, this.height - 25, 20, 20, isFirst ? EvolutionTexts.GUI_MENU_TO_TITLE : EvolutionTexts.GUI_GENERAL_BACK, b -> this.onClose(), this.group));
        if (!isFirst) {
            this.setupPanel0(false);
            this.setupPanel1();
            this.setupPanel2();
            this.setupPanel3();
            this.initialized = true;
        }
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    private static ToggleText makeTip(int x, int width, int number) {
        if (number == 2) {
            return new ToggleText(x + 10, 0, width - 20, new TranslatableComponent("evolution.gui.evolution.tips.tip" + number), 0xff_ffff, new TranslatableComponent("evolution.gui.evolution.tips.tip" + number + ".comment", EvolutionClient.KEY_CRAWL.getTranslatedKeyMessage()), 0x90_9090);
        }
        ToggleText toggle = new ToggleText(x + 10, 0, width - 20, new TranslatableComponent("evolution.gui.evolution.tips.tip" + number), 0xff_ffff, new TranslatableComponent("evolution.gui.evolution.tips.tip" + number + ".comment"), 0x90_9090);
        if (number == 0) {
            toggle.toggle();
        }
        return toggle;
    }

    private boolean goBack() {
        ConfigFolder parent = this.folder.parent();
        if (parent != null) {
            this.setFolder(parent);
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        if (!this.initialized) {
            if (this.minecraft.getLanguageManager().getLanguages().size() == 1) {
                return;
            }
            this.setupPanel0(false);
            this.setupPanel1();
            this.setupPanel2();
            this.setupPanel3();
            this.initialized = true;
        }
        this.panelWidth = Math.max(this.width / 5, 100);
        this.panelArea.setHeight(this.height);
        this.panelArea.setWidth(this.panelWidth, 10);
        this.addRenderableWidget(this.panelArea);
        this.setFolder(this.folder);
        for (Area panel : this.panels) {
            panel.setX(this.panelWidth + 10, 5);
            panel.setHeight(this.height - 32);
            panel.setWidth(this.width - this.panelWidth - 10, 20);
            this.addRenderableWidget(panel);
        }
        this.group.reset();
        this.setSelected(this.group.getSelected());
    }

    @Override
    public boolean keyPressed(@Key int key, int scancode, @Modifiers int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            //noinspection VariableNotUsedInsideIf
            if (this.editBox != null) {
                this.setEditBox(null);
                return true;
            }
            if (this.group.getSelected() == 2) {
                if (this.goBack()) {
                    return true;
                }
            }
            this.onClose();
            return true;
        }
        GuiEventListener focused = this.getFocused();
        return focused != null && focused.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.editBox != null) {
            if (!MathHelper.isMouseInArea(mouseX, mouseY, this.editBox.x, this.editBox.y, this.editBox.getWidth(), this.editBox.getHeight())) {
                this.setEditBox(null);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        if (EvolutionConfig.isDirty()) {
            this.minecraft.setScreen(new ScreenConfirmation(this, this.textUnsaved, result -> {
                if (!result) {
                    return true;
                }
                EvolutionConfig.discardDirty();
                this.minecraft.setScreen(this.parentScreen);
                this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
                return false;
            }));
        }
        else {
            this.minecraft.setScreen(this.parentScreen);
            this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        GUIUtils.renderDirtBackground(0, 0, this.panelWidth, this.height, 32);
        GUIUtils.drawLine(this.panelWidth, 0, this.panelWidth, this.height, 0x50_5050);
        GUIUtils.renderDirtBackground(this.panelWidth + 1, 0, this.width, 32, 64);
        Component title = switch (this.group.getSelected()) {
            case 1 -> this.textTipsAndTricks;
            case 2 -> this.textConfig;
            case 3 -> this.textCredits;
            default -> this.getTitle();
        };
        matrices.pushPose();
        matrices.scale(2, 2, 1);
        drawCenteredString(matrices, Minecraft.getInstance().font, title, (this.width + this.panelWidth) / 4, 4, 0xff_ffff);
        matrices.popPose();
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    public void setEditBox(@Nullable AdvEditBox editBox) {
        assert this.minecraft != null;
        if (this.editBox != editBox) {
            if (this.editBox != null) {
                this.editBox.setFocus(false);
            }
            this.editBox = editBox;
            this.minecraft.keyboardHandler.setSendRepeatsToGui(editBox != null);
        }
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    private void setFolder(ConfigFolder folder) {
        boolean changed = this.folder != folder;
        this.folder = folder;
        this.configPath.set(folder);
        ScrollableArea area = this.configArea;
        this.backButton.active = !folder.isRoot();
        this.updateButtons.run();
        area.clear(changed);
        folder.sort();
        OList<IConfigItem> items = folder.items();
        for (int i = 0, len = items.size(); i < len; ++i) {
            IConfigItem item = items.get(i);
            ButtonLine buttonLine = new ButtonLine(0, 0, 20, 20, 5, 300);
            switch (item.type()) {
                case FOLDER -> {
                    buttonLine.add(new Button(0, 0, 20, 20, item.name(), b -> this.setFolder((ConfigFolder) item), (b, m, mx, my) -> this.tooltip(m, item, mx, my)));
                }
                case BOOLEAN -> {
                    buttonLine.add(new BooleanChanger(0, 0, 0, (ConfigBoolean) item, (m, mx, my) -> this.tooltip(m, item, mx, my), this.updateButtons));
                }
                case INTEGER -> {
                    buttonLine.add(new IntChanger(0, 0, 20, (ConfigInteger) item, (m, mx, my) -> this.tooltip(m, item, mx, my), this.updateButtons));
                }
                case ENUM -> {
                    buttonLine.add(new EnumChanger(0, 0, 20, (ConfigEnum) item, (m, mx, my) -> this.tooltip(m, item, mx, my), this.updateButtons));
                }
            }
            area.addBeginning(buttonLine);
        }
        Area panel = this.panels[2];
        panel.setX(this.panelWidth + 10, 5);
        panel.setHeight(this.height - 32);
        panel.setWidth(this.width - this.panelWidth - 10, 20);
        panel.setScreen(this);
    }

    protected void setSelected(int index) {
        this.group.setSelected(index);
        for (ButtonSelectable b : this.groupButtons) {
            b.refreshActive();
        }
        Area[] panels = this.panels;
        for (int i = 0, len = panels.length; i < len; i++) {
            panels[i].visible = i == index;
        }
    }

    private void setupPanel0(boolean isFirst) {
        Area panel = this.panels[0];
        panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.welcome"), 0xff_ffff));
        panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.welcome.languages"), 0xff_ffff));
        Minecraft mc = Minecraft.getInstance();
        LanguageManager languageManager = mc.getLanguageManager();
        TextWithActionList<LanguageInfo> langList = new TextWithActionList<>(panel.x, panel.y, panel.getWidth(), info -> {
            String code = info.getCode();
            if (!code.equals(languageManager.getSelected().getCode())) {
                languageManager.setSelected(info);
                mc.options.languageCode = code;
                mc.reloadResourcePacks();
                mc.options.save();
                return true;
            }
            return false;
        }, info -> new TextComponent(info.toString()));
        langList.addAction(languageManager.getLanguage("en_us"));
        langList.addAction(languageManager.getLanguage("pt_br"));
        panel.addBeginning(langList);
        panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.welcome.translation", new TextComponent("https://github.com/TheGreatWolf13/Evolution/").withStyle(ChatFormatting.UNDERLINE).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TheGreatWolf13/Evolution/")))), 0xff_ffff));
        if (isFirst) {
            panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.welcome.first"), 0xff_ffff));
        }
    }

    private void setupPanel1() {
        Area panel = this.panels[1];
        int x = panel.x;
        int width = panel.getWidth();
        for (int i = 0; i < 4; ++i) {
            //noinspection ObjectAllocationInLoop
            panel.addBeginning(makeTip(x, width, i));
        }
    }

    private void setupPanel2() {
        Area panel = this.panels[2];
        panel.addBeginning(this.configPath);
        ButtonLine buttonLine = new ButtonLine(0, 0, 20, 20, 5, 150);
        buttonLine.add(this.saveButton);
        buttonLine.add(this.restoreButton);
        buttonLine.add(this.backButton);
        panel.addEnd(buttonLine);
    }

    private void setupPanel3() {
        Area panel = this.panels[3];
        panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.credits.0", "TheGreatWolf13"), 0xff_ffff));
        panel.addBeginning(new TextArea(panel.x, panel.y, panel.getWidth(), new TranslatableComponent("evolution.gui.evolution.credits.1", "loi777"), 0xff_ffff));
    }

    @Override
    public void tick() {
        if (this.editBox != null) {
            this.editBox.tick();
        }
    }

    private void tooltip(PoseStack matrices, IConfigItem config, int mouseX, int mouseY) {
        boolean scissor = GUIUtils.disableScissorTemporarily();
        this.renderTooltip(matrices, Minecraft.getInstance().font.split(config.desc(), 200), mouseX, mouseY);
        if (scissor) {
            GUIUtils.reenableScissor();
        }
    }
}
