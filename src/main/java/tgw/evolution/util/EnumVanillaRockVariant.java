package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import javax.annotation.Nullable;

public enum EnumVanillaRockVariant {
    STONE(Blocks.STONE),
    DIRT(Blocks.DIRT),
    GRASS_BLOCK(Blocks.GRASS_BLOCK),
    GRAVEL(Blocks.GRAVEL),
    SAND(Blocks.SAND),
    STONE_BRICKS(Blocks.STONE_BRICKS),
    COBBLESTONE(Blocks.COBBLESTONE);

    private final Block vanillaBlock;

    EnumVanillaRockVariant(Block vanillaBlock) {
        this.vanillaBlock = vanillaBlock;
    }

    @Nullable
    public static EnumVanillaRockVariant fromBlock(Block block) {
        for (EnumVanillaRockVariant vanilla : values()) {
            if (vanilla.vanillaBlock == block) {
                return vanilla;
            }
        }
        return null;
    }
}
