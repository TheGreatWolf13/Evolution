package tgw.evolution.resources;

import net.minecraft.server.packs.PackType;

public interface ResourceManagerHelper {
    /**
     * Get the ResourceManagerHelper instance for a given resource type.
     *
     * @param type The given resource type.
     * @return The ResourceManagerHelper instance.
     */
    static ResourceManagerHelper get(PackType type) {
        return ResourceManagerHelperImpl.get(type);
    }

    /**
     * Add a resource reload listener for a given registry.
     *
     * @param listener The resource reload listener.
     * @deprecated Use {@link ResourceManagerHelper#registerReloadListener(IKeyedReloadListener)}
     */
    @Deprecated
    default void addReloadListener(IKeyedReloadListener listener) {
        this.registerReloadListener(listener);
    }

    /**
     * Register a resource reload listener for a given resource manager type.
     *
     * @param listener The resource reload listener.
     */
    void registerReloadListener(IKeyedReloadListener listener);
}
