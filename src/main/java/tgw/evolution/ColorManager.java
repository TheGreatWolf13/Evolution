package tgw.evolution;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.init.EvolutionBlocks.GRASS;
import static tgw.evolution.init.EvolutionBlocks.TALLGRASS;

public final class ColorManager {

    private ColorManager() {
    }

    private static void register(BlockColors colors, BlockColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }

    private static void register(BlockColors colors, BlockColor color, Block block) {
        colors.register(color, block);
    }

    private static void register(ItemColors colors, ItemColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }

    private static void register(ItemColors colors, ItemColor color, ItemLike item) {
        colors.register(color, item);
    }

    public static void registerBlockColorHandlers(BlockColors colors) {
        BlockColor grass = (state, world, pos, color) -> BiomeColors.getAverageGrassColor(world, pos);
        BlockColor spruce = (state, world, pos, color) -> FoliageColor.getEvergreenColor();
        BlockColor birch = (state, world, pos, color) -> FoliageColor.getBirchColor();
        BlockColor aspen = (state, world, pos, color) -> 0xff_fc00;
        BlockColor leaves = (state, world, pos, color) -> world != null && pos != null ?
                                                          BiomeColors.getAverageFoliageColor(world, pos) :
                                                          FoliageColor.getDefaultColor();
        //Grass
        for (RockVariant rock : RockVariant.VALUES) {
            register(colors, grass, rock.getGrass());
            try {
                register(colors, grass, rock.getDryGrass());
            }
            catch (IllegalStateException ignored) {
            }
        }
        register(colors, grass, GRASS);
        register(colors, grass, TALLGRASS);
        //Leaves
        for (WoodVariant wood : WoodVariant.VALUES) {
            switch (wood) {
                case ASPEN -> {
                    register(colors, aspen, wood.getLeaves());
                    continue;
                }
                case BIRCH -> {
                    register(colors, birch, wood.getLeaves());
                    continue;
                }
                case EUCALYPTUS, FIR, PINE, SPRUCE -> {
                    register(colors, spruce, wood.getLeaves());
                    continue;
                }
            }
            register(colors, leaves, wood.getLeaves());
        }
    }

    public static void registerItemColorHandlers(ItemColors colors) {
        ItemColor grass = (stack, tint) -> GrassColor.get(0.5, 1.0);
        ItemColor spruce = (stack, tint) -> FoliageColor.getEvergreenColor();
        ItemColor birch = (stack, tint) -> FoliageColor.getBirchColor();
        ItemColor leaves = (stack, tint) -> FoliageColor.getDefaultColor();
        ItemColor aspen = (stack, tint) -> 0xff_fc00;
        ItemColor temperature = (stack, tint) -> {
            if (tint == 1) {
                Item item = stack.getItem();
                if (item instanceof IItemTemperature itemTemperature) {
                    return itemTemperature.getTemperatureColor(stack);
                }
            }
            return 0xffff_ffff;
        };
        //Grass
        for (RockVariant rock : RockVariant.VALUES) {
            register(colors, grass, rock.getGrass());
            try {
                register(colors, grass, rock.getDryGrass());
            }
            catch (IllegalStateException ignored) {
            }
        }
        register(colors, grass, GRASS);
        register(colors, grass, TALLGRASS);
        //Leaves
        for (WoodVariant wood : WoodVariant.VALUES) {
            switch (wood) {
                case ASPEN -> {
                    register(colors, aspen, wood.getLeaves());
                    continue;
                }
                case BIRCH -> {
                    register(colors, birch, wood.getLeaves());
                    continue;
                }
                case EUCALYPTUS, FIR, PINE, SPRUCE -> {
                    register(colors, spruce, wood.getLeaves());
                    continue;
                }
            }
            register(colors, leaves, wood.getLeaves());
        }
        register(colors, temperature, EvolutionItems.INGOT_COPPER.get());
    }
}
