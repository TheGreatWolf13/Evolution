package tgw.evolution.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.mixin.AccessorCreativeModeTab;
import tgw.evolution.util.constants.WoodVariant;

public final class EvolutionCreativeTabs {

    public static final CreativeModeTab DEV = new CreativeModeTab(idForTab(), "evolution.dev") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.DEBUG_ITEM);
        }
    };
    //    public static final CreativeModeTab EGGS = new CreativeModeTab(idForTab(), "evolution.eggs") {
//        @Override
//        public ItemStack makeIcon() {
//            return new ItemStack(EvolutionEntities.SPAWN_EGG_COW);
//        }
//    };
    public static final CreativeModeTab MISC = new CreativeModeTab(idForTab(), "evolution.misc") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.PLACEHOLDER_BLOCK);
        }
    };
    public static final CreativeModeTab PARTS_AND_TOOLS = new CreativeModeTab(idForTab(), "evolution.parts_and_tools") {
        @Override
        public ItemStack makeIcon() {
            return ItemModularTool.createNew(PartTypes.Head.PICKAXE, EvolutionMaterials.COPPER,
                                             PartTypes.Handle.ONE_HANDED, EvolutionMaterials.WOOD,
                                             false);
        }
    };
    public static final CreativeModeTab TREES_AND_WOOD = new CreativeModeTab(idForTab(), "evolution.trees_and_wood") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionBlocks.LOGS.get(WoodVariant.OAK));
        }
    };
    public static final CreativeModeTab METAL = new CreativeModeTab(idForTab(), "metal") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EvolutionItems.INGOT_COPPER);
        }
    };
//    public static final CreativeModeTab LIQUIDS = new CreativeModeTab(idForTab(), "evolution.liquids") {
//        @Override
//        public ItemStack makeIcon() {
//            return new ItemStack(EvolutionItems.BUCKET_CERAMIC_EMPTY);
//        }
//    };

    private EvolutionCreativeTabs() {
    }

    public static int idForTab() {
        CreativeModeTab[] tabs = CreativeModeTab.TABS;
        int size = tabs.length;
        CreativeModeTab[] newTabs = new CreativeModeTab[size + 1];
        System.arraycopy(tabs, 0, newTabs, 0, size);
        AccessorCreativeModeTab.setTabs(newTabs);
        return size;
    }
}
