package tgw.evolution.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class EvolutionCreativeTabs {

    public static final ItemGroup DEV = new ItemGroup("evolution.dev") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.debug_item.get());
        }
    };

    public static final ItemGroup EGGS = new ItemGroup("evolution.eggs") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionEntities.SPAWN_EGG_COW.get());
        }
    };

    public static final ItemGroup MISC = new ItemGroup("evolution.misc") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.PLACEHOLDER_BLOCK.get());
        }
    };

    public static final ItemGroup STONE_TOOLS = new ItemGroup("evolution.stone") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.rock_andesite.get());
        }
    };

    public static final ItemGroup TREES_AND_WOOD = new ItemGroup("evolution.trees_and_wood") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.LOG_OAK.get());
        }
    };

    public static final ItemGroup METAL = new ItemGroup("evolution.metal") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.ingot_copper.get());
        }
    };

    public static final ItemGroup LIQUIDS = new ItemGroup("evolution.liquids") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.bucket_ceramic_empty.get());
        }
    };

    private EvolutionCreativeTabs() {
    }
}
