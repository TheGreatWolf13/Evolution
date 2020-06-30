package tgw.evolution.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class EvolutionCreativeTabs {

    public static final ItemGroup MISC = new ItemGroup("evolution.misc") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(EvolutionBlocks.PLACEHOLDER_BLOCK.get());
        }
    };

    public static final ItemGroup STONE_TOOLS = new ItemGroup("evolution.stone_tools") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(EvolutionItems.axe_head_andesite.get());
        }
    };

    public static final ItemGroup TREES_AND_WOOD = new ItemGroup("evolution.trees_and_wood") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(EvolutionBlocks.LOG_OAK.get());
        }
    };

    public static final ItemGroup EGGS = new ItemGroup("evolution.eggs") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(EvolutionEntities.SPAWN_EGG_COW.get());
        }
    };
}
