package tgw.evolution.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.init.EvolutionTexts;

public final class ConfigBoolean implements IConfigItem {

    private final boolean defaultValue;
    private final Component desc;
    private final Component name;
    private final ConfigFolder parent;
    private boolean dirtyValue;
    private boolean value;

    public ConfigBoolean(ConfigFolder parent, String name, boolean defaultValue) {
        this(parent, new TranslatableComponent(parent.nameKey() + "." + name), new TranslatableComponent(parent.nameKey() + "." + name + ".desc").append("\n").append(EvolutionTexts.configDefault(defaultValue ? EvolutionTexts.GUI_GENERAL_ON : EvolutionTexts.GUI_GENERAL_OFF)), defaultValue);
    }

    public ConfigBoolean(ConfigFolder parent, Component name, Component desc, boolean defaultValue) {
        this.parent = parent;
        this.name = name;
        this.desc = desc;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.dirtyValue = this.get();
        parent.add(this);
    }

    @Override
    public Component desc() {
        return this.desc;
    }

    @Override
    public void discardDirty() {
        this.dirtyValue = this.get();
    }

    public boolean get() {
        return this.value;
    }

    public boolean getDirty() {
        return this.dirtyValue;
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
    public void restore() {
        this.set(this.defaultValue);
    }

    @Override
    public void save() {
        this.value = this.dirtyValue;
    }

    public void set(boolean value) {
        this.dirtyValue = value;
        EvolutionConfig.handle(this);
    }

    public void setAndSave(boolean value) {
        this.dirtyValue = value;
        this.save();
        EvolutionConfig.handle(this);
    }

    @Override
    public Type type() {
        return Type.BOOLEAN;
    }
}
