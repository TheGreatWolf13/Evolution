package tgw.evolution.client.gui.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import tgw.evolution.init.EvolutionTexts;

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
        KeyBinding[] akeybinding = ArrayUtils.clone(mc.options.keyMappings);
        Arrays.sort(akeybinding);
        String s = null;
        for (KeyBinding keybinding : akeybinding) {
            String s1 = keybinding.getCategory();
            if (!s1.equals(s)) {
                s = s1;
                if (!s1.endsWith(".hidden")) {
                    //noinspection ObjectAllocationInLoop
                    this.add(new ListKeyBinding.CategoryEntry(s1));
                }
            }
            int i = mc.font.width(I18n.get(keybinding.getName()));
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
        return super.getRowWidth() + 80;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 25;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends KeyBindingList.Entry {

        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.get(name);
            this.labelWidth = ListKeyBinding.this.minecraft.font.width(this.labelText);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public void render(MatrixStack matrices,
                           int index,
                           int rowY,
                           int rowX,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean isMouseOver,
                           float partialTicks) {
            ListKeyBinding.this.minecraft.font.draw(matrices,
                                                    this.labelText,
                                                    (ListKeyBinding.this.minecraft.screen.width - this.labelWidth) / 2.0f,
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
        private final ITextComponent keyDesc;
        /**
         * The keybinding specified for this KeyEntry
         */
        private final KeyBinding keybinding;

        private KeyEntry(final KeyBinding keyBinding) {
            this.keybinding = keyBinding;
            this.keyDesc = new TranslationTextComponent(keyBinding.getName());
            this.btnChangeKeyBinding = new Button(0, 0, 95, 20, this.keyDesc, button -> ListKeyBinding.this.controlsScreen.selectedKey = keyBinding) {

                private boolean wasHovered;

                @Override
                public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY <= ListKeyBinding.this.y1 &&
                                         mouseY >= ListKeyBinding.this.y0;
                        if (this.wasHovered != this.isHovered()) {
                            if (this.isHovered()) {
                                if (this.isFocused()) {
                                    this.nextNarration = Util.getMillis() + 200L;
                                }
                                else {
                                    this.nextNarration = Util.getMillis() + 750L;
                                }
                            }
                            else {
                                this.nextNarration = Long.MAX_VALUE;
                            }
                        }
                        if (this.visible) {
                            this.renderButton(matrices, mouseX, mouseY, partialTicks);
                        }
                        this.narrate();
                        this.wasHovered = this.isHovered();
                    }
                }
            };
            this.btnResetKeyBinding = new Button(0, 0, 50, 20, EvolutionTexts.GUI_CONTROLS_RESET, button -> {
                this.keybinding.setToDefault();
                ListKeyBinding.this.minecraft.options.setKey(keyBinding, keyBinding.getDefaultKey());
                KeyBinding.resetMapping();
            }) {
                private boolean wasHovered;

                @Override
                public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ListKeyBinding.this.y1 &&
                                         mouseY >= ListKeyBinding.this.y0;
                        if (this.wasHovered != this.isHovered()) {
                            if (this.isHovered()) {
                                if (this.isFocused()) {
                                    this.nextNarration = Util.getMillis() + 200L;
                                }
                                else {
                                    this.nextNarration = Util.getMillis() + 750L;
                                }
                            }
                            else {
                                this.nextNarration = Long.MAX_VALUE;
                            }
                        }
                        if (this.visible) {
                            this.renderButton(matrices, mouseX, mouseY, partialTicks);
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

        public ITextComponent getKeyDesc() {
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
        public void render(MatrixStack matrices,
                           int index,
                           int y,
                           int x,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean isMouseOver,
                           float partialTicks) {
            boolean flag = ListKeyBinding.this.controlsScreen.selectedKey == this.keybinding;
            ListKeyBinding.this.minecraft.font.draw(matrices,
                                                    this.keyDesc,
                                                    x + ListKeyBinding.this.getRowWidth() -
                                                    this.btnResetKeyBinding.getWidth() -
                                                    this.btnChangeKeyBinding.getWidth() -
                                                    ListKeyBinding.this.maxListLabelWidth -
                                                    10,
                                                    y + (height - 4) / 2.0f,
                                                    0xff_ffff);
            this.btnResetKeyBinding.x = x + ListKeyBinding.this.getRowWidth() - this.btnResetKeyBinding.getWidth() - 2;
            this.btnResetKeyBinding.y = y;
            this.btnResetKeyBinding.active = !this.keybinding.isDefault();
            this.btnResetKeyBinding.render(matrices, mouseX, mouseY, partialTicks);
            this.btnChangeKeyBinding.x = x + ListKeyBinding.this.getRowWidth() -
                                         this.btnResetKeyBinding.getWidth() -
                                         this.btnChangeKeyBinding.getWidth() -
                                         4;
            this.btnChangeKeyBinding.y = y;
            this.btnChangeKeyBinding.setMessage(this.keybinding.getTranslatedKeyMessage());
            boolean conflicts = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!this.keybinding.isUnbound()) {
                for (KeyBinding keybinding : ListKeyBinding.this.minecraft.options.keyMappings) {
                    if (keybinding != this.keybinding && this.keybinding.same(keybinding)) {
                        conflicts = true;
                        keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
                    }
                }
            }
            if (flag) {
                this.btnChangeKeyBinding.setMessage(new StringTextComponent("> ").append(this.btnChangeKeyBinding.getMessage()
                                                                                                                 .copy()
                                                                                                                 .withStyle(TextFormatting.YELLOW))
                                                                                 .append(" <")
                                                                                 .withStyle(TextFormatting.YELLOW));
            }
            else if (conflicts) {
                this.btnChangeKeyBinding.setMessage(this.btnChangeKeyBinding.getMessage()
                                                                            .copy()
                                                                            .withStyle(keyCodeModifierConflict ?
                                                                                       TextFormatting.GOLD :
                                                                                       TextFormatting.RED));
            }
            this.btnChangeKeyBinding.render(matrices, mouseX, mouseY, partialTicks);
        }
    }
}
