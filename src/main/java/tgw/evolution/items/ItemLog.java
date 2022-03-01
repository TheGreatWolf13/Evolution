package tgw.evolution.items;

import tgw.evolution.blocks.BlockLog;
import tgw.evolution.util.constants.WoodVariant;

public class ItemLog extends ItemBlock {

    public final WoodVariant variant;

    public ItemLog(WoodVariant variant, BlockLog block, Properties builder) {
        super(block, builder);
        this.variant = variant;
    }
}
