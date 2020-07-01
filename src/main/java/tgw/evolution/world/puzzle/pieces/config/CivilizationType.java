package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.MathHelper;

public enum CivilizationType {
    NORMAL(0),
    TRIBAL(1),
    STEAMPUNK(2),
    BLIGHTTOWN(3);

    private final byte id;

    CivilizationType(int id) {
        this.id = MathHelper.toByteExact(id);
    }

    public static CivilizationType byId(int id) {
        for (CivilizationType type : CivilizationType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown id " + id);
    }

    public byte getId() {
        return this.id;
    }
}
