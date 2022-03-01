package tgw.evolution.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class EvolutionCreativeTabs {

    public static final CreativeModeTab DEV = new CreativeModeTab("evolution.dev") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.debug_item.get());
        }
    };

    public static final CreativeModeTab EGGS = new CreativeModeTab("evolution.eggs") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionEntities.SPAWN_EGG_COW.get());
        }
    };

    public static final CreativeModeTab MISC = new CreativeModeTab("evolution.misc") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.PLACEHOLDER_BLOCK.get());
        }
    };

    public static final CreativeModeTab STONE_TOOLS = new CreativeModeTab("evolution.stone") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.rock_andesite.get());
        }
    };

    public static final CreativeModeTab TREES_AND_WOOD = new CreativeModeTab("evolution.trees_and_wood") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.LOG_OAK.get());
        }
    };

    public static final CreativeModeTab METAL = new CreativeModeTab("evolution.metal") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.ingot_copper.get());
        }
    };

    public static final CreativeModeTab LIQUIDS = new CreativeModeTab("evolution.liquids") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.bucket_ceramic_empty.get());
        }
    };

    private EvolutionCreativeTabs() {
    }
}
