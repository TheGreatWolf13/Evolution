package tgw.evolution.client.gui.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ListKeyBinds extends KeyBindsList {
    private final KeyBindsScreen keyBindsScreen;
    private final Component textReset = new TranslatableComponent("evolution.gui.controls.reset");
    public List<Entry> allEntries;
    private int maxNameWidth;

    public ListKeyBinds(KeyBindsScreen screen, Minecraft mc) {
        super(screen, mc);
        this.width = screen.width + 45;
        this.height = screen.height;
        this.y0 = 43;
        this.y1 = screen.height - 85;
        this.x1 = screen.width + 45;
        this.keyBindsScreen = screen;
        this.children().clear();
        this.allEntries = new ArrayList<>();
        KeyMapping[] keyMappings = ArrayUtils.clone(mc.options.keyMappings);
        Arrays.sort(keyMappings);
        String s = null;
        for (KeyMapping keyMapping : keyMappings) {
            String s1 = keyMapping.getCategory();
            if (!s1.equals(s)) {
                s = s1;
                //noinspection ObjectAllocationInLoop
                this.add(new ListKeyBinds.CategoryEntry(new TranslatableComponent(s1)));
            }
            //noinspection ObjectAllocationInLoop
            Component component = new TranslatableComponent(keyMapping.getName());
            int i = mc.font.width(component);
            if (i > this.maxNameWidth) {
                this.maxNameWidth = i;
            }
            //noinspection ObjectAllocationInLoop
            this.add(new ListKeyBinds.KeyEntry(keyMapping, component));
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
    public class CategoryEntry extends KeyBindsList.Entry {

        private final Component name;
        private final int width;

        public CategoryEntry(Component name) {
            this.name = name;
            this.width = ListKeyBinds.this.minecraft.font.width(this.name);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput p_193906_) {
                    p_193906_.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }

        @Override
        public void render(PoseStack matrices,
                           int index,
                           int rowY,
                           int rowX,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean isMouseOver,
                           float partialTicks) {
            ListKeyBinds.this.minecraft.font.draw(matrices,
                                                  this.name,
                                                  (ListKeyBinds.this.minecraft.screen.width - this.width) / 2.0f,
                                                  rowY + height - 9 - 1,
                                                  0xff_ffff);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class KeyEntry extends KeyBindsList.Entry {

        private final Button changeButton;
        private final KeyMapping key;
        private final Component name;
        private final Button resetButton;

        private KeyEntry(final KeyMapping key, final Component name) {
            this.key = key;
            this.name = name;
            this.changeButton = new Button(0, 0, 95, 20, this.name, button -> ListKeyBinds.this.keyBindsScreen.selectedKey = key) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return key.isUnbound() ?
                           new TranslatableComponent("narrator.controls.unbound", name) :
                           new TranslatableComponent("narrator.controls.bound", name, super.createNarrationMessage());
                }
            };
            this.resetButton = new Button(0, 0, 50, 20, ListKeyBinds.this.textReset, button -> {
                this.key.setToDefault();
                ListKeyBinds.this.minecraft.options.setKey(key, key.getDefaultKey());
                KeyMapping.resetMapping();
            }) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return new TranslatableComponent("narrator.controls.reset", name);
                }
            };
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        public Button getChangeButton() {
            return this.changeButton;
        }

        public KeyMapping getKey() {
            return this.key;
        }

        public Component getName() {
            return this.name;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int modifiers) {
            if (this.changeButton.mouseClicked(mouseX, mouseY, modifiers)) {
                return true;
            }
            return this.resetButton.mouseClicked(mouseX, mouseY, modifiers);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int modifiers) {
            return this.changeButton.mouseReleased(mouseX, mouseY, modifiers);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public void render(PoseStack matrices,
                           int index,
                           int y,
                           int x,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean isMouseOver,
                           float partialTicks) {
            boolean flag = ListKeyBinds.this.keyBindsScreen.selectedKey == this.key;
            float f = Math.max(20, x + 140 - ListKeyBinds.this.maxNameWidth);
            ListKeyBinds.this.minecraft.font.draw(matrices, this.name, f, y + height / 2.0f - 9 / 2.0f, 0xff_ffff);
            this.resetButton.x = x + ListKeyBinds.this.getRowWidth() - this.resetButton.getWidth() - 2;
            this.resetButton.y = y;
            this.resetButton.active = !this.key.isDefault();
            this.resetButton.render(matrices, mouseX, mouseY, partialTicks);
            this.changeButton.x = x + ListKeyBinds.this.getRowWidth() - this.resetButton.getWidth() - this.changeButton.getWidth() - 4;
            this.changeButton.y = y;
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            boolean conflicts = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!this.key.isUnbound()) {
                for (KeyMapping keybinding : ListKeyBinds.this.minecraft.options.keyMappings) {
                    if (keybinding != this.key && this.key.same(keybinding)) {
                        conflicts = true;
                        keyCodeModifierConflict &= keybinding.hasKeyModifierConflict(this.key);
                    }
                }
            }
            if (flag) {
                this.changeButton.setMessage(new TextComponent("> ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW))
                                                                    .append(" <")
                                                                    .withStyle(ChatFormatting.YELLOW));
            }
            else if (conflicts) {
                this.changeButton.setMessage(this.changeButton.getMessage()
                                                              .copy()
                                                              .withStyle(keyCodeModifierConflict ? ChatFormatting.GOLD : ChatFormatting.RED));
            }
            this.changeButton.render(matrices, mouseX, mouseY, partialTicks);
        }
    }
}
