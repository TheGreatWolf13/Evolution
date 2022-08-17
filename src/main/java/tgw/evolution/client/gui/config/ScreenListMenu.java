package tgw.evolution.client.gui.config;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import tgw.evolution.client.gui.widgets.EditBoxAdv;
import tgw.evolution.init.EvolutionTexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ScreenListMenu extends Screen {

    protected final int itemHeight;
    protected final Screen parent;
    protected List<FormattedCharSequence> activeTooltip;
    protected List<Item> entries;
    protected EntryList list;
    protected EditBox searchEditBox;

    public ScreenListMenu(Screen parent, Component title, int itemHeight) {
        super(title);
        this.parent = parent;
        this.itemHeight = itemHeight;
    }

    public static boolean isPlayingGame() {
        return Minecraft.getInstance().player != null;
    }

    protected abstract void constructEntries(List<Item> entries);

    @Override
    protected void init() {
        List<Item> entries = new ArrayList<>();
        this.constructEntries(entries);
        this.entries = ImmutableList.copyOf(entries);
        this.list = new EntryList(this.entries);
        this.addWidget(this.list);
        this.searchEditBox = new EditBoxAdv(this.font, this.width / 2 - 110, 22, 220, 20, EvolutionTexts.GUI_GENERAL_SEARCH);
        this.searchEditBox.setResponder(s -> {
            this.list.replaceEntries(s.isEmpty() ?
                                     this.entries :
                                     this.entries.stream()
                                                 .filter(item -> !(item instanceof IIgnoreSearch) &&
                                                                 item.getLabel().toLowerCase().contains(s.toLowerCase()))
                                                 .collect(Collectors.toList()));
            if (!s.isEmpty()) {
                this.list.setScrollAmount(0);
            }
        });
        this.addWidget(this.searchEditBox);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.searchEditBox.setFocus(false);
        for (GuiEventListener child : this.children()) {
            if (child instanceof EditBox editBox) {
                editBox.setFocus(false);
            }
        }
        for (Item item : this.entries) {
            item.resetFocus();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        this.searchEditBox.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 0xFF_FFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.updateTooltip(mouseX, mouseY);
        if (this.activeTooltip != null) {
            this.renderTooltip(poseStack, this.activeTooltip, mouseX, mouseY);
        }
        else {
            for (GuiEventListener widget : this.children()) {
                if (widget instanceof Button button && button.isHoveredOrFocused()) {
                    button.renderToolTip(poseStack, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    public void setActiveTooltip(List<FormattedCharSequence> tooltip) {
        this.activeTooltip = tooltip;
    }

    @Override
    public void tick() {
        this.searchEditBox.tick();
        for (Item item : this.entries) {
            item.tick();
        }
    }

    protected abstract void updateTooltip(int mouseX, int mouseY);

    protected interface IIgnoreSearch {
    }

    protected abstract static class Item extends ContainerObjectSelectionList.Entry<Item> implements ILabelProvider {

        protected final Component label;
        protected List<FormattedCharSequence> tooltip;

        public Item(Component label) {
            this.label = label;
        }

        public Item(String label) {
            this.label = new TextComponent(label);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public String getLabel() {
            return this.label.getString();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output) {
                    output.add(NarratedElementType.TITLE, Item.this.label);
                }
            });
        }

        public abstract void resetFocus();

        public abstract void tick();
    }

    protected class EntryList extends ContainerObjectSelectionList<Item> {
        public EntryList(List<Item> entries) {
            super(ScreenListMenu.this.minecraft,
                  ScreenListMenu.this.width,
                  ScreenListMenu.this.height,
                  50,
                  ScreenListMenu.this.height - 36,
                  ScreenListMenu.this.itemHeight);
            for (Item item : entries) {
                this.addEntry(item);
            }
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            super.render(poseStack, mouseX, mouseY, partialTicks);
            this.renderToolTips(poseStack, mouseX, mouseY);
        }

        private void renderToolTips(PoseStack poseStack, int mouseX, int mouseY) {
            if (this.isMouseOver(mouseX, mouseY) && mouseX < ScreenListMenu.this.list.getRowLeft() + ScreenListMenu.this.list.getRowWidth() - 67) {
                Item item = this.getEntryAtPosition(mouseX, mouseY);
                if (item != null) {
                    ScreenListMenu.this.setActiveTooltip(item.tooltip);
                }
            }
            for (Item item : this.children()) {
                for (GuiEventListener o : item.children()) {
                    if (o instanceof Button button) {
                        button.renderToolTip(poseStack, mouseX, mouseY);
                    }
                }
            }
        }

        @Override
        public void replaceEntries(Collection<Item> entries) {
            super.replaceEntries(entries);
        }
    }

    public class TitleItem extends Item implements IIgnoreSearch {
        public TitleItem(Component title) {
            super(title);
        }

        @Override
        public void render(PoseStack poseStack,
                           int x,
                           int top,
                           int left,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean selected,
                           float partialTicks) {
            Screen.drawCenteredString(poseStack, ScreenListMenu.this.minecraft.font, this.label, left + width / 2, top + 5, 0xFF_FFFF);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }
}
