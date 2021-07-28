package tgw.evolution.client.gui.controls;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.MouseSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.GuiCheckBox;
import tgw.evolution.util.reflection.FieldHandler;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ScreenControls extends ControlsScreen {
    private static final FieldHandler<ControlsScreen, Screen> PARENT_SCREEN = new FieldHandler<>(ControlsScreen.class, "field_146332_f");
    private static final FieldHandler<ControlsScreen, KeyBindingList> KEY_BINDING_LIST = new FieldHandler<>(ControlsScreen.class, "field_146494_r");
    private final GameSettings options;
    private final Screen parentScreen;
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
        this.parentScreen = PARENT_SCREEN.get(screen);
        this.options = settings;
    }

    @Override
    public boolean charTyped(char var1, int var2) {
        return this.search.charTyped(var1, var2);
    }

    public void filterKeys() {
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        this.lastSearch = this.search.getText();
        keyBindingList.children().clear();
        if (this.lastSearch.isEmpty() && this.displayMode == DisplayMode.ALL && this.sortOrder == SortOrder.NONE) {
            keyBindingList.children().addAll(((ListKeyBinding) keyBindingList).getAllEntries());
            return;
        }
        keyBindingList.setScrollAmount(0);
        Predicate<ListKeyBinding.KeyEntry> filters = this.displayMode.getPredicate();
        switch (this.searchType) {
            case NAME:
                filters = filters.and(keyEntry -> keyEntry.getKeyDesc().toLowerCase().contains(this.lastSearch.toLowerCase()));
                break;
            case CATEGORY:
                filters = filters.and(keyEntry -> I18n.format(keyEntry.getKeybinding().getKeyCategory())
                                                      .toLowerCase()
                                                      .contains(this.lastSearch.toLowerCase()));
                break;
            case KEY:
                filters = filters.and(keyEntry -> keyEntry.getKeybinding().getLocalizedName().toLowerCase().contains(this.lastSearch.toLowerCase()));
                break;
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
                                  I18n.format("options.mouse_settings"),
                                  button -> this.minecraft.displayGuiScreen(new MouseSettingsScreen(this))));
        KEY_BINDING_LIST.set(this, new ListKeyBinding(this, this.minecraft));
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        this.children.add(keyBindingList);
        this.setFocused(keyBindingList);
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  this.height - 29,
                                  150,
                                  20,
                                  I18n.format("gui.done"),
                                  button -> ScreenControls.this.minecraft.displayGuiScreen(ScreenControls.this.parentScreen)));
        this.buttonReset = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controls.resetAll"), button -> {
            if (!this.confirmingReset) {
                this.confirmingReset = true;
                button.setMessage(I18n.format("evolution.options.controls.confirmReset"));
                return;
            }
            this.confirmingReset = false;
            button.setMessage(I18n.format("controls.resetAll"));
            for (KeyBinding keybinding : ScreenControls.this.minecraft.gameSettings.keyBindings) {
                keybinding.setToDefault();
            }
            KeyBinding.resetKeyBindingArrayAndHash();
        }));
        this.buttonUnbound = this.addButton(new Button(this.width / 2 - 155 + 160,
                                                       this.height - 29 - 24 - 24,
                                                       150,
                                                       20,
                                                       this.displayMode != DisplayMode.UNBOUND ?
                                                       I18n.format("evolution.options.controls.showUnbound") :
                                                       I18n.format("evolution.options.controls.showAll"),
                                                       button -> {
                                                           if (this.displayMode == DisplayMode.UNBOUND) {
                                                               this.buttonUnbound.setMessage(I18n.format("evolution.options.controls.showUnbound"));
                                                               this.displayMode = DisplayMode.ALL;
                                                           }
                                                           else {
                                                               this.displayMode = DisplayMode.UNBOUND;
                                                               this.buttonUnbound.setMessage(I18n.format("evolution.options.controls.showAll"));
                                                               this.buttonConflicting.setMessage(I18n.format("evolution.options.controls" +
                                                                                                             ".showConflicts"));
                                                           }
                                                           this.filterKeys();
                                                       }));
        this.buttonConflicting = this.addButton(new Button(this.width / 2 - 155 + 160,
                                                           this.height - 29 - 24,
                                                           150,
                                                           20,
                                                           this.displayMode != DisplayMode.CONFLICTING ?
                                                           I18n.format("evolution.options.controls.showConflicts") :
                                                           I18n.format("evolution.options.controls.showAll"),
                                                           button -> {
                                                               if (this.displayMode == DisplayMode.CONFLICTING) {
                                                                   this.buttonConflicting.setMessage(I18n.format(
                                                                           "evolution.options.controls.showConflicts"));
                                                                   this.displayMode = DisplayMode.ALL;
                                                               }
                                                               else {
                                                                   this.displayMode = DisplayMode.CONFLICTING;
                                                                   this.buttonConflicting.setMessage(I18n.format("evolution.options.controls" +
                                                                                                                 ".showAll"));
                                                                   this.buttonUnbound.setMessage(I18n.format("evolution.options.controls" +
                                                                                                             ".showUnbound"));
                                                               }
                                                               this.filterKeys();
                                                           }));
        this.search = new TextFieldWidget(this.font, this.width / 2 - 154, this.height - 29 - 23, 148, 18, "");
        this.search.setText(this.lastSearch);
        this.buttonKey = this.addButton(new GuiCheckBox(this.width / 2 -
                                                        10 -
                                                        13 -
                                                        this.font.getStringWidth(I18n.format("evolution.options.controls.key")),
                                                        this.height - 29 - 37,
                                                        I18n.format("evolution.options.controls.key"),
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
                                                        I18n.format("evolution.options.controls.category"),
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
                                  I18n.format("evolution.options.controls.sort", this.sortOrder.getName()),
                                  button -> {
                                      this.sortOrder = this.sortOrder.cycle();
                                      button.setMessage(I18n.format("evolution.options.controls.sort", this.sortOrder.getName()));
                                      this.filterKeys();
                                  }));
        this.filterKeys();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        this.buttonKey.setFocused(false);
        this.buttonCat.setFocused(false);
        if (!this.search.isFocused() && this.buttonId == null) {
            if (hasControlDown()) {
                if (InputMappings.isKeyDown(this.minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_F)) {
                    this.search.setFocused2(true);
                    return true;
                }
            }
        }
        if (this.search.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }
        if (this.search.isFocused()) {
            if (p_keyPressed_1_ == 256) {
                this.search.setFocused2(false);
                return true;
            }
        }
        if (this.buttonId != null) {
            if (p_keyPressed_1_ == 256) {
                this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.INPUT_INVALID);
                this.options.setKeyBindingCode(this.buttonId, InputMappings.INPUT_INVALID);
            }
            else {
                this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(),
                                                    InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
                this.options.setKeyBindingCode(this.buttonId, InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
            }
            if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(this.buttonId.getKey())) {
                //noinspection ConstantConditions
                this.buttonId = null;
            }
            this.time = Util.milliTime();
            KeyBinding.resetKeyBindingArrayAndHash();
            return true;
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int mb) {
        KeyBindingList keyBindingList = KEY_BINDING_LIST.get(this);
        boolean valid;
        if (this.buttonId != null) {
            this.options.setKeyBindingCode(this.buttonId, InputMappings.Type.MOUSE.getOrMakeInput(mb));
            //noinspection ConstantConditions
            this.buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
            valid = true;
            this.search.setFocused2(false);
        }
        else if (mb == 0 && keyBindingList.mouseClicked(mx, my, mb)) {
            this.setDragging(true);
            this.setFocused(keyBindingList);
            valid = true;
            this.search.setFocused2(false);
        }
        else {
            valid = this.search.mouseClicked(mx, my, mb);
            if (!valid && this.search.isFocused() && mb == 1) {
                this.search.setText("");
                valid = true;
            }
        }
        if (!valid) {
            for (IGuiEventListener iguieventlistener : this.children()) {
                if (iguieventlistener.mouseClicked(mx, my, mb)) {
                    this.setFocused(iguieventlistener);
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        KEY_BINDING_LIST.get(this).render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 8, 0xff_ffff);
        boolean flag = false;
        for (KeyBinding keybinding : this.options.keyBindings) {
            if (!keybinding.isDefault()) {
                flag = true;
                break;
            }
        }
        this.search.render(mouseX, mouseY, partialTicks);
        this.buttonReset.active = flag;
        if (!flag) {
            this.confirmingReset = false;
            this.buttonReset.setMessage(I18n.format("controls.resetAll"));
        }
        for (Widget button : this.buttons) {
            button.render(mouseX, mouseY, partialTicks);
        }
        String text = I18n.format("evolution.options.controls.search");
        GlStateManager.disableLighting();
        this.font.drawStringWithShadow(text, (this.width - 155 - this.font.getStringWidth(text)) / 2.0f, this.height - 27 - 50, 0xff_ffff);
        GlStateManager.enableLighting();
    }

    @Override
    public void tick() {
        this.search.tick();
        if (!this.lastSearch.equals(this.search.getText())) {
            this.filterKeys();
        }
    }
}
