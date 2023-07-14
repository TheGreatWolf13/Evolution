package tgw.evolution.client.gui.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.widgets.AdvCheckBox;
import tgw.evolution.client.gui.widgets.AdvEditBox;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;

import java.util.function.Predicate;

public class ScreenKeyBinds extends KeyBindsScreen {
    private final Options options;
    private final Component textCategory = new TranslatableComponent("evolution.gui.controls.category");
    private final Component textConfirmReset = new TranslatableComponent("evolution.gui.controls.confirmReset");
    private final Component textKey = new TranslatableComponent("evolution.gui.controls.key");
    private final Component textResetAll = new TranslatableComponent("evolution.gui.controls.resetAll");
    private final Component textShowAll = new TranslatableComponent("evolution.gui.controls.showAll");
    private final Component textShowConflicts = new TranslatableComponent("evolution.gui.controls.showConflicts");
    private final Component textShowUnbound = new TranslatableComponent("evolution.gui.controls.showUnbound");
    private AdvCheckBox buttonCat;
    private Button buttonConflicting;
    private AdvCheckBox buttonKey;
    private Button buttonReset;
    private Button buttonUnbound;
    private boolean confirmingReset;
    private DisplayMode displayMode = DisplayMode.ALL;
    private boolean isCategoryMarked;
    private boolean isKeyMarked;
    private String lastSearch = "";
    private AdvEditBox searchBox;
    private SearchType searchType = SearchType.NAME;
    private SortOrder sortOrder = SortOrder.NONE;

    public ScreenKeyBinds(Screen screen, Options options) {
        super(screen, options);
        this.options = options;
    }

    @Override
    public boolean charTyped(char var1, int var2) {
        this.resetConfirmReset();
        return this.searchBox.charTyped(var1, var2);
    }

    public void filterKeys() {
        KeyBindsList keyBindingList = this.keyBindsList;
        this.lastSearch = this.searchBox.getValue();
        keyBindingList.children().clear();
        if (this.lastSearch.isEmpty() && this.displayMode == DisplayMode.ALL && this.sortOrder == SortOrder.NONE) {
            keyBindingList.children().addAll(((ListKeyBinds) keyBindingList).getAllEntries());
            return;
        }
        keyBindingList.setScrollAmount(0);
        Predicate<ListKeyBinds.KeyEntry> filters = this.displayMode.getPredicate();
        StringBuilder builder = new StringBuilder();
        switch (this.searchType) {
            case NAME -> filters = filters.and(keyEntry -> MathHelper.contains(keyEntry.getName().getString(), this.lastSearch, builder));
            case CATEGORY -> filters = filters.and(keyEntry -> MathHelper.contains(I18n.get(keyEntry.getKey().getCategory()),
                                                                                   this.lastSearch,
                                                                                   builder));
            case KEY -> filters = filters.and(keyEntry -> MathHelper.contains(keyEntry.getKey().getTranslatedKeyMessage().getString(),
                                                                              this.lastSearch,
                                                                              builder));
        }
        for (ListKeyBinds.Entry entry : ((ListKeyBinds) keyBindingList).getAllEntries()) {
            if (entry instanceof ListKeyBinds.KeyEntry keyEntry) {
                if (filters.test(keyEntry)) {
                    keyBindingList.children().add(entry);
                }
            }
        }
        this.sortOrder.sort(keyBindingList.children());
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    protected void init() {
        this.confirmingReset = false;
        assert this.minecraft != null;
        this.keyBindsList = new ListKeyBinds(this, this.minecraft);
        this.addWidget(this.keyBindsList);
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                            this.height - 29,
                                            150,
                                            20,
                                            EvolutionTexts.GUI_GENERAL_DONE,
                                            button -> ScreenKeyBinds.this.minecraft.setScreen(ScreenKeyBinds.this.lastScreen)));
        this.buttonReset = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, this.textResetAll, button -> {
            if (!this.confirmingReset) {
                this.confirmingReset = true;
                button.setMessage(this.textConfirmReset);
                return;
            }
            this.confirmingReset = false;
            button.setMessage(this.textResetAll);
            for (KeyMapping keybinding : ScreenKeyBinds.this.minecraft.options.keyMappings) {
                keybinding.setKey(keybinding.getDefaultKey());
            }
            KeyMapping.resetMapping();
        }));
        this.buttonUnbound = this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                                                 this.height - 29 - 24 - 24,
                                                                 150,
                                                                 20,
                                                                 this.displayMode != DisplayMode.UNBOUND ? this.textShowUnbound : this.textShowAll,
                                                                 button -> {
                                                                     this.resetConfirmReset();
                                                                     if (this.displayMode == DisplayMode.UNBOUND) {
                                                                         this.buttonUnbound.setMessage(this.textShowUnbound);
                                                                         this.displayMode = DisplayMode.ALL;
                                                                     }
                                                                     else {
                                                                         this.displayMode = DisplayMode.UNBOUND;
                                                                         this.buttonUnbound.setMessage(this.textShowAll);
                                                                         this.buttonConflicting.setMessage(this.textShowConflicts);
                                                                     }
                                                                     this.filterKeys();
                                                                 }));
        this.buttonConflicting = this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                                                     this.height - 29 - 24,
                                                                     150,
                                                                     20,
                                                                     this.displayMode != DisplayMode.CONFLICTING ?
                                                                     this.textShowConflicts :
                                                                     this.textShowAll,
                                                                     button -> {
                                                                         this.resetConfirmReset();
                                                                         if (this.displayMode == DisplayMode.CONFLICTING) {
                                                                             this.buttonConflicting.setMessage(this.textShowConflicts);
                                                                             this.displayMode = DisplayMode.ALL;
                                                                         }
                                                                         else {
                                                                             this.displayMode = DisplayMode.CONFLICTING;
                                                                             this.buttonConflicting.setMessage(this.textShowAll);
                                                                             this.buttonUnbound.setMessage(this.textShowUnbound);
                                                                         }
                                                                         this.filterKeys();
                                                                     }));
        this.searchBox = new AdvEditBox(this.font, this.width / 2 - 154, this.height - 29 - 23, 148, 18, EvolutionTexts.EMPTY);
        this.searchBox.setValue(this.lastSearch);
        this.addWidget(this.searchBox);
        this.buttonKey = this.addRenderableWidget(new AdvCheckBox(this.width / 2 - 10 - 13 - this.font.width(this.textKey),
                                                                  this.height - 29 - 37,
                                                                  this.textKey,
                                                                  this.isKeyMarked,
                                                                  b -> {
                                                                      this.resetConfirmReset();
                                                                      this.isKeyMarked = !this.isKeyMarked;
                                                                      this.buttonCat.setIsChecked(false);
                                                                      this.isCategoryMarked = false;
                                                                      this.searchType = b.isChecked() ? SearchType.KEY : SearchType.NAME;
                                                                      this.filterKeys();
                                                                  }));
        this.buttonCat = this.addRenderableWidget(new AdvCheckBox(this.width / 2 - 150,
                                                                  this.height - 29 - 37,
                                                                  this.textCategory,
                                                                  this.isCategoryMarked,
                                                                  b -> {
                                                                      this.resetConfirmReset();
                                                                      this.buttonKey.setIsChecked(false);
                                                                      this.isKeyMarked = false;
                                                                      this.isCategoryMarked = !this.isCategoryMarked;
                                                                      this.searchType = b.isChecked() ? SearchType.CATEGORY : SearchType.NAME;
                                                                      this.filterKeys();
                                                                  }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                            18,
                                            150,
                                            20,
                                            new TranslatableComponent("evolution.gui.controls.sort", this.sortOrder.getName()),
                                            button -> {
                                                this.resetConfirmReset();
                                                this.sortOrder = this.sortOrder.cycle();
                                                button.setMessage(new TranslatableComponent("evolution.gui.controls.sort", this.sortOrder.getName()));
                                                this.filterKeys();
                                            }));
        this.filterKeys();
    }

    @Override
    public boolean keyPressed(@Key int keyCode, int scanCode, @Modifiers int modifiers) {
        this.buttonKey.setFocused(false);
        this.buttonCat.setFocused(false);
        if (!this.searchBox.isFocused() && this.selectedKey == null) {
            if (hasControlDown()) {
                assert this.minecraft != null;
                if (InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F)) {
                    this.resetConfirmReset();
                    this.searchBox.setFocus(true);
                    return true;
                }
            }
        }
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            this.resetConfirmReset();
            return true;
        }
        if (this.searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.resetConfirmReset();
                this.searchBox.setFocus(false);
                return true;
            }
        }
        if (this.selectedKey != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
            }
            else {
                this.options.setKey(this.selectedKey, InputConstants.getKey(keyCode, scanCode));
            }
            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            KeyMapping.resetMapping();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, @MouseButton int mb) {
        this.searchBox.setFocus(false);
        return super.mouseClicked(mx, my, mb);
    }

    @Override
    public boolean mouseReleased(double mx, double my, @MouseButton int mb) {
        if (mb == GLFW.GLFW_MOUSE_BUTTON_1 && this.keyBindsList.mouseReleased(mx, my, mb)) {
            this.setDragging(false);
            return true;
        }
        if (this.searchBox.isFocused()) {
            return this.searchBox.mouseReleased(mx, my, mb);
        }
        this.setDragging(false);
        return false;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        this.keyBindsList.render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 5, 0xff_ffff);
        boolean isResetActive = false;
        for (KeyMapping keybinding : this.options.keyMappings) {
            if (!keybinding.isDefault()) {
                isResetActive = true;
                break;
            }
        }
        this.searchBox.render(matrices, mouseX, mouseY, partialTicks);
        this.buttonReset.active = isResetActive;
        if (!isResetActive) {
            this.confirmingReset = false;
            this.buttonReset.setMessage(this.textResetAll);
        }
        for (Widget button : this.renderables) {
            button.render(matrices, mouseX, mouseY, partialTicks);
        }
        this.font.draw(matrices,
                       EvolutionTexts.GUI_GENERAL_SEARCH,
                       (this.width - 155 - this.font.width(EvolutionTexts.GUI_GENERAL_SEARCH)) / 2.0f,
                       this.height - 27 - 50,
                       0xff_ffff);
    }

    private void resetConfirmReset() {
        this.confirmingReset = false;
        this.buttonReset.setMessage(this.textResetAll);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
        if (!this.lastSearch.equals(this.searchBox.getValue())) {
            this.filterKeys();
        }
    }
}
