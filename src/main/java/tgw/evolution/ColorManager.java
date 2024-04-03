package tgw.evolution;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import tgw.evolution.client.renderer.IBlockColor;
import tgw.evolution.client.renderer.IItemColor;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.util.constants.NutrientVariant;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.init.EvolutionBlocks.*;

public final class ColorManager {

    private ColorManager() {
    }

    public static int getAverageFoliageColor(BlockAndTintGetter level, int x, int y, int z) {
        return level.getBlockTint_(x, y, z, BiomeColors.FOLIAGE_COLOR_RESOLVER);
    }

    public static int getAverageGrassColor(BlockAndTintGetter level, int x, int y, int z) {
        return level.getBlockTint_(x, y, z, BiomeColors.GRASS_COLOR_RESOLVER);
    }

    public static int getAverageWaterColor(BlockAndTintGetter level, int x, int y, int z) {
        return level.getBlockTint_(x, y, z, BiomeColors.WATER_COLOR_RESOLVER);
    }

    public static void registerBlockColorHandlers(BlockColors colors) {
        BlockColor grass = (IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? getAverageGrassColor(level, x, y, z) : GrassColor.get(0.5, 1.0);
        BlockColor spruce = (IBlockColor) (state, level, x, y, z, data) -> FoliageColor.getEvergreenColor();
        BlockColor birch = (IBlockColor) (state, level, x, y, z, data) -> FoliageColor.getBirchColor();
        BlockColor aspen = (IBlockColor) (state, level, x, y, z, data) -> 0xff_fc00;
        BlockColor leaves = (IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? getAverageFoliageColor(level, x, y, z) : FoliageColor.getDefaultColor();
        //Grass
        for (NutrientVariant variant : NutrientVariant.VALUES) {
            register(colors, grass, variant.get(GRASSES));
        }
        register(colors, grass, GRASS_CLAY);
        register(colors, grass, SHORT_GRASS);
        register(colors, grass, TALL_GRASS);
        //Leaves
        for (WoodVariant wood : WoodVariant.VALUES) {
            switch (wood) {
                case ASPEN -> {
                    register(colors, aspen, wood.get(LEAVES));
                    continue;
                }
                case BIRCH -> {
                    register(colors, birch, wood.get(LEAVES));
                    continue;
                }
                case EUCALYPTUS, FIR, PINE, SPRUCE -> {
                    register(colors, spruce, wood.get(LEAVES));
                    continue;
                }
            }
            register(colors, leaves, wood.get(LEAVES));
        }
    }

    public static void registerItemColorHandlers(ItemColors colors) {
        IItemColor grass = (stack, tint, level, x, y, z) -> level != null && x != Integer.MIN_VALUE ? getAverageGrassColor(level, x, y, z) : GrassColor.get(0.5, 1.0);
        IItemColor spruce = (stack, tint, level, x, y, z) -> FoliageColor.getEvergreenColor();
        IItemColor birch = (stack, tint, level, x, y, z) -> FoliageColor.getBirchColor();
        IItemColor leaves = (stack, tint, level, x, y, z) -> level != null && x != Integer.MIN_VALUE ? getAverageFoliageColor(level, x, y, z) : FoliageColor.getDefaultColor();
        IItemColor aspen = (stack, tint, level, x, y, z) -> 0xff_fc00;
        IItemColor temperature = (stack, tint, level, x, y, z) -> {
            if (tint == 1) {
                Item item = stack.getItem();
                if (item instanceof IItemTemperature itemTemperature) {
                    return itemTemperature.getTemperatureColor(stack);
                }
            }
            return 0xffff_ffff;
        };
        //Grass
        for (NutrientVariant variant : NutrientVariant.VALUES) {
            register(colors, grass, variant.get(GRASSES));
        }
        register(colors, grass, GRASS_CLAY);
        register(colors, grass, SHORT_GRASS);
        register(colors, grass, TALL_GRASS);
        //Leaves
        for (WoodVariant wood : WoodVariant.VALUES) {
            switch (wood) {
                case ASPEN -> {
                    register(colors, aspen, wood.get(LEAVES));
                    continue;
                }
                case BIRCH -> {
                    register(colors, birch, wood.get(LEAVES));
                    continue;
                }
                case EUCALYPTUS, FIR, PINE, SPRUCE -> {
                    register(colors, spruce, wood.get(LEAVES));
                    continue;
                }
            }
            register(colors, leaves, wood.get(LEAVES));
        }
        register(colors, temperature, EvolutionItems.INGOT_COPPER);
    }

    private static void register(ItemColors colors, ItemColor color, ItemLike item) {
        colors.register(color, item);
    }

    private static void register(BlockColors colors, BlockColor color, Block block) {
        colors.register(color, block);
    }
}
