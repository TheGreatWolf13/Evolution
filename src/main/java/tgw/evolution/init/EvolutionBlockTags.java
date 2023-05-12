package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

public final class EvolutionBlockTags {

    public static final ObjectList<TagKey<Block>> ALL;

    public static final TagKey<Block> BLOCKS_COMBINED_STEP_SOUND;
    public static final TagKey<Block> COBBLESTONES;
    public static final TagKey<Block> ROCKS;

    static {
        OList<TagKey<Block>> list = new OArrayList<>();
        BLOCKS_COMBINED_STEP_SOUND = register(list, "blocks_combined_step_sound");
        COBBLESTONES = register(list, "cobblestones");
        ROCKS = register(list, "rocks");
        list.trimCollection();
        ALL = ObjectLists.unmodifiable(list);
    }

    private EvolutionBlockTags() {
    }

    private static TagKey<Block> register(OList<TagKey<Block>> registry, String name) {
        TagKey<Block> tag = BlockTags.create(Evolution.getResource(name));
        registry.add(tag);
        return tag;
    }
}
