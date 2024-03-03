package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleResource;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.resources.GroupResourcePack;
import tgw.evolution.resources.ResourcePackSourceTracker;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Mixin(FallbackResourceManager.class)
public abstract class MixinFallbackResourceManager {

    @Unique private final ThreadLocal<List<Resource>> resources = new ThreadLocal<>();
    @Shadow @Final public PackType type;
    @Shadow @Final protected List<PackResources> fallbacks;

    @Shadow
    public static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public Resource getResource(ResourceLocation resLoc) throws IOException {
        this.validateLocation(resLoc);
        PackResources pack = null;
        ResourceLocation metadata = getMetadataLocation(resLoc);
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            PackResources fallback = this.fallbacks.get(i);
            if (pack == null && fallback.hasResource(this.type, metadata)) {
                pack = fallback;
            }
            if (fallback.hasResource(this.type, resLoc)) {
                InputStream input = null;
                if (pack != null) {
                    input = this.getWrappedResource(metadata, pack);
                }
                SimpleResource res = new SimpleResource(fallback.getName(), resLoc, this.getWrappedResource(resLoc, fallback), input);
                res.setPackSource(ResourcePackSourceTracker.getSource(pack));
                return res;
            }
        }
        throw new FileNotFoundException(resLoc.toString());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        this.validateLocation(resourceLocation);
        OList<Resource> list = new OArrayList<>();
        this.resources.set(list);
        ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
        for (PackResources pack : this.fallbacks) {
            if (pack instanceof GroupResourcePack g) {
                g.appendResources((FallbackResourceManager) (Object) this, resourceLocation, this.resources.get());
                continue;
            }
            if (pack.hasResource(this.type, resourceLocation)) {
                InputStream input = pack.hasResource(this.type, resourceLocation2) ?
                                    this.getWrappedResource(resourceLocation2, pack) :
                                    null;
                //noinspection ObjectAllocationInLoop
                SimpleResource res = new SimpleResource(pack.getName(), resourceLocation, this.getWrappedResource(resourceLocation, pack), input);
                res.setPackSource(ResourcePackSourceTracker.getSource(pack));
                list.add(res);
            }
        }
        if (list.isEmpty()) {
            throw new FileNotFoundException(resourceLocation.toString());
        }
        return list;
    }

    @Shadow
    public abstract InputStream getWrappedResource(ResourceLocation resourceLocation, PackResources packResources) throws IOException;

    @Shadow
    protected abstract void validateLocation(ResourceLocation resourceLocation) throws IOException;
}
