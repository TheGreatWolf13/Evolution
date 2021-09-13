package tgw.evolution.client.gui.controls;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.MouseSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.widgets.GuiCheckBox;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.reflection.FieldHandler;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ScreenControls extends ControlsScreen {
    private static final FieldHandler<SettingsScreen, Screen> PARENT_SCREEN = new FieldHandler<>(SettingsScreen.class, "field_228182_a_");
    private static final FieldHandler<ControlsScreen, KeyBindingList> KEY_BINDING_LIST = new FieldHandler<>(ControlsScreen.class, "field_146494_r");
    private final GameSettings options;
    private GuiCheckBox buttonCat;
    private Button buttonConflicting;
    private GuiCheckBox buttonKey;
    private Button buttonReset;
    private Button buttonUnbound;
    private boolean confirmingReset;
    private DisplayMode displayMode = DisplayMode.ALL;
    private boolean isCategoryMarked;
    private boolean isKeyMarked;
    private String lastSearch = "";
    private TextFieldWidget search;
    private SearchType searchType = SearchType.NAME;
    private SortOrder sortOrder = SortOrder.NONE;

    public ScreenControls(ControlsScreen screen, GameSettings settings) {
        super(PARENT_SCREEN.get(screen), settings);
        this.options = settings;
    }

    @Override
    public boolean charTyped(char var1, int var2) {
        return this.search.charTyped(var1, var2);
    }

    public void filterKeys() {
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        this.lastSearch = this.search.getValue();
        keyBindingList.children().clear();
        if (this.lastSearch.isEmpty() && this.displayMode == DisplayMode.ALL && this.sortOrder == SortOrder.NONE) {
            keyBindingList.children().addAll(((ListKeyBinding) keyBindingList).getAllEntries());
            return;
        }
        keyBindingList.setScrollAmount(0);
        Predicate<ListKeyBinding.KeyEntry> filters = this.displayMode.getPredicate();
        switch (this.searchType) {
            case NAME: {
                filters = filters.and(keyEntry -> keyEntry.getKeyDesc().getString().toLowerCase().contains(this.lastSearch.toLowerCase()));
                break;
            }
            case CATEGORY: {
                filters = filters.and(keyEntry -> I18n.get(keyEntry.getKeybinding().getCategory())
                                                      .toLowerCase()
                                                      .contains(this.lastSearch.toLowerCase()));
                break;
            }
            case KEY: {
                filters = filters.and(keyEntry -> keyEntry.getKeybinding()
                                                          .getTranslatedKeyMessage()
                                                          .getString()
                                                          .toLowerCase()
                                                          .contains(this.lastSearch.toLowerCase()));
                break;
            }
        }
        for (ListKeyBinding.Entry entry : ((ListKeyBinding) keyBindingList).getAllEntries()) {
            if (entry instanceof ListKeyBinding.KeyEntry) {
                ListKeyBinding.KeyEntry keyEntry = (ListKeyBinding.KeyEntry) entry;
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
        this.addButton(new Button(this.width / 2 - 155,
                                  18,
                                  150,
                                  20,
                                  EvolutionTexts.GUI_CONTROLS_MOUSE_SETTINGS,
                                  button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.minecraft.options))));
        KEY_BINDING_LIST.set(this, new ListKeyBinding(this, this.minecraft));
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        this.children.add(keyBindingList);
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  this.height - 29,
                                  150,
                                  20,
                                  EvolutionTexts.GUI_GENERAL_DONE,
                                  button -> ScreenControls.this.minecraft.setScreen(ScreenControls.this.lastScreen)));
        this.buttonReset = this.addButton(new Button(this.width / 2 - 155,
                                                     this.height - 29,
                                                     150,
                                                     20,
                                                     EvolutionTexts.GUI_CONTROLS_RESET_ALL,
                                                     button -> {
                                                         if (!this.confirmingReset) {
                                                             this.confirmingReset = true;
                                                             button.setMessage(EvolutionTexts.GUI_CONTROLS_CONFIRM_RESET);
                                                             return;
                                                         }
                                                         this.confirmingReset = false;
                                                         button.setMessage(EvolutionTexts.GUI_CONTROLS_RESET_ALL);
                                                         for (KeyBinding keybinding : ScreenControls.this.minecraft.options.keyMappings) {
                                                             keybinding.setToDefault();
                                                         }
                                                         KeyBinding.resetMapping();
                                                     }));
        this.buttonUnbound = this.addButton(new Button(this.width / 2 - 155 + 160,
                                                       this.height - 29 - 24 - 24,
                                                       150,
                                                       20,
                                                       this.displayMode != DisplayMode.UNBOUND ?
                                                       EvolutionTexts.GUI_CONTROLS_SHOW_UNBOUND :
                                                       EvolutionTexts.GUI_CONTROLS_SHOW_ALL,
                                                       button -> {
                                                           if (this.displayMode == DisplayMode.UNBOUND) {
                                                               this.buttonUnbound.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_UNBOUND);
                                                               this.displayMode = DisplayMode.ALL;
                                                           }
                                                           else {
                                                               this.displayMode = DisplayMode.UNBOUND;
                                                               this.buttonUnbound.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_ALL);
                                                               this.buttonConflicting.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_CONFLICTS);
                                                           }
                                                           this.filterKeys();
                                                       }));
        this.buttonConflicting = this.addButton(new Button(this.width / 2 - 155 + 160,
                                                           this.height - 29 - 24,
                                                           150,
                                                           20,
                                                           this.displayMode != DisplayMode.CONFLICTING ?
                                                           EvolutionTexts.GUI_CONTROLS_SHOW_CONFLICTS :
                                                           EvolutionTexts.GUI_CONTROLS_SHOW_ALL,
                                                           button -> {
                                                               if (this.displayMode == DisplayMode.CONFLICTING) {
                                                                   this.buttonConflicting.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_CONFLICTS);
                                                                   this.displayMode = DisplayMode.ALL;
                                                               }
                                                               else {
                                                                   this.displayMode = DisplayMode.CONFLICTING;
                                                                   this.buttonConflicting.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_ALL);
                                                                   this.buttonUnbound.setMessage(EvolutionTexts.GUI_CONTROLS_SHOW_UNBOUND);
                                                               }
                                                               this.filterKeys();
                                                           }));
        this.search = new TextFieldWidget(this.font, this.width / 2 - 154, this.height - 29 - 23, 148, 18, EvolutionTexts.EMPTY);
        this.search.setValue(this.lastSearch);
        this.buttonKey = this.addButton(new GuiCheckBox(this.width / 2 - 10 - 13 - this.font.width(EvolutionTexts.GUI_CONTROLS_KEY),
                                                        this.height - 29 - 37,
                                                        EvolutionTexts.GUI_CONTROLS_KEY,
                                                        this.isKeyMarked) {
            @Override
            public void onPress() {
                super.onPress();
                ScreenControls.this.isKeyMarked = !ScreenControls.this.isKeyMarked;
                ScreenControls.this.buttonCat.setIsChecked(false);
                ScreenControls.this.isCategoryMarked = false;
                ScreenControls.this.searchType = this.isChecked() ? SearchType.KEY : SearchType.NAME;
                ScreenControls.this.filterKeys();
            }
        });
        this.buttonCat = this.addButton(new GuiCheckBox(this.width / 2 - 150,
                                                        this.height - 29 - 37,
                                                        EvolutionTexts.GUI_CONTROLS_CATEGORY,
                                                        this.isCategoryMarked) {
            @Override
            public void onPress() {
                super.onPress();
                ScreenControls.this.buttonKey.setIsChecked(false);
                ScreenControls.this.isKeyMarked = false;
                ScreenControls.this.isCategoryMarked = !ScreenControls.this.isCategoryMarked;
                ScreenControls.this.searchType = this.isChecked() ? SearchType.CATEGORY : SearchType.NAME;
                ScreenControls.this.filterKeys();
            }
        });
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  18,
                                  150,
                                  20,
                                  new TranslationTextComponent("evolution.gui.controls.sort", this.sortOrder.getName()),
                                  button -> {
                                      this.sortOrder = this.sortOrder.cycle();
                                      button.setMessage(new TranslationTextComponent("evolution.gui.controls.sort", this.sortOrder.getName()));
                                      this.filterKeys();
                                  }));
        this.filterKeys();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.buttonKey.setFocused(false);
        this.buttonCat.setFocused(false);
        if (!this.search.isFocused() && this.selectedKey == null) {
            if (hasControlDown()) {
                if (InputMappings.isKeyDown(this.minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F)) {
                    this.search.setFocus(true);
                    return true;
                }
            }
        }
        if (this.search.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.search.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.search.setFocus(false);
                return true;
            }
        }
        if (this.selectedKey != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.getActiveModifier(), InputMappings.UNKNOWN);
                this.options.setKey(this.selectedKey, InputMappings.UNKNOWN);
            }
            else {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.getActiveModifier(), InputMappings.getKey(keyCode, scanCode));
                this.options.setKey(this.selectedKey, InputMappings.getKey(keyCode, scanCode));
            }
            if (!KeyModifier.isKeyCodeModifier(this.selectedKey.getKey())) {
                //noinspection ConstantConditions
                this.selectedKey = null;
            }
            this.lastKeySelection = Util.getMillis();
            KeyBinding.resetMapping();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int mb) {
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        boolean valid;
        if (this.selectedKey != null) {
            this.options.setKey(this.selectedKey, InputMappings.Type.MOUSE.getOrCreate(mb));
            //noinspection ConstantConditions
            this.selectedKey = null;
            KeyBinding.resetMapping();
            valid = true;
            this.search.setFocus(false);
        }
        else if (mb == 0 && keyBindingList.mouseClicked(mx, my, mb)) {
            this.setDragging(true);
            valid = true;
            this.search.setFocus(false);
        }
        else {
            valid = this.search.mouseClicked(mx, my, mb);
            if (!valid && this.search.isFocused() && mb == 1) {
                this.search.setValue("");
                valid = true;
            }
        }
        if (!valid) {
            for (IGuiEventListener iguieventlistener : this.children()) {
                if (iguieventlistener.mouseClicked(mx, my, mb)) {
                    if (mb == 0) {
                        this.setDragging(true);
                    }
                    return true;
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int mb) {
        if (mb == 0 && KEY_BINDING_LIST.get(this).mouseReleased(mx, my, mb)) {
            this.setDragging(false);
            return true;
        }
        if (this.search.isFocused()) {
            return this.search.mouseReleased(mx, my, mb);
        }
        this.setDragging(false);
        return false;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        KEY_BINDING_LIST.get(this).render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 5, 0xff_ffff);
        boolean isResetActive = false;
        for (KeyBinding keybinding : this.options.keyMappings) {
            if (!keybinding.isDefault()) {
                isResetActive = true;
                break;
            }
        }
        this.search.render(matrices, mouseX, mouseY, partialTicks);
        this.buttonReset.active = isResetActive;
        if (!isResetActive) {
            this.confirmingReset = false;
            this.buttonReset.setMessage(EvolutionTexts.GUI_CONTROLS_RESET_ALL);
        }
        for (Widget button : this.buttons) {
            button.render(matrices, mouseX, mouseY, partialTicks);
        }
        RenderSystem.disableLighting();
        this.font.draw(matrices,
                       EvolutionTexts.GUI_GENERAL_SEARCH,
                       (this.width - 155 - this.font.width(EvolutionTexts.GUI_GENERAL_SEARCH)) / 2.0f,
                       this.height - 27 - 50,
                       0xff_ffff);
        RenderSystem.enableLighting();
    }

    @Override
    public void tick() {
        this.search.tick();
        if (!this.lastSearch.equals(this.search.getValue())) {
            this.filterKeys();
        }
    }
}
