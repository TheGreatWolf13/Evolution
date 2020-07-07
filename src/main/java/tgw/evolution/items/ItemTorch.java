package tgw.evolution.items;

import net.minecraft.block.Block;

public class ItemTorch extends ItemWallOrFloor implements IFireAspect {

    public ItemTorch(Block floorBlock, Block wallBlockIn, Properties propertiesIn) {
        super(floorBlock, wallBlockIn, propertiesIn);
    }

    @Override
    public int getModifier() {
        return 2;
    }

    @Override
    public float getChance() {
        return 0.2f;
    }
}
