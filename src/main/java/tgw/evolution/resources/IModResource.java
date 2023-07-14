package tgw.evolution.resources;

import net.minecraft.server.packs.repository.PackSource;
import org.slf4j.LoggerFactory;

public interface IModResource {

    default PackSource getModPackSource() {
        LoggerFactory.getLogger(IModResource.class)
                     .error("Unknown Resource implementation {}, returning PACK_SOURCE_NONE as the source", this.getClass().getName());
        return PackSource.DEFAULT;
    }
}
