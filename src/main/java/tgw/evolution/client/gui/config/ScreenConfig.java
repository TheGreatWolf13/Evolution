package tgw.evolution.client.gui.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.widgets.AdvEditBox;
import tgw.evolution.client.gui.widgets.ButtonIcon;
import tgw.evolution.client.text.CappedComponent;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.ConfigHelper;
import tgw.evolution.util.collection.*;
import tgw.evolution.util.math.MathHelper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ScreenConfig extends ScreenListMenu {
    private static final Field CLAZZ = ObfuscationReflectionHelper.findField(ForgeConfigSpec.EnumValue.class, "clazz");
    private static final Pattern DOUBLE_SPACE = Pattern.compile("\\s++");
    private static final Pattern PATTERN_CAMEL_CASE = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    protected final ModConfig config;
    protected final FolderEntry folderEntry;
    private final Comparator<Item> sortAlphabetically = (o1, o2) -> {
        if (o1 instanceof FolderItem && o2 instanceof FolderItem) {
            return MathHelper.compare(o1.label.getString(), o2.label.getString());
        }
        if (!(o1 instanceof FolderItem) && o2 instanceof FolderItem) {
            return 1;
        }
        if (o1 instanceof FolderItem) {
            return -1;
        }
        return MathHelper.compare(o1.label.getString(), o2.label.getString());
    };
    private final Component textRequiresRestart = new TranslatableComponent("evolution.gui.config.requiresRestart").withStyle(ChatFormatting.RED);
    private final FormattedText textReset = new TranslatableComponent("evolution.gui.config.reset");
    private final Component textSave = new TranslatableComponent("evolution.gui.config.save");
    private final Component textUnsaved = new TranslatableComponent("evolution.gui.config.unsaved");
    protected Button restoreButton;
    protected Button saveButton;

    private ScreenConfig(Screen parent, Component title, FolderEntry folderEntry, ModConfig config) {
        super(parent, title, 24);
        this.folderEntry = folderEntry;
        this.config = config;
    }

    public ScreenConfig(Screen parent, Component title, ModConfig config) {
        super(parent, title, 24);
        this.folderEntry = new FolderEntry("Root", ((ForgeConfigSpec) config.getSpec()).getValues(), (ForgeConfigSpec) config.getSpec(), true);
        this.config = config;
    }

    private static FormattedText createCommentFromConfig(ForgeConfigSpec.ValueSpec spec) {
        if (spec.getTranslationKey() != null) {
            return new TranslatableComponent(spec.getTranslationKey() + ".comment");
        }
        return spec.getComment() != null ? new TextComponent(spec.getComment()) : new TextComponent("");
    }

    public static Component createEnumComp(String modId, Enum<?> enumValue) {
        if (enumValue instanceof EvolutionFormatter.IUnit unit) {
            return new TextComponent(unit.getName());
        }
        return new TranslatableComponent(modId + ".config.enum_" + enumValue.name().toLowerCase());
    }

    public static MutableComponent createLabel(String input, String modId) {
        return new TranslatableComponent(modId + ".config.label." + input.toLowerCase());
    }

    private static MutableComponent createLabelFromHolder(ValueHolder<?> holder) {
        if (holder.valueSpec.getTranslationKey() != null) {
            return new TranslatableComponent(holder.valueSpec.getTranslationKey());
        }
        return createReadableLabel(lastValue(holder.configValue.getPath(), ""));
    }

    public static MutableComponent createReadableLabel(String input) {
        String valueName = input;
        String[] words = PATTERN_CAMEL_CASE.split(valueName);
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        valueName = Strings.join(words, " ");
        words = valueName.split("_");
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        return new TextComponent(DOUBLE_SPACE.matcher(Strings.join(words, " ")).replaceAll(" "));
    }

    private static <V> V lastValue(List<V> list, V defaultValue) {
        if (!list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        return defaultValue;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    @Override
    protected void constructEntries(List<Item> entries) {
        RList<Item> configEntries = new RArrayList<>();
        for (IEntry c : this.folderEntry.getEntries()) {
            if (c instanceof FolderEntry folder) {
                configEntries.add(new FolderItem(folder, this.config.getModId()));
            }
            else if (c instanceof ValueEntry valueEntry) {
                Object value = valueEntry.getHolder().getValue();
                if (value instanceof Boolean) {
                    configEntries.add(new BooleanItem((ValueHolder<Boolean>) valueEntry.getHolder()));
                }
                else if (value instanceof Integer) {
                    configEntries.add(new IntegerItem((ValueHolder<Integer>) valueEntry.getHolder()));
                }
                else if (value instanceof Double) {
                    configEntries.add(new DoubleItem((ValueHolder<Double>) valueEntry.getHolder()));
                }
                else if (value instanceof Long) {
                    configEntries.add(new LongItem((ValueHolder<Long>) valueEntry.getHolder()));
                }
                else if (value instanceof Enum) {
                    configEntries.add(new EnumItem((ValueHolder<Enum<?>>) valueEntry.getHolder()));
                }
                else if (value instanceof String) {
                    configEntries.add(new StringItem((ValueHolder<String>) valueEntry.getHolder()));
                }
                else if (value instanceof List<?>) {
                    configEntries.add(new ListItem((ListValueHolder) valueEntry.getHolder()));
                }
                else {
                    Evolution.info("Unsupported config value: " + valueEntry.getHolder().configValue.getPath());
                }
            }
        }
        configEntries.sort(this.sortAlphabetically);
        entries.addAll(configEntries);
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;
        if (this.folderEntry.isRoot()) {
            this.saveButton = this.addRenderableWidget(new Button(this.width / 2 - 160, this.height - 29, 100, 20, this.textSave, button -> {
                this.saveConfig();
                this.minecraft.setScreen(this.parent);
            }));
            this.restoreButton = this.addRenderableWidget(
                    new Button(this.width / 2 - 50, this.height - 29, 100, 20, EvolutionTexts.GUI_CONFIG_RESTORE_DEFAULTS, button -> {
                        if (this.folderEntry.isRoot()) {
                            this.showRestoreScreen();
                        }
                    }));
            this.addRenderableWidget(new Button(this.width / 2 + 60, this.height - 29, 100, 20, CommonComponents.GUI_CANCEL, button -> {
                if (this.isChanged(this.folderEntry)) {
                    this.minecraft.setScreen(new ScreenConfirmation(this, this.textUnsaved, result -> {
                        if (!result) {
                            return true;
                        }
                        this.minecraft.setScreen(this.parent);
                        return false;
                    }));
                }
                else {
                    this.minecraft.setScreen(this.parent);
                }
            }));
            this.updateButtons();
        }
        else {
            this.addRenderableWidget(new Button(this.width / 2 - 75, this.height - 29, 150, 20, CommonComponents.GUI_BACK,
                                                button -> this.minecraft.setScreen(this.parent)));
        }
    }

    public boolean isChanged(FolderEntry folder) {
        for (IEntry entry : folder.getEntries()) {
            if (entry instanceof FolderEntry) {
                if (this.isChanged((FolderEntry) entry)) {
                    return true;
                }
            }
            else if (entry instanceof ValueEntry) {
                if (((ValueEntry) entry).getHolder().isChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isModified(FolderEntry folder) {
        for (IEntry entry : folder.getEntries()) {
            if (entry instanceof FolderEntry) {
                if (this.isModified((FolderEntry) entry)) {
                    return true;
                }
            }
            else if (entry instanceof ValueEntry) {
                if (!((ValueEntry) entry).getHolder().isDefaultValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(@Key int keyCode, int scanCode, @Modifiers int modifiers) {
        assert this.minecraft != null;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.folderEntry.isRoot()) {
                if (this.isChanged(this.folderEntry)) {
                    this.minecraft.setScreen(new ScreenConfirmation(this, this.textUnsaved, result -> {
                        if (!result) {
                            return true;
                        }
                        this.minecraft.setScreen(this.parent);
                        return false;
                    }));
                }
                else {
                    this.minecraft.setScreen(this.parent);
                }
                return true;
            }
            this.minecraft.setScreen(this.parent);
            return true;
        }
        if (this.folderEntry.isRoot() && keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.saveConfig();
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        this.searchEditBox.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 0xFF_FFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void restoreDefaults(FolderEntry entry) {
        for (IEntry e : entry.getEntries()) {
            if (e instanceof FolderEntry folder) {
                this.restoreDefaults(folder);
            }
            else if (e instanceof ValueEntry valueEntry) {
                valueEntry.getHolder().restoreDefaultValue();
            }
        }
    }

    private void saveConfig() {
        if (!this.isChanged(this.folderEntry)) {
            return;
        }
        CommentedConfig newConfig = CommentedConfig.copy(this.config.getConfigData());
        Queue<FolderEntry> found = new ArrayDeque<>();
        found.add(this.folderEntry);
        while (!found.isEmpty()) {
            FolderEntry folder = found.poll();
            for (IEntry entry : folder.getEntries()) {
                if (entry instanceof FolderEntry) {
                    found.offer((FolderEntry) entry);
                }
                else if (entry instanceof ValueEntry valueEntry) {
                    ValueHolder<?> holder = valueEntry.getHolder();
                    if (holder.isChanged()) {
                        List<String> path = holder.configValue.getPath();
                        if (holder instanceof ListValueHolder listHolder) {
                            Function<List<?>, List<?>> converter = listHolder.getConverter();
                            if (converter != null) {
                                List<?> convertedList = converter.apply(listHolder.getValue());
                                newConfig.set(path, convertedList);
                                continue;
                            }
                        }
                        newConfig.set(path, holder.getValue());
                    }
                }
            }
        }
        this.config.getConfigData().putAll(newConfig);

        // Post logic for server configs
        if (this.config.getType() == ModConfig.Type.SERVER) {
            if (!ScreenListMenu.isPlayingGame()) {
                // Unload server configs since still in main menu
                this.config.getHandler().unload(this.config.getFullPath().getParent(), this.config);
                ConfigHelper.setConfigData(this.config, null);
            }
            else {
                ConfigHelper.sendConfigDataToServer(this.config);
            }
        }
        else {
            Evolution.info("Sending config reloading event for {}", this.config.getFileName());
            this.config.getSpec().afterReload();
            ConfigHelper.fireEvent(this.config, new ModConfigEvent.Reloading(this.config));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void showRestoreScreen() {
        ScreenConfirmation confirmScreen = new ScreenConfirmation(ScreenConfig.this, EvolutionTexts.GUI_CONFIG_RESTORE_MESSAGE, result -> {
            if (!result) {
                return true;
            }
            this.restoreDefaults(this.folderEntry);
            this.updateButtons();
            return true;
        });
        confirmScreen.setPositiveText(EvolutionTexts.GUI_CONFIG_RESTORE_DEFAULTS);
        confirmScreen.setNegativeText(CommonComponents.GUI_CANCEL);
        Minecraft.getInstance().setScreen(confirmScreen);
    }

    private void updateButtons() {
        if (this.folderEntry.isRoot()) {
            this.saveButton.active = this.isChanged(this.folderEntry);
            this.restoreButton.active = this.isModified(this.folderEntry);
        }
    }

    @Override
    protected void updateTooltip(int mouseX, int mouseY) {
    }

    public interface IEntry {
    }

    public class FolderItem extends Item {
        private final Button button;

        public FolderItem(FolderEntry folderEntry, String modId) {
            super(createLabel(folderEntry.label, modId));
            this.button = new Button(10, 5, 44, 20, new TextComponent(this.getLabel()).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),
                                     onPress -> {
                                         Component newTitle = ScreenConfig.this.title.copy().append(" > " + this.getLabel());
                                         assert ScreenConfig.this.minecraft != null;
                                         ScreenConfig.this.minecraft.setScreen(
                                                 new ScreenConfig(ScreenConfig.this, newTitle, folderEntry, ScreenConfig.this.config));
                                     });
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
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
            this.button.x = left - 1;
            this.button.y = top;
            this.button.setWidth(width);
            this.button.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }

    public abstract class ConfigItem<T> extends Item {
        protected final OList<GuiEventListener> eventListeners = new OArrayList<>();
        protected final ValueHolder<T> holder;
        protected final Button resetButton;

        public ConfigItem(ValueHolder<T> holder) {
            super(createLabelFromHolder(holder));
            this.holder = holder;
            this.tooltip = this.createToolTip(holder);
            int maxTooltipWidth = Math.max(ScreenConfig.this.width / 2 - 43, 170);
            Button.OnTooltip tooltip = ScreenUtil.createButtonTooltip(ScreenConfig.this, ScreenConfig.this.textReset, maxTooltipWidth);
            this.resetButton = new ButtonIcon(0, 0, 0, EvolutionResources.ICON_12_12, onPress -> {
                this.holder.restoreDefaultValue();
                this.onResetValue();
            }, tooltip);
            this.eventListeners.add(this.resetButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.eventListeners;
        }

        private List<FormattedCharSequence> createToolTip(ValueHolder<T> holder) {
            Font font = Minecraft.getInstance().font;
            List<FormattedText> lines = font.getSplitter().splitLines(createCommentFromConfig(holder.getSpec()), 200, Style.EMPTY);
            String name = lastValue(holder.configValue.getPath(), "");
            lines.add(0, new TextComponent(name).withStyle(ChatFormatting.DARK_GRAY));
            lines.add(0, this.label.copy().withStyle(ChatFormatting.YELLOW));
            ForgeConfigSpec.ConfigValue<?> value = holder.getConfigValue();
            if (value instanceof ForgeConfigSpec.DoubleValue ||
                value instanceof ForgeConfigSpec.IntValue ||
                value instanceof ForgeConfigSpec.LongValue) {
                Object range = holder.getSpec().getRange();
                lines.add(EvolutionTexts.configRange(range.toString()));
            }
            else if (value instanceof ForgeConfigSpec.EnumValue<?> enumValue) {
                Class<? extends Enum<?>> clazz = null;
                try {
                    clazz = (Class<? extends Enum<?>>) CLAZZ.get(enumValue);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not get field clazz");
                }
                Enum<?>[] values = clazz.getEnumConstants();
                Component allowedValues = EvolutionTexts.configAllowedValues(Arrays.stream(values)
                                                                                   .map(o -> createEnumComp(ScreenConfig.this.config.getModId(),
                                                                                                            o).getString())
                                                                                   .collect(Collectors.joining(", ")));
                lines.add(allowedValues);
            }
            if (value instanceof ForgeConfigSpec.IntValue ||
                value instanceof ForgeConfigSpec.DoubleValue ||
                value instanceof ForgeConfigSpec.LongValue ||
                value instanceof ForgeConfigSpec.BooleanValue ||
                value instanceof ForgeConfigSpec.EnumValue ||
                value.get() instanceof String) {
                Object def = holder.getSpec().getDefault();
                String str = def.toString();
                if (def instanceof Boolean bool) {
                    if (bool) {
                        str = I18n.get("options.on");
                    }
                    else {
                        str = I18n.get("options.off");
                    }
                }
                else if (def instanceof Enum enumDef) {
                    str = I18n.get(ScreenConfig.this.config.getModId() + ".config.enum_" + enumDef.name().toLowerCase());
                }
                lines.add(EvolutionTexts.configDefault(str));
                if (holder.getSpec().needsWorldRestart() && isPlayingGame()) {
                    lines.add(ScreenConfig.this.textRequiresRestart);
                }
            }
            return Language.getInstance().getVisualOrder(lines);
        }

        private Component getTrimmedLabel(int maxWidth) {
            return new CappedComponent(this.label, maxWidth);
        }

        protected void onResetValue() {
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
                           boolean hovered,
                           float partialTicks) {
            Minecraft.getInstance().font.draw(poseStack, this.getTrimmedLabel(width - 75), left, top + 6, 0xFF_FFFF);
            if (this.isMouseOver(mouseX, mouseY) && mouseX < ScreenConfig.this.list.getRowLeft() + ScreenConfig.this.list.getRowWidth() - 67) {
                ScreenConfig.this.setActiveTooltip(this.tooltip);
            }
            this.resetButton.active = !this.holder.isDefaultValue();
            this.resetButton.x = left + width - 21;
            this.resetButton.y = top;
            this.resetButton.render(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    public abstract class NumberItem<T extends Number> extends ConfigItem<T> {
        private final EditBox editBox;

        public NumberItem(ValueHolder<T> holder, Function<String, Number> parser) {
            super(holder);
            this.editBox = new AdvEditBox(ScreenConfig.this.font, 0, 0, 44, 18, EvolutionTexts.EMPTY);
            this.editBox.setValue(holder.getValue().toString());
            this.editBox.setResponder(s -> {
                try {
                    Number n = parser.apply(s);
                    if (holder.valueSpec.test(n)) {
                        this.editBox.setTextColor(0xe0_e0e0);
                        holder.setValue((T) n);
                        ScreenConfig.this.updateButtons();
                    }
                    else {
                        this.editBox.setTextColor(0xff_0000);
                    }
                }
                catch (Exception ignored) {
                    this.editBox.setTextColor(0xff_0000);
                }
            });
            this.eventListeners.add(this.editBox);
        }

        @Override
        public void onResetValue() {
            this.editBox.setValue(this.holder.getValue().toString());
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
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.editBox.x = left + width - 68;
            this.editBox.y = top + 1;
            this.editBox.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
            this.editBox.setFocus(false);
        }

        @Override
        public void tick() {
            this.editBox.tick();
        }
    }

    public class IntegerItem extends NumberItem<Integer> {
        public IntegerItem(ValueHolder<Integer> holder) {
            super(holder, Integer::parseInt);
        }
    }

    public class DoubleItem extends NumberItem<Double> {
        public DoubleItem(ValueHolder<Double> holder) {
            super(holder, Double::parseDouble);
        }
    }

    public class LongItem extends NumberItem<Long> {
        public LongItem(ValueHolder<Long> holder) {
            super(holder, Long::parseLong);
        }
    }

    public class BooleanItem extends ConfigItem<Boolean> {
        private final Button button;

        public BooleanItem(ValueHolder<Boolean> holder) {
            super(holder);
            this.button = new Button(10, 5, 46, 20, CommonComponents.optionStatus(holder.getValue()), button -> {
                holder.setValue(!holder.getValue());
                button.setMessage(CommonComponents.optionStatus(holder.getValue()));
                ScreenConfig.this.updateButtons();
            });
            this.eventListeners.add(this.button);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(CommonComponents.optionStatus(this.holder.getValue()));
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
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.button.x = left + width - 69;
            this.button.y = top;
            this.button.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }

    public class StringItem extends ConfigItem<String> {
        private final Button button;

        public StringItem(ValueHolder<String> holder) {
            super(holder);
            this.button = new Button(10, 5, 46, 20, EvolutionTexts.GUI_GENERAL_EDIT, button -> Minecraft.getInstance()
                                                                                                        .setScreen(new ScreenEditString(
                                                                                                                ScreenConfig.this, this.label,
                                                                                                                holder.getValue(),
                                                                                                                holder.valueSpec::test, s -> {
                                                                                                            holder.setValue(s);
                                                                                                            ScreenConfig.this.updateButtons();
                                                                                                        })));
            this.eventListeners.add(this.button);
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
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.button.x = left + width - 69;
            this.button.y = top;
            this.button.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }

    public class ListItem extends ConfigItem<List<?>> {
        private final Button button;

        public ListItem(ListValueHolder holder) {
            super(holder);
            this.button = new Button(10, 5, 46, 20, EvolutionTexts.GUI_GENERAL_EDIT,
                                     button -> Minecraft.getInstance().setScreen(new ScreenEditList(ScreenConfig.this, this.label, holder)));
            this.eventListeners.add(this.button);
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
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.button.x = left + width - 69;
            this.button.y = top;
            this.button.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }

    public class EnumItem extends ConfigItem<Enum<?>> {
        private final Button button;

        public EnumItem(ValueHolder<Enum<?>> holder) {
            super(holder);
            this.button = new Button(10, 5, 46, 20, createEnumComp(ScreenConfig.this.config.getModId(), holder.getValue()),
                                     button -> Minecraft.getInstance()
                                                        .setScreen(new ScreenChangeEnum(ScreenConfig.this, this.label, holder.getValue(), e -> {
                                                            holder.setValue(e);
                                                            ScreenConfig.this.updateButtons();
                                                        }, ScreenConfig.this.config.getModId())));
            this.eventListeners.add(this.button);
        }

        @Override
        protected void onResetValue() {
            this.button.setMessage(createEnumComp(ScreenConfig.this.config.getModId(), this.holder.getValue()));
        }

        @Override
        public void render(PoseStack matrices,
                           int index,
                           int top,
                           int left,
                           int width,
                           int p_230432_6_,
                           int mouseX,
                           int mouseY,
                           boolean hovered,
                           float partialTicks) {
            super.render(matrices, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.button.x = left + width - 69;
            this.button.y = top;
            this.button.render(matrices, mouseX, mouseY, partialTicks);
        }

        @Override
        public void resetFocus() {
        }

        @Override
        public void tick() {
        }
    }

    public class ValueHolder<T> {
        private final ForgeConfigSpec.ConfigValue<T> configValue;
        private final T initialValue;
        private final ForgeConfigSpec.ValueSpec valueSpec;
        protected T value;

        public ValueHolder(ForgeConfigSpec.ConfigValue<T> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            this.configValue = configValue;
            this.valueSpec = valueSpec;
            this.initialValue = configValue.get();
            this.value = this.setValue(configValue.get());
        }

        public ForgeConfigSpec.ConfigValue<T> getConfigValue() {
            return this.configValue;
        }

        public ForgeConfigSpec.ValueSpec getSpec() {
            return this.valueSpec;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isChanged() {
            return !this.value.equals(this.initialValue);
        }

        public boolean isDefaultValue() {
            return this.value.equals(this.valueSpec.getDefault());
        }

        public void restoreDefaultValue() {
            this.setValue((T) this.valueSpec.getDefault());
            ScreenConfig.this.updateButtons();
        }

        protected T setValue(T value) {
            this.value = value;
            return this.value;
        }
    }

    public class ListValueHolder extends ValueHolder<List<?>> {
        @Nullable
        private final Function<List<?>, List<?>> converter;

        public ListValueHolder(ForgeConfigSpec.ConfigValue<List<?>> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(configValue, valueSpec);
            this.converter = createConverter(configValue);
        }

        @Nullable
        private static Function<List<?>, List<?>> createConverter(ForgeConfigSpec.ConfigValue<List<?>> configValue) {
            List<?> original = configValue.get();
            if (original instanceof ArrayList) {
                return ArrayList::new;
            }
            if (original instanceof LinkedList) {
                return LinkedList::new;
            }
            if (original instanceof RArrayList) {
                return RArrayList::new;
            }
            if (original instanceof OArrayList) {
                return OArrayList::new;
            }
            if (original instanceof IArrayList) {
                return objects -> new IntArrayList((Collection<? extends Integer>) objects);
            }
            if (original instanceof FArrayList) {
                return objects -> new FArrayList((Collection<? extends Float>) objects);
            }
            return null;
        }

        @Nullable
        public Function<List<?>, List<?>> getConverter() {
            return this.converter;
        }

        @Override
        protected List<?> setValue(List<?> value) {
            this.value = new ArrayList<>(value);
            return this.value;
        }
    }

    public class FolderEntry implements IEntry {
        private final List<IEntry> entries;
        private final String label;
        private final boolean root;

        public FolderEntry(String label, UnmodifiableConfig config, ForgeConfigSpec spec, boolean root) {
            this.label = label;
            this.root = root;
            ImmutableList.Builder<IEntry> builder = ImmutableList.builder();
            for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
                Object o = entry.getValue();
                if (o instanceof UnmodifiableConfig unmodifiableConfig) {
                    //noinspection ObjectAllocationInLoop
                    builder.add(new FolderEntry(entry.getKey(), unmodifiableConfig, spec, false));
                }
                else if (o instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                    ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(configValue.getPath());
                    //noinspection ObjectAllocationInLoop
                    builder.add(new ValueEntry(configValue, valueSpec));
                }
            }
            this.entries = builder.build();
        }

        public List<IEntry> getEntries() {
            return this.entries;
        }

        public boolean isRoot() {
            return this.root;
        }
    }

    public class ValueEntry implements IEntry {
        private final ValueHolder<?> holder;

        public ValueEntry(ForgeConfigSpec.ConfigValue<?> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            this.holder = configValue.get() instanceof List ?
                          new ListValueHolder((ForgeConfigSpec.ConfigValue<List<?>>) configValue, valueSpec) :
                          new ValueHolder<>(configValue, valueSpec);
        }

        public ValueHolder<?> getHolder() {
            return this.holder;
        }
    }
}