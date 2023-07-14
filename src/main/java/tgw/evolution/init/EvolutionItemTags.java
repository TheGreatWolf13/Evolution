package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

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
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, Evolution.getResource(name));
        registry.add(tag);
        return tag;
    }
}
