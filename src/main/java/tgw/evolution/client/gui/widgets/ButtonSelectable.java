package tgw.evolution.client.gui.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ButtonSelectable extends Button {

    protected final @Nullable ButtonGroup group;
    protected final int id;

    public ButtonSelectable(int i, int j, int k, int l, Component component, OnPress onPress) {
        this(i, j, k, l, component, onPress, NO_TOOLTIP, null);
    }

    public ButtonSelectable(int i, int j, int k, int l, Component component, OnPress onPress, @Nullable ButtonGroup group) {
        this(i, j, k, l, component, onPress, NO_TOOLTIP, group);
    }

    public ButtonSelectable(int i, int j, int k, int l, Component component, OnPress onPress, OnTooltip onTooltip) {
        this(i, j, k, l, component, onPress, onTooltip, null);
    }

    public ButtonSelectable(int i, int j, int k, int l, Component component, OnPress onPress, OnTooltip onTooltip, @Nullable ButtonGroup group) {
        super(i, j, k, l, component, onPress, onTooltip);
        this.group = group;
        this.id = this.group != null ? this.group.register() : -1;
    }

    protected final boolean isSelected() {
        if (this.group == null) {
            return true;
        }
        return this.group.getSelected() == this.id;
    }

    public void refreshActive() {
        if (this.group == null) {
            return;
        }
        this.active = this.group.getSelected() != this.id;
    }
}
