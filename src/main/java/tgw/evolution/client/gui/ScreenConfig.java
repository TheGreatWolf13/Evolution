package tgw.evolution.client.gui;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.reflection.FieldHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ScreenConfig extends Screen {

    public static final Comparator<Entry> COMPARATOR = (o1, o2) -> {
        if (o1 instanceof SubMenu && o2 instanceof SubMenu) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
        if (!(o1 instanceof SubMenu) && o2 instanceof SubMenu) {
            return 1;
        }
        if (o1 instanceof SubMenu) {
            return -1;
        }
        return o1.getLabel().compareTo(o2.getLabel());
    };
    @SuppressWarnings("rawtypes")
    private static final FieldHandler<ForgeConfigSpec.EnumValue, Class<? extends Enum<?>>> CLAZZ = new FieldHandler<>(ForgeConfigSpec.EnumValue.class,
                                                                                                                      "clazz");
    private static final Pattern PATTERN_CAMEL_CASE = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    private static final Pattern DOUBLE_SPACE = Pattern.compile("\\s++");
    private final List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> allConfigValues;
    private final ResourceLocation background;
    private final ForgeConfigSpec clientSpec;
    private final UnmodifiableConfig clientValues;
    private final ForgeConfigSpec commonSpec;
    private final UnmodifiableConfig commonValues;
    private final String displayName;
    private final String modId;
    private final Screen parent;
    private ConfigTextFieldWidget activeTextField;
    @Nullable
    private List<String> activeTooltip;
    private List<Entry> entries;
    private ConfigList list;
    private Button restoreDefaultsButton;
    private ConfigTextFieldWidget searchTextField;
    private boolean subMenu;

    public ScreenConfig(Screen parent,
                        String modId,
                        String displayName,
                        ForgeConfigSpec spec,
                        UnmodifiableConfig values,
                        ResourceLocation background) {
        super(new StringTextComponent(displayName));
        this.parent = parent;
        this.displayName = displayName;
        this.modId = modId;
        this.clientSpec = spec;
        this.clientValues = values;
        this.commonSpec = null;
        this.commonValues = null;
        this.subMenu = true;
        this.allConfigValues = null;
        this.background = background;
    }

    public ScreenConfig(Screen parent,
                        String modId,
                        String displayName,
                        @Nullable ForgeConfigSpec clientSpec,
                        @Nullable ForgeConfigSpec commonSpec,
                        ResourceLocation background) {
        super(new StringTextComponent(displayName));
        this.parent = parent;
        this.modId = modId;
        this.displayName = displayName;
        this.clientSpec = clientSpec;
        this.clientValues = clientSpec != null ? clientSpec.getValues() : null;
        this.commonSpec = commonSpec;
        this.commonValues = commonSpec != null ? commonSpec.getValues() : null;
        this.allConfigValues = this.gatherAllConfigValues();
        this.background = background;
    }

    private static String createCommentFromConfig(ForgeConfigSpec.ValueSpec valueSpec) {
        if (valueSpec.getTranslationKey() != null) {
            return new TranslationTextComponent(valueSpec.getTranslationKey() + ".comment").getString();
        }
        return valueSpec.getComment() != null ? valueSpec.getComment() : "";
    }

    /**
     * Tries to create a readable label from the given input. This input should be
     * the raw config value name. For example "shouldShowParticles" will be converted
     * to "Should Show Particles".
     *
     * @param input the config value name
     * @return a readable label string
     */
    private static String createLabel(String input) {
        String valueName = input;
        // Try split by camel case
        String[] words = PATTERN_CAMEL_CASE.split(valueName);
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        valueName = Strings.join(words, " ");
        // Try split by underscores
        words = valueName.split("_");
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        // Finally join words. Some mods have inputs like "Foo_Bar" and this causes a double space.
        // To fix this any whitespace is replaced with a single space
        return DOUBLE_SPACE.matcher(Strings.join(words, " ")).replaceAll(" ");
    }

    /**
     * Tries to create a readable label from the given config value and spec. This will
     * first attempt to create a label from the translation key in the spec, otherwise it
     * will create a readable label from the raw config value name.
     *
     * @param configValue the config value
     * @param valueSpec   the associated value spec
     * @return a readable label string
     */
    private static String createLabelFromConfig(ForgeConfigSpec.ConfigValue<?> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
        if (valueSpec.getTranslationKey() != null) {
            return new TranslationTextComponent(valueSpec.getTranslationKey()).getString();
        }
        return createLabel(lastValue(configValue.getPath(), ""));
    }

    /**
     * Gets the last element in a list
     *
     * @param list         the list of get the value from
     * @param defaultValue if the list is empty, return this value instead
     * @param <V>          the type of list
     * @return the last element
     */
    private static <V> V lastValue(List<V> list, V defaultValue) {
        if (!list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        return defaultValue;
    }

    /**
     * Gathers the entries for each config spec to be later added to the option list
     */
    private void constructEntries() {
        List<Entry> entries = new ArrayList<>();
        if (this.clientValues != null && this.clientSpec != null) {
            if (!this.subMenu) {
                entries.add(new TitleEntry(I18n.format("evolution.config.client_config")));
            }
            this.createEntriesFromConfig(this.clientValues, this.clientSpec, entries);
        }
        if (this.commonValues != null && this.commonSpec != null) {
            entries.add(new TitleEntry(I18n.format("evolution.config.common_config")));
            this.createEntriesFromConfig(this.commonValues, this.commonSpec, entries);
        }
        this.entries = ImmutableList.copyOf(entries);
    }

    /**
     * Scans the given unmodifiable config and creates an entry for each scanned
     * config value based on it's type.
     *
     * @param values  the values to scan
     * @param spec    the spec of config
     * @param entries the list to add the entries to
     */
    private void createEntriesFromConfig(UnmodifiableConfig values, ForgeConfigSpec spec, List<Entry> entries) {
        List<Entry> subEntries = new ArrayList<>();
        values.valueMap().forEach((s, o) -> {
            if (o instanceof AbstractConfig) {
                subEntries.add(new SubMenu(s, spec, (AbstractConfig) o));
            }
            else if (o instanceof ForgeConfigSpec.ConfigValue<?>) {
                ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) o;
                ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
                Object value = configValue.get();
                if (value instanceof Boolean) {
                    subEntries.add(new BooleanEntry((ForgeConfigSpec.ConfigValue<Boolean>) configValue, valueSpec));
                }
                else if (value instanceof Integer) {
                    subEntries.add(new IntegerEntry((ForgeConfigSpec.ConfigValue<Integer>) configValue, valueSpec));
                }
                else if (value instanceof Double) {
                    subEntries.add(new DoubleEntry((ForgeConfigSpec.ConfigValue<Double>) configValue, valueSpec));
                }
                else if (value instanceof Long) {
                    subEntries.add(new LongEntry((ForgeConfigSpec.ConfigValue<Long>) configValue, valueSpec));
                }
                else if (value instanceof Enum) {
                    subEntries.add(new EnumEntry((ForgeConfigSpec.ConfigValue<Enum<?>>) configValue, valueSpec));
                }
                else if (value instanceof String) {
                    subEntries.add(new StringEntry((ForgeConfigSpec.ConfigValue<String>) configValue, valueSpec));
                }
                else if (value instanceof List<?>) {
                    subEntries.add(new ListStringEntry((ForgeConfigSpec.ConfigValue<List<?>>) configValue, valueSpec));
                }
                else {
                    Evolution.LOGGER.info("Unsupported config value: " + configValue.getPath());
                }
            }
        });
        subEntries.sort(COMPARATOR);
        entries.addAll(subEntries);
    }

    private String createTranslatableLabel(String label) {
        return I18n.format(this.modId + ".config.label." + label.toLowerCase());
    }

    /**
     * Gathers all the config values with a deep search. Used for resetting defaults
     */
    private List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> gatherAllConfigValues() {
        List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> values = new ArrayList<>();
        if (this.clientValues != null) {
            this.gatherValuesFromConfig(this.clientValues, this.clientSpec, values);
        }
        if (this.commonValues != null) {
            this.gatherValuesFromConfig(this.commonValues, this.commonSpec, values);
        }
        return ImmutableList.copyOf(values);
    }

    /**
     * Gathers all the config values from the given config and adds it's to the provided list. This
     * will search deeper if it finds another config and recursively call itself.
     */
    private void gatherValuesFromConfig(UnmodifiableConfig config,
                                        ForgeConfigSpec spec,
                                        List<Pair<ForgeConfigSpec.ConfigValue<?>, ForgeConfigSpec.ValueSpec>> values) {
        config.valueMap().forEach((s, o) -> {
            if (o instanceof AbstractConfig) {
                this.gatherValuesFromConfig((UnmodifiableConfig) o, spec, values);
            }
            else if (o instanceof ForgeConfigSpec.ConfigValue<?>) {
                ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) o;
                ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
                values.add(Pair.of(configValue, valueSpec));
            }
        });
    }

    @Override
    protected void init() {
        this.constructEntries();
        this.list = new ConfigList(this.entries);
        this.children.add(this.list);
        this.searchTextField = new ConfigTextFieldWidget(this.font, this.width / 2 - 110, 22, 220, 20, EvolutionTexts.GUI_SEARCH);
        this.searchTextField.setResponder(s -> {
            if (!s.isEmpty()) {
                this.list.replaceEntries(this.entries.stream()
                                                     .filter(entry -> (entry instanceof SubMenu || entry instanceof ConfigEntry<?, ?>) &&
                                                                      entry.getLabel()
                                                                           .toLowerCase(Locale.ENGLISH)
                                                                           .contains(s.toLowerCase(Locale.ENGLISH)))
                                                     .collect(Collectors.toList()));
            }
            else {
                this.list.replaceEntries(this.entries);
            }
        });
        this.children.add(this.searchTextField);
        if (this.subMenu) {
            this.addButton(new Button(this.width / 2 - 75,
                                      this.height - 29,
                                      150,
                                      20,
                                      I18n.format("gui.back"),
                                      button -> this.minecraft.displayGuiScreen(this.parent)));
        }
        else {
            this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done"), button -> {
                if (this.clientSpec != null) {
                    this.clientSpec.save();
                }
                if (this.commonSpec != null) {
                    this.commonSpec.save();
                }
                this.minecraft.displayGuiScreen(this.parent);
            }));
            this.restoreDefaultsButton = this.addButton(new Button(this.width / 2 - 155,
                                                                   this.height - 29,
                                                                   150,
                                                                   20,
                                                                   I18n.format("evolution.gui.restore_defaults"),
                                                                   button -> {
                                                                       if (this.allConfigValues == null) {
                                                                           return;
                                                                       }
                                                                       // Resets all config values
                                                                       this.allConfigValues.forEach(pair -> {
                                                                           //noinspection rawtypes
                                                                           ForgeConfigSpec.ConfigValue configValue = pair.getLeft();
                                                                           ForgeConfigSpec.ValueSpec valueSpec = pair.getRight();
                                                                           configValue.set(valueSpec.getDefault());
                                                                       });
                                                                       // Updates the current entries to process UI changes
                                                                       this.entries.stream()
                                                                                   .filter(entry -> entry instanceof ConfigEntry)
                                                                                   .forEach(entry -> ((ConfigEntry<?, ?>) entry).onResetValue());
                                                                   }));
            // Call during init to avoid the button flashing active
            this.updateRestoreDefaultButton();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.renderBackground();
        this.list.render(mouseX, mouseY, partialTicks);
        this.searchTextField.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 7, 0xFF_FFFF);
        super.render(mouseX, mouseY, partialTicks);
        if (this.activeTooltip != null) {
            GUIUtils.renderTooltip(this, this.activeTooltip, mouseX, mouseY, 200);
        }
    }

    @Override
    public void renderDirtBackground(int vOffset) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(this.background);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        float size = 32.0F;
        bufferbuilder.pos(0.0D, this.height, 0.0D).tex(0.0F, this.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(this.width, this.height, 0.0D).tex(this.width / size, this.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(this.width, 0.0D, 0.0D).tex(this.width / size, vOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0F, vOffset).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
    }

    /**
     * Sets the tool tip to render. Must be actively called in the render method as
     * the tooltip is reset every draw call.
     *
     * @param activeTooltip a tooltip list to show
     */
    public void setActiveTooltip(@Nullable List<String> activeTooltip) {
        this.activeTooltip = activeTooltip;
    }

    @Override
    public void tick() {
        this.updateRestoreDefaultButton();
    }

    /**
     * Updates the active state of the restore default button. It will only be active if values are
     * different from their default.
     */
    private void updateRestoreDefaultButton() {
        if (this.allConfigValues != null && this.restoreDefaultsButton != null) {
            this.restoreDefaultsButton.active = this.allConfigValues.stream()
                                                                    .anyMatch(pair -> !pair.getLeft().get().equals(pair.getRight().getDefault()));
        }
    }

    abstract static class Entry extends AbstractOptionList.Entry<Entry> {
        protected String label;
        protected List<String> tooltip;

        public Entry(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public class TitleEntry extends Entry {
        public TitleEntry(String title) {
            super(title);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            ITextComponent title = new StringTextComponent(this.label).applyTextStyle(TextFormatting.BOLD).applyTextStyle(TextFormatting.YELLOW);
            ScreenConfig.this.drawCenteredString(ScreenConfig.this.minecraft.fontRenderer, title.getFormattedText(), x + width / 2, y + 5, 0xff_ffff);
        }
    }

    public class SubMenu extends Entry {
        private final Button button;

        public SubMenu(String label, ForgeConfigSpec spec, AbstractConfig values) {
            super(ScreenConfig.this.createTranslatableLabel(label));
            this.button = new Button(10,
                                     5,
                                     44,
                                     20,
                                     new StringTextComponent(this.getLabel()).applyTextStyle(TextFormatting.BOLD)
                                                                             .applyTextStyle(TextFormatting.WHITE)
                                                                             .getFormattedText(),
                                     onPress -> {
                                         String newTitle = ScreenConfig.this.displayName + " > " + this.getLabel();
                                         ScreenConfig.this.minecraft.displayGuiScreen(new ScreenConfig(ScreenConfig.this,
                                                                                                       ScreenConfig.this.modId,
                                                                                                       newTitle,
                                                                                                       spec,
                                                                                                       values,
                                                                                                       ScreenConfig.this.background));
                                     }) {
                private boolean wasHovered;

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ScreenConfig.this.height - 36 &&
                                         mouseY >= 50;
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
            return ImmutableList.of(this.button);
        }

        @Override
        public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
            this.button.x = left - 1;
            this.button.y = top;
            this.button.setWidth(width);
            this.button.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract class ConfigEntry<N, T extends ForgeConfigSpec.ConfigValue<N>> extends Entry {
        protected final List<IGuiEventListener> eventListeners = Lists.newArrayList();
        protected T configValue;
        protected Button resetButton;
        protected ForgeConfigSpec.ValueSpec valueSpec;

        public ConfigEntry(T configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(createLabelFromConfig(configValue, valueSpec));
            this.configValue = configValue;
            this.valueSpec = valueSpec;
            this.tooltip = this.createToolTip(configValue, valueSpec);
            this.resetButton = new ButtonIcon(0, 0, 20, 20, 0, 165, onPress -> {
                configValue.set((N) valueSpec.getDefault());
                this.onResetValue();
            }) {
                private boolean wasHovered;

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ScreenConfig.this.height - 36 &&
                                         mouseY >= 50;
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

                @Override
                public void renderToolTip(int mouseX, int mouseY) {
                    if (this.active && this.isHovered()) {
                        ScreenConfig.this.setActiveTooltip(ScreenConfig.this.minecraft.fontRenderer.listFormattedStringToWidth(new TranslationTextComponent(
                                "evolution.gui.reset").getFormattedText(), Math.max(ScreenConfig.this.width / 2 - 43, 170)));
                    }
                }
            };
            this.eventListeners.add(this.resetButton);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return this.eventListeners;
        }

        private List<String> createToolTip(ForgeConfigSpec.ConfigValue<?> value, ForgeConfigSpec.ValueSpec spec) {
            FontRenderer font = ScreenConfig.this.minecraft.fontRenderer;
            List<String> lines = new ArrayList<>(font.listFormattedStringToWidth(new StringTextComponent(createCommentFromConfig(spec)).getText(),
                                                                                 Integer.MAX_VALUE));
            String name = lastValue(value.getPath(), "");
            lines.add(0, new StringTextComponent(name).applyTextStyle(TextFormatting.DARK_GRAY).getFormattedText());
            lines.add(0, new StringTextComponent(this.label).applyTextStyle(TextFormatting.YELLOW).getFormattedText());
            int rangeIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String text = lines.get(i);
                if (text.startsWith("Range: ") || text.startsWith("Allowed Values: ")) {
                    rangeIndex = i;
                    break;
                }
            }
            if (rangeIndex != -1) {
                for (int i = rangeIndex; i < lines.size(); i++) {
                    //noinspection ObjectAllocationInLoop
                    lines.set(i, new StringTextComponent(lines.get(i)).applyTextStyle(TextFormatting.GRAY).getFormattedText());
                }
            }
            else {
                if (value instanceof ForgeConfigSpec.DoubleValue ||
                    value instanceof ForgeConfigSpec.IntValue ||
                    value instanceof ForgeConfigSpec.LongValue) {
                    Object range = spec.getRange();
                    lines.add(EvolutionTexts.configRange(range.toString()).getFormattedText());
                }
                else if (value instanceof ForgeConfigSpec.EnumValue<?>) {
                    ForgeConfigSpec.EnumValue<?> enumValue = (ForgeConfigSpec.EnumValue<?>) value;
                    Class<? extends Enum<?>> clazz = CLAZZ.get(enumValue);
                    Enum<?>[] values = clazz.getEnumConstants();
                    lines.add(EvolutionTexts.configAllowedValues(Arrays.stream(values)
                                                                       .map(o -> I18n.format(ScreenConfig.this.modId +
                                                                                             ".config.enum_" +
                                                                                             o.name().toLowerCase()))
                                                                       .collect(Collectors.joining(", "))).getFormattedText());
                }
                if (value instanceof ForgeConfigSpec.IntValue ||
                    value instanceof ForgeConfigSpec.DoubleValue ||
                    value instanceof ForgeConfigSpec.LongValue ||
                    value instanceof ForgeConfigSpec.BooleanValue ||
                    value instanceof ForgeConfigSpec.EnumValue ||
                    value.get() instanceof String) {
                    Object def = spec.getDefault();
                    String str = def.toString();
                    if (def instanceof Boolean) {
                        if ((Boolean) def) {
                            str = I18n.format("options.on");
                        }
                        else {
                            str = I18n.format("options.off");
                        }
                    }
                    else if (def instanceof Enum) {
                        str = I18n.format(ScreenConfig.this.modId + ".config.enum_" + ((Enum<?>) def).name().toLowerCase());
                    }
                    lines.add(EvolutionTexts.configDefault(str).getFormattedText());
                }
            }
            return lines;
        }

        public void onResetValue() {
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.resetButton.active = !this.configValue.get().equals(this.valueSpec.getDefault());
            ITextComponent title = new StringTextComponent(this.label);
            if (ScreenConfig.this.minecraft.fontRenderer.getStringWidth(title.getUnformattedComponentText()) > width - 90) {
                int tripleDotSize = ScreenConfig.this.minecraft.fontRenderer.getStringWidth("...");
                String trimmed = ScreenConfig.this.minecraft.fontRenderer.trimStringToWidth(title.getFormattedText(), width - 90 - tripleDotSize)
                                                                         .trim() + "...";
                ScreenConfig.this.minecraft.fontRenderer.drawStringWithShadow(trimmed, x, y + 6, 0xFF_FFFF);
            }
            else {
                ScreenConfig.this.minecraft.fontRenderer.drawStringWithShadow(title.getFormattedText(), x, y + 6, 0xFF_FFFF);
            }
            if (this.isMouseOver(mouseX, mouseY) && mouseX < ScreenConfig.this.list.getRowLeft() + ScreenConfig.this.list.getRowWidth() - 87) {
                ScreenConfig.this.setActiveTooltip(this.tooltip);
            }
            this.resetButton.x = x + width - 21;
            this.resetButton.y = y;
            this.resetButton.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class ConfigList extends AbstractOptionList<ScreenConfig.Entry> {
        public ConfigList(List<ScreenConfig.Entry> entries) {
            super(ScreenConfig.this.minecraft, ScreenConfig.this.width, ScreenConfig.this.height, 50, ScreenConfig.this.height - 36, 24);
            entries.forEach(this::addEntry);
        }

        @Override
        public int getRowLeft() {
            return super.getRowLeft();
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
        }

        /**
         * Literally just a copy of the original since the background can't be changed
         *
         * @param mouseX       the current mouse x position
         * @param mouseY       the current mouse y position
         * @param partialTicks the partial ticks
         */
        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            this.renderBackground();
            int scrollBarStart = this.getScrollbarPosition();
            this.minecraft.getTextureManager().bindTexture(ScreenConfig.this.background);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.x0, this.y1, 0).tex(this.x0 / 32.0F, (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            buffer.pos(this.x1, this.y1, 0).tex(this.x1 / 32.0F, (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            buffer.pos(this.x1, this.y0, 0).tex(this.x1 / 32.0F, (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            buffer.pos(this.x0, this.y0, 0).tex(this.x0 / 32.0F, (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tessellator.draw();
            int rowLeft = this.getRowLeft();
            int scrollOffset = this.y0 + 4 - (int) this.getScrollAmount();
            this.renderList(rowLeft, scrollOffset, mouseX, mouseY, partialTicks);
            this.minecraft.getTextureManager().bindTexture(ScreenConfig.this.background);
            GlStateManager.enableDepthTest();
            GlStateManager.depthFunc(GL11.GL_ALWAYS);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.x0, this.y0, -100).tex(0.0F, this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0 + this.width, this.y0, -100).tex(this.width / 32.0F, this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0 + this.width, 0.0D, -100).tex(this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0, 0.0D, -100).tex(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0, this.height, -100).tex(0.0F, this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0 + this.width, this.height, -100).tex(this.width / 32.0F, this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0 + this.width, this.y1, -100).tex(this.width / 32.0F, this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            buffer.pos(this.x0, this.y1, -100).tex(0.0F, this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tessellator.draw();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.disableDepthTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                             GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                             GlStateManager.SourceFactor.ZERO,
                                             GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlphaTest();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableTexture();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(this.x0, this.y0 + 4, 0).tex(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            buffer.pos(this.x1, this.y0 + 4, 0).tex(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            buffer.pos(this.x1, this.y0, 0).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            buffer.pos(this.x0, this.y0, 0).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            buffer.pos(this.x0, this.y1, 0).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            buffer.pos(this.x1, this.y1, 0).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            buffer.pos(this.x1, this.y1 - 4, 0).tex(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            buffer.pos(this.x0, this.y1 - 4, 0).tex(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int maxScroll = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
            if (maxScroll > 0) {
                int scrollBarStartY = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / this.getMaxPosition());
                scrollBarStartY = MathHelper.clamp(scrollBarStartY, 32, this.y1 - this.y0 - 8);
                int scrollBarEndY = (int) this.getScrollAmount() * (this.y1 - this.y0 - scrollBarStartY) / maxScroll + this.y0;
                if (scrollBarEndY < this.y0) {
                    scrollBarEndY = this.y0;
                }
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(scrollBarStart, this.y1, 0).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                int scrollBarEnd = scrollBarStart + 6;
                buffer.pos(scrollBarEnd, this.y1, 0).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                buffer.pos(scrollBarEnd, this.y0, 0).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                buffer.pos(scrollBarStart, this.y0, 0).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                buffer.pos(scrollBarStart, scrollBarEndY + scrollBarStartY, 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                buffer.pos(scrollBarEnd, scrollBarEndY + scrollBarStartY, 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                buffer.pos(scrollBarEnd, scrollBarEndY, 0).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                buffer.pos(scrollBarStart, scrollBarEndY, 0).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                buffer.pos(scrollBarStart, scrollBarEndY + scrollBarStartY - 1, 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                buffer.pos(scrollBarEnd - 1, scrollBarEndY + scrollBarStartY - 1, 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                buffer.pos(scrollBarEnd - 1, scrollBarEndY, 0).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                buffer.pos(scrollBarStart, scrollBarEndY, 0).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }
            this.renderDecorations(mouseX, mouseY);
            GlStateManager.enableTexture();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
            this.renderToolTips(mouseX, mouseY);
        }

        private void renderToolTips(int mouseX, int mouseY) {
            if (this.isMouseOver(mouseX, mouseY) && mouseX < ScreenConfig.this.list.getRowLeft() + ScreenConfig.this.list.getRowWidth() - 87) {
                ScreenConfig.Entry entry = this.getEntryAtPosition(mouseX, mouseY);
                if (entry != null) {
                    ScreenConfig.this.setActiveTooltip(entry.tooltip);
                }
            }
            this.children().forEach(entry -> entry.children().forEach(o -> {
                if (o instanceof Button) {
                    ((Button) o).renderToolTip(mouseX, mouseY);
                }
            }));
        }

        @Override
        public void replaceEntries(Collection<ScreenConfig.Entry> entries) {
            super.replaceEntries(entries);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract class NumberEntry<N extends Number, T extends ForgeConfigSpec.ConfigValue<N>> extends ConfigEntry<N, T> {
        private final ConfigTextFieldWidget textField;

        public NumberEntry(T configValue, ForgeConfigSpec.ValueSpec valueSpec, Function<String, Number> parser) {
            super(configValue, valueSpec);
            this.textField = new ConfigTextFieldWidget(ScreenConfig.this.font, 0, 0, 60, 18, EvolutionTexts.EMPTY);
            this.textField.setText(configValue.get().toString());
            this.textField.setResponder(s -> {
                try {
                    Number n = parser.apply(s);
                    if (valueSpec.test(n)) {
                        this.textField.setTextColor(0xe0_e0e0);
                        configValue.set((N) n);
                    }
                    else {
                        this.textField.setTextColor(0xff_0000);
                    }
                }
                catch (Exception ignored) {
                    this.textField.setTextColor(0xff_0000);
                }
            });
            this.eventListeners.add(this.textField);
        }

        @Override
        public void onResetValue() {
            this.textField.setText(this.configValue.get().toString());
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
            this.textField.x = x + width - 85;
            this.textField.y = y + 1;
            this.textField.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class IntegerEntry extends NumberEntry<Integer, ForgeConfigSpec.ConfigValue<Integer>> {
        public IntegerEntry(ForgeConfigSpec.ConfigValue<Integer> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec, Integer::parseInt);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class DoubleEntry extends NumberEntry<Double, ForgeConfigSpec.ConfigValue<Double>> {
        public DoubleEntry(ForgeConfigSpec.ConfigValue<Double> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec, Double::parseDouble);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class LongEntry extends NumberEntry<Long, ForgeConfigSpec.ConfigValue<Long>> {
        public LongEntry(ForgeConfigSpec.ConfigValue<Long> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec, Long::parseLong);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class BooleanEntry extends ConfigEntry<Boolean, ForgeConfigSpec.ConfigValue<Boolean>> {
        private final Button button;

        public BooleanEntry(ForgeConfigSpec.ConfigValue<Boolean> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec);
            this.button = new Button(10, 5, 64, 20, this.getLabel(), button -> {
                boolean flag = !configValue.get();
                configValue.set(flag);
                button.setMessage(this.getLabel());
            }) {
                private boolean wasHovered;

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ScreenConfig.this.height - 36 &&
                                         mouseY >= 50;
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
            this.eventListeners.add(this.button);
        }

        @Override
        public String getLabel() {
            return I18n.format(this.configValue.get() ? "options.on" : "options.off");
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(this.getLabel());
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
            this.button.x = x + width - 87;
            this.button.y = y;
            this.button.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class StringEntry extends ConfigEntry<String, ForgeConfigSpec.ConfigValue<String>> {
        private final Button button;

        public StringEntry(ForgeConfigSpec.ConfigValue<String> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec);
            String title = createLabelFromConfig(configValue, valueSpec);
            this.button = new Button(10,
                                     5,
                                     64,
                                     20,
                                     new TranslationTextComponent("evolution.gui.edit").getFormattedText(),
                                     button -> ScreenConfig.this.minecraft.displayGuiScreen(new ScreenEditString(ScreenConfig.this,
                                                                                                                 new StringTextComponent(title),
                                                                                                                 configValue.get(),
                                                                                                                 valueSpec::test,
                                                                                                                 configValue::set)));
            this.eventListeners.add(this.button);
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
            this.button.x = x + width - 87;
            this.button.y = y;
            this.button.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class ListStringEntry extends ConfigEntry<List<?>, ForgeConfigSpec.ConfigValue<List<?>>> {
        private final Button button;

        public ListStringEntry(ForgeConfigSpec.ConfigValue<List<?>> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec);
            String title = createLabelFromConfig(configValue, valueSpec);
            this.button = new Button(10,
                                     5,
                                     64,
                                     20,
                                     new TranslationTextComponent("evolution.gui.edit").getFormattedText(),
                                     button -> ScreenConfig.this.minecraft.displayGuiScreen(new ScreenEditListString(ScreenConfig.this,
                                                                                                                     new StringTextComponent(title),
                                                                                                                     configValue,
                                                                                                                     valueSpec)));
            this.eventListeners.add(this.button);
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
            this.button.x = x + width - 87;
            this.button.y = y;
            this.button.render(mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class EnumEntry extends ConfigEntry<Enum<?>, ForgeConfigSpec.ConfigValue<Enum<?>>> {
        private final Button button;

        public EnumEntry(ForgeConfigSpec.ConfigValue<Enum<?>> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec);
            this.button = new Button(10, 5, 64, 20, new StringTextComponent(configValue.get().name()).getText(), button -> {
                Object o = configValue.get();
                if (o != null) {
                    Enum<?> e = (Enum<?>) o;
                    Object[] values = e.getDeclaringClass().getEnumConstants();
                    e = (Enum<?>) values[(e.ordinal() + 1) % values.length];
                    configValue.set(e);
                    button.setMessage(new StringTextComponent(e.name()).getText());
                }
            }) {
                private boolean wasHovered;

                @Override
                public void render(int mouseX, int mouseY, float partialTicks) {
                    if (this.visible) {
                        this.isHovered = mouseX >= this.x &&
                                         mouseY >= this.y &&
                                         mouseX < this.x + this.width &&
                                         mouseY < this.y + this.height &&
                                         mouseY < ScreenConfig.this.height - 36 &&
                                         mouseY >= 50;
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
            this.eventListeners.add(this.button);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(new StringTextComponent(this.configValue.get().name()).getText());
        }

        @Override
        public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
            this.button.x = x + width - 87;
            this.button.y = y;
            this.button.render(mouseX, mouseY, partialTicks);
        }
    }

    /**
     * A custom implementation of the text field widget to help reset the focus when it's used
     * in an option list. This class is specific to {@link ScreenConfig} and won't work anywhere
     * else.
     */
    @OnlyIn(Dist.CLIENT)
    public class ConfigTextFieldWidget extends TextFieldWidget {
        public ConfigTextFieldWidget(FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent label) {
            super(fontRenderer, x, y, width, height, label.getFormattedText());
        }

        @Override
        public void setFocused2(boolean focused) {
            super.setFocused2(focused);
            if (focused) {
                if (ScreenConfig.this.activeTextField != null && ScreenConfig.this.activeTextField != this) {
                    ScreenConfig.this.activeTextField.setFocused2(false);
                }
                ScreenConfig.this.activeTextField = this;
            }
        }
    }
}
