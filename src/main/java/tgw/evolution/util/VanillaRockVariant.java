package tgw.evolution.util;

import net.minecraft.block.Block;

import javax.annotation.Nullable;

public enum VanillaRockVariant {
    STONE,
    DIRT,
    GRASS,
    GRAVEL,
    SAND,
    STONE_BRICKS,
    COBBLESTONE;

    @Nullable
    public static VanillaRockVariant fromBlock(Block block) {
        switch (block.getRegistryName().getPath()) {
            case "stone": {
                return STONE;
            }
            case "dirt": {
                return DIRT;
            }
            case "grass": {
                return GRASS;
            }
            case "gravel": {
                return GRAVEL;
            }
            case "sand": {
                return SAND;
            }
            case "stone_bricks": {
                return STONE_BRICKS;
            }
            case "cobblestone": {
                return COBBLESTONE;
            }
        }
        return null;
    }
}
