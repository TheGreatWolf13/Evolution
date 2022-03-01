package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.math.MathHelper;

public enum ForceType {
    NONE(0),
    SOFT(1),
    HARD(2);

    private final byte id;

    ForceType(int id) {
        this.id = MathHelper.toByteExact(id);
    }

    public static ForceType byId(int id) {
        for (ForceType type : ForceType.values()) {
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
