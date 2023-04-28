package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

public final class EvolutionItemTags {

    public static final ObjectList<TagKey<Item>> ALL;

    public static final TagKey<Item> ROCKS;

    static {
        OList<TagKey<Item>> list = new OArrayList<>();
        ROCKS = register(list, "rocks");
        list.trimCollection();
        ALL = ObjectLists.unmodifiable(list);
    }

    private EvolutionItemTags() {}

    private static TagKey<Item> register(OList<TagKey<Item>> registry, String name) {
        TagKey<Item> tag = ItemTags.create(Evolution.getResource(name));
        registry.add(tag);
        return tag;
    }
}
