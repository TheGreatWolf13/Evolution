package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.grower.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.blocks.BlockBedrock;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.patches.obj.IStatePredicate;
import tgw.evolution.util.collection.lists.OList;

import java.util.function.ToIntFunction;

@Mixin(Blocks.class)
public abstract class Mixin_FS_Blocks {

    @Shadow @Final @Mutable @RestoreFinal public static Block AIR;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRANITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_GRANITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIORITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DIORITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANDESITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_ANDESITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRASS_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIRT;
    @Shadow @Final @Mutable @RestoreFinal public static Block COARSE_DIRT;
    @Shadow @Final @Mutable @RestoreFinal public static Block PODZOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLESTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block BEDROCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block WATER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LAVA;
    @Shadow @Final @Mutable @RestoreFinal public static Block SAND;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SAND;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAVEL;
    @Shadow @Final @Mutable @RestoreFinal public static Block GOLD_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_GOLD_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block IRON_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_IRON_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block COAL_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_COAL_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_GOLD_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_SPRUCE_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_BIRCH_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_JUNGLE_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_ACACIA_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_DARK_OAK_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_OAK_LOG;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_OAK_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_SPRUCE_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_BIRCH_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_JUNGLE_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_ACACIA_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_DARK_OAK_WOOD;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block AZALEA_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block FLOWERING_AZALEA_LEAVES;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPONGE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WET_SPONGE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LAPIS_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_LAPIS_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LAPIS_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DISPENSER;
    @Shadow @Final @Mutable @RestoreFinal public static Block SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block NOTE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_BED;
    @Shadow @Final @Mutable @RestoreFinal public static Block POWERED_RAIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DETECTOR_RAIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block STICKY_PISTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBWEB;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block FERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BUSH;
    @Shadow @Final @Mutable @RestoreFinal public static Block SEAGRASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block TALL_SEAGRASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block PISTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block PISTON_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_WOOL;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOVING_PISTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block DANDELION;
    @Shadow @Final @Mutable @RestoreFinal public static Block POPPY;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_ORCHID;
    @Shadow @Final @Mutable @RestoreFinal public static Block ALLIUM;
    @Shadow @Final @Mutable @RestoreFinal public static Block AZURE_BLUET;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block OXEYE_DAISY;
    @Shadow @Final @Mutable @RestoreFinal public static Block CORNFLOWER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WITHER_ROSE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LILY_OF_THE_VALLEY;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_MUSHROOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_MUSHROOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block GOLD_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block IRON_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block TNT;
    @Shadow @Final @Mutable @RestoreFinal public static Block BOOKSHELF;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_COBBLESTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block OBSIDIAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block WALL_TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block FIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_FIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPAWNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHEST;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_WIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIAMOND_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_DIAMOND_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIAMOND_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRAFTING_TABLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHEAT;
    @Shadow @Final @Mutable @RestoreFinal public static Block FARMLAND;
    @Shadow @Final @Mutable @RestoreFinal public static Block FURNACE;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block LADDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block RAIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLESTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block LEVER;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block IRON_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_REDSTONE_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_WALL_TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block SNOW;
    @Shadow @Final @Mutable @RestoreFinal public static Block ICE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SNOW_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block CACTUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CLAY;
    @Shadow @Final @Mutable @RestoreFinal public static Block SUGAR_CANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUKEBOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PUMPKIN;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHERRACK;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_SAND;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_SOIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BASALT;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BASALT;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_WALL_TORCH;
    @Shadow @Final @Mutable @RestoreFinal public static Block GLOWSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_PORTAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block CARVED_PUMPKIN;
    @Shadow @Final @Mutable @RestoreFinal public static Block JACK_O_LANTERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block REPEATER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_STAINED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRACKED_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_STONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_COBBLESTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_MOSSY_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_CRACKED_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_CHISELED_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_MUSHROOM_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_MUSHROOM_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block MUSHROOM_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block IRON_BARS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHAIN;
    @Shadow @Final @Mutable @RestoreFinal public static Block GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MELON;
    @Shadow @Final @Mutable @RestoreFinal public static Block ATTACHED_PUMPKIN_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block ATTACHED_MELON_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block PUMPKIN_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block MELON_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block VINE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GLOW_LICHEN;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block MYCELIUM;
    @Shadow @Final @Mutable @RestoreFinal public static Block LILY_PAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_BRICK_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_WART;
    @Shadow @Final @Mutable @RestoreFinal public static Block ENCHANTING_TABLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BREWING_STAND;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAULDRON;
    @Shadow @Final @Mutable @RestoreFinal public static Block WATER_CAULDRON;
    @Shadow @Final @Mutable @RestoreFinal public static Block LAVA_CAULDRON;
    @Shadow @Final @Mutable @RestoreFinal public static Block POWDER_SNOW_CAULDRON;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_PORTAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_PORTAL_FRAME;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_STONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DRAGON_EGG;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_LAMP;
    @Shadow @Final @Mutable @RestoreFinal public static Block COCOA;
    @Shadow @Final @Mutable @RestoreFinal public static Block SANDSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block EMERALD_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_EMERALD_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ENDER_CHEST;
    @Shadow @Final @Mutable @RestoreFinal public static Block TRIPWIRE_HOOK;
    @Shadow @Final @Mutable @RestoreFinal public static Block TRIPWIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block EMERALD_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block COMMAND_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BEACON;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLESTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_COBBLESTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block FLOWER_POT;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_OAK_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_SPRUCE_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_BIRCH_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_JUNGLE_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_ACACIA_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_DARK_OAK_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_FERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_DANDELION;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_POPPY;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_BLUE_ORCHID;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_ALLIUM;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_AZURE_BLUET;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_RED_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_ORANGE_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_WHITE_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_PINK_TULIP;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_OXEYE_DAISY;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_CORNFLOWER;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_LILY_OF_THE_VALLEY;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_WITHER_ROSE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_RED_MUSHROOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_BROWN_MUSHROOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_DEAD_BUSH;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_CACTUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CARROTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTATOES;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block SKELETON_SKULL;
    @Shadow @Final @Mutable @RestoreFinal public static Block SKELETON_WALL_SKULL;
    @Shadow @Final @Mutable @RestoreFinal public static Block WITHER_SKELETON_SKULL;
    @Shadow @Final @Mutable @RestoreFinal public static Block WITHER_SKELETON_WALL_SKULL;
    @Shadow @Final @Mutable @RestoreFinal public static Block ZOMBIE_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block ZOMBIE_WALL_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block PLAYER_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block PLAYER_WALL_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block CREEPER_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block CREEPER_WALL_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block DRAGON_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block DRAGON_WALL_HEAD;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANVIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHIPPED_ANVIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DAMAGED_ANVIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block TRAPPED_CHEST;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_WEIGHTED_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block HEAVY_WEIGHTED_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block COMPARATOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block DAYLIGHT_DETECTOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block REDSTONE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_QUARTZ_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block HOPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block QUARTZ_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_QUARTZ_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block QUARTZ_PILLAR;
    @Shadow @Final @Mutable @RestoreFinal public static Block QUARTZ_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACTIVATOR_RAIL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DROPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_STAINED_GLASS_PANE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SLIME_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BARRIER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT;
    @Shadow @Final @Mutable @RestoreFinal public static Block IRON_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_PRISMARINE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_PRISMARINE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_PRISMARINE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SEA_LANTERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block HAY_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block COAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block PACKED_ICE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SUNFLOWER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LILAC;
    @Shadow @Final @Mutable @RestoreFinal public static Block ROSE_BUSH;
    @Shadow @Final @Mutable @RestoreFinal public static Block PEONY;
    @Shadow @Final @Mutable @RestoreFinal public static Block TALL_GRASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LARGE_FERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_WALL_BANNER;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_RED_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_RED_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SANDSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block OAK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_STONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block PETRIFIED_OAK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLESTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block QUARTZ_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_RED_SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPUR_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_STONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_QUARTZ;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_RED_SANDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPRUCE_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIRCH_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block JUNGLE_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block ACACIA_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block DARK_OAK_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_ROD;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHORUS_PLANT;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHORUS_FLOWER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPUR_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPUR_PILLAR;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPUR_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_STONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BEETROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIRT_PATH;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_GATEWAY;
    @Shadow @Final @Mutable @RestoreFinal public static Block REPEATING_COMMAND_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHAIN_COMMAND_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block FROSTED_ICE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGMA_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_WART_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_NETHER_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BONE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRUCTURE_VOID;
    @Shadow @Final @Mutable @RestoreFinal public static Block OBSERVER;
    @Shadow @Final @Mutable @RestoreFinal public static Block SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_SHULKER_BOX;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_GLAZED_TERRACOTTA;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_CONCRETE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_CONCRETE_POWDER;
    @Shadow @Final @Mutable @RestoreFinal public static Block KELP;
    @Shadow @Final @Mutable @RestoreFinal public static Block KELP_PLANT;
    @Shadow @Final @Mutable @RestoreFinal public static Block DRIED_KELP_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block TURTLE_EGG;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_TUBE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BRAIN_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BUBBLE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_FIRE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_HORN_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block TUBE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRAIN_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUBBLE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block FIRE_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block HORN_CORAL_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_TUBE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BRAIN_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BUBBLE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_FIRE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_HORN_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block TUBE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRAIN_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUBBLE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block FIRE_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block HORN_CORAL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_TUBE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BRAIN_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BUBBLE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_FIRE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_HORN_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block TUBE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRAIN_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUBBLE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block FIRE_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block HORN_CORAL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_TUBE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BRAIN_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_BUBBLE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_FIRE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEAD_HORN_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block TUBE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRAIN_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUBBLE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block FIRE_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block HORN_CORAL_WALL_FAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block SEA_PICKLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_ICE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CONDUIT;
    @Shadow @Final @Mutable @RestoreFinal public static Block BAMBOO_SAPLING;
    @Shadow @Final @Mutable @RestoreFinal public static Block BAMBOO;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_BAMBOO;
    @Shadow @Final @Mutable @RestoreFinal public static Block VOID_AIR;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAVE_AIR;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUBBLE_COLUMN;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_GRANITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_RED_SANDSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_STONE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DIORITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_COBBLESTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_STONE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_SANDSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_QUARTZ_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRANITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANDESITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_NETHER_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_ANDESITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIORITE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_GRANITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_RED_SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_STONE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DIORITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_COBBLESTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_STONE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_SANDSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_QUARTZ_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRANITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANDESITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_NETHER_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_ANDESITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIORITE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block PRISMARINE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_SANDSTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSSY_STONE_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRANITE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONE_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANDESITE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_NETHER_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block SANDSTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block END_STONE_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DIORITE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block SCAFFOLDING;
    @Shadow @Final @Mutable @RestoreFinal public static Block LOOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block BARREL;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOKER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLAST_FURNACE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CARTOGRAPHY_TABLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block FLETCHING_TABLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRINDSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LECTERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMITHING_TABLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block STONECUTTER;
    @Shadow @Final @Mutable @RestoreFinal public static Block BELL;
    @Shadow @Final @Mutable @RestoreFinal public static Block LANTERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_LANTERN;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAMPFIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SOUL_CAMPFIRE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SWEET_BERRY_BUSH;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_WARPED_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_HYPHAE;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_WARPED_HYPHAE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_NYLIUM;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_FUNGUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_WART_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_ROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHER_SPROUTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_CRIMSON_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_HYPHAE;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRIPPED_CRIMSON_HYPHAE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_NYLIUM;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_FUNGUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block SHROOMLIGHT;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEEPING_VINES;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEEPING_VINES_PLANT;
    @Shadow @Final @Mutable @RestoreFinal public static Block TWISTING_VINES;
    @Shadow @Final @Mutable @RestoreFinal public static Block TWISTING_VINES_PLANT;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_ROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_PLANKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_FENCE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_TRAPDOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_FENCE_GATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_DOOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRIMSON_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block WARPED_WALL_SIGN;
    @Shadow @Final @Mutable @RestoreFinal public static Block STRUCTURE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block JIGSAW;
    @Shadow @Final @Mutable @RestoreFinal public static Block COMPOSTER;
    @Shadow @Final @Mutable @RestoreFinal public static Block TARGET;
    @Shadow @Final @Mutable @RestoreFinal public static Block BEE_NEST;
    @Shadow @Final @Mutable @RestoreFinal public static Block BEEHIVE;
    @Shadow @Final @Mutable @RestoreFinal public static Block HONEY_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block HONEYCOMB_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block NETHERITE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block ANCIENT_DEBRIS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRYING_OBSIDIAN;
    @Shadow @Final @Mutable @RestoreFinal public static Block RESPAWN_ANCHOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_CRIMSON_FUNGUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_WARPED_FUNGUS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_CRIMSON_ROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_WARPED_ROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block LODESTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACKSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACKSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACKSTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACKSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRACKED_POLISHED_BLACKSTONE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_POLISHED_BLACKSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block GILDED_BLACKSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_PRESSURE_PLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_BUTTON;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_BLACKSTONE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_NETHER_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRACKED_NETHER_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block QUARTZ_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_CANDLE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block WHITE_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block ORANGE_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block MAGENTA_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_BLUE_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block YELLOW_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIME_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PINK_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GRAY_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHT_GRAY_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CYAN_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block PURPLE_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLUE_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BROWN_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block GREEN_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block RED_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block BLACK_CANDLE_CAKE;
    @Shadow @Final @Mutable @RestoreFinal public static Block AMETHYST_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BUDDING_AMETHYST;
    @Shadow @Final @Mutable @RestoreFinal public static Block AMETHYST_CLUSTER;
    @Shadow @Final @Mutable @RestoreFinal public static Block LARGE_AMETHYST_BUD;
    @Shadow @Final @Mutable @RestoreFinal public static Block MEDIUM_AMETHYST_BUD;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMALL_AMETHYST_BUD;
    @Shadow @Final @Mutable @RestoreFinal public static Block TUFF;
    @Shadow @Final @Mutable @RestoreFinal public static Block CALCITE;
    @Shadow @Final @Mutable @RestoreFinal public static Block TINTED_GLASS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POWDER_SNOW;
    @Shadow @Final @Mutable @RestoreFinal public static Block SCULK_SENSOR;
    @Shadow @Final @Mutable @RestoreFinal public static Block OXIDIZED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEATHERED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block EXPOSED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block COPPER_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block COPPER_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_COPPER_ORE;
    @Shadow @Final @Mutable @RestoreFinal public static Block OXIDIZED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEATHERED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block EXPOSED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block OXIDIZED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEATHERED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block EXPOSED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block OXIDIZED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WEATHERED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block EXPOSED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_COPPER_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_WEATHERED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_EXPOSED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_OXIDIZED_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_OXIDIZED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_WEATHERED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_EXPOSED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_CUT_COPPER;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_OXIDIZED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_WEATHERED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_EXPOSED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_CUT_COPPER_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_OXIDIZED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_WEATHERED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_EXPOSED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block WAXED_CUT_COPPER_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block LIGHTNING_ROD;
    @Shadow @Final @Mutable @RestoreFinal public static Block POINTED_DRIPSTONE;
    @Shadow @Final @Mutable @RestoreFinal public static Block DRIPSTONE_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAVE_VINES;
    @Shadow @Final @Mutable @RestoreFinal public static Block CAVE_VINES_PLANT;
    @Shadow @Final @Mutable @RestoreFinal public static Block SPORE_BLOSSOM;
    @Shadow @Final @Mutable @RestoreFinal public static Block AZALEA;
    @Shadow @Final @Mutable @RestoreFinal public static Block FLOWERING_AZALEA;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSS_CARPET;
    @Shadow @Final @Mutable @RestoreFinal public static Block MOSS_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIG_DRIPLEAF;
    @Shadow @Final @Mutable @RestoreFinal public static Block BIG_DRIPLEAF_STEM;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMALL_DRIPLEAF;
    @Shadow @Final @Mutable @RestoreFinal public static Block HANGING_ROOTS;
    @Shadow @Final @Mutable @RestoreFinal public static Block ROOTED_DIRT;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLED_DEEPSLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLED_DEEPSLATE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLED_DEEPSLATE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block COBBLED_DEEPSLATE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DEEPSLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DEEPSLATE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DEEPSLATE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block POLISHED_DEEPSLATE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_TILES;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_TILE_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_TILE_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_TILE_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_BRICK_STAIRS;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_BRICK_SLAB;
    @Shadow @Final @Mutable @RestoreFinal public static Block DEEPSLATE_BRICK_WALL;
    @Shadow @Final @Mutable @RestoreFinal public static Block CHISELED_DEEPSLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRACKED_DEEPSLATE_BRICKS;
    @Shadow @Final @Mutable @RestoreFinal public static Block CRACKED_DEEPSLATE_TILES;
    @Shadow @Final @Mutable @RestoreFinal public static Block INFESTED_DEEPSLATE;
    @Shadow @Final @Mutable @RestoreFinal public static Block SMOOTH_BASALT;
    @Shadow @Final @Mutable @RestoreFinal public static Block RAW_IRON_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block RAW_COPPER_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block RAW_GOLD_BLOCK;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_AZALEA;
    @Shadow @Final @Mutable @RestoreFinal public static Block POTTED_FLOWERING_AZALEA;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void rebuildCache() {
        for (long it = Block.BLOCK_STATE_REGISTRY.beginIteration(); Block.BLOCK_STATE_REGISTRY.hasNextIteration(it); it = Block.BLOCK_STATE_REGISTRY.nextEntry(it)) {
            ((BlockBehaviour.BlockStateBase) Block.BLOCK_STATE_REGISTRY.getIteration(it)).initCache();
        }
    }

    @Unique
    private static BlockEntityType<? extends ChestBlockEntity> _chest() {
        return BlockEntityType.CHEST;
    }

    @Unique
    private static Holder<ConfiguredFeature<HugeFungusConfiguration, ?>> _crimson() {
        return TreeFeatures.CRIMSON_FUNGUS_PLANTED;
    }

    @Unique
    private static boolean _fireImmune(BlockState s, BlockGetter l, int x, int y, int z, EntityType<?> e) {
        return e.fireImmune();
    }

    @Unique
    private static Holder<? extends ConfiguredFeature<?, ?>> _hugeBrownMushroom() {
        return TreeFeatures.HUGE_BROWN_MUSHROOM;
    }

    @Unique
    private static Holder<? extends ConfiguredFeature<?, ?>> _hugeRedMushroom() {
        return TreeFeatures.HUGE_RED_MUSHROOM;
    }

    @Unique
    private static Item _melonSeeds() {
        return Items.MELON_SEEDS;
    }

    @Unique
    private static boolean _ocelotOrParrot(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    @Unique
    private static int _pickle(BlockState s) {
        return SeaPickleBlock.isDead(s) ? 0 : 3 + 3 * s.getValue(SeaPickleBlock.PICKLES);
    }

    @Unique
    private static boolean _polarBear(BlockState state, BlockGetter level, int x, int y, int z, EntityType<?> entity) {
        return entity == EntityType.POLAR_BEAR;
    }

    @Unique
    private static Item _pumpkinSeeds() {
        return Items.PUMPKIN_SEEDS;
    }

    @Unique
    private static int _respawnAnchor(BlockState s) {
        return RespawnAnchorBlock.getScaledChargeLevel(s, 15);
    }

    @Unique
    private static boolean _sculkSensor(BlockState s, BlockGetter l, int x, int y, int z) {
        return SculkSensorBlock.getPhase(s) == SculkSensorPhase.ACTIVE;
    }

    @Unique
    private static boolean _snow(BlockState state, BlockGetter level, int x, int y, int z) {
        return state.getValue(SnowLayerBlock.LAYERS) >= 8;
    }

    @Unique
    private static Holder<ConfiguredFeature<HugeFungusConfiguration, ?>> _warped() {
        return TreeFeatures.WARPED_FUNGUS_PLANTED;
    }

    @Shadow
    private static BedBlock bed(DyeColor dyeColor) {
        throw new AbstractMethodError();
    }

    @ModifyStatic
    @Unique
    private static void clinit() {
        AIR = register("air", new AirBlock(of(Material.AIR).noCollission().noDrops().air()));
        STONE = register("stone", new Block(of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        GRANITE = register("granite", new Block(of(Material.STONE, MaterialColor.DIRT).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        POLISHED_GRANITE = register("polished_granite", new Block(of(Material.STONE, MaterialColor.DIRT)
                                                                          .requiresCorrectToolForDrops()
                                                                          .strength(1.5F, 6.0F)));
        DIORITE = register("diorite", new Block(of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        POLISHED_DIORITE = register("polished_diorite", new Block(of(Material.STONE, MaterialColor.QUARTZ)
                                                                          .requiresCorrectToolForDrops()
                                                                          .strength(1.5F, 6.0F)));
        ANDESITE = register("andesite", new Block(of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        POLISHED_ANDESITE = register("polished_andesite", new Block(of(Material.STONE, MaterialColor.STONE)
                                                                            .requiresCorrectToolForDrops()
                                                                            .strength(1.5F, 6.0F)));
        GRASS_BLOCK = register("grass_block", new GrassBlock(of(Material.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS)));
        DIRT = register("dirt", new Block(of(Material.DIRT, MaterialColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
        COARSE_DIRT = register("coarse_dirt", new Block(of(Material.DIRT, MaterialColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
        PODZOL = register("podzol", new SnowyDirtBlock(of(Material.DIRT, MaterialColor.PODZOL).strength(0.5F).sound(SoundType.GRAVEL)));
        COBBLESTONE = register("cobblestone", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        OAK_PLANKS = register("oak_planks", new Block(of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        SPRUCE_PLANKS = register("spruce_planks", new Block(of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        BIRCH_PLANKS = register("birch_planks", new Block(of(Material.WOOD, MaterialColor.SAND).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        JUNGLE_PLANKS = register("jungle_planks", new Block(of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        ACACIA_PLANKS = register("acacia_planks", new Block(of(Material.WOOD, MaterialColor.COLOR_ORANGE)
                                                                    .strength(2.0F, 3.0F)
                                                                    .sound(SoundType.WOOD)));
        DARK_OAK_PLANKS = register("dark_oak_planks", new Block(of(Material.WOOD, MaterialColor.COLOR_BROWN)
                                                                        .strength(2.0F, 3.0F)
                                                                        .sound(SoundType.WOOD)));
        OAK_SAPLING = register("oak_sapling", new SaplingBlock(new OakTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        SPRUCE_SAPLING = register("spruce_sapling", new SaplingBlock(new SpruceTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        BIRCH_SAPLING = register("birch_sapling", new SaplingBlock(new BirchTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        JUNGLE_SAPLING = register("jungle_sapling", new SaplingBlock(new JungleTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        ACACIA_SAPLING = register("acacia_sapling", new SaplingBlock(new AcaciaTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        DARK_OAK_SAPLING = register("dark_oak_sapling", new SaplingBlock(new DarkOakTreeGrower(), of(Material.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)));
        BEDROCK = register("bedrock", new BlockBedrock());
        WATER = register("water", new LiquidBlock(Fluids.WATER, of(Material.WATER).noCollission().strength(100.0F).noDrops()));
        LAVA = register("lava", new LiquidBlock(Fluids.LAVA, of(Material.LAVA)
                .noCollission()
                .randomTicks()
                .strength(100.0F)
                .lightLevel(BlockUtils.LIGHT_15)
                .noDrops()));
        SAND = register("sand", new SandBlock(0xdb_d3a0, of(Material.SAND, MaterialColor.SAND).strength(0.5F).sound(SoundType.SAND)));
        RED_SAND = register("red_sand", new SandBlock(0xa9_5821, of(Material.SAND, MaterialColor.COLOR_ORANGE)
                .strength(0.5F)
                .sound(SoundType.SAND)));
        GRAVEL = register("gravel", new GravelBlock(of(Material.SAND, MaterialColor.STONE).strength(0.6F).sound(SoundType.GRAVEL)));
        GOLD_ORE = register("gold_ore", new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
        DEEPSLATE_GOLD_ORE = register("deepslate_gold_ore", new OreBlock(BlockBehaviour.Properties.copy(GOLD_ORE)
                                                                                                  .color(MaterialColor.DEEPSLATE)
                                                                                                  .strength(4.5F, 3.0F)
                                                                                                  .sound(SoundType.DEEPSLATE)));
        IRON_ORE = register("iron_ore", new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
        DEEPSLATE_IRON_ORE = register("deepslate_iron_ore", new OreBlock(BlockBehaviour.Properties.copy(IRON_ORE)
                                                                                                  .color(MaterialColor.DEEPSLATE)
                                                                                                  .strength(4.5F, 3.0F)
                                                                                                  .sound(SoundType.DEEPSLATE)));
        COAL_ORE = register("coal_ore", new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F), UniformInt.of(0, 2)));
        DEEPSLATE_COAL_ORE = register("deepslate_coal_ore", new OreBlock(BlockBehaviour.Properties.copy(COAL_ORE)
                                                                                                  .color(MaterialColor.DEEPSLATE)
                                                                                                  .strength(4.5F, 3.0F)
                                                                                                  .sound(SoundType.DEEPSLATE), UniformInt.of(0, 2)));
        NETHER_GOLD_ORE = register("nether_gold_ore", new OreBlock(of(Material.STONE, MaterialColor.NETHER)
                                                                           .requiresCorrectToolForDrops()
                                                                           .strength(3.0F, 3.0F)
                                                                           .sound(SoundType.NETHER_GOLD_ORE), UniformInt.of(0, 1)));
        OAK_LOG = register("oak_log", log(MaterialColor.WOOD, MaterialColor.PODZOL));
        SPRUCE_LOG = register("spruce_log", log(MaterialColor.PODZOL, MaterialColor.COLOR_BROWN));
        BIRCH_LOG = register("birch_log", log(MaterialColor.SAND, MaterialColor.QUARTZ));
        JUNGLE_LOG = register("jungle_log", log(MaterialColor.DIRT, MaterialColor.PODZOL));
        ACACIA_LOG = register("acacia_log", log(MaterialColor.COLOR_ORANGE, MaterialColor.STONE));
        DARK_OAK_LOG = register("dark_oak_log", log(MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIPPED_SPRUCE_LOG = register("stripped_spruce_log", log(MaterialColor.PODZOL, MaterialColor.PODZOL));
        STRIPPED_BIRCH_LOG = register("stripped_birch_log", log(MaterialColor.SAND, MaterialColor.SAND));
        STRIPPED_JUNGLE_LOG = register("stripped_jungle_log", log(MaterialColor.DIRT, MaterialColor.DIRT));
        STRIPPED_ACACIA_LOG = register("stripped_acacia_log", log(MaterialColor.COLOR_ORANGE, MaterialColor.COLOR_ORANGE));
        STRIPPED_DARK_OAK_LOG = register("stripped_dark_oak_log", log(MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN));
        STRIPPED_OAK_LOG = register("stripped_oak_log", log(MaterialColor.WOOD, MaterialColor.WOOD));
        OAK_WOOD = register("oak_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
        SPRUCE_WOOD = register("spruce_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD)));
        BIRCH_WOOD = register("birch_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.SAND).strength(2.0F).sound(SoundType.WOOD)));
        JUNGLE_WOOD = register("jungle_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.DIRT).strength(2.0F).sound(SoundType.WOOD)));
        ACACIA_WOOD = register("acacia_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.COLOR_GRAY)
                                                                             .strength(2.0F)
                                                                             .sound(SoundType.WOOD)));
        DARK_OAK_WOOD = register("dark_oak_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.COLOR_BROWN)
                                                                                 .strength(2.0F)
                                                                                 .sound(SoundType.WOOD)));
        STRIPPED_OAK_WOOD = register("stripped_oak_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.WOOD)
                                                                                         .strength(2.0F)
                                                                                         .sound(SoundType.WOOD)));
        STRIPPED_SPRUCE_WOOD = register("stripped_spruce_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.PODZOL)
                                                                                               .strength(2.0F)
                                                                                               .sound(SoundType.WOOD)));
        STRIPPED_BIRCH_WOOD = register("stripped_birch_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.SAND)
                                                                                             .strength(2.0F)
                                                                                             .sound(SoundType.WOOD)));
        STRIPPED_JUNGLE_WOOD = register("stripped_jungle_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.DIRT)
                                                                                               .strength(2.0F)
                                                                                               .sound(SoundType.WOOD)));
        STRIPPED_ACACIA_WOOD = register("stripped_acacia_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.COLOR_ORANGE)
                                                                                               .strength(2.0F)
                                                                                               .sound(SoundType.WOOD)));
        STRIPPED_DARK_OAK_WOOD = register("stripped_dark_oak_wood", new RotatedPillarBlock(of(Material.WOOD, MaterialColor.COLOR_BROWN)
                                                                                                   .strength(2.0F)
                                                                                                   .sound(SoundType.WOOD)));
        OAK_LEAVES = register("oak_leaves", leaves(SoundType.GRASS));
        SPRUCE_LEAVES = register("spruce_leaves", leaves(SoundType.GRASS));
        BIRCH_LEAVES = register("birch_leaves", leaves(SoundType.GRASS));
        JUNGLE_LEAVES = register("jungle_leaves", leaves(SoundType.GRASS));
        ACACIA_LEAVES = register("acacia_leaves", leaves(SoundType.GRASS));
        DARK_OAK_LEAVES = register("dark_oak_leaves", leaves(SoundType.GRASS));
        AZALEA_LEAVES = register("azalea_leaves", leaves(SoundType.AZALEA_LEAVES));
        FLOWERING_AZALEA_LEAVES = register("flowering_azalea_leaves", leaves(SoundType.AZALEA_LEAVES));
        SPONGE = register("sponge", new SpongeBlock(of(Material.SPONGE).strength(0.6F).sound(SoundType.GRASS)));
        WET_SPONGE = register("wet_sponge", new WetSpongeBlock(of(Material.SPONGE).strength(0.6F).sound(SoundType.GRASS)));
        GLASS = register("glass", new GlassBlock(of(Material.GLASS)
                                                         .strength(0.3F)
                                                         .sound(SoundType.GLASS)
                                                         .noOcclusion()
                                                         .isValidSpawn_(BlockUtils.NEVER_SPAWN)
                                                         .isRedstoneConductor_(BlockUtils.NEVER)
                                                         .isSuffocating_(BlockUtils.NEVER)
                                                         .isViewBlocking_(BlockUtils.NEVER)));
        LAPIS_ORE = register("lapis_ore", new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F), UniformInt.of(2, 5)));
        DEEPSLATE_LAPIS_ORE = register("deepslate_lapis_ore", new OreBlock(BlockBehaviour.Properties.copy(LAPIS_ORE)
                                                                                                    .color(MaterialColor.DEEPSLATE)
                                                                                                    .strength(4.5F, 3.0F)
                                                                                                    .sound(SoundType.DEEPSLATE),
                                                                           UniformInt.of(2, 5)));
        LAPIS_BLOCK = register("lapis_block", new Block(of(Material.METAL, MaterialColor.LAPIS).requiresCorrectToolForDrops().strength(3.0F, 3.0F).lightLevel(s -> 0b1_1111_0_0000_0_0000)));
        DISPENSER = register("dispenser", new DispenserBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F)));
        SANDSTONE = register("sandstone", new Block(of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(0.8F)));
        CHISELED_SANDSTONE = register("chiseled_sandstone", new Block(of(Material.STONE, MaterialColor.SAND)
                                                                              .requiresCorrectToolForDrops()
                                                                              .strength(0.8F)));
        CUT_SANDSTONE = register("cut_sandstone", new Block(of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(0.8F)));
        NOTE_BLOCK = register("note_block", new NoteBlock(of(Material.WOOD).sound(SoundType.WOOD).strength(0.8F)));
        WHITE_BED = register("white_bed", bed(DyeColor.WHITE));
        ORANGE_BED = register("orange_bed", bed(DyeColor.ORANGE));
        MAGENTA_BED = register("magenta_bed", bed(DyeColor.MAGENTA));
        LIGHT_BLUE_BED = register("light_blue_bed", bed(DyeColor.LIGHT_BLUE));
        YELLOW_BED = register("yellow_bed", bed(DyeColor.YELLOW));
        LIME_BED = register("lime_bed", bed(DyeColor.LIME));
        PINK_BED = register("pink_bed", bed(DyeColor.PINK));
        GRAY_BED = register("gray_bed", bed(DyeColor.GRAY));
        LIGHT_GRAY_BED = register("light_gray_bed", bed(DyeColor.LIGHT_GRAY));
        CYAN_BED = register("cyan_bed", bed(DyeColor.CYAN));
        PURPLE_BED = register("purple_bed", bed(DyeColor.PURPLE));
        BLUE_BED = register("blue_bed", bed(DyeColor.BLUE));
        BROWN_BED = register("brown_bed", bed(DyeColor.BROWN));
        GREEN_BED = register("green_bed", bed(DyeColor.GREEN));
        RED_BED = register("red_bed", bed(DyeColor.RED));
        BLACK_BED = register("black_bed", bed(DyeColor.BLACK));
        POWERED_RAIL = register("powered_rail", new PoweredRailBlock(of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL)));
        DETECTOR_RAIL = register("detector_rail", new DetectorRailBlock(of(Material.DECORATION)
                                                                                .noCollission()
                                                                                .strength(0.7F)
                                                                                .sound(SoundType.METAL)));
        STICKY_PISTON = register("sticky_piston", pistonBase(true));
        COBWEB = register("cobweb", new WebBlock(of(Material.WEB).noCollission().requiresCorrectToolForDrops().strength(4.0F)));
        GRASS = register("grass", new TallGrassBlock(of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        FERN = register("fern", new TallGrassBlock(of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        DEAD_BUSH = register("dead_bush", new DeadBushBlock(of(Material.REPLACEABLE_PLANT, MaterialColor.WOOD)
                                                                    .noCollission()
                                                                    .instabreak()
                                                                    .sound(SoundType.GRASS)));
        SEAGRASS = register("seagrass", new SeagrassBlock(of(Material.REPLACEABLE_WATER_PLANT)
                                                                  .noCollission()
                                                                  .instabreak()
                                                                  .sound(SoundType.WET_GRASS)));
        TALL_SEAGRASS = register("tall_seagrass", new TallSeagrassBlock(of(Material.REPLACEABLE_WATER_PLANT)
                                                                                .noCollission()
                                                                                .instabreak()
                                                                                .sound(SoundType.WET_GRASS)));
        PISTON = register("piston", pistonBase(false));
        PISTON_HEAD = register("piston_head", new PistonHeadBlock(of(Material.PISTON).strength(1.5F).noDrops()));
        WHITE_WOOL = register("white_wool", new Block(of(Material.WOOL, MaterialColor.SNOW).strength(0.8F).sound(SoundType.WOOL)));
        ORANGE_WOOL = register("orange_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_ORANGE).strength(0.8F).sound(SoundType.WOOL)));
        MAGENTA_WOOL = register("magenta_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_MAGENTA).strength(0.8F).sound(SoundType.WOOL)));
        LIGHT_BLUE_WOOL = register("light_blue_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_LIGHT_BLUE)
                                                                        .strength(0.8F)
                                                                        .sound(SoundType.WOOL)));
        YELLOW_WOOL = register("yellow_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_YELLOW).strength(0.8F).sound(SoundType.WOOL)));
        LIME_WOOL = register("lime_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_LIGHT_GREEN).strength(0.8F).sound(SoundType.WOOL)));
        PINK_WOOL = register("pink_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_PINK).strength(0.8F).sound(SoundType.WOOL)));
        GRAY_WOOL = register("gray_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_GRAY).strength(0.8F).sound(SoundType.WOOL)));
        LIGHT_GRAY_WOOL = register("light_gray_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_LIGHT_GRAY)
                                                                        .strength(0.8F)
                                                                        .sound(SoundType.WOOL)));
        CYAN_WOOL = register("cyan_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_CYAN).strength(0.8F).sound(SoundType.WOOL)));
        PURPLE_WOOL = register("purple_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_PURPLE).strength(0.8F).sound(SoundType.WOOL)));
        BLUE_WOOL = register("blue_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_BLUE).strength(0.8F).sound(SoundType.WOOL)));
        BROWN_WOOL = register("brown_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_BROWN).strength(0.8F).sound(SoundType.WOOL)));
        GREEN_WOOL = register("green_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_GREEN).strength(0.8F).sound(SoundType.WOOL)));
        RED_WOOL = register("red_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_RED).strength(0.8F).sound(SoundType.WOOL)));
        BLACK_WOOL = register("black_wool", new Block(of(Material.WOOL, MaterialColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL)));
        MOVING_PISTON = register("moving_piston", new MovingPistonBlock(of(Material.PISTON)
                                                                                .strength(-1.0F)
                                                                                .dynamicShape()
                                                                                .noDrops()
                                                                                .noOcclusion()
                                                                                .isRedstoneConductor_(BlockUtils.NEVER)
                                                                                .isSuffocating_(BlockUtils.NEVER)
                                                                                .isViewBlocking_(BlockUtils.NEVER)));
        DANDELION = register("dandelion", new FlowerBlock(MobEffects.SATURATION, 7, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        POPPY = register("poppy", new FlowerBlock(MobEffects.NIGHT_VISION, 5, of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        BLUE_ORCHID = register("blue_orchid", new FlowerBlock(MobEffects.SATURATION, 7, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        ALLIUM = register("allium", new FlowerBlock(MobEffects.FIRE_RESISTANCE, 4, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        AZURE_BLUET = register("azure_bluet", new FlowerBlock(MobEffects.BLINDNESS, 8, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        RED_TULIP = register("red_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        ORANGE_TULIP = register("orange_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        WHITE_TULIP = register("white_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        PINK_TULIP = register("pink_tulip", new FlowerBlock(MobEffects.WEAKNESS, 9, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        OXEYE_DAISY = register("oxeye_daisy", new FlowerBlock(MobEffects.REGENERATION, 8, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        CORNFLOWER = register("cornflower", new FlowerBlock(MobEffects.JUMP, 6, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        WITHER_ROSE = register("wither_rose", new WitherRoseBlock(MobEffects.WITHER, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        LILY_OF_THE_VALLEY = register("lily_of_the_valley", new FlowerBlock(MobEffects.POISON, 12, of(Material.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)));
        BROWN_MUSHROOM = register("brown_mushroom", new MushroomBlock(of(Material.PLANT, MaterialColor.COLOR_BROWN)
                                                                              .noCollission()
                                                                              .randomTicks()
                                                                              .instabreak()
                                                                              .sound(SoundType.GRASS)
                                                                              .lightLevel(BlockUtils.LIGHT_1)
                                                                              .hasPostProcess_(BlockUtils.ALWAYS),
                                                                      Mixin_FS_Blocks::_hugeBrownMushroom));
        RED_MUSHROOM = register("red_mushroom", new MushroomBlock(of(Material.PLANT, MaterialColor.COLOR_RED)
                                                                          .noCollission()
                                                                          .randomTicks()
                                                                          .instabreak()
                                                                          .sound(SoundType.GRASS)
                                                                          .hasPostProcess_(BlockUtils.ALWAYS),
                                                                  Mixin_FS_Blocks::_hugeRedMushroom));
        GOLD_BLOCK = register("gold_block", new Block(of(Material.METAL, MaterialColor.GOLD)
                                                              .requiresCorrectToolForDrops()
                                                              .strength(3.0F, 6.0F)
                                                              .sound(SoundType.METAL)));
        IRON_BLOCK = register("iron_block", new Block(of(Material.METAL, MaterialColor.METAL)
                                                              .requiresCorrectToolForDrops()
                                                              .strength(5.0F, 6.0F)
                                                              .sound(SoundType.METAL).lightLevel(s -> 0b1_1111_1_1111_1_1111)));
        BRICKS = register("bricks", new Block(of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        TNT = register("tnt", new TntBlock(of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS)));
        BOOKSHELF = register("bookshelf", new Block(of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)));
        MOSSY_COBBLESTONE = register("mossy_cobblestone", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        OBSIDIAN = register("obsidian", new Block(of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                          .requiresCorrectToolForDrops()
                                                          .strength(50.0F, 1_200.0F)));
        TORCH = register("torch", new TorchBlock(of(Material.DECORATION)
                                                         .noCollission()
                                                         .instabreak()
                                                         .lightLevel(BlockUtils.LIGHT_14)
                                                         .sound(SoundType.WOOD), ParticleTypes.FLAME));
        WALL_TORCH = register("wall_torch", new WallTorchBlock(of(Material.DECORATION)
                                                                       .noCollission()
                                                                       .instabreak()
                                                                       .lightLevel(BlockUtils.LIGHT_14)
                                                                       .sound(SoundType.WOOD)
                                                                       .dropsLike(TORCH), ParticleTypes.FLAME));
        FIRE = register("fire", new FireBlock(of(Material.FIRE, MaterialColor.FIRE)
                                                      .noCollission()
                                                      .instabreak()
                                                      .lightLevel(BlockUtils.LIGHT_15)
                                                      .sound(SoundType.WOOL)
                                                      .noDrops()));
        SOUL_FIRE = register("soul_fire", new SoulFireBlock(of(Material.FIRE, MaterialColor.COLOR_LIGHT_BLUE)
                                                                    .noCollission()
                                                                    .instabreak()
                                                                    .lightLevel(BlockUtils.LIGHT_10)
                                                                    .sound(SoundType.WOOL)
                                                                    .noDrops()));
        SPAWNER = register("spawner", new SpawnerBlock(of(Material.STONE)
                                                               .requiresCorrectToolForDrops()
                                                               .strength(5.0F)
                                                               .sound(SoundType.METAL)
                                                               .noOcclusion()));
        OAK_STAIRS = register("oak_stairs", new StairBlock(OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(OAK_PLANKS)));
        CHEST = register("chest", new ChestBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD), Mixin_FS_Blocks::_chest));
        REDSTONE_WIRE = register("redstone_wire", new RedStoneWireBlock(of(Material.DECORATION).noCollission().instabreak()));
        DIAMOND_ORE = register("diamond_ore", new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F),
                                                           UniformInt.of(3, 7)));
        DEEPSLATE_DIAMOND_ORE = register("deepslate_diamond_ore", new OreBlock(BlockBehaviour.Properties.copy(DIAMOND_ORE)
                                                                                                        .color(MaterialColor.DEEPSLATE)
                                                                                                        .strength(4.5F, 3.0F)
                                                                                                        .sound(SoundType.DEEPSLATE),
                                                                               UniformInt.of(3, 7)));
        DIAMOND_BLOCK = register("diamond_block", new Block(of(Material.METAL, MaterialColor.DIAMOND)
                                                                    .requiresCorrectToolForDrops()
                                                                    .strength(5.0F, 6.0F)
                                                                    .sound(SoundType.METAL)));
        CRAFTING_TABLE = register("crafting_table", new CraftingTableBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        WHEAT = register("wheat", new CropBlock(of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
        FARMLAND = register("farmland", new FarmBlock(of(Material.DIRT)
                                                              .randomTicks()
                                                              .strength(0.6F)
                                                              .sound(SoundType.GRAVEL)
                                                              .isViewBlocking_(BlockUtils.ALWAYS)
                                                              .isSuffocating_(BlockUtils.ALWAYS)));
        FURNACE = register("furnace", new FurnaceBlock(of(Material.STONE)
                                                               .requiresCorrectToolForDrops()
                                                               .strength(3.5F)
                                                               .lightLevel(litBlockEmission(0xDD))));
        OAK_SIGN = register("oak_sign",
                            new StandingSignBlock(of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD), WoodType.OAK));
        SPRUCE_SIGN = register("spruce_sign", new StandingSignBlock(of(Material.WOOD, SPRUCE_LOG.defaultMaterialColor())
                                                                            .noCollission()
                                                                            .strength(1.0F)
                                                                            .sound(SoundType.WOOD), WoodType.SPRUCE));
        BIRCH_SIGN = register("birch_sign", new StandingSignBlock(of(Material.WOOD, MaterialColor.SAND)
                                                                          .noCollission()
                                                                          .strength(1.0F)
                                                                          .sound(SoundType.WOOD), WoodType.BIRCH));
        ACACIA_SIGN = register("acacia_sign", new StandingSignBlock(of(Material.WOOD, MaterialColor.COLOR_ORANGE)
                                                                            .noCollission()
                                                                            .strength(1.0F)
                                                                            .sound(SoundType.WOOD), WoodType.ACACIA));
        JUNGLE_SIGN = register("jungle_sign", new StandingSignBlock(of(Material.WOOD, JUNGLE_LOG.defaultMaterialColor())
                                                                            .noCollission()
                                                                            .strength(1.0F)
                                                                            .sound(SoundType.WOOD), WoodType.JUNGLE));
        DARK_OAK_SIGN = register("dark_oak_sign", new StandingSignBlock(of(Material.WOOD, DARK_OAK_LOG.defaultMaterialColor())
                                                                                .noCollission()
                                                                                .strength(1.0F)
                                                                                .sound(SoundType.WOOD), WoodType.DARK_OAK));
        OAK_DOOR = register("oak_door", new DoorBlock(of(Material.WOOD, OAK_PLANKS.defaultMaterialColor())
                                                              .strength(3.0F)
                                                              .sound(SoundType.WOOD)
                                                              .noOcclusion()));
        LADDER = register("ladder", new LadderBlock(of(Material.DECORATION).strength(0.4F).sound(SoundType.LADDER).noOcclusion()));
        RAIL = register("rail", new RailBlock(of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL)));
        COBBLESTONE_STAIRS = register("cobblestone_stairs",
                                      new StairBlock(COBBLESTONE.defaultBlockState(), BlockBehaviour.Properties.copy(COBBLESTONE)));
        OAK_WALL_SIGN = register("oak_wall_sign", new WallSignBlock(of(Material.WOOD)
                                                                            .noCollission()
                                                                            .strength(1.0F)
                                                                            .sound(SoundType.WOOD).dropsLike(OAK_SIGN), WoodType.OAK));
        SPRUCE_WALL_SIGN = register("spruce_wall_sign", new WallSignBlock(of(Material.WOOD, SPRUCE_LOG.defaultMaterialColor())
                                                                                  .noCollission()
                                                                                  .strength(1.0F)
                                                                                  .sound(SoundType.WOOD)
                                                                                  .dropsLike(SPRUCE_SIGN), WoodType.SPRUCE));
        BIRCH_WALL_SIGN = register("birch_wall_sign", new WallSignBlock(of(Material.WOOD, MaterialColor.SAND)
                                                                                .noCollission()
                                                                                .strength(1.0F)
                                                                                .sound(SoundType.WOOD)
                                                                                .dropsLike(BIRCH_SIGN), WoodType.BIRCH));
        ACACIA_WALL_SIGN = register("acacia_wall_sign", new WallSignBlock(of(Material.WOOD, MaterialColor.COLOR_ORANGE)
                                                                                  .noCollission()
                                                                                  .strength(1.0F)
                                                                                  .sound(SoundType.WOOD)
                                                                                  .dropsLike(ACACIA_SIGN), WoodType.ACACIA));
        JUNGLE_WALL_SIGN = register("jungle_wall_sign", new WallSignBlock(of(Material.WOOD, JUNGLE_LOG.defaultMaterialColor())
                                                                                  .noCollission()
                                                                                  .strength(1.0F)
                                                                                  .sound(SoundType.WOOD)
                                                                                  .dropsLike(JUNGLE_SIGN), WoodType.JUNGLE));
        DARK_OAK_WALL_SIGN = register("dark_oak_wall_sign", new WallSignBlock(of(Material.WOOD, DARK_OAK_LOG.defaultMaterialColor())
                                                                                      .noCollission()
                                                                                      .strength(1.0F)
                                                                                      .sound(SoundType.WOOD)
                                                                                      .dropsLike(DARK_OAK_SIGN), WoodType.DARK_OAK));
        LEVER = register("lever", new LeverBlock(of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        STONE_PRESSURE_PLATE = register("stone_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.MOBS, of(Material.STONE)
                .requiresCorrectToolForDrops()
                .noCollission()
                .strength(0.5F)));
        IRON_DOOR = register("iron_door", new DoorBlock(of(Material.METAL, MaterialColor.METAL)
                                                                .requiresCorrectToolForDrops()
                                                                .strength(5.0F)
                                                                .sound(SoundType.METAL)
                                                                .noOcclusion()));
        OAK_PRESSURE_PLATE = register("oak_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                   of(Material.WOOD, OAK_PLANKS.defaultMaterialColor())
                                                                                           .noCollission()
                                                                                           .strength(0.5F)
                                                                                           .sound(SoundType.WOOD)));
        SPRUCE_PRESSURE_PLATE = register("spruce_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                         of(Material.WOOD, SPRUCE_PLANKS.defaultMaterialColor())
                                                                                                 .noCollission()
                                                                                                 .strength(0.5F)
                                                                                                 .sound(SoundType.WOOD)));
        BIRCH_PRESSURE_PLATE = register("birch_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                       of(Material.WOOD, BIRCH_PLANKS.defaultMaterialColor())
                                                                                               .noCollission()
                                                                                               .strength(0.5F)
                                                                                               .sound(SoundType.WOOD)));
        JUNGLE_PRESSURE_PLATE = register("jungle_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                         of(Material.WOOD, JUNGLE_PLANKS.defaultMaterialColor())
                                                                                                 .noCollission()
                                                                                                 .strength(0.5F)
                                                                                                 .sound(SoundType.WOOD)));
        ACACIA_PRESSURE_PLATE = register("acacia_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                         of(Material.WOOD, ACACIA_PLANKS.defaultMaterialColor())
                                                                                                 .noCollission()
                                                                                                 .strength(0.5F)
                                                                                                 .sound(SoundType.WOOD)));
        DARK_OAK_PRESSURE_PLATE = register("dark_oak_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                             of(Material.WOOD, DARK_OAK_PLANKS.defaultMaterialColor())
                                                                                                     .noCollission()
                                                                                                     .strength(0.5F)
                                                                                                     .sound(SoundType.WOOD)));
        REDSTONE_ORE = register("redstone_ore", new RedStoneOreBlock(of(Material.STONE)
                                                                             .requiresCorrectToolForDrops()
                                                                             .randomTicks()
                                                                             .lightLevel(litBlockEmission(0x009))
                                                                             .strength(3.0F, 3.0F)));
        DEEPSLATE_REDSTONE_ORE = register("deepslate_redstone_ore", new RedStoneOreBlock(BlockBehaviour.Properties.copy(REDSTONE_ORE)
                                                                                                                  .color(MaterialColor.DEEPSLATE)
                                                                                                                  .strength(4.5F, 3.0F)
                                                                                                                  .sound(SoundType.DEEPSLATE)));
        REDSTONE_TORCH = register("redstone_torch", new RedstoneTorchBlock(of(Material.DECORATION)
                                                                                   .noCollission()
                                                                                   .instabreak()
                                                                                   .lightLevel(litBlockEmission(0x007))
                                                                                   .sound(SoundType.WOOD)));
        REDSTONE_WALL_TORCH = register("redstone_wall_torch", new RedstoneWallTorchBlock(of(Material.DECORATION)
                                                                                                 .noCollission()
                                                                                                 .instabreak()
                                                                                                 .lightLevel(litBlockEmission(0x007))
                                                                                                 .sound(SoundType.WOOD)
                                                                                                 .dropsLike(REDSTONE_TORCH)));
        STONE_BUTTON = register("stone_button", new StoneButtonBlock(of(Material.DECORATION).noCollission().strength(0.5F)));
        SNOW = register("snow", new SnowLayerBlock(of(Material.TOP_SNOW)
                                                           .randomTicks()
                                                           .strength(0.1F)
                                                           .requiresCorrectToolForDrops()
                                                           .sound(SoundType.SNOW)
                                                           .isViewBlocking_(Mixin_FS_Blocks::_snow)));
        ICE = register("ice", new IceBlock(of(Material.ICE)
                                                   .friction(0.98F)
                                                   .randomTicks()
                                                   .strength(0.5F)
                                                   .sound(SoundType.GLASS)
                                                   .noOcclusion()
                                                   .isValidSpawn_(Mixin_FS_Blocks::_polarBear)));
        SNOW_BLOCK = register("snow_block", new Block(of(Material.SNOW).requiresCorrectToolForDrops().strength(0.2F).sound(SoundType.SNOW)));
        CACTUS = register("cactus", new CactusBlock(of(Material.CACTUS).randomTicks().strength(0.4F).sound(SoundType.WOOL)));
        CLAY = register("clay", new Block(of(Material.CLAY).strength(0.6F).sound(SoundType.GRAVEL)));
        SUGAR_CANE = register("sugar_cane", new SugarCaneBlock(of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
        JUKEBOX = register("jukebox", new JukeboxBlock(of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F)));
        OAK_FENCE = register("oak_fence", new FenceBlock(of(Material.WOOD, OAK_PLANKS.defaultMaterialColor())
                                                                 .strength(2.0F, 3.0F)
                                                                 .sound(SoundType.WOOD)));
        PUMPKIN = register("pumpkin", new PumpkinBlock(of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD)));
        NETHERRACK = register("netherrack", new NetherrackBlock(of(Material.STONE, MaterialColor.NETHER)
                                                                        .requiresCorrectToolForDrops()
                                                                        .strength(0.4F)
                                                                        .sound(SoundType.NETHERRACK).lightLevel(s -> 0b1_1111)));
        SOUL_SAND = register("soul_sand", new SoulSandBlock(of(Material.SAND, MaterialColor.COLOR_BROWN)
                                                                    .strength(0.5F)
                                                                    .speedFactor(0.4F)
                                                                    .sound(SoundType.SOUL_SAND)
                                                                    .isValidSpawn_(BlockUtils.ALWAYS_SPAWN)
                                                                    .isRedstoneConductor_(BlockUtils.ALWAYS)
                                                                    .isViewBlocking_(BlockUtils.ALWAYS)
                                                                    .isSuffocating_(BlockUtils.ALWAYS)));
        SOUL_SOIL = register("soul_soil", new Block(of(Material.DIRT, MaterialColor.COLOR_BROWN).strength(0.5F).sound(SoundType.SOUL_SOIL)));
        BASALT = register("basalt", new RotatedPillarBlock(of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                   .requiresCorrectToolForDrops()
                                                                   .strength(1.25F, 4.2F)
                                                                   .sound(SoundType.BASALT)));
        POLISHED_BASALT = register("polished_basalt", new RotatedPillarBlock(of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                                     .requiresCorrectToolForDrops()
                                                                                     .strength(1.25F, 4.2F)
                                                                                     .sound(SoundType.BASALT)));
        SOUL_TORCH = register("soul_torch", new TorchBlock(of(Material.DECORATION)
                                                                   .noCollission()
                                                                   .instabreak()
                                                                   .lightLevel(BlockUtils.LIGHT_10)
                                                                   .sound(SoundType.WOOD), ParticleTypes.SOUL_FIRE_FLAME));
        SOUL_WALL_TORCH = register("soul_wall_torch", new WallTorchBlock(of(Material.DECORATION)
                                                                                 .noCollission()
                                                                                 .instabreak()
                                                                                 .lightLevel(BlockUtils.LIGHT_10)
                                                                                 .sound(SoundType.WOOD)
                                                                                 .dropsLike(SOUL_TORCH), ParticleTypes.SOUL_FIRE_FLAME));
        GLOWSTONE = register("glowstone", new Block(of(Material.GLASS, MaterialColor.SAND)
                                                            .strength(0.3F)
                                                            .sound(SoundType.GLASS)
                                                            .lightLevel(BlockUtils.LIGHT_YELLOW_15)));
        NETHER_PORTAL = register("nether_portal", new NetherPortalBlock(of(Material.PORTAL)
                                                                                .noCollission()
                                                                                .randomTicks()
                                                                                .strength(-1.0F)
                                                                                .sound(SoundType.GLASS)
                                                                                .lightLevel(BlockUtils.LIGHT_11)));
        CARVED_PUMPKIN = register("carved_pumpkin", new CarvedPumpkinBlock(of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE)
                                                                                   .strength(1.0F)
                                                                                   .sound(SoundType.WOOD)
                                                                                   .isValidSpawn_(BlockUtils.ALWAYS_SPAWN)));
        JACK_O_LANTERN = register("jack_o_lantern", new CarvedPumpkinBlock(of(Material.VEGETABLE, MaterialColor.COLOR_ORANGE)
                                                                                   .strength(1.0F)
                                                                                   .sound(SoundType.WOOD)
                                                                                   .lightLevel(BlockUtils.LIGHT_15)
                                                                                   .isValidSpawn_(BlockUtils.ALWAYS_SPAWN)));
        CAKE = register("cake", new CakeBlock(of(Material.CAKE).strength(0.5F).sound(SoundType.WOOL)));
        REPEATER = register("repeater", new RepeaterBlock(of(Material.DECORATION).instabreak().sound(SoundType.WOOD)));
        WHITE_STAINED_GLASS = register("white_stained_glass", stainedGlass(DyeColor.WHITE));
        ORANGE_STAINED_GLASS = register("orange_stained_glass", stainedGlass(DyeColor.ORANGE));
        MAGENTA_STAINED_GLASS = register("magenta_stained_glass", stainedGlass(DyeColor.MAGENTA));
        LIGHT_BLUE_STAINED_GLASS = register("light_blue_stained_glass", stainedGlass(DyeColor.LIGHT_BLUE));
        YELLOW_STAINED_GLASS = register("yellow_stained_glass", stainedGlass(DyeColor.YELLOW));
        LIME_STAINED_GLASS = register("lime_stained_glass", stainedGlass(DyeColor.LIME));
        PINK_STAINED_GLASS = register("pink_stained_glass", stainedGlass(DyeColor.PINK));
        GRAY_STAINED_GLASS = register("gray_stained_glass", stainedGlass(DyeColor.GRAY));
        LIGHT_GRAY_STAINED_GLASS = register("light_gray_stained_glass", stainedGlass(DyeColor.LIGHT_GRAY));
        CYAN_STAINED_GLASS = register("cyan_stained_glass", stainedGlass(DyeColor.CYAN));
        PURPLE_STAINED_GLASS = register("purple_stained_glass", stainedGlass(DyeColor.PURPLE));
        BLUE_STAINED_GLASS = register("blue_stained_glass", stainedGlass(DyeColor.BLUE));
        BROWN_STAINED_GLASS = register("brown_stained_glass", stainedGlass(DyeColor.BROWN));
        GREEN_STAINED_GLASS = register("green_stained_glass", stainedGlass(DyeColor.GREEN));
        RED_STAINED_GLASS = register("red_stained_glass", stainedGlass(DyeColor.RED));
        BLACK_STAINED_GLASS = register("black_stained_glass", stainedGlass(DyeColor.BLACK));
        OAK_TRAPDOOR = register("oak_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.WOOD)
                                                                          .strength(3.0F)
                                                                          .sound(SoundType.WOOD)
                                                                          .noOcclusion()
                                                                          .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        SPRUCE_TRAPDOOR = register("spruce_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.PODZOL)
                                                                                .strength(3.0F)
                                                                                .sound(SoundType.WOOD)
                                                                                .noOcclusion()
                                                                                .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        BIRCH_TRAPDOOR = register("birch_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.SAND)
                                                                              .strength(3.0F)
                                                                              .sound(SoundType.WOOD)
                                                                              .noOcclusion()
                                                                              .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        JUNGLE_TRAPDOOR = register("jungle_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.DIRT)
                                                                                .strength(3.0F)
                                                                                .sound(SoundType.WOOD)
                                                                                .noOcclusion()
                                                                                .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        ACACIA_TRAPDOOR = register("acacia_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.COLOR_ORANGE)
                                                                                .strength(3.0F)
                                                                                .sound(SoundType.WOOD)
                                                                                .noOcclusion()
                                                                                .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        DARK_OAK_TRAPDOOR = register("dark_oak_trapdoor", new TrapDoorBlock(of(Material.WOOD, MaterialColor.COLOR_BROWN)
                                                                                    .strength(3.0F)
                                                                                    .sound(SoundType.WOOD)
                                                                                    .noOcclusion()
                                                                                    .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        STONE_BRICKS = register("stone_bricks", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        MOSSY_STONE_BRICKS = register("mossy_stone_bricks", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        CRACKED_STONE_BRICKS = register("cracked_stone_bricks", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        CHISELED_STONE_BRICKS = register("chiseled_stone_bricks", new Block(of(Material.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        INFESTED_STONE = register("infested_stone", new InfestedBlock(STONE, of(Material.CLAY)));
        INFESTED_COBBLESTONE = register("infested_cobblestone", new InfestedBlock(COBBLESTONE, of(Material.CLAY)));
        INFESTED_STONE_BRICKS = register("infested_stone_bricks", new InfestedBlock(STONE_BRICKS, of(Material.CLAY)));
        INFESTED_MOSSY_STONE_BRICKS = register("infested_mossy_stone_bricks", new InfestedBlock(MOSSY_STONE_BRICKS, of(Material.CLAY)));
        INFESTED_CRACKED_STONE_BRICKS = register("infested_cracked_stone_bricks", new InfestedBlock(CRACKED_STONE_BRICKS, of(Material.CLAY)));
        INFESTED_CHISELED_STONE_BRICKS = register("infested_chiseled_stone_bricks", new InfestedBlock(CHISELED_STONE_BRICKS, of(Material.CLAY)));
        BROWN_MUSHROOM_BLOCK = register("brown_mushroom_block", new HugeMushroomBlock(of(Material.WOOD, MaterialColor.DIRT)
                                                                                              .strength(0.2F)
                                                                                              .sound(SoundType.WOOD)));
        RED_MUSHROOM_BLOCK = register("red_mushroom_block", new HugeMushroomBlock(of(Material.WOOD, MaterialColor.COLOR_RED)
                                                                                          .strength(0.2F)
                                                                                          .sound(SoundType.WOOD)));
        MUSHROOM_STEM = register("mushroom_stem", new HugeMushroomBlock(of(Material.WOOD, MaterialColor.WOOL).strength(0.2F).sound(SoundType.WOOD)));
        IRON_BARS = register("iron_bars", new IronBarsBlock(of(Material.METAL, MaterialColor.NONE)
                                                                    .requiresCorrectToolForDrops()
                                                                    .strength(5.0F, 6.0F)
                                                                    .sound(SoundType.METAL)
                                                                    .noOcclusion()));
        CHAIN = register("chain", new ChainBlock(of(Material.METAL, MaterialColor.NONE)
                                                         .requiresCorrectToolForDrops()
                                                         .strength(5.0F, 6.0F)
                                                         .sound(SoundType.CHAIN)
                                                         .noOcclusion()));
        GLASS_PANE = register("glass_pane", new IronBarsBlock(of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion()));
        MELON = register("melon", new MelonBlock(of(Material.VEGETABLE, MaterialColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.WOOD)));
        ATTACHED_PUMPKIN_STEM = register("attached_pumpkin_stem", new AttachedStemBlock((StemGrownBlock) PUMPKIN, Mixin_FS_Blocks::_pumpkinSeeds,
                                                                                        of(Material.PLANT).noCollission()
                                                                                                          .instabreak()
                                                                                                          .sound(SoundType.WOOD)));
        ATTACHED_MELON_STEM = register("attached_melon_stem", new AttachedStemBlock((StemGrownBlock) MELON, Mixin_FS_Blocks::_melonSeeds,
                                                                                    of(Material.PLANT).noCollission()
                                                                                                      .instabreak()
                                                                                                      .sound(SoundType.WOOD)));
        PUMPKIN_STEM = register("pumpkin_stem", new StemBlock((StemGrownBlock) PUMPKIN, Mixin_FS_Blocks::_pumpkinSeeds,
                                                              of(Material.PLANT).noCollission()
                                                                                .randomTicks()
                                                                                .instabreak()
                                                                                .sound(SoundType.HARD_CROP)));
        MELON_STEM = register("melon_stem", new StemBlock((StemGrownBlock) MELON, Mixin_FS_Blocks::_melonSeeds,
                                                          of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.HARD_CROP)));
        VINE = register("vine", new VineBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().randomTicks().strength(0.2F).sound(SoundType.VINE)));
        GLOW_LICHEN = register("glow_lichen", new GlowLichenBlock(of(Material.REPLACEABLE_PLANT, MaterialColor.GLOW_LICHEN)
                                                                          .noCollission()
                                                                          .strength(0.2F)
                                                                          .sound(SoundType.GLOW_LICHEN)
                                                                          .lightLevel(GlowLichenBlock.emission(7))));
        OAK_FENCE_GATE = register("oak_fence_gate", new FenceGateBlock(of(Material.WOOD, OAK_PLANKS.defaultMaterialColor())
                                                                               .strength(2.0F, 3.0F)
                                                                               .sound(SoundType.WOOD)));
        BRICK_STAIRS = register("brick_stairs", new StairBlock(BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(BRICKS)));
        STONE_BRICK_STAIRS = register("stone_brick_stairs",
                                      new StairBlock(STONE_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(STONE_BRICKS)));
        MYCELIUM = register("mycelium", new MyceliumBlock(
                of(Material.GRASS, MaterialColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundType.GRASS)));
        LILY_PAD = register("lily_pad",
                            new WaterlilyBlock(of(Material.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));
        NETHER_BRICKS = register("nether_bricks", new Block(of(Material.STONE, MaterialColor.NETHER)
                                                                    .requiresCorrectToolForDrops()
                                                                    .strength(2.0F, 6.0F)
                                                                    .sound(SoundType.NETHER_BRICKS)));
        NETHER_BRICK_FENCE = register("nether_brick_fence", new FenceBlock(of(Material.STONE, MaterialColor.NETHER)
                                                                                   .requiresCorrectToolForDrops()
                                                                                   .strength(2.0F, 6.0F)
                                                                                   .sound(SoundType.NETHER_BRICKS)));
        NETHER_BRICK_STAIRS = register("nether_brick_stairs",
                                       new StairBlock(NETHER_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(NETHER_BRICKS)));
        NETHER_WART = register("nether_wart", new NetherWartBlock(
                of(Material.PLANT, MaterialColor.COLOR_RED).noCollission().randomTicks().sound(SoundType.NETHER_WART)));
        ENCHANTING_TABLE = register("enchanting_table",
                                    new EnchantmentTableBlock(of(Material.STONE, MaterialColor.COLOR_RED)
                                                                      .requiresCorrectToolForDrops()
                                                                      .lightLevel(BlockUtils.LIGHT_7)
                                                                      .strength(5.0F, 1_200.0F)));
        BREWING_STAND = register("brewing_stand", new BrewingStandBlock(of(Material.METAL)
                                                                                .requiresCorrectToolForDrops()
                                                                                .strength(0.5F)
                                                                                .lightLevel(BlockUtils.LIGHT_1)
                                                                                .noOcclusion()));
        CAULDRON = register("cauldron", new CauldronBlock(
                of(Material.METAL, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion()));
        WATER_CAULDRON = register("water_cauldron", new LayeredCauldronBlock(BlockBehaviour.Properties.copy(CAULDRON), LayeredCauldronBlock.RAIN,
                                                                             CauldronInteraction.WATER));
        LAVA_CAULDRON = register("lava_cauldron", new LavaCauldronBlock(BlockBehaviour.Properties.copy(CAULDRON).lightLevel(BlockUtils.LIGHT_15)));
        POWDER_SNOW_CAULDRON = register("powder_snow_cauldron",
                                        new PowderSnowCauldronBlock(BlockBehaviour.Properties.copy(CAULDRON), LayeredCauldronBlock.SNOW,
                                                                    CauldronInteraction.POWDER_SNOW));
        END_PORTAL = register("end_portal", new EndPortalBlock(
                of(Material.PORTAL, MaterialColor.COLOR_BLACK)
                        .noCollission()
                        .lightLevel(BlockUtils.LIGHT_15)
                        .strength(-1.0F, 3_600_000.0F)
                        .noDrops()));
        END_PORTAL_FRAME = register("end_portal_frame", new EndPortalFrameBlock(
                of(Material.STONE, MaterialColor.COLOR_GREEN)
                        .sound(SoundType.GLASS)
                        .lightLevel(BlockUtils.LIGHT_1)
                        .strength(-1.0F, 3_600_000.0F)
                        .noDrops()));
        END_STONE = register("end_stone", new Block(
                of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(3.0F, 9.0F)));
        DRAGON_EGG = register("dragon_egg", new DragonEggBlock(of(Material.EGG, MaterialColor.COLOR_BLACK)
                                                                       .strength(3.0F, 9.0F)
                                                                       .lightLevel(BlockUtils.LIGHT_1)
                                                                       .noOcclusion()));
        REDSTONE_LAMP = register("redstone_lamp", new RedstoneLampBlock(of(Material.BUILDABLE_GLASS)
                                                                                .lightLevel(litBlockEmission(0xFF))
                                                                                .strength(0.3F)
                                                                                .sound(SoundType.GLASS)
                                                                                .isValidSpawn_(BlockUtils.ALWAYS_SPAWN)));
        COCOA = register("cocoa", new CocoaBlock(
                of(Material.PLANT).randomTicks().strength(0.2F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
        SANDSTONE_STAIRS = register("sandstone_stairs", new StairBlock(SANDSTONE.defaultBlockState(), BlockBehaviour.Properties.copy(SANDSTONE)));
        EMERALD_ORE = register("emerald_ore",
                               new OreBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F),
                                            UniformInt.of(3, 7)));
        DEEPSLATE_EMERALD_ORE = register("deepslate_emerald_ore", new OreBlock(
                BlockBehaviour.Properties.copy(EMERALD_ORE).color(MaterialColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE),
                UniformInt.of(3, 7)));
        ENDER_CHEST = register("ender_chest", new EnderChestBlock(
                of(Material.STONE).requiresCorrectToolForDrops().strength(22.5F, 600.0F).lightLevel(BlockUtils.LIGHT_7)));
        TRIPWIRE_HOOK = register("tripwire_hook", new TripWireHookBlock(of(Material.DECORATION).noCollission()));
        TRIPWIRE = register("tripwire",
                            new TripWireBlock((TripWireHookBlock) TRIPWIRE_HOOK, of(Material.DECORATION).noCollission()));
        EMERALD_BLOCK = register("emerald_block", new Block(of(Material.METAL, MaterialColor.EMERALD)
                                                                    .requiresCorrectToolForDrops()
                                                                    .strength(5.0F, 6.0F)
                                                                    .sound(SoundType.METAL).lightLevel(s -> 0b1_1111_0_0000)));
        SPRUCE_STAIRS = register("spruce_stairs", new StairBlock(SPRUCE_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(SPRUCE_PLANKS)));
        BIRCH_STAIRS = register("birch_stairs", new StairBlock(BIRCH_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(BIRCH_PLANKS)));
        JUNGLE_STAIRS = register("jungle_stairs", new StairBlock(JUNGLE_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(JUNGLE_PLANKS)));
        COMMAND_BLOCK = register("command_block", new CommandBlock(of(Material.METAL, MaterialColor.COLOR_BROWN)
                                                                           .requiresCorrectToolForDrops()
                                                                           .strength(-1.0F, 3_600_000.0F)
                                                                           .noDrops(), false));
        BEACON = register("beacon", new BeaconBlock(of(Material.GLASS, MaterialColor.DIAMOND)
                                                            .strength(3.0F)
                                                            .lightLevel(BlockUtils.LIGHT_15)
                                                            .noOcclusion()
                                                            .isRedstoneConductor_(BlockUtils.NEVER)));
        COBBLESTONE_WALL = register("cobblestone_wall", new WallBlock(BlockBehaviour.Properties.copy(COBBLESTONE)));
        MOSSY_COBBLESTONE_WALL = register("mossy_cobblestone_wall", new WallBlock(BlockBehaviour.Properties.copy(COBBLESTONE)));
        FLOWER_POT = register("flower_pot", new FlowerPotBlock(AIR, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_OAK_SAPLING = register("potted_oak_sapling",
                                      new FlowerPotBlock(OAK_SAPLING, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_SPRUCE_SAPLING = register("potted_spruce_sapling", new FlowerPotBlock(SPRUCE_SAPLING, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_BIRCH_SAPLING = register("potted_birch_sapling", new FlowerPotBlock(BIRCH_SAPLING, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_JUNGLE_SAPLING = register("potted_jungle_sapling", new FlowerPotBlock(JUNGLE_SAPLING, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_ACACIA_SAPLING = register("potted_acacia_sapling", new FlowerPotBlock(ACACIA_SAPLING, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_DARK_OAK_SAPLING = register("potted_dark_oak_sapling", new FlowerPotBlock(DARK_OAK_SAPLING,
                                                                                         of(Material.DECORATION)
                                                                                                 .instabreak()
                                                                                                 .noOcclusion()));
        POTTED_FERN = register("potted_fern", new FlowerPotBlock(FERN, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_DANDELION = register("potted_dandelion",
                                    new FlowerPotBlock(DANDELION, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_POPPY = register("potted_poppy",
                                new FlowerPotBlock(POPPY, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_BLUE_ORCHID = register("potted_blue_orchid",
                                      new FlowerPotBlock(BLUE_ORCHID, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_ALLIUM = register("potted_allium",
                                 new FlowerPotBlock(ALLIUM, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_AZURE_BLUET = register("potted_azure_bluet",
                                      new FlowerPotBlock(AZURE_BLUET, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_RED_TULIP = register("potted_red_tulip",
                                    new FlowerPotBlock(RED_TULIP, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_ORANGE_TULIP = register("potted_orange_tulip", new FlowerPotBlock(ORANGE_TULIP, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_WHITE_TULIP = register("potted_white_tulip",
                                      new FlowerPotBlock(WHITE_TULIP, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_PINK_TULIP = register("potted_pink_tulip",
                                     new FlowerPotBlock(PINK_TULIP, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_OXEYE_DAISY = register("potted_oxeye_daisy",
                                      new FlowerPotBlock(OXEYE_DAISY, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CORNFLOWER = register("potted_cornflower",
                                     new FlowerPotBlock(CORNFLOWER, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_LILY_OF_THE_VALLEY = register("potted_lily_of_the_valley", new FlowerPotBlock(LILY_OF_THE_VALLEY,
                                                                                             of(Material.DECORATION)
                                                                                                     .instabreak()
                                                                                                     .noOcclusion()));
        POTTED_WITHER_ROSE = register("potted_wither_rose",
                                      new FlowerPotBlock(WITHER_ROSE, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_RED_MUSHROOM = register("potted_red_mushroom", new FlowerPotBlock(RED_MUSHROOM, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_BROWN_MUSHROOM = register("potted_brown_mushroom", new FlowerPotBlock(BROWN_MUSHROOM, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_DEAD_BUSH = register("potted_dead_bush",
                                    new FlowerPotBlock(DEAD_BUSH, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_CACTUS = register("potted_cactus",
                                 new FlowerPotBlock(CACTUS, of(Material.DECORATION).instabreak().noOcclusion()));
        CARROTS = register("carrots", new CarrotBlock(
                of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
        POTATOES = register("potatoes", new PotatoBlock(
                of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
        OAK_BUTTON = register("oak_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        SPRUCE_BUTTON = register("spruce_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        BIRCH_BUTTON = register("birch_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        JUNGLE_BUTTON = register("jungle_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        ACACIA_BUTTON = register("acacia_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        DARK_OAK_BUTTON = register("dark_oak_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        SKELETON_SKULL = register("skeleton_skull",
                                  new SkullBlock(SkullBlock.Types.SKELETON, of(Material.DECORATION).strength(1.0F)));
        SKELETON_WALL_SKULL = register("skeleton_wall_skull", new WallSkullBlock(SkullBlock.Types.SKELETON,
                                                                                 of(Material.DECORATION)
                                                                                         .strength(1.0F)
                                                                                         .dropsLike(SKELETON_SKULL)));
        WITHER_SKELETON_SKULL = register("wither_skeleton_skull",
                                         new WitherSkullBlock(of(Material.DECORATION).strength(1.0F)));
        WITHER_SKELETON_WALL_SKULL = register("wither_skeleton_wall_skull", new WitherWallSkullBlock(
                of(Material.DECORATION).strength(1.0F).dropsLike(WITHER_SKELETON_SKULL)));
        ZOMBIE_HEAD = register("zombie_head",
                               new SkullBlock(SkullBlock.Types.ZOMBIE, of(Material.DECORATION).strength(1.0F)));
        ZOMBIE_WALL_HEAD = register("zombie_wall_head", new WallSkullBlock(SkullBlock.Types.ZOMBIE, of(Material.DECORATION)
                .strength(1.0F)
                .dropsLike(
                        ZOMBIE_HEAD)));
        PLAYER_HEAD = register("player_head", new PlayerHeadBlock(of(Material.DECORATION).strength(1.0F)));
        PLAYER_WALL_HEAD = register("player_wall_head",
                                    new PlayerWallHeadBlock(of(Material.DECORATION).strength(1.0F).dropsLike(PLAYER_HEAD)));
        CREEPER_HEAD = register("creeper_head",
                                new SkullBlock(SkullBlock.Types.CREEPER, of(Material.DECORATION).strength(1.0F)));
        CREEPER_WALL_HEAD = register("creeper_wall_head", new WallSkullBlock(SkullBlock.Types.CREEPER,
                                                                             of(Material.DECORATION)
                                                                                     .strength(1.0F)
                                                                                     .dropsLike(CREEPER_HEAD)));
        DRAGON_HEAD = register("dragon_head",
                               new SkullBlock(SkullBlock.Types.DRAGON, of(Material.DECORATION).strength(1.0F)));
        DRAGON_WALL_HEAD = register("dragon_wall_head", new WallSkullBlock(SkullBlock.Types.DRAGON, of(Material.DECORATION)
                .strength(1.0F)
                .dropsLike(
                        DRAGON_HEAD)));
        ANVIL = register("anvil", new AnvilBlock(of(Material.HEAVY_METAL, MaterialColor.METAL)
                                                         .requiresCorrectToolForDrops()
                                                         .strength(5.0F, 1_200.0F)
                                                         .sound(SoundType.ANVIL)));
        CHIPPED_ANVIL = register("chipped_anvil", new AnvilBlock(of(Material.HEAVY_METAL, MaterialColor.METAL)
                                                                         .requiresCorrectToolForDrops()
                                                                         .strength(5.0F, 1_200.0F)
                                                                         .sound(SoundType.ANVIL)));
        DAMAGED_ANVIL = register("damaged_anvil", new AnvilBlock(of(Material.HEAVY_METAL, MaterialColor.METAL)
                                                                         .requiresCorrectToolForDrops()
                                                                         .strength(5.0F, 1_200.0F)
                                                                         .sound(SoundType.ANVIL)));
        TRAPPED_CHEST = register("trapped_chest",
                                 new TrappedChestBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        LIGHT_WEIGHTED_PRESSURE_PLATE = register("light_weighted_pressure_plate", new WeightedPressurePlateBlock(15, of(
                Material.METAL, MaterialColor.GOLD).requiresCorrectToolForDrops().noCollission().strength(0.5F).sound(SoundType.WOOD)));
        HEAVY_WEIGHTED_PRESSURE_PLATE = register("heavy_weighted_pressure_plate", new WeightedPressurePlateBlock(150, of(
                Material.METAL).requiresCorrectToolForDrops().noCollission().strength(0.5F).sound(SoundType.WOOD)));
        COMPARATOR = register("comparator",
                              new ComparatorBlock(of(Material.DECORATION).instabreak().sound(SoundType.WOOD)));
        DAYLIGHT_DETECTOR = register("daylight_detector",
                                     new DaylightDetectorBlock(of(Material.WOOD).strength(0.2F).sound(SoundType.WOOD)));
        REDSTONE_BLOCK = register("redstone_block", new PoweredBlock(of(Material.METAL, MaterialColor.FIRE)
                                                                             .requiresCorrectToolForDrops()
                                                                             .strength(5.0F, 6.0F)
                                                                             .sound(SoundType.METAL)
                                                                             .isRedstoneConductor_(BlockUtils.NEVER)));
        NETHER_QUARTZ_ORE = register("nether_quartz_ore", new OreBlock(of(Material.STONE, MaterialColor.NETHER)
                                                                               .requiresCorrectToolForDrops()
                                                                               .strength(3.0F, 3.0F)
                                                                               .sound(SoundType.NETHER_ORE), UniformInt.of(2, 5)));
        HOPPER = register("hopper", new HopperBlock(of(Material.METAL, MaterialColor.STONE)
                                                            .requiresCorrectToolForDrops()
                                                            .strength(3.0F, 4.8F)
                                                            .sound(SoundType.METAL)
                                                            .noOcclusion()));
        QUARTZ_BLOCK = register("quartz_block", new Block(
                of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(0.8F)));
        CHISELED_QUARTZ_BLOCK = register("chiseled_quartz_block", new Block(
                of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(0.8F)));
        QUARTZ_PILLAR = register("quartz_pillar", new RotatedPillarBlock(
                of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(0.8F)));
        QUARTZ_STAIRS = register("quartz_stairs", new StairBlock(QUARTZ_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(QUARTZ_BLOCK)));
        ACTIVATOR_RAIL = register("activator_rail", new PoweredRailBlock(
                of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL)));
        DROPPER = register("dropper", new DropperBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F)));
        WHITE_TERRACOTTA = register("white_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        ORANGE_TERRACOTTA = register("orange_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_ORANGE).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        MAGENTA_TERRACOTTA = register("magenta_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_MAGENTA).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        LIGHT_BLUE_TERRACOTTA = register("light_blue_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_BLUE)
                        .requiresCorrectToolForDrops()
                        .strength(1.25F, 4.2F)));
        YELLOW_TERRACOTTA = register("yellow_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        LIME_TERRACOTTA = register("lime_terracotta", new Block(of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_GREEN)
                                                                        .requiresCorrectToolForDrops()
                                                                        .strength(1.25F, 4.2F)));
        PINK_TERRACOTTA = register("pink_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_PINK).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        GRAY_TERRACOTTA = register("gray_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_GRAY).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        LIGHT_GRAY_TERRACOTTA = register("light_gray_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_GRAY)
                        .requiresCorrectToolForDrops()
                        .strength(1.25F, 4.2F)));
        CYAN_TERRACOTTA = register("cyan_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        PURPLE_TERRACOTTA = register("purple_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        BLUE_TERRACOTTA = register("blue_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_BLUE).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        BROWN_TERRACOTTA = register("brown_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        GREEN_TERRACOTTA = register("green_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_GREEN).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        RED_TERRACOTTA = register("red_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_RED).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        BLACK_TERRACOTTA = register("black_terracotta", new Block(
                of(Material.STONE, MaterialColor.TERRACOTTA_BLACK).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        WHITE_STAINED_GLASS_PANE = register("white_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.WHITE,
                                                                                                  of(Material.GLASS)
                                                                                                          .strength(0.3F)
                                                                                                          .sound(SoundType.GLASS)
                                                                                                          .noOcclusion()));
        ORANGE_STAINED_GLASS_PANE = register("orange_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.ORANGE,
                                                                                                    of(Material.GLASS)
                                                                                                            .strength(0.3F)
                                                                                                            .sound(SoundType.GLASS)
                                                                                                            .noOcclusion()));
        MAGENTA_STAINED_GLASS_PANE = register("magenta_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.MAGENTA,
                                                                                                      of(Material.GLASS)
                                                                                                              .strength(0.3F)
                                                                                                              .sound(SoundType.GLASS)
                                                                                                              .noOcclusion()));
        LIGHT_BLUE_STAINED_GLASS_PANE = register("light_blue_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.LIGHT_BLUE,
                                                                                                            of(
                                                                                                                    Material.GLASS)
                                                                                                                    .strength(0.3F)
                                                                                                                    .sound(SoundType.GLASS)
                                                                                                                    .noOcclusion()));
        YELLOW_STAINED_GLASS_PANE = register("yellow_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.YELLOW,
                                                                                                    of(Material.GLASS)
                                                                                                            .strength(0.3F)
                                                                                                            .sound(SoundType.GLASS)
                                                                                                            .noOcclusion()));
        LIME_STAINED_GLASS_PANE = register("lime_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.LIME,
                                                                                                of(Material.GLASS)
                                                                                                        .strength(0.3F)
                                                                                                        .sound(SoundType.GLASS)
                                                                                                        .noOcclusion()));
        PINK_STAINED_GLASS_PANE = register("pink_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.PINK,
                                                                                                of(Material.GLASS)
                                                                                                        .strength(0.3F)
                                                                                                        .sound(SoundType.GLASS)
                                                                                                        .noOcclusion()));
        GRAY_STAINED_GLASS_PANE = register("gray_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.GRAY,
                                                                                                of(Material.GLASS)
                                                                                                        .strength(0.3F)
                                                                                                        .sound(SoundType.GLASS)
                                                                                                        .noOcclusion()));
        LIGHT_GRAY_STAINED_GLASS_PANE = register("light_gray_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.LIGHT_GRAY,
                                                                                                            of(
                                                                                                                    Material.GLASS)
                                                                                                                    .strength(0.3F)
                                                                                                                    .sound(SoundType.GLASS)
                                                                                                                    .noOcclusion()));
        CYAN_STAINED_GLASS_PANE = register("cyan_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.CYAN,
                                                                                                of(Material.GLASS)
                                                                                                        .strength(0.3F)
                                                                                                        .sound(SoundType.GLASS)
                                                                                                        .noOcclusion()));
        PURPLE_STAINED_GLASS_PANE = register("purple_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.PURPLE,
                                                                                                    of(Material.GLASS)
                                                                                                            .strength(0.3F)
                                                                                                            .sound(SoundType.GLASS)
                                                                                                            .noOcclusion()));
        BLUE_STAINED_GLASS_PANE = register("blue_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.BLUE,
                                                                                                of(Material.GLASS)
                                                                                                        .strength(0.3F)
                                                                                                        .sound(SoundType.GLASS)
                                                                                                        .noOcclusion()));
        BROWN_STAINED_GLASS_PANE = register("brown_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.BROWN,
                                                                                                  of(Material.GLASS)
                                                                                                          .strength(0.3F)
                                                                                                          .sound(SoundType.GLASS)
                                                                                                          .noOcclusion()));
        GREEN_STAINED_GLASS_PANE = register("green_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.GREEN,
                                                                                                  of(Material.GLASS)
                                                                                                          .strength(0.3F)
                                                                                                          .sound(SoundType.GLASS)
                                                                                                          .noOcclusion()));
        RED_STAINED_GLASS_PANE = register("red_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.RED,
                                                                                              of(Material.GLASS)
                                                                                                      .strength(0.3F)
                                                                                                      .sound(SoundType.GLASS)
                                                                                                      .noOcclusion()));
        BLACK_STAINED_GLASS_PANE = register("black_stained_glass_pane", new StainedGlassPaneBlock(DyeColor.BLACK,
                                                                                                  of(Material.GLASS)
                                                                                                          .strength(0.3F)
                                                                                                          .sound(SoundType.GLASS)
                                                                                                          .noOcclusion()));
        ACACIA_STAIRS = register("acacia_stairs", new StairBlock(ACACIA_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(ACACIA_PLANKS)));
        DARK_OAK_STAIRS = register("dark_oak_stairs",
                                   new StairBlock(DARK_OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(DARK_OAK_PLANKS)));
        SLIME_BLOCK = register("slime_block", new SlimeBlock(
                of(Material.CLAY, MaterialColor.GRASS).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion()));
        BARRIER = register("barrier", new BarrierBlock(
                of(Material.BARRIER)
                        .strength(-1.0F, 3_600_000.8F)
                        .noDrops()
                        .noOcclusion()
                        .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        LIGHT = register("light", new LightBlock(of(Material.AIR)
                                                         .strength(-1.0F, 3_600_000.8F)
                                                         .noDrops()
                                                         .noOcclusion()
                                                         .lightLevel(LightBlock.LIGHT_EMISSION)));
        IRON_TRAPDOOR = register("iron_trapdoor", new TrapDoorBlock(of(Material.METAL)
                                                                            .requiresCorrectToolForDrops()
                                                                            .strength(5.0F)
                                                                            .sound(SoundType.METAL)
                                                                            .noOcclusion()
                                                                            .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        PRISMARINE = register("prismarine", new Block(
                of(Material.STONE, MaterialColor.COLOR_CYAN).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PRISMARINE_BRICKS = register("prismarine_bricks", new Block(
                of(Material.STONE, MaterialColor.DIAMOND).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DARK_PRISMARINE = register("dark_prismarine", new Block(
                of(Material.STONE, MaterialColor.DIAMOND).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PRISMARINE_STAIRS = register("prismarine_stairs", new StairBlock(PRISMARINE.defaultBlockState(), BlockBehaviour.Properties.copy(PRISMARINE)));
        PRISMARINE_BRICK_STAIRS = register("prismarine_brick_stairs",
                                           new StairBlock(PRISMARINE_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(PRISMARINE_BRICKS)));
        DARK_PRISMARINE_STAIRS = register("dark_prismarine_stairs",
                                          new StairBlock(DARK_PRISMARINE.defaultBlockState(), BlockBehaviour.Properties.copy(DARK_PRISMARINE)));
        PRISMARINE_SLAB = register("prismarine_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.COLOR_CYAN).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PRISMARINE_BRICK_SLAB = register("prismarine_brick_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.DIAMOND).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DARK_PRISMARINE_SLAB = register("dark_prismarine_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.DIAMOND).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        SEA_LANTERN = register("sea_lantern", new Block(of(Material.GLASS, MaterialColor.QUARTZ)
                                                                .strength(0.3F)
                                                                .sound(SoundType.GLASS)
                                                                .lightLevel(s -> 0b1_1111_1_1111_0_0000)));
        HAY_BLOCK = register("hay_block", new HayBlock(
                of(Material.GRASS, MaterialColor.COLOR_YELLOW).strength(0.5F).sound(SoundType.GRASS)));
        WHITE_CARPET = register("white_carpet", new WoolCarpetBlock(DyeColor.WHITE,
                                                                    of(Material.CLOTH_DECORATION, MaterialColor.SNOW)
                                                                            .strength(0.1F)
                                                                            .sound(SoundType.WOOL)));
        ORANGE_CARPET = register("orange_carpet", new WoolCarpetBlock(DyeColor.ORANGE, of(Material.CLOTH_DECORATION,
                                                                                          MaterialColor.COLOR_ORANGE)
                .strength(0.1F)
                .sound(SoundType.WOOL)));
        MAGENTA_CARPET = register("magenta_carpet", new WoolCarpetBlock(DyeColor.MAGENTA, of(Material.CLOTH_DECORATION,
                                                                                             MaterialColor.COLOR_MAGENTA)
                .strength(0.1F)
                .sound(SoundType.WOOL)));
        LIGHT_BLUE_CARPET = register("light_blue_carpet", new WoolCarpetBlock(DyeColor.LIGHT_BLUE,
                                                                              of(Material.CLOTH_DECORATION,
                                                                                 MaterialColor.COLOR_LIGHT_BLUE)
                                                                                      .strength(0.1F)
                                                                                      .sound(SoundType.WOOL)));
        YELLOW_CARPET = register("yellow_carpet", new WoolCarpetBlock(DyeColor.YELLOW, of(Material.CLOTH_DECORATION,
                                                                                          MaterialColor.COLOR_YELLOW)
                .strength(0.1F)
                .sound(SoundType.WOOL)));
        LIME_CARPET = register("lime_carpet", new WoolCarpetBlock(DyeColor.LIME, of(Material.CLOTH_DECORATION,
                                                                                    MaterialColor.COLOR_LIGHT_GREEN)
                .strength(0.1F)
                .sound(SoundType.WOOL)));
        PINK_CARPET = register("pink_carpet", new WoolCarpetBlock(DyeColor.PINK,
                                                                  of(Material.CLOTH_DECORATION, MaterialColor.COLOR_PINK)
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.WOOL)));
        GRAY_CARPET = register("gray_carpet", new WoolCarpetBlock(DyeColor.GRAY,
                                                                  of(Material.CLOTH_DECORATION, MaterialColor.COLOR_GRAY)
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.WOOL)));
        LIGHT_GRAY_CARPET = register("light_gray_carpet", new WoolCarpetBlock(DyeColor.LIGHT_GRAY,
                                                                              of(Material.CLOTH_DECORATION,
                                                                                 MaterialColor.COLOR_LIGHT_GRAY)
                                                                                      .strength(0.1F)
                                                                                      .sound(SoundType.WOOL)));
        CYAN_CARPET = register("cyan_carpet", new WoolCarpetBlock(DyeColor.CYAN,
                                                                  of(Material.CLOTH_DECORATION, MaterialColor.COLOR_CYAN)
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.WOOL)));
        PURPLE_CARPET = register("purple_carpet", new WoolCarpetBlock(DyeColor.PURPLE, of(Material.CLOTH_DECORATION,
                                                                                          MaterialColor.COLOR_PURPLE)
                .strength(0.1F)
                .sound(SoundType.WOOL)));
        BLUE_CARPET = register("blue_carpet", new WoolCarpetBlock(DyeColor.BLUE,
                                                                  of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BLUE)
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.WOOL)));
        BROWN_CARPET = register("brown_carpet", new WoolCarpetBlock(DyeColor.BROWN,
                                                                    of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BROWN)
                                                                            .strength(0.1F)
                                                                            .sound(SoundType.WOOL)));
        GREEN_CARPET = register("green_carpet", new WoolCarpetBlock(DyeColor.GREEN,
                                                                    of(Material.CLOTH_DECORATION, MaterialColor.COLOR_GREEN)
                                                                            .strength(0.1F)
                                                                            .sound(SoundType.WOOL)));
        RED_CARPET = register("red_carpet", new WoolCarpetBlock(DyeColor.RED,
                                                                of(Material.CLOTH_DECORATION, MaterialColor.COLOR_RED)
                                                                        .strength(0.1F)
                                                                        .sound(SoundType.WOOL)));
        BLACK_CARPET = register("black_carpet", new WoolCarpetBlock(DyeColor.BLACK,
                                                                    of(Material.CLOTH_DECORATION, MaterialColor.COLOR_BLACK)
                                                                            .strength(0.1F)
                                                                            .sound(SoundType.WOOL)));
        TERRACOTTA = register("terracotta", new Block(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
        COAL_BLOCK = register("coal_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
        PACKED_ICE = register("packed_ice",
                              new Block(of(Material.ICE_SOLID).friction(0.98F).strength(0.5F).sound(SoundType.GLASS)));
        SUNFLOWER = register("sunflower", new TallFlowerBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        LILAC = register("lilac", new TallFlowerBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        ROSE_BUSH = register("rose_bush", new TallFlowerBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        PEONY = register("peony", new TallFlowerBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        TALL_GRASS = register("tall_grass", new DoublePlantBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        LARGE_FERN = register("large_fern", new DoublePlantBlock(
                of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        WHITE_BANNER = register("white_banner", new BannerBlock(DyeColor.WHITE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        ORANGE_BANNER = register("orange_banner", new BannerBlock(DyeColor.ORANGE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        MAGENTA_BANNER = register("magenta_banner", new BannerBlock(DyeColor.MAGENTA, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        LIGHT_BLUE_BANNER = register("light_blue_banner", new BannerBlock(DyeColor.LIGHT_BLUE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        YELLOW_BANNER = register("yellow_banner", new BannerBlock(DyeColor.YELLOW, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        LIME_BANNER = register("lime_banner", new BannerBlock(DyeColor.LIME, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        PINK_BANNER = register("pink_banner", new BannerBlock(DyeColor.PINK, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        GRAY_BANNER = register("gray_banner", new BannerBlock(DyeColor.GRAY, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        LIGHT_GRAY_BANNER = register("light_gray_banner", new BannerBlock(DyeColor.LIGHT_GRAY, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        CYAN_BANNER = register("cyan_banner", new BannerBlock(DyeColor.CYAN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        PURPLE_BANNER = register("purple_banner", new BannerBlock(DyeColor.PURPLE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        BLUE_BANNER = register("blue_banner", new BannerBlock(DyeColor.BLUE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        BROWN_BANNER = register("brown_banner", new BannerBlock(DyeColor.BROWN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        GREEN_BANNER = register("green_banner", new BannerBlock(DyeColor.GREEN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        RED_BANNER = register("red_banner", new BannerBlock(DyeColor.RED, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        BLACK_BANNER = register("black_banner", new BannerBlock(DyeColor.BLACK, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)));
        WHITE_WALL_BANNER = register("white_wall_banner", new WallBannerBlock(DyeColor.WHITE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(WHITE_BANNER)));
        ORANGE_WALL_BANNER = register("orange_wall_banner", new WallBannerBlock(DyeColor.ORANGE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(ORANGE_BANNER)));
        MAGENTA_WALL_BANNER = register("magenta_wall_banner", new WallBannerBlock(DyeColor.MAGENTA, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(
                        MAGENTA_BANNER)));
        LIGHT_BLUE_WALL_BANNER = register("light_blue_wall_banner", new WallBannerBlock(DyeColor.LIGHT_BLUE,
                                                                                        of(Material.WOOD)
                                                                                                .noCollission()
                                                                                                .strength(1.0F)
                                                                                                .sound(SoundType.WOOD)
                                                                                                .dropsLike(LIGHT_BLUE_BANNER)));
        YELLOW_WALL_BANNER = register("yellow_wall_banner", new WallBannerBlock(DyeColor.YELLOW, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(YELLOW_BANNER)));
        LIME_WALL_BANNER = register("lime_wall_banner", new WallBannerBlock(DyeColor.LIME, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(LIME_BANNER)));
        PINK_WALL_BANNER = register("pink_wall_banner", new WallBannerBlock(DyeColor.PINK, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(PINK_BANNER)));
        GRAY_WALL_BANNER = register("gray_wall_banner", new WallBannerBlock(DyeColor.GRAY, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(GRAY_BANNER)));
        LIGHT_GRAY_WALL_BANNER = register("light_gray_wall_banner", new WallBannerBlock(DyeColor.LIGHT_GRAY,
                                                                                        of(Material.WOOD)
                                                                                                .noCollission()
                                                                                                .strength(1.0F)
                                                                                                .sound(SoundType.WOOD)
                                                                                                .dropsLike(LIGHT_GRAY_BANNER)));
        CYAN_WALL_BANNER = register("cyan_wall_banner", new WallBannerBlock(DyeColor.CYAN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(CYAN_BANNER)));
        PURPLE_WALL_BANNER = register("purple_wall_banner", new WallBannerBlock(DyeColor.PURPLE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(PURPLE_BANNER)));
        BLUE_WALL_BANNER = register("blue_wall_banner", new WallBannerBlock(DyeColor.BLUE, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(BLUE_BANNER)));
        BROWN_WALL_BANNER = register("brown_wall_banner", new WallBannerBlock(DyeColor.BROWN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(BROWN_BANNER)));
        GREEN_WALL_BANNER = register("green_wall_banner", new WallBannerBlock(DyeColor.GREEN, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(GREEN_BANNER)));
        RED_WALL_BANNER = register("red_wall_banner", new WallBannerBlock(DyeColor.RED, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(RED_BANNER)));
        BLACK_WALL_BANNER = register("black_wall_banner", new WallBannerBlock(DyeColor.BLACK, of(Material.WOOD)
                .noCollission()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .dropsLike(BLACK_BANNER)));
        RED_SANDSTONE = register("red_sandstone", new Block(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(0.8F)));
        CHISELED_RED_SANDSTONE = register("chiseled_red_sandstone", new Block(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(0.8F)));
        CUT_RED_SANDSTONE = register("cut_red_sandstone", new Block(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(0.8F)));
        RED_SANDSTONE_STAIRS = register("red_sandstone_stairs",
                                        new StairBlock(RED_SANDSTONE.defaultBlockState(), BlockBehaviour.Properties.copy(RED_SANDSTONE)));
        OAK_SLAB = register("oak_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        SPRUCE_SLAB = register("spruce_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        BIRCH_SLAB = register("birch_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.SAND).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        JUNGLE_SLAB = register("jungle_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        ACACIA_SLAB = register("acacia_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.COLOR_ORANGE).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        DARK_OAK_SLAB = register("dark_oak_slab", new SlabBlock(
                of(Material.WOOD, MaterialColor.COLOR_BROWN).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        STONE_SLAB = register("stone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SMOOTH_STONE_SLAB = register("smooth_stone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SANDSTONE_SLAB = register("sandstone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        CUT_SANDSTONE_SLAB = register("cut_sandstone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        PETRIFIED_OAK_SLAB = register("petrified_oak_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.WOOD).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        COBBLESTONE_SLAB = register("cobblestone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        BRICK_SLAB = register("brick_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.COLOR_RED).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        STONE_BRICK_SLAB = register("stone_brick_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        NETHER_BRICK_SLAB = register("nether_brick_slab", new SlabBlock(of(Material.STONE, MaterialColor.NETHER)
                                                                                .requiresCorrectToolForDrops()
                                                                                .strength(2.0F, 6.0F)
                                                                                .sound(SoundType.NETHER_BRICKS)));
        QUARTZ_SLAB = register("quartz_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        RED_SANDSTONE_SLAB = register("red_sandstone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        CUT_RED_SANDSTONE_SLAB = register("cut_red_sandstone_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        PURPUR_SLAB = register("purpur_slab", new SlabBlock(
                of(Material.STONE, MaterialColor.COLOR_MAGENTA).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SMOOTH_STONE = register("smooth_stone", new Block(
                of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SMOOTH_SANDSTONE = register("smooth_sandstone", new Block(
                of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SMOOTH_QUARTZ = register("smooth_quartz", new Block(
                of(Material.STONE, MaterialColor.QUARTZ).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SMOOTH_RED_SANDSTONE = register("smooth_red_sandstone", new Block(
                of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
        SPRUCE_FENCE_GATE = register("spruce_fence_gate", new FenceGateBlock(
                of(Material.WOOD, SPRUCE_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        BIRCH_FENCE_GATE = register("birch_fence_gate", new FenceGateBlock(
                of(Material.WOOD, BIRCH_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        JUNGLE_FENCE_GATE = register("jungle_fence_gate", new FenceGateBlock(
                of(Material.WOOD, JUNGLE_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        ACACIA_FENCE_GATE = register("acacia_fence_gate", new FenceGateBlock(
                of(Material.WOOD, ACACIA_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        DARK_OAK_FENCE_GATE = register("dark_oak_fence_gate", new FenceGateBlock(
                of(Material.WOOD, DARK_OAK_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        SPRUCE_FENCE = register("spruce_fence", new FenceBlock(
                of(Material.WOOD, SPRUCE_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        BIRCH_FENCE = register("birch_fence", new FenceBlock(
                of(Material.WOOD, BIRCH_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        JUNGLE_FENCE = register("jungle_fence", new FenceBlock(
                of(Material.WOOD, JUNGLE_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        ACACIA_FENCE = register("acacia_fence", new FenceBlock(
                of(Material.WOOD, ACACIA_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        DARK_OAK_FENCE = register("dark_oak_fence", new FenceBlock(
                of(Material.WOOD, DARK_OAK_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        SPRUCE_DOOR = register("spruce_door", new DoorBlock(of(Material.WOOD, SPRUCE_PLANKS.defaultMaterialColor())
                                                                    .strength(3.0F)
                                                                    .sound(SoundType.WOOD)));
        BIRCH_DOOR = register("birch_door", new DoorBlock(
                of(Material.WOOD, BIRCH_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
        JUNGLE_DOOR = register("jungle_door", new DoorBlock(of(Material.WOOD, JUNGLE_PLANKS.defaultMaterialColor())
                                                                    .strength(3.0F)
                                                                    .sound(SoundType.WOOD)
                                                                    .noOcclusion()));
        ACACIA_DOOR = register("acacia_door", new DoorBlock(of(Material.WOOD, ACACIA_PLANKS.defaultMaterialColor())
                                                                    .strength(3.0F)
                                                                    .sound(SoundType.WOOD)
                                                                    .noOcclusion()));
        DARK_OAK_DOOR = register("dark_oak_door", new DoorBlock(of(Material.WOOD, DARK_OAK_PLANKS.defaultMaterialColor())
                                                                        .strength(3.0F)
                                                                        .sound(SoundType.WOOD)
                                                                        .noOcclusion()));
        END_ROD = register("end_rod",
                           new EndRodBlock(of(Material.DECORATION).instabreak().lightLevel(BlockUtils.LIGHT_14).sound(SoundType.WOOD).noOcclusion()));
        CHORUS_PLANT = register("chorus_plant", new ChorusPlantBlock(
                of(Material.PLANT, MaterialColor.COLOR_PURPLE).strength(0.4F).sound(SoundType.WOOD).noOcclusion()));
        CHORUS_FLOWER = register("chorus_flower", new ChorusFlowerBlock((ChorusPlantBlock) CHORUS_PLANT,
                                                                        of(Material.PLANT, MaterialColor.COLOR_PURPLE)
                                                                                .randomTicks()
                                                                                .strength(0.4F)
                                                                                .sound(SoundType.WOOD)
                                                                                .noOcclusion()));
        PURPUR_BLOCK = register("purpur_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_MAGENTA).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PURPUR_PILLAR = register("purpur_pillar", new RotatedPillarBlock(
                of(Material.STONE, MaterialColor.COLOR_MAGENTA).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        PURPUR_STAIRS = register("purpur_stairs", new StairBlock(PURPUR_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(PURPUR_BLOCK)));
        END_STONE_BRICKS = register("end_stone_bricks", new Block(
                of(Material.STONE, MaterialColor.SAND).requiresCorrectToolForDrops().strength(3.0F, 9.0F)));
        BEETROOTS = register("beetroots", new BeetrootBlock(of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
        DIRT_PATH = register("dirt_path", new DirtPathBlock(of(Material.DIRT)
                                                                    .strength(0.65F)
                                                                    .sound(SoundType.GRASS)
                                                                    .isViewBlocking_(BlockUtils.ALWAYS)
                                                                    .isSuffocating_(BlockUtils.ALWAYS)));
        END_GATEWAY = register("end_gateway", new EndGatewayBlock(of(Material.PORTAL, MaterialColor.COLOR_BLACK).noCollission()
                                                                                                                .lightLevel(BlockUtils.LIGHT_15)
                                                                                                                .strength(-1.0F, 3_600_000.0F)
                                                                                                                .noDrops()));
        REPEATING_COMMAND_BLOCK = register("repeating_command_block", new CommandBlock(of(Material.METAL, MaterialColor.COLOR_PURPLE)
                                                                                               .requiresCorrectToolForDrops()
                                                                                               .strength(-1.0F, 3_600_000.0F)
                                                                                               .noDrops(), false));
        CHAIN_COMMAND_BLOCK = register("chain_command_block", new CommandBlock(of(Material.METAL, MaterialColor.COLOR_GREEN)
                                                                                       .requiresCorrectToolForDrops()
                                                                                       .strength(-1.0F, 3_600_000.0F)
                                                                                       .noDrops(), true));
        FROSTED_ICE = register("frosted_ice", new FrostedIceBlock(of(Material.ICE)
                                                                          .friction(0.98F)
                                                                          .randomTicks()
                                                                          .strength(0.5F)
                                                                          .sound(SoundType.GLASS)
                                                                          .noOcclusion()
                                                                          .isValidSpawn_(Mixin_FS_Blocks::_polarBear)));
        MAGMA_BLOCK = register("magma_block", new MagmaBlock(of(Material.STONE, MaterialColor.NETHER).requiresCorrectToolForDrops()
                                                                                                     .lightLevel(BlockUtils.LIGHT_3)
                                                                                                     .randomTicks()
                                                                                                     .strength(0.5F)
                                                                                                     .isValidSpawn_(Mixin_FS_Blocks::_fireImmune)
                                                                                                     .hasPostProcess_(BlockUtils.ALWAYS)
                                                                                                     .emissiveRendering_(BlockUtils.ALWAYS)));
        NETHER_WART_BLOCK = register("nether_wart_block",
                                     new Block(of(Material.GRASS, MaterialColor.COLOR_RED).strength(1.0F).sound(SoundType.WART_BLOCK)));
        RED_NETHER_BRICKS = register("red_nether_bricks", new Block(of(Material.STONE, MaterialColor.NETHER)
                                                                            .requiresCorrectToolForDrops()
                                                                            .strength(2.0F, 6.0F)
                                                                            .sound(SoundType.NETHER_BRICKS)));
        BONE_BLOCK = register("bone_block", new RotatedPillarBlock(of(Material.STONE, MaterialColor.SAND)
                                                                           .requiresCorrectToolForDrops()
                                                                           .strength(2.0F)
                                                                           .sound(SoundType.BONE_BLOCK)));
        STRUCTURE_VOID = register("structure_void",
                                  new StructureVoidBlock(of(Material.STRUCTURAL_AIR).noCollission().noDrops()));
        OBSERVER = register("observer", new ObserverBlock(
                of(Material.STONE).strength(3.0F).requiresCorrectToolForDrops().isRedstoneConductor_(BlockUtils.NEVER)));
        SHULKER_BOX = register("shulker_box", shulkerBox(null, of(Material.SHULKER_SHELL)));
        WHITE_SHULKER_BOX = register("white_shulker_box",
                                     shulkerBox(DyeColor.WHITE, of(Material.SHULKER_SHELL, MaterialColor.SNOW)));
        ORANGE_SHULKER_BOX = register("orange_shulker_box",
                                      shulkerBox(DyeColor.ORANGE, of(Material.SHULKER_SHELL, MaterialColor.COLOR_ORANGE)));
        MAGENTA_SHULKER_BOX = register("magenta_shulker_box", shulkerBox(DyeColor.MAGENTA, of(Material.SHULKER_SHELL,
                                                                                              MaterialColor.COLOR_MAGENTA)));
        LIGHT_BLUE_SHULKER_BOX = register("light_blue_shulker_box", shulkerBox(DyeColor.LIGHT_BLUE,
                                                                               of(Material.SHULKER_SHELL,
                                                                                  MaterialColor.COLOR_LIGHT_BLUE)));
        YELLOW_SHULKER_BOX = register("yellow_shulker_box",
                                      shulkerBox(DyeColor.YELLOW, of(Material.SHULKER_SHELL, MaterialColor.COLOR_YELLOW)));
        LIME_SHULKER_BOX = register("lime_shulker_box",
                                    shulkerBox(DyeColor.LIME, of(Material.SHULKER_SHELL, MaterialColor.COLOR_LIGHT_GREEN)));
        PINK_SHULKER_BOX = register("pink_shulker_box",
                                    shulkerBox(DyeColor.PINK, of(Material.SHULKER_SHELL, MaterialColor.COLOR_PINK)));
        GRAY_SHULKER_BOX = register("gray_shulker_box",
                                    shulkerBox(DyeColor.GRAY, of(Material.SHULKER_SHELL, MaterialColor.COLOR_GRAY)));
        LIGHT_GRAY_SHULKER_BOX = register("light_gray_shulker_box", shulkerBox(DyeColor.LIGHT_GRAY,
                                                                               of(Material.SHULKER_SHELL,
                                                                                  MaterialColor.COLOR_LIGHT_GRAY)));
        CYAN_SHULKER_BOX = register("cyan_shulker_box",
                                    shulkerBox(DyeColor.CYAN, of(Material.SHULKER_SHELL, MaterialColor.COLOR_CYAN)));
        PURPLE_SHULKER_BOX = register("purple_shulker_box", shulkerBox(DyeColor.PURPLE, of(Material.SHULKER_SHELL,
                                                                                           MaterialColor.TERRACOTTA_PURPLE)));
        BLUE_SHULKER_BOX = register("blue_shulker_box",
                                    shulkerBox(DyeColor.BLUE, of(Material.SHULKER_SHELL, MaterialColor.COLOR_BLUE)));
        BROWN_SHULKER_BOX = register("brown_shulker_box",
                                     shulkerBox(DyeColor.BROWN, of(Material.SHULKER_SHELL, MaterialColor.COLOR_BROWN)));
        GREEN_SHULKER_BOX = register("green_shulker_box",
                                     shulkerBox(DyeColor.GREEN, of(Material.SHULKER_SHELL, MaterialColor.COLOR_GREEN)));
        RED_SHULKER_BOX = register("red_shulker_box",
                                   shulkerBox(DyeColor.RED, of(Material.SHULKER_SHELL, MaterialColor.COLOR_RED)));
        BLACK_SHULKER_BOX = register("black_shulker_box",
                                     shulkerBox(DyeColor.BLACK, of(Material.SHULKER_SHELL, MaterialColor.COLOR_BLACK)));
        WHITE_GLAZED_TERRACOTTA = register("white_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.WHITE).requiresCorrectToolForDrops().strength(1.4F)));
        ORANGE_GLAZED_TERRACOTTA = register("orange_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.ORANGE).requiresCorrectToolForDrops().strength(1.4F)));
        MAGENTA_GLAZED_TERRACOTTA = register("magenta_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.MAGENTA).requiresCorrectToolForDrops().strength(1.4F)));
        LIGHT_BLUE_GLAZED_TERRACOTTA = register("light_blue_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.LIGHT_BLUE).requiresCorrectToolForDrops().strength(1.4F)));
        YELLOW_GLAZED_TERRACOTTA = register("yellow_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.YELLOW).requiresCorrectToolForDrops().strength(1.4F)));
        LIME_GLAZED_TERRACOTTA = register("lime_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.LIME).requiresCorrectToolForDrops().strength(1.4F)));
        PINK_GLAZED_TERRACOTTA = register("pink_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.PINK).requiresCorrectToolForDrops().strength(1.4F)));
        GRAY_GLAZED_TERRACOTTA = register("gray_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.GRAY).requiresCorrectToolForDrops().strength(1.4F)));
        LIGHT_GRAY_GLAZED_TERRACOTTA = register("light_gray_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops().strength(1.4F)));
        CYAN_GLAZED_TERRACOTTA = register("cyan_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.CYAN).requiresCorrectToolForDrops().strength(1.4F)));
        PURPLE_GLAZED_TERRACOTTA = register("purple_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.PURPLE).requiresCorrectToolForDrops().strength(1.4F)));
        BLUE_GLAZED_TERRACOTTA = register("blue_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.BLUE).requiresCorrectToolForDrops().strength(1.4F)));
        BROWN_GLAZED_TERRACOTTA = register("brown_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.BROWN).requiresCorrectToolForDrops().strength(1.4F)));
        GREEN_GLAZED_TERRACOTTA = register("green_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.GREEN).requiresCorrectToolForDrops().strength(1.4F)));
        RED_GLAZED_TERRACOTTA = register("red_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.RED).requiresCorrectToolForDrops().strength(1.4F)));
        BLACK_GLAZED_TERRACOTTA = register("black_glazed_terracotta", new GlazedTerracottaBlock(
                of(Material.STONE, DyeColor.BLACK).requiresCorrectToolForDrops().strength(1.4F)));
        WHITE_CONCRETE = register("white_concrete", new Block(
                of(Material.STONE, DyeColor.WHITE).requiresCorrectToolForDrops().strength(1.8F)));
        ORANGE_CONCRETE = register("orange_concrete", new Block(
                of(Material.STONE, DyeColor.ORANGE).requiresCorrectToolForDrops().strength(1.8F)));
        MAGENTA_CONCRETE = register("magenta_concrete", new Block(
                of(Material.STONE, DyeColor.MAGENTA).requiresCorrectToolForDrops().strength(1.8F)));
        LIGHT_BLUE_CONCRETE = register("light_blue_concrete", new Block(
                of(Material.STONE, DyeColor.LIGHT_BLUE).requiresCorrectToolForDrops().strength(1.8F)));
        YELLOW_CONCRETE = register("yellow_concrete", new Block(
                of(Material.STONE, DyeColor.YELLOW).requiresCorrectToolForDrops().strength(1.8F)));
        LIME_CONCRETE = register("lime_concrete",
                                 new Block(of(Material.STONE, DyeColor.LIME).requiresCorrectToolForDrops().strength(1.8F)));
        PINK_CONCRETE = register("pink_concrete",
                                 new Block(of(Material.STONE, DyeColor.PINK).requiresCorrectToolForDrops().strength(1.8F)));
        GRAY_CONCRETE = register("gray_concrete",
                                 new Block(of(Material.STONE, DyeColor.GRAY).requiresCorrectToolForDrops().strength(1.8F)));
        LIGHT_GRAY_CONCRETE = register("light_gray_concrete", new Block(
                of(Material.STONE, DyeColor.LIGHT_GRAY).requiresCorrectToolForDrops().strength(1.8F)));
        CYAN_CONCRETE = register("cyan_concrete",
                                 new Block(of(Material.STONE, DyeColor.CYAN).requiresCorrectToolForDrops().strength(1.8F)));
        PURPLE_CONCRETE = register("purple_concrete", new Block(
                of(Material.STONE, DyeColor.PURPLE).requiresCorrectToolForDrops().strength(1.8F)));
        BLUE_CONCRETE = register("blue_concrete",
                                 new Block(of(Material.STONE, DyeColor.BLUE).requiresCorrectToolForDrops().strength(1.8F)));
        BROWN_CONCRETE = register("brown_concrete", new Block(
                of(Material.STONE, DyeColor.BROWN).requiresCorrectToolForDrops().strength(1.8F)));
        GREEN_CONCRETE = register("green_concrete", new Block(
                of(Material.STONE, DyeColor.GREEN).requiresCorrectToolForDrops().strength(1.8F)));
        RED_CONCRETE = register("red_concrete",
                                new Block(of(Material.STONE, DyeColor.RED).requiresCorrectToolForDrops().strength(1.8F)));
        BLACK_CONCRETE = register("black_concrete", new Block(
                of(Material.STONE, DyeColor.BLACK).requiresCorrectToolForDrops().strength(1.8F)));
        WHITE_CONCRETE_POWDER = register("white_concrete_powder", new ConcretePowderBlock(WHITE_CONCRETE,
                                                                                          of(Material.SAND, DyeColor.WHITE)
                                                                                                  .strength(0.5F)
                                                                                                  .sound(SoundType.SAND)));
        ORANGE_CONCRETE_POWDER = register("orange_concrete_powder", new ConcretePowderBlock(ORANGE_CONCRETE,
                                                                                            of(Material.SAND,
                                                                                               DyeColor.ORANGE)
                                                                                                    .strength(0.5F)
                                                                                                    .sound(SoundType.SAND)));
        MAGENTA_CONCRETE_POWDER = register("magenta_concrete_powder", new ConcretePowderBlock(MAGENTA_CONCRETE,
                                                                                              of(Material.SAND,
                                                                                                 DyeColor.MAGENTA)
                                                                                                      .strength(0.5F)
                                                                                                      .sound(SoundType.SAND)));
        LIGHT_BLUE_CONCRETE_POWDER = register("light_blue_concrete_powder", new ConcretePowderBlock(LIGHT_BLUE_CONCRETE,
                                                                                                    of(Material.SAND,
                                                                                                       DyeColor.LIGHT_BLUE)
                                                                                                            .strength(0.5F)
                                                                                                            .sound(SoundType.SAND)));
        YELLOW_CONCRETE_POWDER = register("yellow_concrete_powder", new ConcretePowderBlock(YELLOW_CONCRETE,
                                                                                            of(Material.SAND,
                                                                                               DyeColor.YELLOW)
                                                                                                    .strength(0.5F)
                                                                                                    .sound(SoundType.SAND)));
        LIME_CONCRETE_POWDER = register("lime_concrete_powder", new ConcretePowderBlock(LIME_CONCRETE,
                                                                                        of(Material.SAND, DyeColor.LIME)
                                                                                                .strength(0.5F)
                                                                                                .sound(SoundType.SAND)));
        PINK_CONCRETE_POWDER = register("pink_concrete_powder", new ConcretePowderBlock(PINK_CONCRETE,
                                                                                        of(Material.SAND, DyeColor.PINK)
                                                                                                .strength(0.5F)
                                                                                                .sound(SoundType.SAND)));
        GRAY_CONCRETE_POWDER = register("gray_concrete_powder", new ConcretePowderBlock(GRAY_CONCRETE,
                                                                                        of(Material.SAND, DyeColor.GRAY)
                                                                                                .strength(0.5F)
                                                                                                .sound(SoundType.SAND)));
        LIGHT_GRAY_CONCRETE_POWDER = register("light_gray_concrete_powder", new ConcretePowderBlock(LIGHT_GRAY_CONCRETE,
                                                                                                    of(Material.SAND,
                                                                                                       DyeColor.LIGHT_GRAY)
                                                                                                            .strength(0.5F)
                                                                                                            .sound(SoundType.SAND)));
        CYAN_CONCRETE_POWDER = register("cyan_concrete_powder", new ConcretePowderBlock(CYAN_CONCRETE,
                                                                                        of(Material.SAND, DyeColor.CYAN)
                                                                                                .strength(0.5F)
                                                                                                .sound(SoundType.SAND)));
        PURPLE_CONCRETE_POWDER = register("purple_concrete_powder", new ConcretePowderBlock(PURPLE_CONCRETE,
                                                                                            of(Material.SAND,
                                                                                               DyeColor.PURPLE)
                                                                                                    .strength(0.5F)
                                                                                                    .sound(SoundType.SAND)));
        BLUE_CONCRETE_POWDER = register("blue_concrete_powder", new ConcretePowderBlock(BLUE_CONCRETE,
                                                                                        of(Material.SAND, DyeColor.BLUE)
                                                                                                .strength(0.5F)
                                                                                                .sound(SoundType.SAND)));
        BROWN_CONCRETE_POWDER = register("brown_concrete_powder", new ConcretePowderBlock(BROWN_CONCRETE,
                                                                                          of(Material.SAND, DyeColor.BROWN)
                                                                                                  .strength(0.5F)
                                                                                                  .sound(SoundType.SAND)));
        GREEN_CONCRETE_POWDER = register("green_concrete_powder", new ConcretePowderBlock(GREEN_CONCRETE,
                                                                                          of(Material.SAND, DyeColor.GREEN)
                                                                                                  .strength(0.5F)
                                                                                                  .sound(SoundType.SAND)));
        RED_CONCRETE_POWDER = register("red_concrete_powder", new ConcretePowderBlock(RED_CONCRETE,
                                                                                      of(Material.SAND, DyeColor.RED)
                                                                                              .strength(0.5F)
                                                                                              .sound(SoundType.SAND)));
        BLACK_CONCRETE_POWDER = register("black_concrete_powder", new ConcretePowderBlock(BLACK_CONCRETE,
                                                                                          of(Material.SAND, DyeColor.BLACK)
                                                                                                  .strength(0.5F)
                                                                                                  .sound(SoundType.SAND)));
        KELP = register("kelp", new KelpBlock(
                of(Material.WATER_PLANT).noCollission().randomTicks().instabreak().sound(SoundType.WET_GRASS)));
        KELP_PLANT = register("kelp_plant", new KelpPlantBlock(
                of(Material.WATER_PLANT).noCollission().instabreak().sound(SoundType.WET_GRASS)));
        DRIED_KELP_BLOCK = register("dried_kelp_block", new Block(
                of(Material.GRASS, MaterialColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundType.GRASS)));
        TURTLE_EGG = register("turtle_egg", new TurtleEggBlock(
                of(Material.EGG, MaterialColor.SAND).strength(0.5F).sound(SoundType.METAL).randomTicks().noOcclusion()));
        DEAD_TUBE_CORAL_BLOCK = register("dead_tube_coral_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DEAD_BRAIN_CORAL_BLOCK = register("dead_brain_coral_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DEAD_BUBBLE_CORAL_BLOCK = register("dead_bubble_coral_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DEAD_FIRE_CORAL_BLOCK = register("dead_fire_coral_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        DEAD_HORN_CORAL_BLOCK = register("dead_horn_coral_block", new Block(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        TUBE_CORAL_BLOCK = register("tube_coral_block", new CoralBlock(DEAD_TUBE_CORAL_BLOCK,
                                                                       of(Material.STONE, MaterialColor.COLOR_BLUE)
                                                                               .requiresCorrectToolForDrops()
                                                                               .strength(1.5F, 6.0F)
                                                                               .sound(SoundType.CORAL_BLOCK)));
        BRAIN_CORAL_BLOCK = register("brain_coral_block", new CoralBlock(DEAD_BRAIN_CORAL_BLOCK,
                                                                         of(Material.STONE, MaterialColor.COLOR_PINK)
                                                                                 .requiresCorrectToolForDrops()
                                                                                 .strength(1.5F, 6.0F)
                                                                                 .sound(SoundType.CORAL_BLOCK)));
        BUBBLE_CORAL_BLOCK = register("bubble_coral_block", new CoralBlock(DEAD_BUBBLE_CORAL_BLOCK,
                                                                           of(Material.STONE, MaterialColor.COLOR_PURPLE)
                                                                                   .requiresCorrectToolForDrops()
                                                                                   .strength(1.5F, 6.0F)
                                                                                   .sound(SoundType.CORAL_BLOCK)));
        FIRE_CORAL_BLOCK = register("fire_coral_block", new CoralBlock(DEAD_FIRE_CORAL_BLOCK,
                                                                       of(Material.STONE, MaterialColor.COLOR_RED)
                                                                               .requiresCorrectToolForDrops()
                                                                               .strength(1.5F, 6.0F)
                                                                               .sound(SoundType.CORAL_BLOCK)));
        HORN_CORAL_BLOCK = register("horn_coral_block", new CoralBlock(DEAD_HORN_CORAL_BLOCK,
                                                                       of(Material.STONE, MaterialColor.COLOR_YELLOW)
                                                                               .requiresCorrectToolForDrops()
                                                                               .strength(1.5F, 6.0F)
                                                                               .sound(SoundType.CORAL_BLOCK)));
        DEAD_TUBE_CORAL = register("dead_tube_coral", new BaseCoralPlantBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_BRAIN_CORAL = register("dead_brain_coral", new BaseCoralPlantBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_BUBBLE_CORAL = register("dead_bubble_coral", new BaseCoralPlantBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_FIRE_CORAL = register("dead_fire_coral", new BaseCoralPlantBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_HORN_CORAL = register("dead_horn_coral", new BaseCoralPlantBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        TUBE_CORAL = register("tube_coral", new CoralPlantBlock(DEAD_TUBE_CORAL,
                                                                of(Material.WATER_PLANT, MaterialColor.COLOR_BLUE)
                                                                        .noCollission()
                                                                        .instabreak()
                                                                        .sound(SoundType.WET_GRASS)));
        BRAIN_CORAL = register("brain_coral", new CoralPlantBlock(DEAD_BRAIN_CORAL,
                                                                  of(Material.WATER_PLANT, MaterialColor.COLOR_PINK)
                                                                          .noCollission()
                                                                          .instabreak()
                                                                          .sound(SoundType.WET_GRASS)));
        BUBBLE_CORAL = register("bubble_coral", new CoralPlantBlock(DEAD_BUBBLE_CORAL,
                                                                    of(Material.WATER_PLANT, MaterialColor.COLOR_PURPLE)
                                                                            .noCollission()
                                                                            .instabreak()
                                                                            .sound(SoundType.WET_GRASS)));
        FIRE_CORAL = register("fire_coral", new CoralPlantBlock(DEAD_FIRE_CORAL,
                                                                of(Material.WATER_PLANT, MaterialColor.COLOR_RED)
                                                                        .noCollission()
                                                                        .instabreak()
                                                                        .sound(SoundType.WET_GRASS)));
        HORN_CORAL = register("horn_coral", new CoralPlantBlock(DEAD_HORN_CORAL,
                                                                of(Material.WATER_PLANT, MaterialColor.COLOR_YELLOW)
                                                                        .noCollission()
                                                                        .instabreak()
                                                                        .sound(SoundType.WET_GRASS)));
        DEAD_TUBE_CORAL_FAN = register("dead_tube_coral_fan", new BaseCoralFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_BRAIN_CORAL_FAN = register("dead_brain_coral_fan", new BaseCoralFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_BUBBLE_CORAL_FAN = register("dead_bubble_coral_fan", new BaseCoralFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_FIRE_CORAL_FAN = register("dead_fire_coral_fan", new BaseCoralFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        DEAD_HORN_CORAL_FAN = register("dead_horn_coral_fan", new BaseCoralFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission().instabreak()));
        TUBE_CORAL_FAN = register("tube_coral_fan", new CoralFanBlock(DEAD_TUBE_CORAL_FAN,
                                                                      of(Material.WATER_PLANT, MaterialColor.COLOR_BLUE)
                                                                              .noCollission()
                                                                              .instabreak()
                                                                              .sound(SoundType.WET_GRASS)));
        BRAIN_CORAL_FAN = register("brain_coral_fan", new CoralFanBlock(DEAD_BRAIN_CORAL_FAN,
                                                                        of(Material.WATER_PLANT, MaterialColor.COLOR_PINK)
                                                                                .noCollission()
                                                                                .instabreak()
                                                                                .sound(SoundType.WET_GRASS)));
        BUBBLE_CORAL_FAN = register("bubble_coral_fan", new CoralFanBlock(DEAD_BUBBLE_CORAL_FAN, of(Material.WATER_PLANT,
                                                                                                    MaterialColor.COLOR_PURPLE)
                .noCollission()
                .instabreak()
                .sound(SoundType.WET_GRASS)));
        FIRE_CORAL_FAN = register("fire_coral_fan", new CoralFanBlock(DEAD_FIRE_CORAL_FAN,
                                                                      of(Material.WATER_PLANT, MaterialColor.COLOR_RED)
                                                                              .noCollission()
                                                                              .instabreak()
                                                                              .sound(SoundType.WET_GRASS)));
        HORN_CORAL_FAN = register("horn_coral_fan", new CoralFanBlock(DEAD_HORN_CORAL_FAN,
                                                                      of(Material.WATER_PLANT, MaterialColor.COLOR_YELLOW)
                                                                              .noCollission()
                                                                              .instabreak()
                                                                              .sound(SoundType.WET_GRASS)));
        DEAD_TUBE_CORAL_WALL_FAN = register("dead_tube_coral_wall_fan", new BaseCoralWallFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .instabreak()
                        .dropsLike(DEAD_TUBE_CORAL_FAN)));
        DEAD_BRAIN_CORAL_WALL_FAN = register("dead_brain_coral_wall_fan", new BaseCoralWallFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .instabreak()
                        .dropsLike(DEAD_BRAIN_CORAL_FAN)));
        DEAD_BUBBLE_CORAL_WALL_FAN = register("dead_bubble_coral_wall_fan", new BaseCoralWallFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .instabreak()
                        .dropsLike(DEAD_BUBBLE_CORAL_FAN)));
        DEAD_FIRE_CORAL_WALL_FAN = register("dead_fire_coral_wall_fan", new BaseCoralWallFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .instabreak()
                        .dropsLike(DEAD_FIRE_CORAL_FAN)));
        DEAD_HORN_CORAL_WALL_FAN = register("dead_horn_coral_wall_fan", new BaseCoralWallFanBlock(
                of(Material.STONE, MaterialColor.COLOR_GRAY)
                        .requiresCorrectToolForDrops()
                        .noCollission()
                        .instabreak()
                        .dropsLike(DEAD_HORN_CORAL_FAN)));
        TUBE_CORAL_WALL_FAN = register("tube_coral_wall_fan", new CoralWallFanBlock(DEAD_TUBE_CORAL_WALL_FAN,
                                                                                    of(Material.WATER_PLANT,
                                                                                       MaterialColor.COLOR_BLUE)
                                                                                            .noCollission()
                                                                                            .instabreak()
                                                                                            .sound(SoundType.WET_GRASS)
                                                                                            .dropsLike(TUBE_CORAL_FAN)));
        BRAIN_CORAL_WALL_FAN = register("brain_coral_wall_fan", new CoralWallFanBlock(DEAD_BRAIN_CORAL_WALL_FAN,
                                                                                      of(Material.WATER_PLANT,
                                                                                         MaterialColor.COLOR_PINK)
                                                                                              .noCollission()
                                                                                              .instabreak()
                                                                                              .sound(SoundType.WET_GRASS)
                                                                                              .dropsLike(BRAIN_CORAL_FAN)));
        BUBBLE_CORAL_WALL_FAN = register("bubble_coral_wall_fan", new CoralWallFanBlock(DEAD_BUBBLE_CORAL_WALL_FAN,
                                                                                        of(Material.WATER_PLANT,
                                                                                           MaterialColor.COLOR_PURPLE)
                                                                                                .noCollission()
                                                                                                .instabreak()
                                                                                                .sound(SoundType.WET_GRASS)
                                                                                                .dropsLike(BUBBLE_CORAL_FAN)));
        FIRE_CORAL_WALL_FAN = register("fire_coral_wall_fan", new CoralWallFanBlock(DEAD_FIRE_CORAL_WALL_FAN,
                                                                                    of(Material.WATER_PLANT,
                                                                                       MaterialColor.COLOR_RED)
                                                                                            .noCollission()
                                                                                            .instabreak()
                                                                                            .sound(SoundType.WET_GRASS)
                                                                                            .dropsLike(FIRE_CORAL_FAN)));
        HORN_CORAL_WALL_FAN = register("horn_coral_wall_fan", new CoralWallFanBlock(DEAD_HORN_CORAL_WALL_FAN,
                                                                                    of(Material.WATER_PLANT,
                                                                                       MaterialColor.COLOR_YELLOW)
                                                                                            .noCollission()
                                                                                            .instabreak()
                                                                                            .sound(SoundType.WET_GRASS)
                                                                                            .dropsLike(HORN_CORAL_FAN)));
        SEA_PICKLE = register("sea_pickle", new SeaPickleBlock(
                of(Material.WATER_PLANT, MaterialColor.COLOR_GREEN).lightLevel(Mixin_FS_Blocks::_pickle).sound(SoundType.SLIME_BLOCK).noOcclusion()));
        BLUE_ICE = register("blue_ice", new HalfTransparentBlock(
                of(Material.ICE_SOLID).strength(2.8F).friction(0.989F).sound(SoundType.GLASS)));
        CONDUIT = register("conduit", new ConduitBlock(
                of(Material.GLASS, MaterialColor.DIAMOND).strength(3.0F).lightLevel(BlockUtils.LIGHT_15).noOcclusion()));
        BAMBOO_SAPLING = register("bamboo_sapling", new BambooSaplingBlock(of(Material.BAMBOO_SAPLING)
                                                                                   .randomTicks()
                                                                                   .instabreak()
                                                                                   .noCollission()
                                                                                   .strength(1.0F)
                                                                                   .sound(SoundType.BAMBOO_SAPLING)));
        BAMBOO = register("bamboo", new BambooBlock(of(Material.BAMBOO, MaterialColor.PLANT)
                                                            .randomTicks()
                                                            .instabreak()
                                                            .strength(1.0F)
                                                            .sound(SoundType.BAMBOO)
                                                            .noOcclusion()
                                                            .dynamicShape()));
        POTTED_BAMBOO = register("potted_bamboo",
                                 new FlowerPotBlock(BAMBOO, of(Material.DECORATION).instabreak().noOcclusion()));
        VOID_AIR = register("void_air", new AirBlock(of(Material.AIR).noCollission().noDrops().air()));
        CAVE_AIR = register("cave_air", new AirBlock(of(Material.AIR).noCollission().noDrops().air()));
        BUBBLE_COLUMN = register("bubble_column",
                                 new BubbleColumnBlock(of(Material.BUBBLE_COLUMN).noCollission().noDrops()));
        POLISHED_GRANITE_STAIRS = register("polished_granite_stairs",
                                           new StairBlock(POLISHED_GRANITE.defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_GRANITE)));
        SMOOTH_RED_SANDSTONE_STAIRS = register("smooth_red_sandstone_stairs", new StairBlock(SMOOTH_RED_SANDSTONE.defaultBlockState(),
                                                                                             BlockBehaviour.Properties.copy(SMOOTH_RED_SANDSTONE)));
        MOSSY_STONE_BRICK_STAIRS = register("mossy_stone_brick_stairs", new StairBlock(MOSSY_STONE_BRICKS.defaultBlockState(),
                                                                                       BlockBehaviour.Properties.copy(MOSSY_STONE_BRICKS)));
        POLISHED_DIORITE_STAIRS = register("polished_diorite_stairs",
                                           new StairBlock(POLISHED_DIORITE.defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_DIORITE)));
        MOSSY_COBBLESTONE_STAIRS = register("mossy_cobblestone_stairs",
                                            new StairBlock(MOSSY_COBBLESTONE.defaultBlockState(), BlockBehaviour.Properties.copy(MOSSY_COBBLESTONE)));
        END_STONE_BRICK_STAIRS = register("end_stone_brick_stairs",
                                          new StairBlock(END_STONE_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(END_STONE_BRICKS)));
        STONE_STAIRS = register("stone_stairs", new StairBlock(STONE.defaultBlockState(), BlockBehaviour.Properties.copy(STONE)));
        SMOOTH_SANDSTONE_STAIRS = register("smooth_sandstone_stairs",
                                           new StairBlock(SMOOTH_SANDSTONE.defaultBlockState(), BlockBehaviour.Properties.copy(SMOOTH_SANDSTONE)));
        SMOOTH_QUARTZ_STAIRS = register("smooth_quartz_stairs",
                                        new StairBlock(SMOOTH_QUARTZ.defaultBlockState(), BlockBehaviour.Properties.copy(SMOOTH_QUARTZ)));
        GRANITE_STAIRS = register("granite_stairs", new StairBlock(GRANITE.defaultBlockState(), BlockBehaviour.Properties.copy(GRANITE)));
        ANDESITE_STAIRS = register("andesite_stairs", new StairBlock(ANDESITE.defaultBlockState(), BlockBehaviour.Properties.copy(ANDESITE)));
        RED_NETHER_BRICK_STAIRS = register("red_nether_brick_stairs",
                                           new StairBlock(RED_NETHER_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(RED_NETHER_BRICKS)));
        POLISHED_ANDESITE_STAIRS = register("polished_andesite_stairs",
                                            new StairBlock(POLISHED_ANDESITE.defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_ANDESITE)));
        DIORITE_STAIRS = register("diorite_stairs", new StairBlock(DIORITE.defaultBlockState(), BlockBehaviour.Properties.copy(DIORITE)));
        POLISHED_GRANITE_SLAB = register("polished_granite_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_GRANITE)));
        SMOOTH_RED_SANDSTONE_SLAB = register("smooth_red_sandstone_slab", new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_RED_SANDSTONE)));
        MOSSY_STONE_BRICK_SLAB = register("mossy_stone_brick_slab", new SlabBlock(BlockBehaviour.Properties.copy(MOSSY_STONE_BRICKS)));
        POLISHED_DIORITE_SLAB = register("polished_diorite_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_DIORITE)));
        MOSSY_COBBLESTONE_SLAB = register("mossy_cobblestone_slab", new SlabBlock(BlockBehaviour.Properties.copy(MOSSY_COBBLESTONE)));
        END_STONE_BRICK_SLAB = register("end_stone_brick_slab", new SlabBlock(BlockBehaviour.Properties.copy(END_STONE_BRICKS)));
        SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_SANDSTONE)));
        SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", new SlabBlock(BlockBehaviour.Properties.copy(SMOOTH_QUARTZ)));
        GRANITE_SLAB = register("granite_slab", new SlabBlock(BlockBehaviour.Properties.copy(GRANITE)));
        ANDESITE_SLAB = register("andesite_slab", new SlabBlock(BlockBehaviour.Properties.copy(ANDESITE)));
        RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", new SlabBlock(BlockBehaviour.Properties.copy(RED_NETHER_BRICKS)));
        POLISHED_ANDESITE_SLAB = register("polished_andesite_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_ANDESITE)));
        DIORITE_SLAB = register("diorite_slab", new SlabBlock(BlockBehaviour.Properties.copy(DIORITE)));
        BRICK_WALL = register("brick_wall", new WallBlock(BlockBehaviour.Properties.copy(BRICKS)));
        PRISMARINE_WALL = register("prismarine_wall", new WallBlock(BlockBehaviour.Properties.copy(PRISMARINE)));
        RED_SANDSTONE_WALL = register("red_sandstone_wall", new WallBlock(BlockBehaviour.Properties.copy(RED_SANDSTONE)));
        MOSSY_STONE_BRICK_WALL = register("mossy_stone_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(MOSSY_STONE_BRICKS)));
        GRANITE_WALL = register("granite_wall", new WallBlock(BlockBehaviour.Properties.copy(GRANITE)));
        STONE_BRICK_WALL = register("stone_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(STONE_BRICKS)));
        NETHER_BRICK_WALL = register("nether_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(NETHER_BRICKS)));
        ANDESITE_WALL = register("andesite_wall", new WallBlock(BlockBehaviour.Properties.copy(ANDESITE)));
        RED_NETHER_BRICK_WALL = register("red_nether_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(RED_NETHER_BRICKS)));
        SANDSTONE_WALL = register("sandstone_wall", new WallBlock(BlockBehaviour.Properties.copy(SANDSTONE)));
        END_STONE_BRICK_WALL = register("end_stone_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(END_STONE_BRICKS)));
        DIORITE_WALL = register("diorite_wall", new WallBlock(BlockBehaviour.Properties.copy(DIORITE)));
        SCAFFOLDING = register("scaffolding", new ScaffoldingBlock(
                of(Material.DECORATION, MaterialColor.SAND).noCollission().sound(SoundType.SCAFFOLDING).dynamicShape()));
        LOOM = register("loom", new LoomBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        BARREL = register("barrel", new BarrelBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        SMOKER = register("smoker", new SmokerBlock(
                of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(0xDD))));
        BLAST_FURNACE = register("blast_furnace", new BlastFurnaceBlock(
                of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(0xDD))));
        CARTOGRAPHY_TABLE = register("cartography_table",
                                     new CartographyTableBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        FLETCHING_TABLE = register("fletching_table",
                                   new FletchingTableBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        GRINDSTONE = register("grindstone", new GrindstoneBlock(of(Material.HEAVY_METAL, MaterialColor.METAL)
                                                                        .requiresCorrectToolForDrops()
                                                                        .strength(2.0F, 6.0F)
                                                                        .sound(SoundType.STONE)));
        LECTERN = register("lectern", new LecternBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        SMITHING_TABLE = register("smithing_table",
                                  new SmithingTableBlock(of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        STONECUTTER = register("stonecutter",
                               new StonecutterBlock(of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F)));
        BELL = register("bell", new BellBlock(of(Material.METAL, MaterialColor.GOLD)
                                                      .requiresCorrectToolForDrops()
                                                      .strength(5.0F)
                                                      .sound(SoundType.ANVIL)));
        LANTERN = register("lantern", new LanternBlock(of(Material.METAL)
                                                               .requiresCorrectToolForDrops()
                                                               .strength(3.5F)
                                                               .sound(SoundType.LANTERN)
                                                               .lightLevel(BlockUtils.LIGHT_15)
                                                               .noOcclusion()));
        SOUL_LANTERN = register("soul_lantern", new LanternBlock(of(Material.METAL)
                                                                         .requiresCorrectToolForDrops()
                                                                         .strength(3.5F)
                                                                         .sound(SoundType.LANTERN)
                                                                         .lightLevel(BlockUtils.LIGHT_10)
                                                                         .noOcclusion()));
        CAMPFIRE = register("campfire", new CampfireBlock(true, 1, of(Material.WOOD, MaterialColor.PODZOL)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .lightLevel(litBlockEmission(0x8F))
                .noOcclusion()));
        SOUL_CAMPFIRE = register("soul_campfire", new CampfireBlock(false, 2, of(Material.WOOD, MaterialColor.PODZOL)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .lightLevel(litBlockEmission(0xaa0))
                .noOcclusion()));
        SWEET_BERRY_BUSH = register("sweet_berry_bush", new SweetBerryBushBlock(
                of(Material.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH)));
        WARPED_STEM = register("warped_stem", netherStem(MaterialColor.WARPED_STEM));
        STRIPPED_WARPED_STEM = register("stripped_warped_stem", netherStem(MaterialColor.WARPED_STEM));
        WARPED_HYPHAE = register("warped_hyphae", new RotatedPillarBlock(
                of(Material.NETHER_WOOD, MaterialColor.WARPED_HYPHAE).strength(2.0F).sound(SoundType.STEM)));
        STRIPPED_WARPED_HYPHAE = register("stripped_warped_hyphae", new RotatedPillarBlock(
                of(Material.NETHER_WOOD, MaterialColor.WARPED_HYPHAE).strength(2.0F).sound(SoundType.STEM)));
        WARPED_NYLIUM = register("warped_nylium", new NyliumBlock(of(Material.STONE, MaterialColor.WARPED_NYLIUM)
                                                                          .requiresCorrectToolForDrops()
                                                                          .strength(0.4F)
                                                                          .sound(SoundType.NYLIUM)
                                                                          .randomTicks()));
        WARPED_FUNGUS = register("warped_fungus", new FungusBlock(
                of(Material.PLANT, MaterialColor.COLOR_CYAN).instabreak().noCollission().sound(SoundType.FUNGUS), Mixin_FS_Blocks::_warped));
        WARPED_WART_BLOCK = register("warped_wart_block", new Block(
                of(Material.GRASS, MaterialColor.WARPED_WART_BLOCK).strength(1.0F).sound(SoundType.WART_BLOCK)));
        WARPED_ROOTS = register("warped_roots", new RootsBlock(
                of(Material.REPLACEABLE_FIREPROOF_PLANT, MaterialColor.COLOR_CYAN)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.ROOTS)));
        NETHER_SPROUTS = register("nether_sprouts", new NetherSproutsBlock(
                of(Material.REPLACEABLE_FIREPROOF_PLANT, MaterialColor.COLOR_CYAN)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.NETHER_SPROUTS)));
        CRIMSON_STEM = register("crimson_stem", netherStem(MaterialColor.CRIMSON_STEM));
        STRIPPED_CRIMSON_STEM = register("stripped_crimson_stem", netherStem(MaterialColor.CRIMSON_STEM));
        CRIMSON_HYPHAE = register("crimson_hyphae", new RotatedPillarBlock(
                of(Material.NETHER_WOOD, MaterialColor.CRIMSON_HYPHAE).strength(2.0F).sound(SoundType.STEM)));
        STRIPPED_CRIMSON_HYPHAE = register("stripped_crimson_hyphae", new RotatedPillarBlock(
                of(Material.NETHER_WOOD, MaterialColor.CRIMSON_HYPHAE).strength(2.0F).sound(SoundType.STEM)));
        CRIMSON_NYLIUM = register("crimson_nylium", new NyliumBlock(of(Material.STONE, MaterialColor.CRIMSON_NYLIUM)
                                                                            .requiresCorrectToolForDrops()
                                                                            .strength(0.4F)
                                                                            .sound(SoundType.NYLIUM)
                                                                            .randomTicks()));
        CRIMSON_FUNGUS = register("crimson_fungus", new FungusBlock(
                of(Material.PLANT, MaterialColor.NETHER).instabreak().noCollission().sound(SoundType.FUNGUS), Mixin_FS_Blocks::_crimson));
        SHROOMLIGHT = register("shroomlight", new Block(of(Material.GRASS, MaterialColor.COLOR_RED)
                                                                .strength(1.0F)
                                                                .sound(SoundType.SHROOMLIGHT)
                                                                .lightLevel(BlockUtils.LIGHT_15)));
        WEEPING_VINES = register("weeping_vines", new WeepingVinesBlock(of(Material.PLANT, MaterialColor.NETHER)
                                                                                .randomTicks()
                                                                                .noCollission()
                                                                                .instabreak()
                                                                                .sound(SoundType.WEEPING_VINES)));
        WEEPING_VINES_PLANT = register("weeping_vines_plant", new WeepingVinesPlantBlock(
                of(Material.PLANT, MaterialColor.NETHER).noCollission().instabreak().sound(SoundType.WEEPING_VINES)));
        TWISTING_VINES = register("twisting_vines", new TwistingVinesBlock(of(Material.PLANT, MaterialColor.COLOR_CYAN)
                                                                                   .randomTicks()
                                                                                   .noCollission()
                                                                                   .instabreak()
                                                                                   .sound(SoundType.WEEPING_VINES)));
        TWISTING_VINES_PLANT = register("twisting_vines_plant", new TwistingVinesPlantBlock(
                of(Material.PLANT, MaterialColor.COLOR_CYAN).noCollission().instabreak().sound(SoundType.WEEPING_VINES)));
        CRIMSON_ROOTS = register("crimson_roots", new RootsBlock(
                of(Material.REPLACEABLE_FIREPROOF_PLANT, MaterialColor.NETHER)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.ROOTS)));
        CRIMSON_PLANKS = register("crimson_planks", new Block(
                of(Material.NETHER_WOOD, MaterialColor.CRIMSON_STEM).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        WARPED_PLANKS = register("warped_planks", new Block(
                of(Material.NETHER_WOOD, MaterialColor.WARPED_STEM).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        CRIMSON_SLAB = register("crimson_slab", new SlabBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)));
        WARPED_SLAB = register("warped_slab", new SlabBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        CRIMSON_PRESSURE_PLATE = register("crimson_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                           of(Material.NETHER_WOOD,
                                                                                              CRIMSON_PLANKS.defaultMaterialColor())
                                                                                                   .noCollission()
                                                                                                   .strength(0.5F)
                                                                                                   .sound(SoundType.WOOD)));
        WARPED_PRESSURE_PLATE = register("warped_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                                                                                         of(Material.NETHER_WOOD,
                                                                                            WARPED_PLANKS.defaultMaterialColor())
                                                                                                 .noCollission()
                                                                                                 .strength(0.5F)
                                                                                                 .sound(SoundType.WOOD)));
        CRIMSON_FENCE = register("crimson_fence", new FenceBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)));
        WARPED_FENCE = register("warped_fence", new FenceBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        CRIMSON_TRAPDOOR = register("crimson_trapdoor", new TrapDoorBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .strength(3.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        WARPED_TRAPDOOR = register("warped_trapdoor", new TrapDoorBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor())
                        .strength(3.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .isValidSpawn_(BlockUtils.NEVER_SPAWN)));
        CRIMSON_FENCE_GATE = register("crimson_fence_gate", new FenceGateBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)));
        WARPED_FENCE_GATE = register("warped_fence_gate", new FenceGateBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
        CRIMSON_STAIRS = register("crimson_stairs",
                                  new StairBlock(CRIMSON_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(CRIMSON_PLANKS)));
        WARPED_STAIRS = register("warped_stairs", new StairBlock(WARPED_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(WARPED_PLANKS)));
        CRIMSON_BUTTON = register("crimson_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        WARPED_BUTTON = register("warped_button", new WoodButtonBlock(
                of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
        CRIMSON_DOOR = register("crimson_door", new DoorBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .strength(3.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()));
        WARPED_DOOR = register("warped_door", new DoorBlock(of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor())
                                                                    .strength(3.0F)
                                                                    .sound(SoundType.WOOD)
                                                                    .noOcclusion()));
        CRIMSON_SIGN = register("crimson_sign", new StandingSignBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .noCollission()
                        .strength(1.0F)
                        .sound(SoundType.WOOD), WoodType.CRIMSON));
        WARPED_SIGN = register("warped_sign", new StandingSignBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor())
                        .noCollission()
                        .strength(1.0F)
                        .sound(SoundType.WOOD), WoodType.WARPED));
        CRIMSON_WALL_SIGN = register("crimson_wall_sign", new WallSignBlock(
                of(Material.NETHER_WOOD, CRIMSON_PLANKS.defaultMaterialColor())
                        .noCollission()
                        .strength(1.0F)
                        .sound(SoundType.WOOD)
                        .dropsLike(CRIMSON_SIGN), WoodType.CRIMSON));
        WARPED_WALL_SIGN = register("warped_wall_sign", new WallSignBlock(
                of(Material.NETHER_WOOD, WARPED_PLANKS.defaultMaterialColor())
                        .noCollission()
                        .strength(1.0F)
                        .sound(SoundType.WOOD)
                        .dropsLike(WARPED_SIGN), WoodType.WARPED));
        STRUCTURE_BLOCK = register("structure_block", new StructureBlock(of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY)
                                                                                 .requiresCorrectToolForDrops()
                                                                                 .strength(-1.0F, 3_600_000.0F)
                                                                                 .noDrops()));
        JIGSAW = register("jigsaw", new JigsawBlock(of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY)
                                                            .requiresCorrectToolForDrops()
                                                            .strength(-1.0F, 3_600_000.0F)
                                                            .noDrops()));
        COMPOSTER = register("composter", new ComposterBlock(of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD)));
        TARGET = register("target",
                          new TargetBlock(of(Material.GRASS, MaterialColor.QUARTZ).strength(0.5F).sound(SoundType.GRASS)));
        BEE_NEST = register("bee_nest", new BeehiveBlock(
                of(Material.WOOD, MaterialColor.COLOR_YELLOW).strength(0.3F).sound(SoundType.WOOD)));
        BEEHIVE = register("beehive", new BeehiveBlock(of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD)));
        HONEY_BLOCK = register("honey_block", new HoneyBlock(of(Material.CLAY, MaterialColor.COLOR_ORANGE)
                                                                     .speedFactor(0.4F)
                                                                     .jumpFactor(0.5F)
                                                                     .noOcclusion()
                                                                     .sound(SoundType.HONEY_BLOCK)));
        HONEYCOMB_BLOCK = register("honeycomb_block", new Block(
                of(Material.CLAY, MaterialColor.COLOR_ORANGE).strength(0.6F).sound(SoundType.CORAL_BLOCK)));
        NETHERITE_BLOCK = register("netherite_block", new Block(of(Material.METAL, MaterialColor.COLOR_BLACK)
                                                                        .requiresCorrectToolForDrops()
                                                                        .strength(50.0F, 1_200.0F)
                                                                        .sound(SoundType.NETHERITE_BLOCK)));
        ANCIENT_DEBRIS = register("ancient_debris", new Block(of(Material.METAL, MaterialColor.COLOR_BLACK)
                                                                      .requiresCorrectToolForDrops()
                                                                      .strength(30.0F, 1_200.0F)
                                                                      .sound(SoundType.ANCIENT_DEBRIS)));
        CRYING_OBSIDIAN = register("crying_obsidian", new CryingObsidianBlock(of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                                      .requiresCorrectToolForDrops()
                                                                                      .strength(50.0F, 1_200.0F)
                                                                                      .lightLevel(BlockUtils.LIGHT_10)));
        RESPAWN_ANCHOR = register("respawn_anchor", new RespawnAnchorBlock(of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                                   .requiresCorrectToolForDrops()
                                                                                   .strength(50.0F, 1_200.0F)
                                                                                   .lightLevel(Mixin_FS_Blocks::_respawnAnchor)));
        POTTED_CRIMSON_FUNGUS = register("potted_crimson_fungus", new FlowerPotBlock(CRIMSON_FUNGUS, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_WARPED_FUNGUS = register("potted_warped_fungus", new FlowerPotBlock(WARPED_FUNGUS, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_CRIMSON_ROOTS = register("potted_crimson_roots", new FlowerPotBlock(CRIMSON_ROOTS, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        POTTED_WARPED_ROOTS = register("potted_warped_roots", new FlowerPotBlock(WARPED_ROOTS, of(Material.DECORATION)
                .instabreak()
                .noOcclusion()));
        LODESTONE = register("lodestone", new Block(
                of(Material.HEAVY_METAL).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.LODESTONE)));
        BLACKSTONE = register("blackstone", new Block(
                of(Material.STONE, MaterialColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
        BLACKSTONE_STAIRS = register("blackstone_stairs", new StairBlock(BLACKSTONE.defaultBlockState(), BlockBehaviour.Properties.copy(BLACKSTONE)));
        BLACKSTONE_WALL = register("blackstone_wall", new WallBlock(BlockBehaviour.Properties.copy(BLACKSTONE)));
        BLACKSTONE_SLAB = register("blackstone_slab", new SlabBlock(BlockBehaviour.Properties.copy(BLACKSTONE).strength(2.0F, 6.0F)));
        POLISHED_BLACKSTONE = register("polished_blackstone", new Block(BlockBehaviour.Properties.copy(BLACKSTONE).strength(2.0F, 6.0F)));
        POLISHED_BLACKSTONE_BRICKS = register("polished_blackstone_bricks",
                                              new Block(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F)));
        CRACKED_POLISHED_BLACKSTONE_BRICKS = register("cracked_polished_blackstone_bricks",
                                                      new Block(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE_BRICKS)));
        CHISELED_POLISHED_BLACKSTONE = register("chiseled_polished_blackstone",
                                                new Block(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE).strength(1.5F, 6.0F)));
        POLISHED_BLACKSTONE_BRICK_SLAB = register("polished_blackstone_brick_slab",
                                                  new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE_BRICKS).strength(2.0F, 6.0F)));
        POLISHED_BLACKSTONE_BRICK_STAIRS = register("polished_blackstone_brick_stairs", new StairBlock(POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
                                                                                                       BlockBehaviour.Properties.copy(
                                                                                                               POLISHED_BLACKSTONE_BRICKS)));
        POLISHED_BLACKSTONE_BRICK_WALL = register("polished_blackstone_brick_wall",
                                                  new WallBlock(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE_BRICKS)));
        GILDED_BLACKSTONE = register("gilded_blackstone", new Block(BlockBehaviour.Properties.copy(BLACKSTONE).sound(SoundType.GILDED_BLACKSTONE)));
        POLISHED_BLACKSTONE_STAIRS = register("polished_blackstone_stairs", new StairBlock(POLISHED_BLACKSTONE.defaultBlockState(),
                                                                                           BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE)));
        POLISHED_BLACKSTONE_SLAB = register("polished_blackstone_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE)));
        POLISHED_BLACKSTONE_PRESSURE_PLATE = register("polished_blackstone_pressure_plate",
                                                      new PressurePlateBlock(PressurePlateBlock.Sensitivity.MOBS,
                                                                             of(Material.STONE, MaterialColor.COLOR_BLACK)
                                                                                     .requiresCorrectToolForDrops()
                                                                                     .noCollission()
                                                                                     .strength(0.5F)));
        POLISHED_BLACKSTONE_BUTTON = register("polished_blackstone_button",
                                              new StoneButtonBlock(of(Material.DECORATION).noCollission().strength(0.5F)));
        POLISHED_BLACKSTONE_WALL = register("polished_blackstone_wall", new WallBlock(BlockBehaviour.Properties.copy(POLISHED_BLACKSTONE)));
        CHISELED_NETHER_BRICKS = register("chiseled_nether_bricks", new Block(of(Material.STONE, MaterialColor.NETHER)
                                                                                      .requiresCorrectToolForDrops()
                                                                                      .strength(2.0F, 6.0F)
                                                                                      .sound(SoundType.NETHER_BRICKS)));
        CRACKED_NETHER_BRICKS = register("cracked_nether_bricks", new Block(of(Material.STONE, MaterialColor.NETHER)
                                                                                    .requiresCorrectToolForDrops()
                                                                                    .strength(2.0F, 6.0F)
                                                                                    .sound(SoundType.NETHER_BRICKS)));
        QUARTZ_BRICKS = register("quartz_bricks", new Block(BlockBehaviour.Properties.copy(QUARTZ_BLOCK)));
        CANDLE = register("candle", new CandleBlock(of(Material.DECORATION, MaterialColor.SAND)
                                                            .noOcclusion()
                                                            .strength(0.1F)
                                                            .sound(SoundType.CANDLE)
                                                            .lightLevel(CandleBlock.LIGHT_EMISSION)));
        WHITE_CANDLE = register("white_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.WOOL)
                                                                        .noOcclusion()
                                                                        .strength(0.1F)
                                                                        .sound(SoundType.CANDLE)
                                                                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        ORANGE_CANDLE = register("orange_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_ORANGE)
                                                                          .noOcclusion()
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.CANDLE)
                                                                          .lightLevel(CandleBlock.LIGHT_EMISSION)));
        MAGENTA_CANDLE = register("magenta_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_MAGENTA)
                                                                            .noOcclusion()
                                                                            .strength(0.1F)
                                                                            .sound(SoundType.CANDLE)
                                                                            .lightLevel(CandleBlock.LIGHT_EMISSION)));
        LIGHT_BLUE_CANDLE = register("light_blue_candle", new CandleBlock(
                of(Material.DECORATION, MaterialColor.COLOR_LIGHT_BLUE)
                        .noOcclusion()
                        .strength(0.1F)
                        .sound(SoundType.CANDLE)
                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        YELLOW_CANDLE = register("yellow_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_YELLOW)
                                                                          .noOcclusion()
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.CANDLE)
                                                                          .lightLevel(CandleBlock.LIGHT_EMISSION)));
        LIME_CANDLE = register("lime_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_LIGHT_GREEN)
                                                                      .noOcclusion()
                                                                      .strength(0.1F)
                                                                      .sound(SoundType.CANDLE)
                                                                      .lightLevel(CandleBlock.LIGHT_EMISSION)));
        PINK_CANDLE = register("pink_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_PINK)
                                                                      .noOcclusion()
                                                                      .strength(0.1F)
                                                                      .sound(SoundType.CANDLE)
                                                                      .lightLevel(CandleBlock.LIGHT_EMISSION)));
        GRAY_CANDLE = register("gray_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_GRAY)
                                                                      .noOcclusion()
                                                                      .strength(0.1F)
                                                                      .sound(SoundType.CANDLE)
                                                                      .lightLevel(CandleBlock.LIGHT_EMISSION)));
        LIGHT_GRAY_CANDLE = register("light_gray_candle", new CandleBlock(
                of(Material.DECORATION, MaterialColor.COLOR_LIGHT_GRAY)
                        .noOcclusion()
                        .strength(0.1F)
                        .sound(SoundType.CANDLE)
                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        CYAN_CANDLE = register("cyan_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_CYAN)
                                                                      .noOcclusion()
                                                                      .strength(0.1F)
                                                                      .sound(SoundType.CANDLE)
                                                                      .lightLevel(CandleBlock.LIGHT_EMISSION)));
        PURPLE_CANDLE = register("purple_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_PURPLE)
                                                                          .noOcclusion()
                                                                          .strength(0.1F)
                                                                          .sound(SoundType.CANDLE)
                                                                          .lightLevel(CandleBlock.LIGHT_EMISSION)));
        BLUE_CANDLE = register("blue_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_BLUE)
                                                                      .noOcclusion()
                                                                      .strength(0.1F)
                                                                      .sound(SoundType.CANDLE)
                                                                      .lightLevel(CandleBlock.LIGHT_EMISSION)));
        BROWN_CANDLE = register("brown_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_BROWN)
                                                                        .noOcclusion()
                                                                        .strength(0.1F)
                                                                        .sound(SoundType.CANDLE)
                                                                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        GREEN_CANDLE = register("green_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_GREEN)
                                                                        .noOcclusion()
                                                                        .strength(0.1F)
                                                                        .sound(SoundType.CANDLE)
                                                                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        RED_CANDLE = register("red_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_RED)
                                                                    .noOcclusion()
                                                                    .strength(0.1F)
                                                                    .sound(SoundType.CANDLE)
                                                                    .lightLevel(CandleBlock.LIGHT_EMISSION)));
        BLACK_CANDLE = register("black_candle", new CandleBlock(of(Material.DECORATION, MaterialColor.COLOR_BLACK)
                                                                        .noOcclusion()
                                                                        .strength(0.1F)
                                                                        .sound(SoundType.CANDLE)
                                                                        .lightLevel(CandleBlock.LIGHT_EMISSION)));
        CANDLE_CAKE = register("candle_cake", new CandleCakeBlock(CANDLE, BlockBehaviour.Properties.copy(CAKE).lightLevel(litBlockEmission(0x33))));
        WHITE_CANDLE_CAKE = register("white_candle_cake", new CandleCakeBlock(WHITE_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        ORANGE_CANDLE_CAKE = register("orange_candle_cake", new CandleCakeBlock(ORANGE_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        MAGENTA_CANDLE_CAKE = register("magenta_candle_cake", new CandleCakeBlock(MAGENTA_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        LIGHT_BLUE_CANDLE_CAKE = register("light_blue_candle_cake",
                                          new CandleCakeBlock(LIGHT_BLUE_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        YELLOW_CANDLE_CAKE = register("yellow_candle_cake", new CandleCakeBlock(YELLOW_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        LIME_CANDLE_CAKE = register("lime_candle_cake", new CandleCakeBlock(LIME_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        PINK_CANDLE_CAKE = register("pink_candle_cake", new CandleCakeBlock(PINK_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        GRAY_CANDLE_CAKE = register("gray_candle_cake", new CandleCakeBlock(GRAY_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        LIGHT_GRAY_CANDLE_CAKE = register("light_gray_candle_cake",
                                          new CandleCakeBlock(LIGHT_GRAY_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        CYAN_CANDLE_CAKE = register("cyan_candle_cake", new CandleCakeBlock(CYAN_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        PURPLE_CANDLE_CAKE = register("purple_candle_cake", new CandleCakeBlock(PURPLE_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        BLUE_CANDLE_CAKE = register("blue_candle_cake", new CandleCakeBlock(BLUE_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        BROWN_CANDLE_CAKE = register("brown_candle_cake", new CandleCakeBlock(BROWN_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        GREEN_CANDLE_CAKE = register("green_candle_cake", new CandleCakeBlock(GREEN_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        RED_CANDLE_CAKE = register("red_candle_cake", new CandleCakeBlock(RED_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        BLACK_CANDLE_CAKE = register("black_candle_cake", new CandleCakeBlock(BLACK_CANDLE, BlockBehaviour.Properties.copy(CANDLE_CAKE)));
        AMETHYST_BLOCK = register("amethyst_block", new AmethystBlock(of(Material.AMETHYST, MaterialColor.COLOR_PURPLE)
                                                                              .strength(1.5F)
                                                                              .sound(SoundType.AMETHYST)
                                                                              .requiresCorrectToolForDrops()));
        BUDDING_AMETHYST = register("budding_amethyst", new BuddingAmethystBlock(of(Material.AMETHYST)
                                                                                         .randomTicks()
                                                                                         .strength(1.5F)
                                                                                         .sound(SoundType.AMETHYST)
                                                                                         .requiresCorrectToolForDrops()));
        AMETHYST_CLUSTER = register("amethyst_cluster", new AmethystClusterBlock(7, 3, of(Material.AMETHYST)
                .noOcclusion()
                .randomTicks()
                .sound(SoundType.AMETHYST_CLUSTER)
                .strength(1.5F)
                .lightLevel(BlockUtils.LIGHT_5)));
        LARGE_AMETHYST_BUD = register("large_amethyst_bud", new AmethystClusterBlock(5, 3,
                                                                                     BlockBehaviour.Properties.copy(AMETHYST_CLUSTER)
                                                                                                              .sound(SoundType.MEDIUM_AMETHYST_BUD)
                                                                                                              .lightLevel(BlockUtils.LIGHT_4)));
        MEDIUM_AMETHYST_BUD = register("medium_amethyst_bud", new AmethystClusterBlock(4, 3,
                                                                                       BlockBehaviour.Properties.copy(AMETHYST_CLUSTER)
                                                                                                                .sound(SoundType.LARGE_AMETHYST_BUD)
                                                                                                                .lightLevel(BlockUtils.LIGHT_2)));
        SMALL_AMETHYST_BUD = register("small_amethyst_bud", new AmethystClusterBlock(3, 4,
                                                                                     BlockBehaviour.Properties.copy(AMETHYST_CLUSTER)
                                                                                                              .sound(SoundType.SMALL_AMETHYST_BUD)
                                                                                                              .lightLevel(BlockUtils.LIGHT_1)));
        TUFF = register("tuff", new Block(of(Material.STONE, MaterialColor.TERRACOTTA_GRAY)
                                                  .sound(SoundType.TUFF)
                                                  .requiresCorrectToolForDrops()
                                                  .strength(1.5F, 6.0F)));
        CALCITE = register("calcite", new Block(of(Material.STONE, MaterialColor.TERRACOTTA_WHITE)
                                                        .sound(SoundType.CALCITE)
                                                        .requiresCorrectToolForDrops()
                                                        .strength(0.75F)));
        TINTED_GLASS = register("tinted_glass", new TintedGlassBlock(BlockBehaviour.Properties.copy(GLASS)
                                                                                              .color(MaterialColor.COLOR_GRAY)
                                                                                              .noOcclusion()
                                                                                              .isValidSpawn_(BlockUtils.NEVER_SPAWN)
                                                                                              .isRedstoneConductor_(BlockUtils.NEVER)
                                                                                              .isSuffocating_(BlockUtils.NEVER)
                                                                                              .isViewBlocking_(BlockUtils.NEVER)));
        POWDER_SNOW = register("powder_snow", new PowderSnowBlock(
                of(Material.POWDER_SNOW).strength(0.25F).sound(SoundType.POWDER_SNOW).dynamicShape()));
        SCULK_SENSOR = register("sculk_sensor", new SculkSensorBlock(of(Material.SCULK, MaterialColor.COLOR_CYAN)
                                                                             .strength(1.5F)
                                                                             .sound(SoundType.SCULK_SENSOR)
                                                                             .lightLevel(BlockUtils.LIGHT_1)
                                                                             .emissiveRendering_(Mixin_FS_Blocks::_sculkSensor), 8));
        OXIDIZED_COPPER = register("oxidized_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED,
                                                                                    of(Material.METAL,
                                                                                       MaterialColor.WARPED_NYLIUM)
                                                                                            .requiresCorrectToolForDrops()
                                                                                            .strength(3.0F, 6.0F)
                                                                                            .sound(SoundType.COPPER)));
        WEATHERED_COPPER = register("weathered_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED,
                                                                                      of(Material.METAL,
                                                                                         MaterialColor.WARPED_STEM)
                                                                                              .requiresCorrectToolForDrops()
                                                                                              .strength(3.0F, 6.0F)
                                                                                              .sound(SoundType.COPPER)));
        EXPOSED_COPPER = register("exposed_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED,
                                                                                  of(Material.METAL,
                                                                                     MaterialColor.TERRACOTTA_LIGHT_GRAY)
                                                                                          .requiresCorrectToolForDrops()
                                                                                          .strength(3.0F, 6.0F)
                                                                                          .sound(SoundType.COPPER)));
        COPPER_BLOCK = register("copper_block", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED,
                                                                              of(Material.METAL, MaterialColor.COLOR_ORANGE)
                                                                                      .requiresCorrectToolForDrops()
                                                                                      .strength(3.0F, 6.0F)
                                                                                      .sound(SoundType.COPPER)));
        COPPER_ORE = register("copper_ore", new OreBlock(BlockBehaviour.Properties.copy(IRON_ORE)));
        DEEPSLATE_COPPER_ORE = register("deepslate_copper_ore", new OreBlock(
                BlockBehaviour.Properties.copy(COPPER_ORE).color(MaterialColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
        OXIDIZED_CUT_COPPER = register("oxidized_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED,
                                                                                            BlockBehaviour.Properties.copy(OXIDIZED_COPPER)));
        WEATHERED_CUT_COPPER = register("weathered_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED,
                                                                                              BlockBehaviour.Properties.copy(WEATHERED_COPPER)));
        EXPOSED_CUT_COPPER = register("exposed_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED,
                                                                                          BlockBehaviour.Properties.copy(EXPOSED_COPPER)));
        CUT_COPPER = register("cut_copper",
                              new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.copy(COPPER_BLOCK)));
        OXIDIZED_CUT_COPPER_STAIRS = register("oxidized_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.OXIDIZED,
                                                                                                           OXIDIZED_CUT_COPPER.defaultBlockState(),
                                                                                                           BlockBehaviour.Properties.copy(
                                                                                                                   OXIDIZED_CUT_COPPER)));
        WEATHERED_CUT_COPPER_STAIRS = register("weathered_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.WEATHERED,
                                                                                                             WEATHERED_CUT_COPPER.defaultBlockState(),
                                                                                                             BlockBehaviour.Properties.copy(
                                                                                                                     WEATHERED_COPPER)));
        EXPOSED_CUT_COPPER_STAIRS = register("exposed_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.EXPOSED,
                                                                                                         EXPOSED_CUT_COPPER.defaultBlockState(),
                                                                                                         BlockBehaviour.Properties.copy(
                                                                                                                 EXPOSED_COPPER)));
        CUT_COPPER_STAIRS = register("cut_copper_stairs",
                                     new WeatheringCopperStairBlock(WeatheringCopper.WeatherState.UNAFFECTED, CUT_COPPER.defaultBlockState(),
                                                                    BlockBehaviour.Properties.copy(COPPER_BLOCK)));
        OXIDIZED_CUT_COPPER_SLAB = register("oxidized_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.OXIDIZED,
                                                                                                      BlockBehaviour.Properties.copy(
                                                                                                                            OXIDIZED_CUT_COPPER)
                                                                                                                               .requiresCorrectToolForDrops()));
        WEATHERED_CUT_COPPER_SLAB = register("weathered_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.WEATHERED,
                                                                                                        BlockBehaviour.Properties.copy(
                                                                                                                              WEATHERED_CUT_COPPER)
                                                                                                                                 .requiresCorrectToolForDrops()));
        EXPOSED_CUT_COPPER_SLAB = register("exposed_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.EXPOSED,
                                                                                                    BlockBehaviour.Properties.copy(EXPOSED_CUT_COPPER)
                                                                                                                             .requiresCorrectToolForDrops()));
        CUT_COPPER_SLAB = register("cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.WeatherState.UNAFFECTED,
                                                                                    BlockBehaviour.Properties.copy(CUT_COPPER)
                                                                                                             .requiresCorrectToolForDrops()));
        WAXED_COPPER_BLOCK = register("waxed_copper_block", new Block(BlockBehaviour.Properties.copy(COPPER_BLOCK)));
        WAXED_WEATHERED_COPPER = register("waxed_weathered_copper", new Block(BlockBehaviour.Properties.copy(WEATHERED_COPPER)));
        WAXED_EXPOSED_COPPER = register("waxed_exposed_copper", new Block(BlockBehaviour.Properties.copy(EXPOSED_COPPER)));
        WAXED_OXIDIZED_COPPER = register("waxed_oxidized_copper", new Block(BlockBehaviour.Properties.copy(OXIDIZED_COPPER)));
        WAXED_OXIDIZED_CUT_COPPER = register("waxed_oxidized_cut_copper", new Block(BlockBehaviour.Properties.copy(OXIDIZED_COPPER)));
        WAXED_WEATHERED_CUT_COPPER = register("waxed_weathered_cut_copper", new Block(BlockBehaviour.Properties.copy(WEATHERED_COPPER)));
        WAXED_EXPOSED_CUT_COPPER = register("waxed_exposed_cut_copper", new Block(BlockBehaviour.Properties.copy(EXPOSED_COPPER)));
        WAXED_CUT_COPPER = register("waxed_cut_copper", new Block(BlockBehaviour.Properties.copy(COPPER_BLOCK)));
        WAXED_OXIDIZED_CUT_COPPER_STAIRS = register("waxed_oxidized_cut_copper_stairs", new StairBlock(WAXED_OXIDIZED_CUT_COPPER.defaultBlockState(),
                                                                                                       BlockBehaviour.Properties.copy(
                                                                                                               OXIDIZED_COPPER)));
        WAXED_WEATHERED_CUT_COPPER_STAIRS = register("waxed_weathered_cut_copper_stairs",
                                                     new StairBlock(WAXED_WEATHERED_CUT_COPPER.defaultBlockState(),
                                                                    BlockBehaviour.Properties.copy(WEATHERED_COPPER)));
        WAXED_EXPOSED_CUT_COPPER_STAIRS = register("waxed_exposed_cut_copper_stairs", new StairBlock(WAXED_EXPOSED_CUT_COPPER.defaultBlockState(),
                                                                                                     BlockBehaviour.Properties.copy(EXPOSED_COPPER)));
        WAXED_CUT_COPPER_STAIRS = register("waxed_cut_copper_stairs",
                                           new StairBlock(WAXED_CUT_COPPER.defaultBlockState(), BlockBehaviour.Properties.copy(COPPER_BLOCK)));
        WAXED_OXIDIZED_CUT_COPPER_SLAB = register("waxed_oxidized_cut_copper_slab", new SlabBlock(
                BlockBehaviour.Properties.copy(WAXED_OXIDIZED_CUT_COPPER).requiresCorrectToolForDrops()));
        WAXED_WEATHERED_CUT_COPPER_SLAB = register("waxed_weathered_cut_copper_slab", new SlabBlock(
                BlockBehaviour.Properties.copy(WAXED_WEATHERED_CUT_COPPER).requiresCorrectToolForDrops()));
        WAXED_EXPOSED_CUT_COPPER_SLAB = register("waxed_exposed_cut_copper_slab", new SlabBlock(
                BlockBehaviour.Properties.copy(WAXED_EXPOSED_CUT_COPPER).requiresCorrectToolForDrops()));
        WAXED_CUT_COPPER_SLAB = register("waxed_cut_copper_slab",
                                         new SlabBlock(BlockBehaviour.Properties.copy(WAXED_CUT_COPPER).requiresCorrectToolForDrops()));
        LIGHTNING_ROD = register("lightning_rod", new LightningRodBlock(of(Material.METAL, MaterialColor.COLOR_ORANGE)
                                                                                .requiresCorrectToolForDrops()
                                                                                .strength(3.0F, 6.0F)
                                                                                .sound(SoundType.COPPER)
                                                                                .noOcclusion()));
        POINTED_DRIPSTONE = register("pointed_dripstone", new PointedDripstoneBlock(of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).noOcclusion().sound(SoundType.POINTED_DRIPSTONE).randomTicks().strength(1.5F, 3.0F).dynamicShape()));
        DRIPSTONE_BLOCK = register("dripstone_block", new Block(of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).sound(SoundType.DRIPSTONE_BLOCK).requiresCorrectToolForDrops().strength(1.5F, 1.0F)));
        CAVE_VINES = register("cave_vines", new CaveVinesBlock(of(Material.PLANT).randomTicks().noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundType.CAVE_VINES)));
        CAVE_VINES_PLANT = register("cave_vines_plant", new CaveVinesPlantBlock(of(Material.PLANT).noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundType.CAVE_VINES)));
        SPORE_BLOSSOM = register("spore_blossom", new SporeBlossomBlock(of(Material.PLANT).instabreak().noCollission().sound(SoundType.SPORE_BLOSSOM)));
        AZALEA = register("azalea", new AzaleaBlock(of(Material.PLANT).instabreak().sound(SoundType.AZALEA).noOcclusion()));
        FLOWERING_AZALEA = register("flowering_azalea", new AzaleaBlock(of(Material.PLANT).instabreak().sound(SoundType.FLOWERING_AZALEA).noOcclusion()));
        MOSS_CARPET = register("moss_carpet", new CarpetBlock(of(Material.PLANT, MaterialColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS_CARPET)));
        MOSS_BLOCK = register("moss_block", new MossBlock(of(Material.MOSS, MaterialColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS)));
        BIG_DRIPLEAF = register("big_dripleaf", new BigDripleafBlock(of(Material.PLANT).strength(0.1F).sound(SoundType.BIG_DRIPLEAF)));
        BIG_DRIPLEAF_STEM = register("big_dripleaf_stem", new BigDripleafStemBlock(of(Material.PLANT).noCollission().strength(0.1F).sound(SoundType.BIG_DRIPLEAF)));
        SMALL_DRIPLEAF = register("small_dripleaf", new SmallDripleafBlock(of(Material.PLANT).noCollission().instabreak().sound(SoundType.SMALL_DRIPLEAF)));
        HANGING_ROOTS = register("hanging_roots", new HangingRootsBlock(of(Material.REPLACEABLE_PLANT, MaterialColor.DIRT).noCollission().instabreak().sound(SoundType.HANGING_ROOTS)));
        ROOTED_DIRT = register("rooted_dirt", new RootedDirtBlock(of(Material.DIRT, MaterialColor.DIRT).strength(0.5F).sound(SoundType.ROOTED_DIRT)));
        DEEPSLATE = register("deepslate", new RotatedPillarBlock(of(Material.STONE, MaterialColor.DEEPSLATE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE)));
        COBBLED_DEEPSLATE = register("cobbled_deepslate", new Block(BlockBehaviour.Properties.copy(DEEPSLATE).strength(3.5F, 6.0F)));
        COBBLED_DEEPSLATE_STAIRS = register("cobbled_deepslate_stairs", new StairBlock(COBBLED_DEEPSLATE.defaultBlockState(), BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE)));
        COBBLED_DEEPSLATE_SLAB = register("cobbled_deepslate_slab", new SlabBlock(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE)));
        COBBLED_DEEPSLATE_WALL = register("cobbled_deepslate_wall", new WallBlock(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE)));
        POLISHED_DEEPSLATE = register("polished_deepslate", new Block(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE).sound(SoundType.POLISHED_DEEPSLATE)));
        POLISHED_DEEPSLATE_STAIRS = register("polished_deepslate_stairs", new StairBlock(POLISHED_DEEPSLATE.defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_DEEPSLATE)));
        POLISHED_DEEPSLATE_SLAB = register("polished_deepslate_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_DEEPSLATE)));
        POLISHED_DEEPSLATE_WALL = register("polished_deepslate_wall", new WallBlock(BlockBehaviour.Properties.copy(POLISHED_DEEPSLATE)));
        DEEPSLATE_TILES = register("deepslate_tiles", new Block(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_TILES)));
        DEEPSLATE_TILE_STAIRS = register("deepslate_tile_stairs", new StairBlock(DEEPSLATE_TILES.defaultBlockState(), BlockBehaviour.Properties.copy(DEEPSLATE_TILES)));
        DEEPSLATE_TILE_SLAB = register("deepslate_tile_slab", new SlabBlock(BlockBehaviour.Properties.copy(DEEPSLATE_TILES)));
        DEEPSLATE_TILE_WALL = register("deepslate_tile_wall", new WallBlock(BlockBehaviour.Properties.copy(DEEPSLATE_TILES)));
        DEEPSLATE_BRICKS = register("deepslate_bricks", new Block(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS)));
        DEEPSLATE_BRICK_STAIRS = register("deepslate_brick_stairs", new StairBlock(DEEPSLATE_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(DEEPSLATE_BRICKS)));
        DEEPSLATE_BRICK_SLAB = register("deepslate_brick_slab", new SlabBlock(BlockBehaviour.Properties.copy(DEEPSLATE_BRICKS)));
        DEEPSLATE_BRICK_WALL = register("deepslate_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(DEEPSLATE_BRICKS)));
        CHISELED_DEEPSLATE = register("chiseled_deepslate", new Block(BlockBehaviour.Properties.copy(COBBLED_DEEPSLATE).sound(SoundType.DEEPSLATE_BRICKS)));
        CRACKED_DEEPSLATE_BRICKS = register("cracked_deepslate_bricks", new Block(BlockBehaviour.Properties.copy(DEEPSLATE_BRICKS)));
        CRACKED_DEEPSLATE_TILES = register("cracked_deepslate_tiles", new Block(BlockBehaviour.Properties.copy(DEEPSLATE_TILES)));
        INFESTED_DEEPSLATE = register("infested_deepslate", new InfestedRotatedPillarBlock(DEEPSLATE, of(Material.CLAY, MaterialColor.DEEPSLATE).sound(SoundType.DEEPSLATE)));
        SMOOTH_BASALT = register("smooth_basalt", new Block(BlockBehaviour.Properties.copy(BASALT)));
        RAW_IRON_BLOCK = register("raw_iron_block", new Block(of(Material.STONE, MaterialColor.RAW_IRON).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
        RAW_COPPER_BLOCK = register("raw_copper_block", new Block(of(Material.STONE, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
        RAW_GOLD_BLOCK = register("raw_gold_block", new Block(of(Material.STONE, MaterialColor.GOLD).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
        POTTED_AZALEA = register("potted_azalea_bush", new FlowerPotBlock(AZALEA, of(Material.DECORATION).instabreak().noOcclusion()));
        POTTED_FLOWERING_AZALEA = register("potted_flowering_azalea_bush", new FlowerPotBlock(FLOWERING_AZALEA, of(Material.DECORATION).instabreak().noOcclusion()));
        EvolutionBlocks.register();
        for (long it = Registry.BLOCK.beginIteration(); Registry.BLOCK.hasNextIteration(it); it = Registry.BLOCK.nextEntry(it)) {
            Block block = (Block) Registry.BLOCK.getIteration(it);
            OList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                Block.BLOCK_STATE_REGISTRY.add(possibleStates.get(i));
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static LeavesBlock leaves(SoundType soundType) {
        return new LeavesBlock(of(Material.LEAVES)
                                       .strength(0.2F)
                                       .randomTicks()
                                       .sound(soundType)
                                       .noOcclusion()
                                       .isValidSpawn_(Mixin_FS_Blocks::_ocelotOrParrot)
                                       .isSuffocating_(BlockUtils.NEVER)
                                       .isViewBlocking_(BlockUtils.NEVER));
    }

    @Shadow
    private static ToIntFunction<BlockState> litBlockEmission(int i) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static RotatedPillarBlock log(MaterialColor materialColor, MaterialColor materialColor2) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Block netherStem(MaterialColor materialColor) {
        throw new AbstractMethodError();
    }

    @Unique
    private static BlockBehaviour.Properties of(Material material) {
        return BlockBehaviour.Properties.of(material);
    }

    @Unique
    private static BlockBehaviour.Properties of(Material material, DyeColor color) {
        return BlockBehaviour.Properties.of(material, color);
    }

    @Unique
    private static BlockBehaviour.Properties of(Material material, MaterialColor color) {
        return BlockBehaviour.Properties.of(material, color);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static PistonBaseBlock pistonBase(boolean bl) {
        IStatePredicate statePredicate = (state, level, x, y, z) -> {
            return !state.getValue(PistonBaseBlock.EXTENDED);
        };
        return new PistonBaseBlock(bl, of(Material.PISTON)
                .strength(1.5F)
                .isRedstoneConductor_(BlockUtils.NEVER)
                .isSuffocating_(statePredicate)
                .isViewBlocking_(statePredicate));
    }

    @Shadow
    private static Block register(String string, Block block) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static ShulkerBoxBlock shulkerBox(@Nullable DyeColor dyeColor, BlockBehaviour.Properties properties) {
        IStatePredicate predicate = (state, level, x, y, z) -> {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            if (!(tile instanceof ShulkerBoxBlockEntity te)) {
                return true;
            }
            return te.isClosed();
        };
        return new ShulkerBoxBlock(dyeColor, properties.strength(2.0F)
                                                       .dynamicShape()
                                                       .noOcclusion()
                                                       .isSuffocating_(predicate)
                                                       .isViewBlocking_(predicate));
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static StainedGlassBlock stainedGlass(DyeColor dyeColor) {
        return new StainedGlassBlock(dyeColor, of(Material.GLASS, dyeColor)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn_(BlockUtils.NEVER_SPAWN)
                .isRedstoneConductor_(BlockUtils.NEVER)
                .isSuffocating_(BlockUtils.NEVER)
                .isViewBlocking_(BlockUtils.NEVER));
    }
}
