package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderLayer;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.items.ItemBlock;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public abstract class Mixin_FS_ItemBlockRenderTypes {

    @Shadow @Final @DeleteField private static Map<Block, RenderType> TYPE_BY_BLOCK;
    @Shadow @Final @DeleteField private static Map<Fluid, RenderType> TYPE_BY_FLUID;
    @Shadow private static boolean renderCutout;

    @ModifyStatic
    private static void clinit() {
        RenderLayer.set(Blocks.TRIPWIRE, RenderLayer.TRIPWIRE);
        RenderLayer.set(Blocks.GRASS_BLOCK, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.IRON_BARS, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.GLASS_PANE, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.TRIPWIRE_HOOK, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.HOPPER, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.CHAIN, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.JUNGLE_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.OAK_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.SPRUCE_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.ACACIA_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.BIRCH_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.DARK_OAK_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.AZALEA_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.FLOWERING_AZALEA_LEAVES, RenderLayer.CUTOUT_MIPPED);
        RenderLayer.set(Blocks.OAK_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SPRUCE_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BIRCH_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.JUNGLE_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ACACIA_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DARK_OAK_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.GLASS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WHITE_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ORANGE_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.MAGENTA_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LIGHT_BLUE_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.YELLOW_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LIME_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.PINK_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.GRAY_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LIGHT_GRAY_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CYAN_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.PURPLE_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BLUE_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BROWN_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.GREEN_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.RED_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BLACK_BED, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POWERED_RAIL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DETECTOR_RAIL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.COBWEB, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.GRASS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FERN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BUSH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SEAGRASS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TALL_SEAGRASS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DANDELION, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POPPY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BLUE_ORCHID, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ALLIUM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.AZURE_BLUET, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.RED_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ORANGE_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WHITE_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.PINK_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.OXEYE_DAISY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CORNFLOWER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WITHER_ROSE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LILY_OF_THE_VALLEY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BROWN_MUSHROOM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.RED_MUSHROOM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WALL_TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SOUL_TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SOUL_WALL_TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FIRE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SOUL_FIRE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SPAWNER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.REDSTONE_WIRE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WHEAT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.OAK_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LADDER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.RAIL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.IRON_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.REDSTONE_TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.REDSTONE_WALL_TORCH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CACTUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SUGAR_CANE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.REPEATER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.OAK_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SPRUCE_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BIRCH_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.JUNGLE_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ACACIA_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DARK_OAK_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CRIMSON_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WARPED_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ATTACHED_PUMPKIN_STEM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ATTACHED_MELON_STEM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.PUMPKIN_STEM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.MELON_STEM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.VINE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.GLOW_LICHEN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LILY_PAD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.NETHER_WART, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BREWING_STAND, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.COCOA, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BEACON, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FLOWER_POT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_OAK_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_SPRUCE_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_BIRCH_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_JUNGLE_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_ACACIA_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_DARK_OAK_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_FERN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_DANDELION, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_POPPY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_BLUE_ORCHID, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_ALLIUM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_AZURE_BLUET, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_RED_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_ORANGE_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_WHITE_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_PINK_TULIP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_OXEYE_DAISY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_CORNFLOWER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_LILY_OF_THE_VALLEY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_WITHER_ROSE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_RED_MUSHROOM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_BROWN_MUSHROOM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_DEAD_BUSH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_CACTUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_AZALEA, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_FLOWERING_AZALEA, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CARROTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTATOES, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.COMPARATOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ACTIVATOR_RAIL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.IRON_TRAPDOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SUNFLOWER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LILAC, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ROSE_BUSH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.PEONY, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TALL_GRASS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LARGE_FERN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SPRUCE_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BIRCH_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.JUNGLE_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ACACIA_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DARK_OAK_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.END_ROD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CHORUS_PLANT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CHORUS_FLOWER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BEETROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.KELP, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.KELP_PLANT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TURTLE_EGG, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_TUBE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BRAIN_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BUBBLE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_FIRE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_HORN_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TUBE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BRAIN_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BUBBLE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FIRE_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.HORN_CORAL, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_TUBE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BRAIN_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BUBBLE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_FIRE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_HORN_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TUBE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BRAIN_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BUBBLE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FIRE_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.HORN_CORAL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_TUBE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_FIRE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.DEAD_HORN_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TUBE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BRAIN_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BUBBLE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FIRE_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.HORN_CORAL_WALL_FAN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SEA_PICKLE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CONDUIT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BAMBOO_SAPLING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BAMBOO, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_BAMBOO, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SCAFFOLDING, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.STONECUTTER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LANTERN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SOUL_LANTERN, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CAMPFIRE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SOUL_CAMPFIRE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SWEET_BERRY_BUSH, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WEEPING_VINES, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WEEPING_VINES_PLANT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TWISTING_VINES, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.TWISTING_VINES_PLANT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.NETHER_SPROUTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CRIMSON_FUNGUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WARPED_FUNGUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CRIMSON_ROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WARPED_ROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_CRIMSON_FUNGUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_WARPED_FUNGUS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_CRIMSON_ROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POTTED_WARPED_ROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CRIMSON_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.WARPED_DOOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.POINTED_DRIPSTONE, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SMALL_AMETHYST_BUD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.MEDIUM_AMETHYST_BUD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LARGE_AMETHYST_BUD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.AMETHYST_CLUSTER, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.LIGHTNING_ROD, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CAVE_VINES, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.CAVE_VINES_PLANT, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SPORE_BLOSSOM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.FLOWERING_AZALEA, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.AZALEA, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.MOSS_CARPET, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BIG_DRIPLEAF, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.BIG_DRIPLEAF_STEM, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SMALL_DRIPLEAF, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.HANGING_ROOTS, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.SCULK_SENSOR, RenderLayer.CUTOUT);
        RenderLayer.set(Blocks.ICE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.NETHER_PORTAL, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.WHITE_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.ORANGE_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.MAGENTA_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIGHT_BLUE_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.YELLOW_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIME_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.PINK_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.GRAY_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIGHT_GRAY_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.CYAN_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.PURPLE_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BLUE_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BROWN_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.GREEN_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.RED_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BLACK_STAINED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.WHITE_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.ORANGE_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.MAGENTA_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.YELLOW_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIME_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.PINK_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.GRAY_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.CYAN_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.PURPLE_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BLUE_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BROWN_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.GREEN_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.RED_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BLACK_STAINED_GLASS_PANE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.SLIME_BLOCK, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.HONEY_BLOCK, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.FROSTED_ICE, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.BUBBLE_COLUMN, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Blocks.TINTED_GLASS, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Fluids.FLOWING_WATER, RenderLayer.TRANSLUCENT);
        RenderLayer.set(Fluids.WATER, RenderLayer.TRANSLUCENT);
    }

    @Overwrite
    public static RenderType getChunkRenderType(BlockState state) {
        return RenderLayer.get(state.getBlock(), renderCutout);
    }

    @Overwrite
    public static RenderType getMovingBlockRenderType(BlockState state) {
        RenderType renderType = RenderLayer.get(state.getBlock(), renderCutout);
        return renderType == RenderType.translucent() ? RenderType.translucentMovingBlock() : renderType;
    }

    @Overwrite
    public static RenderType getRenderLayer(FluidState state) {
        return RenderLayer.get(state.getType(), renderCutout);
    }

    @Overwrite
    public static RenderType getRenderType(ItemStack stack, boolean translucent) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock b) {
            return getRenderType(b.getBlock().defaultBlockState(), translucent);
        }
        if (item instanceof BlockItem b) {
            return getRenderType(b.getBlock().defaultBlockState(), translucent);
        }
        return translucent ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
    }

    @Overwrite
    public static RenderType getRenderType(BlockState state, boolean translucent) {
        RenderType renderType = getChunkRenderType(state);
        if (renderType == RenderType.translucent()) {
            if (!Minecraft.useShaderTransparency()) {
                return Sheets.translucentCullBlockSheet();
            }
            return translucent ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
        }
        return Sheets.cutoutBlockSheet();
    }
}
