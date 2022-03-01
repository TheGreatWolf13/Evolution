package tgw.evolution.client.gui.controls;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public enum DisplayMode {
    ALL(keyEntry -> true),
    UNBOUND(keyEntry -> keyEntry.getKey().isUnbound()),
    CONFLICTING(keyEntry -> {

        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (key.getName().equals(keyEntry.getKey().getName()) || key.isUnbound()) {
                continue;
            }
            if (key.getKey().getValue() == keyEntry.getKey().getKey().getValue()) {
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
