package tgw.evolution.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import tgw.evolution.init.EvolutionTexts;

public final class ConfigInteger implements IConfigItem {

    private final int defaultValue;
    private final Component desc;
    private final int maxValue;
    private final int minValue;
    private final Component name;
    private final ConfigFolder parent;
    private int dirtyValue;
    private Priority priority = Priority.NORMAL;
    private int value;

    public ConfigInteger(ConfigFolder parent, String name, int defaultValue) {
        this(parent, name, Integer.MIN_VALUE, Integer.MAX_VALUE, defaultValue);
    }

    public ConfigInteger(ConfigFolder parent, String name, int minValue, int maxValue, int defaultValue) {
        this(parent, new TranslatableComponent(parent.nameKey() + "." + name), makeDesc(parent, name, minValue, maxValue, defaultValue), minValue, maxValue, defaultValue);
    }

    public ConfigInteger(ConfigFolder parent, Component name, Component desc, int minValue, int maxValue, int defaultValue) {
        assert minValue < maxValue : "MinValue (" + minValue + ") has to be lower than MaxValue (" + maxValue + ")!";
        assert minValue <= defaultValue && defaultValue <= maxValue : "Default Value out of bounds: found " + defaultValue + ", range [" + minValue + ", " + maxValue + "]";
        this.name = name;
        this.desc = desc;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.dirtyValue = this.get();
        parent.add(this);
    }

    private static Component makeDesc(ConfigFolder parent, String name, int minValue, int maxValue, int defaultValue) {
        MutableComponent component = new TranslatableComponent(parent.nameKey() + "." + name + ".desc");
        if (minValue != Integer.MIN_VALUE) {
            if (maxValue != Integer.MAX_VALUE) {
                component.append("\n").append(EvolutionTexts.configRange(minValue, maxValue));
            }
            else {
                component.append("\n").append(EvolutionTexts.configRangeMin(minValue));
            }
        }
        else {
            if (maxValue != Integer.MAX_VALUE) {
                component.append("\n").append(EvolutionTexts.configRangeMax(maxValue));
            }
        }
        return component.append("\n").append(EvolutionTexts.configDefault(new TextComponent(String.valueOf(defaultValue))));
    }

    @Override
    public Component desc() {
        return this.desc;
    }

    @Override
    public void discardDirty() {
        this.dirtyValue = this.get();
    }

    public int get() {
        return this.value;
    }

    public int getDirty() {
        return this.dirtyValue;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public boolean isDefault() {
        return this.getDirty() == this.defaultValue;
    }

    @Override
    public boolean isDirty() {
        return this.get() != this.getDirty();
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public ConfigFolder parent() {
        return this.parent;
    }

    @Override
    public Priority priority() {
        return this.priority;
    }

    @Override
    public void restore() {
        this.set(this.defaultValue);
    }

    @Override
    public void save() {
        this.value = Mth.clamp(this.dirtyValue, this.minValue, this.maxValue);
    }

    public void set(int value) {
        this.dirtyValue = Mth.clamp(value, this.minValue, this.maxValue);
        EvolutionConfig.handle(this);
    }

    public void setAndSave(int value) {
        this.dirtyValue = Mth.clamp(value, this.minValue, this.maxValue);
        this.save();
        EvolutionConfig.handle(this);
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public Type type() {
        return Type.INTEGER;
    }
}
