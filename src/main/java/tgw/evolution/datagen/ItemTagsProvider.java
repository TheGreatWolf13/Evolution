//package tgw.evolution.datagen;
//
//import net.minecraft.core.Registry;
//import net.minecraft.tags.Tag;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import tgw.evolution.Evolution;
//import tgw.evolution.init.EvolutionBlockTags;
//import tgw.evolution.init.EvolutionItemTags;
//
//import java.util.function.Function;
//
//public class ItemTagsProvider extends TagsProvider<Item> {
//
//    private final Function<TagKey<Block>, Tag.Builder> blockTags;
//
//    public ItemTagsProvider(GenBundle bundle, BlockTagsProvider blockTagsProvider) {
//        super(bundle, Registry.ITEM, Evolution.MODID, EvolutionItemTags.ALL);
//        this.blockTags = blockTagsProvider::getOrCreateRawBuilder;
//    }
//
//    @Override
//    protected void addTags() {
//        this.copy(EvolutionBlockTags.ROCKS, EvolutionItemTags.ROCKS);
//    }
//
//    protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
//        Tag.Builder builder = this.getOrCreateRawBuilder(itemTag);
//        Tag.Builder blockBuilder = this.blockTags.apply(blockTag);
//        blockBuilder.getEntries().forEach(builder::add);
//    }
//
//    @Override
//    public String getName() {
//        return "Evolution Item Tags";
//    }
//
//    @Override
//    protected String tagType() {
//        return "Item";
//    }
//}
