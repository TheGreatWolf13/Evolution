package tgw.evolution.resources;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackSource;

import java.util.WeakHashMap;

public final class ResourcePackSourceTracker {

    // Use a weak hash map so that if resource packs would be deleted, this won't keep them alive.
    private static final WeakHashMap<PackResources, PackSource> SOURCES = new WeakHashMap<>();

    private ResourcePackSourceTracker() {}

    /**
     * Gets the source of a pack.
     *
     * @param pack the resource pack
     * @return the source, or {@link PackSource#DEFAULT} if not tracked
     */
    public static PackSource getSource(PackResources pack) {
        return SOURCES.getOrDefault(pack, PackSource.DEFAULT);
    }

    /**
     * Sets the source of a pack.
     *
     * @param pack   the resource pack
     * @param source the source
     */
    public static void setSource(PackResources pack, PackSource source) {
        SOURCES.put(pack, source);
    }
}
