package tgw.evolution.test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemChessboard extends Item {
    public static int MAXIMUM_NUMBER_OF_COUNTERS = 64;

    public ItemChessboard() {
        super(new Properties().maxStackSize(MAXIMUM_NUMBER_OF_COUNTERS).group(ItemGroup.MISC));
    }
}
