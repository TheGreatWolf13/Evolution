package tgw.evolution;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.GrassColors;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.fml.RegistryObject;

import static tgw.evolution.init.EvolutionBlocks.*;

public class ColorManager {

    public static void registerBlockColorHandlers(BlockColors colors) {
        IBlockColor grass = (state, blockReader, pos, color) -> BiomeColors.getGrassColor(blockReader, pos);
        IBlockColor spruce = (state, blockReader, pos, color) -> FoliageColors.getSpruce();
        IBlockColor birch = (state, blockReader, pos, color) -> FoliageColors.getBirch();
        IBlockColor aspen = (state, blockReader, pos, color) -> 16776192;
        IBlockColor leaves = (state, blockReader, pos, color) -> blockReader != null && pos != null ? BiomeColors.getFoliageColor(blockReader, pos) : FoliageColors.getDefault();
        //Grass
        register(colors, grass, GRASS_ANDESITE);
        register(colors, grass, GRASS_ANDESITE);
        register(colors, grass, GRASS_BASALT);
        register(colors, grass, GRASS_CHALK);
        register(colors, grass, GRASS_CHERT);
        register(colors, grass, GRASS_CONGLOMERATE);
        register(colors, grass, GRASS_DACITE);
        register(colors, grass, GRASS_DIORITE);
        register(colors, grass, GRASS_DOLOMITE);
        register(colors, grass, GRASS_GABBRO);
        register(colors, grass, GRASS_GNEISS);
        register(colors, grass, GRASS_GRANITE);
        register(colors, grass, GRASS_LIMESTONE);
        register(colors, grass, GRASS_MARBLE);
        register(colors, grass, GRASS_PHYLLITE);
        register(colors, grass, GRASS_QUARTZITE);
        register(colors, grass, GRASS_RED_SANDSTONE);
        register(colors, grass, GRASS_SANDSTONE);
        register(colors, grass, GRASS_SCHIST);
        register(colors, grass, GRASS_SHALE);
        register(colors, grass, GRASS_SLATE);
        register(colors, grass, GRASS_CLAY);
        register(colors, grass, GRASS_PEAT);
        register(colors, grass, DRY_GRASS_ANDESITE);
        register(colors, grass, DRY_GRASS_BASALT);
        register(colors, grass, DRY_GRASS_CHALK);
        register(colors, grass, DRY_GRASS_CHERT);
        register(colors, grass, DRY_GRASS_CONGLOMERATE);
        register(colors, grass, DRY_GRASS_DACITE);
        register(colors, grass, DRY_GRASS_DIORITE);
        register(colors, grass, DRY_GRASS_DOLOMITE);
        register(colors, grass, DRY_GRASS_GABBRO);
        register(colors, grass, DRY_GRASS_GNEISS);
        register(colors, grass, DRY_GRASS_GRANITE);
        register(colors, grass, DRY_GRASS_LIMESTONE);
        register(colors, grass, DRY_GRASS_MARBLE);
        register(colors, grass, DRY_GRASS_PHYLLITE);
        register(colors, grass, DRY_GRASS_QUARTZITE);
        register(colors, grass, DRY_GRASS_RED_SANDSTONE);
        register(colors, grass, DRY_GRASS_SANDSTONE);
        register(colors, grass, DRY_GRASS_SCHIST);
        register(colors, grass, DRY_GRASS_SHALE);
        register(colors, grass, DRY_GRASS_SLATE);
        register(colors, grass, GRASS);
        register(colors, grass, TALLGRASS);
        //Leaves
        register(colors, leaves, LEAVES_ACACIA);
        register(colors, aspen, LEAVES_ASPEN);
        register(colors, birch, LEAVES_BIRCH);
        register(colors, leaves, LEAVES_EBONY);
        register(colors, leaves, LEAVES_ELM);
        register(colors, spruce, LEAVES_EUCALYPTUS);
        register(colors, spruce, LEAVES_FIR);
        register(colors, leaves, LEAVES_KAPOK);
        register(colors, leaves, LEAVES_MANGROVE);
        register(colors, leaves, LEAVES_OAK);
        register(colors, leaves, LEAVES_OLD_OAK);
        register(colors, leaves, LEAVES_PALM);
        register(colors, spruce, LEAVES_PINE);
        register(colors, leaves, LEAVES_REDWOOD);
        register(colors, spruce, LEAVES_SPRUCE);
        register(colors, leaves, LEAVES_WILLOW);
    }

    public static void registerItemColorHandlers(ItemColors colors) {
        IItemColor grass = (itemStack, color) -> GrassColors.get(0.5, 1.0);
        IItemColor spruce = (itemStack, color) -> FoliageColors.getSpruce();
        IItemColor birch = (itemStack, color) -> FoliageColors.getBirch();
        IItemColor leaves = (itemStack, color) -> FoliageColors.getDefault();
        IItemColor aspen = (stack, color) -> 16776192;
        //Grass
        register(colors, grass, GRASS_ANDESITE);
        register(colors, grass, GRASS_BASALT);
        register(colors, grass, GRASS_CHALK);
        register(colors, grass, GRASS_CHERT);
        register(colors, grass, GRASS_CONGLOMERATE);
        register(colors, grass, GRASS_DACITE);
        register(colors, grass, GRASS_DIORITE);
        register(colors, grass, GRASS_DOLOMITE);
        register(colors, grass, GRASS_GABBRO);
        register(colors, grass, GRASS_GNEISS);
        register(colors, grass, GRASS_GRANITE);
        register(colors, grass, GRASS_LIMESTONE);
        register(colors, grass, GRASS_MARBLE);
        register(colors, grass, GRASS_PHYLLITE);
        register(colors, grass, GRASS_QUARTZITE);
        register(colors, grass, GRASS_RED_SANDSTONE);
        register(colors, grass, GRASS_SANDSTONE);
        register(colors, grass, GRASS_SCHIST);
        register(colors, grass, GRASS_SHALE);
        register(colors, grass, GRASS_SLATE);
        register(colors, grass, DRY_GRASS_ANDESITE);
        register(colors, grass, DRY_GRASS_BASALT);
        register(colors, grass, DRY_GRASS_CHALK);
        register(colors, grass, DRY_GRASS_CHERT);
        register(colors, grass, DRY_GRASS_CONGLOMERATE);
        register(colors, grass, DRY_GRASS_DACITE);
        register(colors, grass, DRY_GRASS_DIORITE);
        register(colors, grass, DRY_GRASS_DOLOMITE);
        register(colors, grass, DRY_GRASS_GABBRO);
        register(colors, grass, DRY_GRASS_GNEISS);
        register(colors, grass, DRY_GRASS_GRANITE);
        register(colors, grass, DRY_GRASS_LIMESTONE);
        register(colors, grass, DRY_GRASS_MARBLE);
        register(colors, grass, DRY_GRASS_PHYLLITE);
        register(colors, grass, DRY_GRASS_QUARTZITE);
        register(colors, grass, DRY_GRASS_RED_SANDSTONE);
        register(colors, grass, DRY_GRASS_SANDSTONE);
        register(colors, grass, DRY_GRASS_SCHIST);
        register(colors, grass, DRY_GRASS_SHALE);
        register(colors, grass, DRY_GRASS_SLATE);
        register(colors, grass, GRASS);
        register(colors, grass, GRASS_PEAT);
        register(colors, grass, TALLGRASS);
        //Leaves
        register(colors, leaves, LEAVES_ACACIA);
        register(colors, aspen, LEAVES_ASPEN);
        register(colors, birch, LEAVES_BIRCH);
        register(colors, leaves, LEAVES_EBONY);
        register(colors, leaves, LEAVES_ELM);
        register(colors, spruce, LEAVES_EUCALYPTUS);
        register(colors, spruce, LEAVES_FIR);
        register(colors, leaves, LEAVES_KAPOK);
        register(colors, leaves, LEAVES_MANGROVE);
        register(colors, leaves, LEAVES_OAK);
        register(colors, leaves, LEAVES_OLD_OAK);
        register(colors, leaves, LEAVES_PALM);
        register(colors, spruce, LEAVES_PINE);
        register(colors, leaves, LEAVES_REDWOOD);
        register(colors, spruce, LEAVES_SPRUCE);
        register(colors, leaves, LEAVES_WILLOW);
    }

    private static void register(BlockColors colors, IBlockColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }

    private static void register(ItemColors colors, IItemColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }
}
