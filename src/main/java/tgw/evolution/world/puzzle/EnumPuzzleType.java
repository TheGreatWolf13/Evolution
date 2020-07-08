package tgw.evolution.world.puzzle;

import tgw.evolution.util.MathHelper;

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
        for (EnumPuzzleType type : EnumPuzzleType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public byte getId() {
        return this.id;
    }
}
