package tgw.evolution.client.gui.controls;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ListKeyBinding extends KeyBindingList {
    private final ControlsScreen controlsScreen;
    public List<Entry> allEntries;
    private int maxListLabelWidth;

    public ListKeyBinding(ControlsScreen controls, Minecraft mc) {
        super(controls, mc);
        this.width = controls.width + 45;
        this.height = controls.height;
        this.y0 = 43;
        this.y1 = controls.height - 85;
        this.x1 = controls.width + 45;
        this.controlsScreen = controls;
        this.children().clear();
        this.allEntries = new ArrayList<>();
        KeyBinding[] akeybinding = ArrayUtils.clone(mc.gameSettings.keyBindings);
        Arrays.sort(akeybinding);
        String s = null;
        for (KeyBinding keybinding : akeybinding) {
            String s1 = keybinding.getKeyCategory();
            if (!s1.equals(s)) {
                s = s1;
                if (!s1.endsWith(".hidden")) {
                    //noinspection ObjectAllocationInLoop
                    this.add(new ListKeyBinding.CategoryEntry(s1));
                }
            }
            int i = mc.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription()));
            if (i > this.maxListLabelWidth) {
                this.maxListLabelWidth = i;
            }
            if (!s1.endsWith(".hidden")) {
                //noinspection ObjectAllocationInLoop
                this.add(new ListKeyBinding.KeyEntry(keybinding));
            }
        }
    }

    public void add(Entry ent) {
        this.children().add(ent);
        this.allEntries.add(ent);
    }

    public List<Entry> getAllEntries() {
        return this.allEntries;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends KeyBindingList.Entry {

        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.format(name);
            this.labelWidth = ListKeyBinding.this.minecraft.fontRenderer.getStringWidth(this.labelText);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public void render(int index, int rowY, int rowX, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            ListKeyBinding.this.minecraft.fontRenderer.drawString(this.labelText,
                                                                  (ListKeyBinding.this.minecraft.currentScreen.width - this.labelWidth) / 2.0f,
                                                                  rowY + height - 9 - 1,
                                                                  0xff_ffff);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class KeyEntry extends KeyBindingList.Entry {

        private final Button btnChangeKeyBinding;
        private final Button btnResetKeyBinding;
        /**
         * The localized key description for this KeyEntry
         */
        private final String keyDesc;
        /**
         * The keybinding specified for this KeyEntry
         */
        private final KeyBinding keybinding;

        private KeyEntry(final KeyBinding name) {
            this.keybinding = name;
            this.keyDesc = I18n.format(name.getKeyDescription());
            this.btnChangeKeyBinding = new Button(0, 0, 95, 20, this.keyDesc, button -> ListKeyBinding.this.controlsScreen.buttonId = name) {

                private boolean wasHovered;

                @Override
                protected String getNarrationMessage() {
                    return name.isInvalid() ?
                           I18n.format("narrator.controls.unbound", ListKeyBinding.KeyEntry.this.keyDesc) :
                           I18n.format("narrator.controls.bound", ListKeyBinding.KeyEntry.this.keyDesc, super.getNarrationMessage());
                }

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ListKeyBinding.this.y1;
                        if (this.wasHovered != this.isHovered()) {
                            if (this.isHovered()) {
                                if (this.isFocused()) {
                                    this.nextNarration = Util.milliTime() + 200L;
                                }
                                else {
                                    this.nextNarration = Util.milliTime() + 750L;
                                }
                            }
                            else {
                                this.nextNarration = Long.MAX_VALUE;
                            }
                        }
                        if (this.visible) {
                            this.renderButton(mouseX, mouseY, partialTicks);
                        }
                        this.narrate();
                        this.wasHovered = this.isHovered();
                    }
                }
            };
            this.btnResetKeyBinding = new Button(0, 0, 50, 20, I18n.format("controls.reset"), button -> {
                this.keybinding.setToDefault();
                ListKeyBinding.this.minecraft.gameSettings.setKeyBindingCode(name, name.getDefault());
                KeyBinding.resetKeyBindingArrayAndHash();
            }) {
                private boolean wasHovered;

                @Override
                protected String getNarrationMessage() {
                    return I18n.format("narrator.controls.reset", ListKeyBinding.KeyEntry.this.keyDesc);
                }

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ListKeyBinding.this.y1;
                        if (this.wasHovered != this.isHovered()) {
                            if (this.isHovered()) {
                                if (this.isFocused()) {
                                    this.nextNarration = Util.milliTime() + 200L;
                                }
                                else {
                                    this.nextNarration = Util.milliTime() + 750L;
                                }
                            }
                            else {
                                this.nextNarration = Long.MAX_VALUE;
                            }
                        }
                        if (this.visible) {
                            this.renderButton(mouseX, mouseY, partialTicks);
                        }
                        this.narrate();
                        this.wasHovered = this.isHovered();
                    }
                }
            };
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.btnChangeKeyBinding, this.btnResetKeyBinding);
        }

        public Button getBtnChangeKeyBinding() {
            return this.btnChangeKeyBinding;
        }

        public String getKeyDesc() {
            return this.keyDesc;
        }

        public KeyBinding getKeybinding() {
            return this.keybinding;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int modifiers) {
            if (this.btnChangeKeyBinding.mouseClicked(mouseX, mouseY, modifiers)) {
                return true;
            }
            return this.btnResetKeyBinding.mouseClicked(mouseX, mouseY, modifiers);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int modifiers) {
            return this.btnChangeKeyBinding.mouseReleased(mouseX, mouseY, modifiers);
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            boolean flag = ListKeyBinding.this.controlsScreen.buttonId == this.keybinding;
            ListKeyBinding.this.minecraft.fontRenderer.drawString(this.keyDesc,
                                                                  x + 90 - ListKeyBinding.this.maxListLabelWidth,
                                                                  y + (height - 9) / 2.0f,
                                                                  0xff_ffff);
            this.btnResetKeyBinding.x = x + 190 + 20;
            this.btnResetKeyBinding.y = y;
            this.btnResetKeyBinding.active = !this.keybinding.isDefault();
            this.btnResetKeyBinding.render(mouseX, mouseY, partialTicks);
            this.btnChangeKeyBinding.x = x + 105;
            this.btnChangeKeyBinding.y = y;
            this.btnChangeKeyBinding.setMessage(this.keybinding.getLocalizedName());
            boolean conflicts = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!this.keybinding.isInvalid()) {
                for (KeyBinding keybinding : ListKeyBinding.this.minecraft.gameSettings.keyBindings) {
                    if (keybinding != this.keybinding && this.keybinding.conflicts(keybinding)) {
                        conflicts = true;
                        keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
                    }
                }
            }
            if (flag) {
                this.btnChangeKeyBinding.setMessage(TextFormatting.WHITE +
                                                    "> " +
                                                    TextFormatting.YELLOW +
                                                    this.btnChangeKeyBinding.getMessage() +
                                                    TextFormatting.WHITE +
                                                    " <");
            }
            else if (conflicts) {
                this.btnChangeKeyBinding.setMessage((keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED) +
                                                    this.btnChangeKeyBinding.getMessage());
            }
            this.btnChangeKeyBinding.render(mouseX, mouseY, partialTicks);
        }
    }
}
