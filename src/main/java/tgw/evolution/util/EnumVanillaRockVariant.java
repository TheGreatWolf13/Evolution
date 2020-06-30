package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import javax.annotation.Nullable;

public enum EnumVanillaRockVariant {
    DIRT(Blocks.DIRT),
    GRAVEL(Blocks.GRAVEL),
    GRASS_BLOCK(Blocks.GRASS_BLOCK),
    STONE(Blocks.STONE),
    STONE_BRICKS(Blocks.STONE_BRICKS);

    private final Block vanillaBlock;

    EnumVanillaRockVariant(Block vanillaBlock) {
        this.vanillaBlock = vanillaBlock;
    }

    @Nullable
    public static EnumVanillaRockVariant fromBlock(Block block) {
        for (EnumVanillaRockVariant vanilla : EnumVanillaRockVariant.values()) {
            if (vanilla.vanillaBlock == block) {
                return vanilla;
            }
        }
        return null;
    }
}
