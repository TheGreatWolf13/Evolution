package tgw.evolution.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.util.CreativeTabs;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;

@Mixin(CreativeModeTab.class)
public abstract class Mixin_CFS_CreativeModeTab {

    @Shadow @Final @DeleteField public static CreativeModeTab[] TABS;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_BUILDING_BLOCKS;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_DECORATIONS;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_REDSTONE;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_TRANSPORTATION;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_MISC;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_SEARCH;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_FOOD;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_TOOLS;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_COMBAT;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_BREWING;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_MATERIALS;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_INVENTORY;
    @Mutable @Shadow @Final @RestoreFinal public static CreativeModeTab TAB_HOTBAR;
    @Unique @RestoreFinal private static EnchantmentCategory[] EMPTY_ENCH;
    @Shadow private String backgroundSuffix;
    @Shadow private boolean canScroll;
    @Mutable @Shadow @Final @RestoreFinal private Component displayName;
    @Shadow private EnchantmentCategory[] enchantmentCategories;
    @Shadow private ItemStack iconItemStack;
    @Mutable @Shadow @Final @RestoreFinal private int id;
    @Mutable @Shadow @Final @RestoreFinal private String langId;
    @Shadow private boolean showTitle;

    @ModifyConstructor
    public Mixin_CFS_CreativeModeTab(int id, String name) {
        this.backgroundSuffix = "items.png";
        this.canScroll = true;
        this.showTitle = true;
        this.enchantmentCategories = EMPTY_ENCH;
        this.id = id;
        this.langId = name;
        this.displayName = new TranslatableComponent("itemGroup." + name);
        CreativeTabs.add((CreativeModeTab) (Object) this);
        this.iconItemStack = ItemStack.EMPTY;
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        EMPTY_ENCH = new EnchantmentCategory[0];
        TAB_HOTBAR = new CreativeModeTab(0, "hotbar") {
            @Override
            public void fillItemList(NonNullList<ItemStack> list) {
                throw new RuntimeException("Implement exception client-side.");
            }

            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.BOOKSHELF);
            }
        };
        TAB_SEARCH = new CreativeModeTab(1, "search") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.COMPASS);
            }
        }.setBackgroundSuffix("item_search.png");
        TAB_INVENTORY = new CreativeModeTab(2, "inventory") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.CHEST);
            }
        }.setBackgroundSuffix("inventory.png").hideScroll().hideTitle();
        TAB_BUILDING_BLOCKS = new CreativeModeTab(3, "buildingBlocks") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.BRICKS);
            }
        }.setRecipeFolderName("building_blocks");
        TAB_DECORATIONS = new CreativeModeTab(4, "decorations") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.PEONY);
            }
        };
        TAB_REDSTONE = new CreativeModeTab(5, "redstone") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.REDSTONE);
            }
        };
        TAB_TRANSPORTATION = new CreativeModeTab(6, "transportation") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.POWERED_RAIL);
            }
        };

        TAB_MISC = new CreativeModeTab(7, "misc") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.LAVA_BUCKET);
            }
        };
        TAB_FOOD = new CreativeModeTab(8, "food") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.APPLE);
            }
        };
        TAB_TOOLS = new CreativeModeTab(9, "tools") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.IRON_AXE);
            }
        }.setEnchantmentCategories(EnchantmentCategory.VANISHABLE, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.BREAKABLE);
        TAB_COMBAT = new CreativeModeTab(10, "combat") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.GOLDEN_SWORD);
            }
        }.setEnchantmentCategories(EnchantmentCategory.VANISHABLE, EnchantmentCategory.ARMOR, EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_HEAD, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.BOW, EnchantmentCategory.WEAPON, EnchantmentCategory.WEARABLE, EnchantmentCategory.BREAKABLE, EnchantmentCategory.TRIDENT, EnchantmentCategory.CROSSBOW);
        TAB_BREWING = new CreativeModeTab(11, "brewing") {
            @Override
            public ItemStack makeIcon() {
                return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            }
        };
        TAB_MATERIALS = TAB_MISC;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public int getColumn() {
        return switch (this.id) {
            case 0 -> {
                yield 4;
            }
            case 1, 2 -> {
                yield 5;
            }
            default -> {
                int mod = (this.id - 3) % 9;
                if (mod < 4) {
                    yield mod;
                }
                yield mod - 4;
            }
        };
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isAlignedRight() {
        return switch (this.id) {
            case 0, 1, 2 -> true;
            default -> false;
        };
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isTopRow() {
        return switch (this.id) {
            case 0, 1 -> true;
            case 2 -> false;
            default -> (this.id - 3) % 9 < 4;
        };
    }
}
