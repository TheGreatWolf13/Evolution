package tgw.evolution.datagen;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionBlocks;

public class EvBlockTagsProvider extends BlockTagsProvider {

    private final ObjectSet<TagKey<Block>> addedTags = new ObjectOpenHashSet<>();

    public EvBlockTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Evolution.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.VALID_SPAWN).add(EvolutionBlocks.GRASSES.values().stream().map(RegistryObject::get).toArray(Block[]::new));
        this.tag(EvolutionBlockTags.COBBLESTONES).add(EvolutionBlocks.COBBLESTONES.values().stream().map(RegistryObject::get).toArray(Block[]::new));
        this.tag(EvolutionBlockTags.ROCKS).add(EvolutionBlocks.ROCKS.values().stream().map(RegistryObject::get).toArray(Block[]::new));
        for (int i = 0, len = EvolutionBlockTags.ALL.size(); i < len; i++) {
            if (!this.addedTags.contains(EvolutionBlockTags.ALL.get(i))) {
                throw new IllegalStateException("Block Tag " + EvolutionBlockTags.ALL.get(i).location() + " has not been registered!");
            }
        }
    }

    @Override
    protected TagAppender<Block> tag(TagKey<Block> tag) {
        this.addedTags.add(tag);
        return super.tag(tag);
    }
}
