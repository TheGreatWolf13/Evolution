package tgw.evolution.blocks.tileentities;

import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nonnull;

public enum KnappingRecipe {
    NULL(0, KnappingPatterns.NULL),
    AXE(1, KnappingPatterns.AXE),
    SPEAR(2, KnappingPatterns.JAVELIN),
    SHOVEL(3, KnappingPatterns.SHOVEL),
    HAMMER(4, KnappingPatterns.HAMMER),
    HOE(5, KnappingPatterns.HOE),
    KNIFE(6, KnappingPatterns.KNIFE);

    public static final KnappingRecipe[] VALUES = values();
    private final byte id;
    private final long pattern;
    private final VoxelShape shape;

    KnappingRecipe(int id, long pattern) {
        this.id = MathHelper.toByteExact(id);
        this.shape = MathHelper.generateShapeFromPattern(pattern);
        this.pattern = pattern;
    }

    @Nonnull
    public static KnappingRecipe byId(int id) {
        for (KnappingRecipe knapping : VALUES) {
            if (knapping.id == id) {
                return knapping;
            }
        }
        Evolution.warn("Could not find KnappingRecipe with id {}, replacing with NULL", id);
        return NULL;
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
