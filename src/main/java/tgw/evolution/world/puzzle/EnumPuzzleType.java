package tgw.evolution.world.puzzle;

import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.MathHelper;

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

    public static @Nullable EnumPuzzleType byId(int id) {
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
