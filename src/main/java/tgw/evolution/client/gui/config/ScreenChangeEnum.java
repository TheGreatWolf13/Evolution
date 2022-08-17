package tgw.evolution.client.gui.config;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import tgw.evolution.client.gui.widgets.EditBoxAdv;
import tgw.evolution.init.EvolutionTexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScreenChangeEnum extends Screen {
    private final String modId;
    private final Consumer<Enum<?>> onSave;
    private final Screen parent;
    private List<Entry> entries;
    private EnumList list;
    private EditBox searchEditBox;
    private Enum<?> selectedValue;

    protected ScreenChangeEnum(Screen parent, Component title, Enum<?> value, Consumer<Enum<?>> onSave, String modId) {
        super(title);
        this.parent = parent;
        this.onSave = onSave;
        this.selectedValue = value;
        this.modId = modId;
    }

    private void constructEntries() {
        List<Entry> entries = new ArrayList<>();
        Object value = this.selectedValue;
        if (value != null) {
            Object[] enums = ((Enum<?>) value).getDeclaringClass().getEnumConstants();
            for (Object e : enums) {
                //noinspection ObjectAllocationInLoop
                entries.add(new Entry((Enum<?>) e));
            }
        }
        entries.sort(Comparator.comparing(entry -> entry.getFormattedLabel().getString()));
        this.entries = ImmutableList.copyOf(entries);
    }

    @Override
    protected void init() {
        this.constructEntries();
        this.list = new EnumList(this.entries);
        this.list.setRenderBackground(!ScreenListMenu.isPlayingGame());
        Entry selected = null;
        for (Entry entry : this.list.children()) {
            if (entry.getEnumValue() == this.selectedValue) {
                selected = entry;
                break;
            }
        }
        this.list.setSelected(selected);
        this.addWidget(this.list);
        this.searchEditBox = new EditBoxAdv(this.font, this.width / 2 - 110, 22, 220, 20, EvolutionTexts.GUI_GENERAL_SEARCH);
        this.searchEditBox.setResponder(s -> {
            this.list.replaceEntries(s.isEmpty() ?
                                     this.entries :
                                     this.entries.stream()
                                                 .filter(entry -> entry.getFormattedLabel().getString().toLowerCase().contains(s.toLowerCase()))
                                                 .collect(Collectors.toList()));
            if (!s.isEmpty()) {
                this.list.setScrollAmount(0);
            }
        });
        this.addWidget(this.searchEditBox);
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, CommonComponents.GUI_DONE, button -> {
            if (this.list.getSelected() != null) {
                this.onSave.accept(this.list.getSelected().enumValue);
            }
            this.minecraft.setScreen(this.parent);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                            this.height - 29,
                                            150,
                                            20,
                                            CommonComponents.GUI_CANCEL,
                                            button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean onSearchBox = this.searchEditBox.mouseClicked(mouseX, mouseY, button);
        if (!onSearchBox && this.searchEditBox.isFocused() && button == 1) {
            this.searchEditBox.setValue("");
            return true;
        }
        this.searchEditBox.setFocus(false);
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        this.searchEditBox.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 0xFF_FFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        List<FormattedCharSequence> activeTooltip = null;
        if (activeTooltip != null) {
            this.renderTooltip(poseStack, activeTooltip, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {
        this.searchEditBox.tick();
    }

    public class EnumList extends AbstractSelectionList<Entry> {
        public EnumList(List<ScreenChangeEnum.Entry> entries) {
            super(ScreenChangeEnum.this.minecraft,
                  ScreenChangeEnum.this.width,
                  ScreenChangeEnum.this.height,
                  50,
                  ScreenChangeEnum.this.height - 36,
                  20);
            for (ScreenChangeEnum.Entry entry : entries) {
                this.addEntry(entry);
            }
        }

        @Override
        public void replaceEntries(Collection<ScreenChangeEnum.Entry> entries) {
            super.replaceEntries(entries);
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
            if (this.getSelected() != null) {
                output.add(NarratedElementType.TITLE, this.getSelected().label);
            }
        }
    }

    public class Entry extends AbstractSelectionList.Entry<Entry> implements ILabelProvider {
        private final Enum<?> enumValue;
        private final Component label;

        public Entry(Enum<?> enumValue) {
            this.enumValue = enumValue;
            this.label = new TranslatableComponent(ScreenConfig.createEnumKey(ScreenChangeEnum.this.modId, enumValue));
        }

        public Enum<?> getEnumValue() {
            return this.enumValue;
        }

        public Component getFormattedLabel() {
            return this.label;
        }

        @Override
        public String getLabel() {
            return this.label.getString();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ScreenChangeEnum.this.list.setSelected(this);
            ScreenChangeEnum.this.selectedValue = this.enumValue;
            return true;
        }

        @Override
        public void render(PoseStack poseStack,
                           int index,
                           int top,
                           int left,
                           int width,
                           int p_230432_6_,
                           int mouseX,
                           int mouseY,
                           boolean hovered,
                           float partialTicks) {
            Component label = new TextComponent(this.label.getString()).withStyle(ScreenChangeEnum.this.list.getSelected() == this ?
                                                                                  ChatFormatting.YELLOW :
                                                                                  ChatFormatting.WHITE);
            Screen.drawString(poseStack, ScreenChangeEnum.this.minecraft.font, label, left + 5, top + 4, 0xFF_FFFF);
        }
    }
}
