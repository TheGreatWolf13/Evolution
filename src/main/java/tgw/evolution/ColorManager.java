package tgw.evolution;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.GrassColors;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.WoodVariant;

import static tgw.evolution.init.EvolutionBlocks.GRASS;
import static tgw.evolution.init.EvolutionBlocks.TALLGRASS;

public final class ColorManager {

    private ColorManager() {
    }

    private static void register(BlockColors colors, IBlockColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }

    private static void register(BlockColors colors, IBlockColor color, Block block) {
        colors.register(color, block);
    }

    private static void register(ItemColors colors, IItemColor color, RegistryObject<Block> block) {
        colors.register(color, block.get());
    }

    private static void register(ItemColors colors, IItemColor color, IItemProvider item) {
        colors.register(color, item);
    }

    public static void registerBlockColorHandlers(BlockColors colors) {
        IBlockColor grass = (state, world, pos, color) -> BiomeColors.getAverageGrassColor(world, pos);
        IBlockColor spruce = (state, world, pos, color) -> FoliageColors.getEvergreenColor();
        IBlockColor birch = (state, world, pos, color) -> FoliageColors.getBirchColor();
        IBlockColor aspen = (state, world, pos, color) -> 0xff_fc00;
        IBlockColor leaves = (state, world, pos, color) -> world != null && pos != null ?
                                                           BiomeColors.getAverageFoliageColor(world, pos) :
                                                           FoliageColors.getDefaultColor();
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
                case ASPEN: {
                    register(colors, aspen, wood.getLeaves());
                    continue;
                }
                case BIRCH: {
                    register(colors, birch, wood.getLeaves());
                    continue;
                }
                case EUCALYPTUS:
                case FIR:
                case PINE:
                case SPRUCE: {
                    register(colors, spruce, wood.getLeaves());
                    continue;
                }
            }
            register(colors, leaves, wood.getLeaves());
        }
    }

    public static void registerItemColorHandlers(ItemColors colors) {
        IItemColor grass = (stack, tint) -> GrassColors.get(0.5, 1.0);
        IItemColor spruce = (stack, tint) -> FoliageColors.getEvergreenColor();
        IItemColor birch = (stack, tint) -> FoliageColors.getBirchColor();
        IItemColor leaves = (stack, tint) -> FoliageColors.getDefaultColor();
        IItemColor aspen = (stack, tint) -> 0xff_fc00;
        IItemColor temperature = (stack, tint) -> {
            if (tint == 1) {
                Item item = stack.getItem();
                if (item instanceof IItemTemperature) {
                    return ((IItemTemperature) item).getTemperatureColor(stack);
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
                case ASPEN: {
                    register(colors, aspen, wood.getLeaves());
                    continue;
                }
                case BIRCH: {
                    register(colors, birch, wood.getLeaves());
                    continue;
                }
                case EUCALYPTUS:
                case FIR:
                case PINE:
                case SPRUCE: {
                    register(colors, spruce, wood.getLeaves());
                    continue;
                }
            }
            register(colors, leaves, wood.getLeaves());
        }
        register(colors, temperature, EvolutionItems.ingot_copper.get());
    }
}
