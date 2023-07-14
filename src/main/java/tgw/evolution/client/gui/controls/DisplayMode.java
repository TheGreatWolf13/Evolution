package tgw.evolution.client.gui.controls;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Predicate;

public enum DisplayMode {
    ALL(keyEntry -> true),
    UNBOUND(keyEntry -> keyEntry.getKey().isUnbound()),
    CONFLICTING(keyEntry -> {

        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (key.getName().equals(keyEntry.getKey().getName()) || key.isUnbound()) {
                continue;
            }
            if (key.same(keyEntry.getKey())) {
                return true;
            }
        }
        return false;
    });

    private final Predicate<ListKeyBinds.KeyEntry> predicate;

    DisplayMode(Predicate<ListKeyBinds.KeyEntry> predicate) {
        this.predicate = predicate;
    }

    public Predicate<ListKeyBinds.KeyEntry> getPredicate() {
        return this.predicate;
    }
}
