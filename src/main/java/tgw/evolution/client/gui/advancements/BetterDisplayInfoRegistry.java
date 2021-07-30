package tgw.evolution.client.gui.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BetterDisplayInfoRegistry {
    private final Map<ResourceLocation, BetterDisplayInfo> registry;

    public BetterDisplayInfoRegistry() {
        this.registry = new HashMap<>();
    }

    public BetterDisplayInfo get(Advancement advancement) {
        return this.registry.getOrDefault(advancement.getId(), new BetterDisplayInfo(advancement));
    }
}
