package tgw.evolution.datagen;

import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionBlocks;

public class BlockTagsProvider extends TagsProvider<Block> {

    public BlockTagsProvider(GenBundle bundle) {
        super(bundle, Registry.BLOCK, Evolution.MODID, EvolutionBlockTags.ALL);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.VALID_SPAWN).add(EvolutionBlocks.GRASSES.values().stream().map(RegistryObject::get).toArray(Block[]::new));
        this.tag(EvolutionBlockTags.BLOCKS_COMBINED_STEP_SOUND).addTag(BlockTags.CARPETS);
        this.tag(EvolutionBlockTags.COBBLESTONES).add(EvolutionBlocks.COBBLESTONES.values().stream().map(RegistryObject::get).toArray(Block[]::new));
        this.tag(EvolutionBlockTags.ROCKS).add(EvolutionBlocks.ROCKS.values().stream().map(RegistryObject::get).toArray(Block[]::new));
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
