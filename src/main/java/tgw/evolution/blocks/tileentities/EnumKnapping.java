package tgw.evolution.blocks.tileentities;

import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.MathHelper;

public enum EnumKnapping {
    NULL(0, VoxelShapes.empty(), KnappingPatterns.NULL),
    AXE(1, EvolutionHitBoxes.AXE, KnappingPatterns.AXE),
    JAVELIN(2, EvolutionHitBoxes.JAVELIN, KnappingPatterns.JAVELIN),
    SHOVEL(3, EvolutionHitBoxes.SHOVEL, KnappingPatterns.SHOVEL),
    HAMMER(4, EvolutionHitBoxes.HAMMER, KnappingPatterns.HAMMER),
    HOE(5, EvolutionHitBoxes.HOE, KnappingPatterns.HOE),
    KNIFE(6, EvolutionHitBoxes.KNIFE, KnappingPatterns.KNIFE);

    private final byte id;
    private final boolean[][] pattern;
    private final VoxelShape shape;

    EnumKnapping(int id, VoxelShape shape, boolean[][] pattern) {
        this.id = MathHelper.toByteExact(id);
        this.shape = shape;
        this.pattern = pattern;
    }

    public static EnumKnapping byId(int id) {
        for (EnumKnapping knapping : values()) {
            if (knapping.id == id) {
                return knapping;
            }
        }
        Evolution.LOGGER.warn("Could not find EnumKnapping with id {}, replacing with NULL", id);
        return NULL;
    }

    public byte getId() {
        return this.id;
    }

    public boolean[][] getPattern() {
        return this.pattern;
    }

    public VoxelShape getShape() {
        return this.shape;
    }
}
