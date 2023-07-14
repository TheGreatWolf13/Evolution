package tgw.evolution.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleResource;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

public abstract class GroupResourcePack implements PackResources {

    protected final O2OMap<String, OList<IModResourcePack>> namespacedPacks = new O2OHashMap<>();
    protected final OList<IModResourcePack> packs;
    protected final PackType type;

    public GroupResourcePack(PackType type, OList<IModResourcePack> packs) {
        this.type = type;
        this.packs = packs;
        for (int i = 0, len = packs.size(); i < len; ++i) {
            IModResourcePack pack = packs.get(i);
            Set<String> namespaces = pack.getNamespaces(this.type);
            for (String namespace : namespaces) {
                OList<IModResourcePack> list = this.namespacedPacks.get(namespace);
                if (list == null) {
                    list = new OArrayList<>();
                    this.namespacedPacks.put(namespace, list);
                }
                list.add(pack);
            }
        }
    }

    public void appendResources(FallbackResourceManager manager, ResourceLocation id, List<Resource> resources) throws IOException {
        List<IModResourcePack> packs = this.namespacedPacks.get(id.getNamespace());
        if (packs == null) {
            return;
        }
        ResourceLocation metadataId = FallbackResourceManager.getMetadataLocation(id);
        for (IModResourcePack pack : packs) {
            if (pack.hasResource(manager.type, id)) {
                InputStream metadataInputStream = pack.hasResource(manager.type, metadataId) ?
                                                  manager.getWrappedResource(metadataId, pack) :
                                                  null;
                //noinspection ObjectAllocationInLoop
                SimpleResource resource = new SimpleResource(pack.getName(), id, manager.getWrappedResource(id, pack), metadataInputStream);
                resource.setPackSource(ModdedPackSource.RESOURCE_PACK_SOURCE);
                resources.add(resource);
            }
        }
    }

    @Override
    public void close() {
        for (int i = 0, len = this.packs.size(); i < len; ++i) {
            this.packs.get(i).close();
        }
    }

    public String getFullName() {
        StringBuilder builder = new StringBuilder(this.getName()).append(" (");
        int len = this.packs.size() - 1;
        for (int i = 0; i < len; ++i) {
            builder.append(this.packs.get(i).getName()).append(", ");
        }
        builder.append(this.packs.get(len).getName()).append(")");
        return builder.toString();
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.namespacedPacks.keySet();
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation id) throws IOException {
        List<IModResourcePack> packs = this.namespacedPacks.get(id.getNamespace());
        if (packs != null) {
            for (int i = packs.size() - 1; i >= 0; i--) {
                PackResources pack = packs.get(i);
                if (pack.hasResource(type, id)) {
                    return pack.getResource(type, id);
                }
            }
        }
        //noinspection ConstantConditions
        throw new ResourcePackFileNotFoundException(null, String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        List<IModResourcePack> packs = this.namespacedPacks.get(namespace);
        if (packs == null) {
            return Collections.emptyList();
        }
        Set<ResourceLocation> resources = new HashSet<>();
        for (int i = packs.size() - 1; i >= 0; i--) {
            PackResources pack = packs.get(i);
            Collection<ResourceLocation> modResources = pack.getResources(type, namespace, prefix, maxDepth, pathFilter);
            resources.addAll(modResources);
        }
        return resources;
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation id) {
        List<IModResourcePack> packs = this.namespacedPacks.get(id.getNamespace());
        if (packs == null) {
            return false;
        }
        for (int i = packs.size() - 1; i >= 0; i--) {
            PackResources pack = packs.get(i);
            if (pack.hasResource(type, id)) {
                return true;
            }
        }
        return false;
    }
}
