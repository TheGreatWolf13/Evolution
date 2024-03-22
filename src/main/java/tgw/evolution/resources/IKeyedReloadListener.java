package tgw.evolution.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import tgw.evolution.util.collection.lists.OList;

public interface IKeyedReloadListener extends PreparableReloadListener {

    /**
     * @return The identifiers of listeners this listener expects to have been
     * executed before itself. Please keep in mind that this only takes effect
     * during the application stage!
     */
    default OList<ResourceLocation> getDependencies() {
        return OList.emptyList();
    }

    /**
     * @return The unique identifier of this listener.
     */
    ResourceLocation getKey();
}
