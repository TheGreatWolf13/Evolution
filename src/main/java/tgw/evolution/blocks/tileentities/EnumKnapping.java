package tgw.evolution.blocks.tileentities;

import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import tgw.evolution.blocks.EvolutionHitBoxes;

public enum EnumKnapping {
    NULL(0, VoxelShapes.empty(), KnappingPatterns.NULL),
    AXE(1, EvolutionHitBoxes.AXE, KnappingPatterns.AXE),
    JAVELIN(2, EvolutionHitBoxes.JAVELIN, KnappingPatterns.JAVELIN),
    SHOVEL(3, EvolutionHitBoxes.SHOVEL, KnappingPatterns.SHOVEL),
    HAMMER(4, EvolutionHitBoxes.HAMMER, KnappingPatterns.HAMMER),
    HOE(5, EvolutionHitBoxes.HOE, KnappingPatterns.HOE),
    KNIFE(6, EvolutionHitBoxes.KNIFE, KnappingPatterns.KNIFE);

    private final byte type;
    private final VoxelShape shape;
    private final boolean[][] pattern;

    EnumKnapping(int type, VoxelShape shape, boolean[][] pattern) {
        this.type = (byte) type;
        this.shape = shape;
        this.pattern = pattern;
    }

    public byte getByte() {
        return this.type;
    }

    public VoxelShape getShape() {
        return this.shape;
    }

    public boolean[][] getPattern() {
        return this.pattern;
    }

    public static EnumKnapping fromByte(byte type) {
        for (EnumKnapping knapping : EnumKnapping.values()) {
            if (knapping.type == type) {
                return knapping;
            }
        }
        throw new IllegalStateException("No EnumKnapping for type " + type);
    }
}
