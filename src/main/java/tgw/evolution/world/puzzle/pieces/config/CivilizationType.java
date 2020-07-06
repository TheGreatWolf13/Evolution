package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.MathHelper;

import java.util.Random;

public enum CivilizationType {
    NORMAL(0, 15),
    TRIBAL(1, 5),
    STEAMPUNK(2, 1),
    BLIGHTTOWN(3, 5);

    private static int weightSum;
    private final byte id;
    private final int weight;

    CivilizationType(int id, int weight) {
        this.id = MathHelper.toByteExact(id);
        this.weight = weight;
    }

    public static CivilizationType byId(int id) {
        for (CivilizationType type : CivilizationType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown id " + id);
    }

    public static CivilizationType getRandom(Random random) {
        if (weightSum == 0) {
            for (CivilizationType type : values()) {
                weightSum += type.weight;
            }
        }
        int chosen = random.nextInt(weightSum);
        for (CivilizationType type : values()) {
            if (chosen < type.weight) {
                return type;
            }
            chosen -= type.weight;
        }
        throw new IllegalStateException("Unexpected error while chosing CivilizationType");
    }

    public byte getId() {
        return this.id;
    }
}
