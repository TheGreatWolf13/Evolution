package tgw.evolution.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.OList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class ProgrammerArtResourcePack extends GroupResourcePack {
    private final AbstractPackResources originalResourcePack;

    public ProgrammerArtResourcePack(AbstractPackResources originalResourcePack, OList<IModResourcePack> modResourcePacks) {
        super(PackType.CLIENT_RESOURCES, modResourcePacks);
        this.originalResourcePack = originalResourcePack;
    }

    @Override
    public void close() {
        this.originalResourcePack.close();
        super.close();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> metaReader) throws IOException {
        return this.originalResourcePack.getMetadataSection(metaReader);
    }

    @Override
    public String getName() {
        return "Programmer Art";
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = this.originalResourcePack.getNamespaces(type);
        namespaces.addAll(super.getNamespaces(type));
        return namespaces;
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation id) throws IOException {
        if (this.originalResourcePack.hasResource(type, id)) {
            return this.originalResourcePack.getResource(type, id);
        }
        return super.getResource(type, id);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        Set<ResourceLocation> resources = new HashSet<>(this.originalResourcePack.getResources(type, namespace, prefix, maxDepth, pathFilter));
        resources.addAll(super.getResources(type, namespace, prefix, maxDepth, pathFilter));
        return resources;
    }

    @Override
    public InputStream getRootResource(String fileName) throws IOException {
        if (!fileName.contains("/") && !fileName.contains("\\")) {
            // There should be nothing to read at the root of mod's Programmer Art extensions.
            return this.originalResourcePack.getRootResource(fileName);
        }
        throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation id) {
        return this.originalResourcePack.hasResource(type, id) || super.hasResource(type, id);
    }
}
