package tgw.evolution.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.Enum2OMap;
import tgw.evolution.util.collection.maps.R2OMap;

public enum RecipeCategory {

    UNKNOWN(new ItemStack(Items.BARRIER)),
    CRAFTING_SEARCH(new ItemStack(Items.COMPASS)),
    CRAFTING_BUILDING_BLOCKS(new ItemStack(Blocks.BRICKS)),
    CRAFTING_EQUIPMENT(new ItemStack(Items.IRON_AXE), new ItemStack(Items.GOLDEN_SWORD)),
    CRAFTING_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.APPLE)),
    FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
    FURNACE_FOOD(new ItemStack(Items.PORKCHOP)),
    FURNACE_BLOCKS(new ItemStack(Blocks.STONE)),
    FURNACE_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.EMERALD)),
    BLAST_FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
    BLAST_FURNACE_BLOCKS(new ItemStack(Blocks.REDSTONE_ORE)),
    BLAST_FURNACE_MISC(new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.GOLDEN_LEGGINGS)),
    SMOKER_SEARCH(new ItemStack(Items.COMPASS)),
    SMOKER_FOOD(new ItemStack(Items.PORKCHOP)),
    STONECUTTER(new ItemStack(Items.CHISELED_STONE_BRICKS)),
    SMITHING(new ItemStack(Items.NETHERITE_CHESTPLATE)),
    CAMPFIRE(new ItemStack(Items.PORKCHOP));
    public static final RecipeCategory[] VALUES = values();
    public static final R2OMap<RecipeCategory, OList<RecipeCategory>> AGGREGATE_CATEGORIES;

    static {
        R2OMap<RecipeCategory, OList<RecipeCategory>> map = new Enum2OMap<>(RecipeCategory.class);
        map.put(CRAFTING_SEARCH, OList.of(CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC));
        map.put(FURNACE_SEARCH, OList.of(FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC));
        map.put(BLAST_FURNACE_SEARCH, OList.of(BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC));
        map.put(SMOKER_SEARCH, OList.of(SMOKER_FOOD));
        map.trim();
        AGGREGATE_CATEGORIES = map.view();
    }

    private final OList<ItemStack> itemIcons;
    private final Component name;

    RecipeCategory(ItemStack stack) {
        this(OList.singleton(stack));
    }

    RecipeCategory(ItemStack... stacks) {
        this(OList.of(stacks));
    }

    RecipeCategory(OList<ItemStack> list) {
        this.itemIcons = list.view();
        this.name = new TranslatableComponent("evolution.gui.recipebook.category." + this.name().toLowerCase());
    }

    public OList<ItemStack> getIconItems() {
        return this.itemIcons;
    }

    public Component getName() {
        return this.name;
    }

    public boolean isSearch() {
        return switch (this) {
            case CRAFTING_SEARCH, FURNACE_SEARCH, BLAST_FURNACE_SEARCH, SMOKER_SEARCH -> true;
            default -> false;
        };
    }
}
