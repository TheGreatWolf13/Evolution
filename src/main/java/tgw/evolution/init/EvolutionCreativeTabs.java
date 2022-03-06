package tgw.evolution.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.util.constants.WoodVariant;

public final class EvolutionCreativeTabs {

    public static final CreativeModeTab DEV = new CreativeModeTab("evolution.dev") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.DEBUG_ITEM.get());
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

    public static final CreativeModeTab PARTS_AND_TOOLS = new CreativeModeTab("evolution.parts_and_tools") {
        @Override
        public ItemStack makeIcon() {
            return ItemModularTool.createNew(PartTypes.Head.PICKAXE, ItemMaterial.COPPER, PartTypes.Handle.ONE_HANDED, ItemMaterial.WOOD, false);
        }
    };

    public static final CreativeModeTab TREES_AND_WOOD = new CreativeModeTab("evolution.trees_and_wood") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.ALL_LOG.get(WoodVariant.OAK).get());
        }
    };

    public static final CreativeModeTab METAL = new CreativeModeTab("evolution.metal") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.INGOT_COPPER.get());
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
