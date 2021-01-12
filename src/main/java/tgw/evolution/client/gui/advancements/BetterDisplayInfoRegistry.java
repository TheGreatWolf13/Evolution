package tgw.evolution.client.gui.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BetterDisplayInfoRegistry {
    private final Map<ResourceLocation, BetterDisplayInfo> registry;

    public BetterDisplayInfoRegistry() {
        this.registry = new HashMap<>();
    }

    public BetterDisplayInfo get(Advancement advancement) {
        return this.registry.getOrDefault(advancement.getId(), new BetterDisplayInfo(advancement));
    }
}
