package tgw.evolution.resources;

public enum PackActivationType {

    /**
     * The user has full control over the activation of the resource pack.
     */
    NORMAL,
    /**
     * The user has still full control over the activation of the resource pack.
     */
    DEFAULT_ENABLED,
    /**
     * The user cannot disable the resource pack.
     */
    ALWAYS_ENABLED;

    public boolean isEnabledByDefault() {
        return this == DEFAULT_ENABLED || this == ALWAYS_ENABLED;
    }
}
