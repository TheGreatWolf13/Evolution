package tgw.evolution.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionTexts;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class ConfigEnum<E extends Enum<E>> implements IConfigItem {

    private final E defaultValue;
    private final Component desc;
    private final TranslatableComponent name;
    private final ConfigFolder parent;
    private final E[] values;
    private E dirtyValue;
    private Priority priority = Priority.NORMAL;
    private E value;

    public ConfigEnum(ConfigFolder parent, String name, E[] values, E defaultValue) {
        this(parent, new TranslatableComponent(parent.nameKey() + "." + name), new TranslatableComponent(parent.nameKey() + "." + name + ".desc"), values, defaultValue);
    }

    public ConfigEnum(ConfigFolder parent, TranslatableComponent name, Component desc, E[] values, E defaultValue) {
        this.parent = parent;
        this.name = name;
        this.desc = desc;
        this.values = values;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.dirtyValue = this.get();
        parent.add(this);
    }

    public E byId(int id) {
        return this.values[id];
    }

    public void cycle(boolean forward) {
        int index = this.getDirty().ordinal();
        if (forward) {
            if (++index >= this.values.length) {
                index = 0;
            }
        }
        else {
            if (--index < 0) {
                index = this.values.length - 1;
            }
        }
        this.set(this.values[index]);
    }

    @Override
    public Component desc() {
        return this.desc.copy().append("\n").append(EvolutionTexts.configAllowedValues(Arrays.stream(this.values).map(e -> this.getValueDesc(e).getString()).collect(Collectors.joining(", ")))).append("\n").append(EvolutionTexts.configDefault(this.getValueDesc(this.defaultValue)));
    }

    @Override
    public void discardDirty() {
        this.dirtyValue = this.get();
    }

    public E get() {
        return this.value;
    }

    public E getDirty() {
        return this.dirtyValue;
    }

    public Component getValueDesc(E e) {
        if (e instanceof EvolutionFormatter.IUnit unit) {
            return new TextComponent(unit.getName());
        }
        return new TranslatableComponent(this.name.getKey() + ".enum." + e.name().toLowerCase());
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
        this.value = this.dirtyValue;
    }

    public void set(E value) {
        this.dirtyValue = value;
        EvolutionConfig.handle(this);
    }

    public void setAndSave(E value) {
        this.dirtyValue = value;
        this.save();
        EvolutionConfig.handle(this);
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public Type type() {
        return Type.ENUM;
    }
}
