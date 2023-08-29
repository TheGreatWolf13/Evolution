package tgw.evolution.datagen;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.util.ExistingFileHelper;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionBlocks;

import java.nio.file.Path;
import java.util.Collection;

public class BlockTagsProvider extends TagsProvider<Block> {

    public BlockTagsProvider(DataGenerator dataGenerator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, existingPaths, existingFileHelper, Registry.BLOCK, Evolution.MODID, EvolutionBlockTags.ALL);
    }

    @Override
    protected void addTags() {
        this.tag(EvolutionBlockTags.BLOCKS_COMBINED_STEP_PARTICLE).addTag(BlockTags.CARPETS);
        this.tag(EvolutionBlockTags.BLOCKS_COMBINED_STEP_SOUND).addTag(EvolutionBlockTags.BLOCKS_COMBINED_STEP_PARTICLE);
        this.tag(EvolutionBlockTags.COBBLESTONES).add(EvolutionBlocks.COBBLESTONES.values().toArray(Block[]::new));
        this.tag(EvolutionBlockTags.ROCKS).add(EvolutionBlocks.ROCKS.values().toArray(Block[]::new));
    }

    @Override
    public String getName() {
        return "Evolution Block Tags";
    }

    @Override
    protected String tagType() {
        return "Block";
    }
}
