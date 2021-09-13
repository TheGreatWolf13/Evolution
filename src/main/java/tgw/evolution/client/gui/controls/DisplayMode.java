package tgw.evolution.client.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public enum DisplayMode {
    ALL(keyEntry -> true),
    UNBOUND(keyEntry -> keyEntry.getKeybinding().isUnbound()),
    CONFLICTING(keyEntry -> {

        for (KeyBinding key : Minecraft.getInstance().options.keyMappings) {
            if (key.getName().equals(keyEntry.getKeybinding().getName()) || key.isUnbound()) {
                continue;
            }
            if (key.getKey().getValue() == keyEntry.getKeybinding().getKey().getValue()) {
                return true;
            }
        }
        return false;
    });

    private final Predicate<ListKeyBinding.KeyEntry> predicate;

    DisplayMode(Predicate<ListKeyBinding.KeyEntry> predicate) {
        this.predicate = predicate;
    }

    public Predicate<ListKeyBinding.KeyEntry> getPredicate() {
        return this.predicate;
    }
}
