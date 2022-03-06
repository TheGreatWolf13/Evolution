package tgw.evolution.items;

import net.minecraft.world.level.block.Block;
import tgw.evolution.util.constants.WoodVariant;

public class ItemLog extends ItemBlock {

    public final WoodVariant variant;

    public ItemLog(WoodVariant variant, Block block, Properties builder) {
        super(block, builder);
        this.variant = variant;
    }
}
