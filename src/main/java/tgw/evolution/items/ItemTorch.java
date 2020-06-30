package tgw.evolution.items;

import net.minecraft.block.Block;

import java.util.Random;

public class ItemTorch extends ItemWallOrFloor implements IFireAspect {

    public ItemTorch(Block floorBlock, Block wallBlockIn, Properties propertiesIn) {
        super(floorBlock, wallBlockIn, propertiesIn);
    }

    @Override
    public int getModifier() {
        return 2;
    }

    @Override
    public boolean activate(Random rand) {
        return rand.nextInt(5) == 0;
    }
}
