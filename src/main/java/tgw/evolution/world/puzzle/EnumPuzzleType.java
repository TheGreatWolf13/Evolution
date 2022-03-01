package tgw.evolution.world.puzzle;

import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;

public enum EnumPuzzleType {
    EMPTY(0),
    SINGLE(1),
    FEATURE(2),
    LIST(3),
    CONFIGURED(4),
    CAVE(5);

    private final byte id;

    EnumPuzzleType(int id) {
        this.id = MathHelper.toByteExact(id);
    }

    @Nullable
    public static EnumPuzzleType byId(int id) {
        return switch (id) {
            case 0 -> EMPTY;
            case 1 -> SINGLE;
            case 2 -> FEATURE;
            case 3 -> LIST;
            case 4 -> CONFIGURED;
            case 5 -> CAVE;
            default -> null;
        };
    }

    public byte getId() {
        return this.id;
    }
}
