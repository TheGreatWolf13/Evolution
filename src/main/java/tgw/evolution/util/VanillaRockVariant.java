package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import javax.annotation.Nullable;

public enum VanillaRockVariant {
    STONE(Blocks.STONE),
    DIRT(Blocks.DIRT),
    GRASS_BLOCK(Blocks.GRASS_BLOCK),
    GRAVEL(Blocks.GRAVEL),
    SAND(Blocks.SAND),
    STONE_BRICKS(Blocks.STONE_BRICKS),
    COBBLESTONE(Blocks.COBBLESTONE);

    private final Block vanillaBlock;

    VanillaRockVariant(Block vanillaBlock) {
        this.vanillaBlock = vanillaBlock;
    }

    @Nullable
    public static VanillaRockVariant fromBlock(Block block) {
        for (VanillaRockVariant vanilla : values()) {
            if (vanilla.vanillaBlock == block) {
                return vanilla;
            }
        }
        return null;
    }
}
