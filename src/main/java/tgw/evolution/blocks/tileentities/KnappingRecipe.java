package tgw.evolution.blocks.tileentities;

import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.MathHelper;

public enum KnappingRecipe {
    NULL(0, Patterns.MATRIX_FALSE),
    AXE(1, Patterns.AXE_TRUE),
    SPEAR(2, Patterns.JAVELIN_TRUE),
    SHOVEL(3, Patterns.SHOVEL_TRUE),
    HAMMER(4, Patterns.HAMMER_TRUE),
    HOE(5, Patterns.HOE_TRUE),
    KNIFE(6, Patterns.KNIFE_TRUE);

    public static final KnappingRecipe[] VALUES = values();
    private final byte id;
    private final long pattern;
    private final VoxelShape shape;

    KnappingRecipe(int id, long pattern) {
        this.id = MathHelper.toByteExact(id);
        this.shape = MathHelper.generateShapeFromPattern(pattern);
        this.pattern = pattern;
    }

    public static KnappingRecipe byId(int id) {
        return switch (id) {
            case 0 -> NULL;
            case 1 -> AXE;
            case 2 -> SPEAR;
            case 3 -> SHOVEL;
            case 4 -> HAMMER;
            case 5 -> HOE;
            case 6 -> KNIFE;
            default -> {
                Evolution.warn("Could not find KnappingRecipe with id {}, replacing with NULL", id);
                yield NULL;
            }
        };
    }

    public byte getId() {
        return this.id;
    }

    public long getPattern() {
        return this.pattern;
    }

    public boolean getPatternPart(int i, int j) {
        return (this.pattern >> (7 - j) * 8 + 7 - i & 1) != 0;
    }

    public VoxelShape getShape() {
        return this.shape;
    }
}
