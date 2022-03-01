package tgw.evolution.util.constants;

import net.minecraft.world.level.block.Block;

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
        return switch (block.getRegistryName().getPath()) {
            case "stone" -> STONE;
            case "dirt" -> DIRT;
            case "grass" -> GRASS;
            case "gravel" -> GRAVEL;
            case "sand" -> SAND;
            case "stone_bricks" -> STONE_BRICKS;
            case "cobblestone" -> COBBLESTONE;
            default -> null;
        };
    }
}
