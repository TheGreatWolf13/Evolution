package tgw.evolution.init;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import tgw.evolution.Evolution;

public final class EvolutionItemTags {

    public static final TagKey<Item> ROCKS = register("rocks");

    private EvolutionItemTags() {}

    private static TagKey<Item> register(String name) {
        return ItemTags.create(Evolution.getResource(name));
    }
}
