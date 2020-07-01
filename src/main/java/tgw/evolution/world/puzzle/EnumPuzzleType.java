package tgw.evolution.world.puzzle;

import javax.annotation.Nullable;

public enum EnumPuzzleType {
    EMPTY("empty_pool"),
    FEATURE("feature_pool"),
    FORCED("forced"),
    HEIGHT("height"),
    LIST("list_pool"),
    SINGLE("single_pool"),
    UNDERGROUND("underground");

    private final String key;

    EnumPuzzleType(String key) {
        this.key = key;
    }

    @Nullable
    public static EnumPuzzleType byKey(String key) {
        for (EnumPuzzleType type : EnumPuzzleType.values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }

    public String getKey() {
        return this.key;
    }
}
