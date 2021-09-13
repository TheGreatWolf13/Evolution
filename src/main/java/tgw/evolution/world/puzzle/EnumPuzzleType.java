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
        switch (id) {
            case 0: {
                return EMPTY;
            }
            case 1: {
                return SINGLE;
            }
            case 2: {
                return FEATURE;
            }
            case 3: {
                return LIST;
            }
            case 4: {
                return CONFIGURED;
            }
            case 5: {
                return CAVE;
            }
        }
        return null;
    }

    public byte getId() {
        return this.id;
    }
}
