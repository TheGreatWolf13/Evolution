package tgw.evolution.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.init.EvolutionTexts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ScreenEditListString extends Screen {
    private final ForgeConfigSpec.ConfigValue<List<?>> listValue;
    private final Screen parent;
    private final ForgeConfigSpec.ValueSpec valueSpec;
    private final List<StringHolder> values = new ArrayList<>();
    @Nullable
    private List<IReorderingProcessor> activeTooltip;
    private StringList list;

    public ScreenEditListString(Screen parent,
                                ITextComponent title,
                                ForgeConfigSpec.ConfigValue<List<?>> listValue,
                                ForgeConfigSpec.ValueSpec valueSpec) {
        super(title);
        this.parent = parent;
        this.listValue = listValue;
        this.valueSpec = valueSpec;
        this.values.addAll(listValue.get().stream().map(o -> new StringHolder(o.toString())).collect(Collectors.toList()));
    }

    @Override
    protected void init() {
        this.list = new StringList();
        this.children.add(this.list);
        this.addButton(new Button(this.width / 2 - 140, this.height - 29, 90, 20, EvolutionTexts.GUI_GENERAL_DONE, button -> {
            List<String> newValues = this.values.stream().map(StringHolder::getValue).collect(Collectors.toList());
            this.valueSpec.correct(newValues);
            this.listValue.set(newValues);
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 - 45,
                                  this.height - 29,
                                  90,
                                  20,
                                  EvolutionTexts.GUI_CONFIG_ADD_VALUE,
                                  button -> this.minecraft.setScreen(new ScreenEditString(ScreenEditListString.this,
                                                                                          EvolutionTexts.GUI_CONFIG_EDIT_VALUE,
                                                                                          "",
                                                                                          o -> true,
                                                                                          s -> {
                                                                                              StringHolder holder = new StringHolder(s);
                                                                                              this.values.add(holder);
                                                                                              this.list.addEntry(new StringEntry(this.list, holder));
                                                                                          }))));
        this.addButton(new Button(this.width / 2 + 50,
                                  this.height - 29,
                                  90,
                                  20,
                                  EvolutionTexts.GUI_GENERAL_CANCEL,
                                  button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 14, 0xFF_FFFF);
        super.render(matrices, mouseX, mouseY, partialTicks);
        if (this.activeTooltip != null) {
            this.renderTooltip(matrices, this.activeTooltip, mouseX, mouseY);
        }
    }

    public void setActiveTooltip(@Nullable List<IReorderingProcessor> activeTooltip) {
        this.activeTooltip = activeTooltip;
    }

    public static class StringHolder {
        private String value;

        public StringHolder(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class StringList extends ExtendedList<StringEntry> {
        public StringList() {
            super(ScreenEditListString.this.minecraft,
                  ScreenEditListString.this.width,
                  ScreenEditListString.this.height,
                  36,
                  ScreenEditListString.this.height - 36,
                  24);
            ScreenEditListString.this.values.forEach(value -> this.addEntry(new StringEntry(this, value)));
        }

        @Override
        public int addEntry(StringEntry entry) {
            return super.addEntry(entry);
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
        public boolean removeEntry(StringEntry entry) {
            return super.removeEntry(entry);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
            super.render(matrices, mouseX, mouseY, partialTicks);
            this.children().forEach(entry -> entry.children().forEach(o -> {
                if (o instanceof Button) {
                    ((Button) o).renderToolTip(matrices, mouseX, mouseY);
                }
            }));
        }
    }

    public class StringEntry extends AbstractOptionList.Entry<StringEntry> {
        private final Button deleteButton;
        private final Button editButton;
        private final StringHolder holder;
        private final StringList list;

        public StringEntry(StringList list, StringHolder holder) {
            this.list = list;
            this.holder = holder;
            this.editButton = new Button(0,
                                         0,
                                         42,
                                         20,
                                         EvolutionTexts.GUI_GENERAL_EDIT,
                                         onPress -> ScreenEditListString.this.minecraft.setScreen(new ScreenEditString(ScreenEditListString.this,
                                                                                                                       EvolutionTexts.GUI_CONFIG_EDIT_VALUE,
                                                                                                                       this.holder.getValue(),
                                                                                                                       o -> true,
                                                                                                                       this.holder::setValue)));
            this.deleteButton = new ButtonIcon(0, 0, 20, 20, 11, 165, onPress -> {
                ScreenEditListString.this.values.remove(this.holder);
                this.list.removeEntry(this);
            }) {
                @Override
                public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
                    if (this.active && this.isHovered()) {
                        ScreenEditListString.this.setActiveTooltip(ScreenEditListString.this.minecraft.font.split(EvolutionTexts.GUI_GENERAL_REMOVE,
                                                                                                                  Math.max(ScreenEditListString.this.width /
                                                                                                                           2 - 43, 170)));
                    }
                }
            };
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.editButton, this.deleteButton);
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
                           boolean selected,
                           float partialTicks) {
            ScreenEditListString.this.minecraft.font.draw(matrices,
                                                          new StringTextComponent(this.holder.getValue()).getText(),
                                                          x + 5,
                                                          y + 6,
                                                          0xFF_FFFF);
            this.editButton.visible = true;
            this.editButton.x = x + width - 65;
            this.editButton.y = y;
            this.editButton.render(matrices, mouseX, mouseY, partialTicks);
            this.deleteButton.visible = true;
            this.deleteButton.x = x + width - 21;
            this.deleteButton.y = y;
            this.deleteButton.render(matrices, mouseX, mouseY, partialTicks);

        }
    }
}
