package tgw.evolution.datagen;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionItemTags;

public class EvItemTagsProvider extends ItemTagsProvider {

    private final ObjectSet<TagKey<Item>> addedTags = new ObjectOpenHashSet<>();

    public EvItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, Evolution.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.copy(EvolutionBlockTags.ROCKS, EvolutionItemTags.ROCKS);
        for (int i = 0, len = EvolutionItemTags.ALL.size(); i < len; i++) {
            if (!this.addedTags.contains(EvolutionItemTags.ALL.get(i))) {
                throw new IllegalStateException("Item Tag " + EvolutionItemTags.ALL.get(i).location() + " has not been registered!");
            }
        }
    }

    @Override
    protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        this.addedTags.add(itemTag);
        super.copy(blockTag, itemTag);
    }

    @Override
    protected TagAppender<Item> tag(TagKey<Item> tag) {
        this.addedTags.add(tag);
        return super.tag(tag);
    }
}
