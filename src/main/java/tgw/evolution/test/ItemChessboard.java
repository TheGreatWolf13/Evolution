package tgw.evolution.test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemChessboard extends Item {
    public static final int MAXIMUM_NUMBER_OF_COUNTERS = 64;

    public ItemChessboard() {
        super(new Properties().stacksTo(MAXIMUM_NUMBER_OF_COUNTERS).tab(ItemGroup.TAB_MISC));
    }
}
