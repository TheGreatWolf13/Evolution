package tgw.evolution.world.puzzle;

public enum EnumPuzzleType {
    EMPTY("empty_pool"),
    FEATURE("feature_pool"),
    FORCED("forced"),
    LIST("list_pool"),
    SINGLE("single_pool");

    private final String key;

    EnumPuzzleType(String key) {
        this.key = key;
    }

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
