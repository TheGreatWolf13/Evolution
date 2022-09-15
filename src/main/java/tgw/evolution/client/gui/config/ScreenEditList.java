package tgw.evolution.client.gui.config;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ScreenEditList extends Screen {
    private final ScreenConfig.ListValueHolder holder;
    private final ListType listType;
    private final Screen parent;
    private final Component textAddValue = new TranslatableComponent("evolution.gui.config.addValue");
    private final ForgeConfigSpec.ValueSpec valueSpec;
    private final OList<StringHolder> values = new OArrayList<>();
    private ObjectList list;

    public ScreenEditList(Screen parent, Component title, ScreenConfig.ListValueHolder holder) {
        super(title);
        this.parent = parent;
        this.holder = holder;
        this.valueSpec = holder.getSpec();
        this.listType = ListType.fromHolder(holder);
        for (Object o : holder.getValue()) {
            //noinspection ObjectAllocationInLoop
            this.values.add(new StringHolder(this.listType.getStringParser().apply(o)));
        }
    }

    @Override
    protected void init() {
        this.list = new ObjectList();
        this.list.setRenderBackground(!ScreenListMenu.isPlayingGame());
        this.addWidget(this.list);
        this.addRenderableWidget(new Button(this.width / 2 - 140, this.height - 29, 90, 20, CommonComponents.GUI_DONE, button -> {
            List<?> newValues = this.values.stream()
                                           .map(StringHolder::getValue)
                                           .map(s -> this.listType.getValueParser().apply(s))
                                           .collect(Collectors.toList());
            this.valueSpec.correct(newValues);
            this.holder.setValue(newValues);
            this.minecraft.setScreen(this.parent);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 45, this.height - 29, 90, 20, this.textAddValue, button -> this.minecraft.setScreen(
                new ScreenEditString(ScreenEditList.this, EvolutionTexts.GUI_GENERAL_EDIT, "", s -> {
                    Object value = this.listType.getValueParser().apply(s);
                    return value != null && this.valueSpec.test(Collections.singletonList(value));
                }, s -> {
                    StringHolder holder = new StringHolder(s);
                    this.values.add(holder);
                    this.list.addEntry(new StringEntry(this.list, holder));
                }))));
        this.addRenderableWidget(new Button(this.width / 2 + 50, this.height - 29, 90, 20, CommonComponents.GUI_CANCEL,
                                            button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public boolean keyPressed(@Key int keyCode, int scanCode, @Modifiers int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 14, 0xFF_FFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected enum ListType {
        BOOLEAN(Object::toString, Boolean::valueOf),
        INTEGER(Object::toString, Ints::tryParse),
        LONG(Object::toString, Longs::tryParse),
        DOUBLE(Object::toString, Doubles::tryParse),
        STRING(Object::toString, o -> o),
        UNKNOWN(Object::toString, o -> o);

        final Function<Object, String> stringParser;
        final Function<String, ?> valueParser;

        ListType(Function<Object, String> stringParser, Function<String, ?> valueParser) {
            this.stringParser = stringParser;
            this.valueParser = valueParser;
        }

        private static ListType fromElementValidator(ForgeConfigSpec.ValueSpec spec) {
            if (spec.test(Collections.singletonList("s"))) {
                return STRING;
            }
            if (spec.test(Collections.singletonList(true))) {
                return BOOLEAN;
            }
            if (spec.test(Collections.singletonList(0.0D))) {
                return DOUBLE;
            }
            if (spec.test(Collections.singletonList(0L))) {
                return LONG;
            }
            if (spec.test(Collections.singletonList(0))) {
                return INTEGER;
            }
            return UNKNOWN;
        }

        private static ListType fromHolder(ScreenConfig.ListValueHolder holder) {
            ListType type = UNKNOWN;
            List<?> defaultList = (List<?>) holder.getSpec().getDefault();
            if (!defaultList.isEmpty()) {
                type = fromObject(defaultList.get(0));
            }
            if (type == UNKNOWN) {
                return fromElementValidator(holder.getSpec());
            }
            return type;
        }

        private static ListType fromObject(Object o) {
            if (o instanceof Boolean) {
                return BOOLEAN;
            }
            if (o instanceof Integer) {
                return INTEGER;
            }
            if (o instanceof Long) {
                return LONG;
            }
            if (o instanceof Double) {
                return DOUBLE;
            }
            if (o instanceof String) {
                return STRING;
            }
            return UNKNOWN;
        }

        public Function<Object, String> getStringParser() {
            return this.stringParser;
        }

        public Function<String, ?> getValueParser() {
            return this.valueParser;
        }
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
    public class ObjectList extends ContainerObjectSelectionList<StringEntry> {
        public ObjectList() {
            super(ScreenEditList.this.minecraft, ScreenEditList.this.width, ScreenEditList.this.height, 36, ScreenEditList.this.height - 36, 24);
            for (StringHolder value : ScreenEditList.this.values) {
                //noinspection ObjectAllocationInLoop
                this.addEntry(new StringEntry(this, value));
            }
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
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            super.render(poseStack, mouseX, mouseY, partialTicks);
            for (StringEntry entry : this.children()) {
                for (GuiEventListener o : entry.children()) {
                    if (o instanceof Button button) {
                        button.renderToolTip(poseStack, mouseX, mouseY);
                    }
                }
            }
        }
    }

    public class StringEntry extends ContainerObjectSelectionList.Entry<StringEntry> {
        private final Button deleteButton;
        private final Button editButton;
        private final StringHolder holder;
        private final ObjectList list;

        public StringEntry(ObjectList list, StringHolder holder) {
            this.list = list;
            this.holder = holder;
            this.editButton = new Button(0, 0, 42, 20, EvolutionTexts.GUI_GENERAL_EDIT, onPress -> ScreenEditList.this.minecraft.setScreen(
                    new ScreenEditString(ScreenEditList.this, EvolutionTexts.GUI_GENERAL_EDIT, this.holder.getValue(), s -> {
                        Object value = ScreenEditList.this.listType.getValueParser().apply(s);
                        return value != null && ScreenEditList.this.valueSpec.test(Collections.singletonList(value));
                    }, this.holder::setValue)));
            Button.OnTooltip tooltip = (button, matrixStack, mouseX, mouseY) -> {
                if (button.active && button.isHoveredOrFocused()) {
                    ScreenEditList.this.renderTooltip(matrixStack, ScreenEditList.this.minecraft.font.split(EvolutionTexts.GUI_GENERAL_REMOVE,
                                                                                                            Math.max(ScreenEditList.this.width / 2 -
                                                                                                                     43, 170)), mouseX, mouseY);
                }
            };
            this.deleteButton = new ButtonIcon(0, 0, 12, EvolutionResources.ICON_12_12, onPress -> {
                ScreenEditList.this.values.remove(this.holder);
                this.list.removeEntry(this);
            }, tooltip);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton, this.deleteButton);
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
                    output.add(NarratedElementType.TITLE, StringEntry.this.holder.getValue());
                }
            }, StringEntry.this.editButton, StringEntry.this.deleteButton);
        }

        @Override
        public void render(PoseStack poseStack,
                           int x,
                           int top,
                           int left,
                           int width,
                           int p_230432_6_,
                           int mouseX,
                           int mouseY,
                           boolean selected,
                           float partialTicks) {
            ScreenEditList.this.minecraft.font.draw(poseStack, new TextComponent(this.holder.getValue()), left + 5, top + 6, 0xFF_FFFF);
            this.editButton.visible = true;
            this.editButton.x = left + width - 65;
            this.editButton.y = top;
            this.editButton.render(poseStack, mouseX, mouseY, partialTicks);
            this.deleteButton.visible = true;
            this.deleteButton.x = left + width - 21;
            this.deleteButton.y = top;
            this.deleteButton.render(poseStack, mouseX, mouseY, partialTicks);
        }
    }
}