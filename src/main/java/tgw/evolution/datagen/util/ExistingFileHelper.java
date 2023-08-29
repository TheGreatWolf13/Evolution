package tgw.evolution.datagen.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExistingFileHelper {

    private final MultiPackResourceManager clientResources;
    private final boolean enable;
    private final Multimap<PackType, ResourceLocation> generated = HashMultimap.create();
    private final MultiPackResourceManager serverData;

    /**
     * Create a new helper. This should probably <em>NOT</em> be used by mods, as
     * the instance provided by forge is designed to be a central instance that
     * tracks existence of generated data.
     * <p>
     * Only create a new helper if you intentionally want to ignore the existence of
     * other generated files.
     *
     * @param existingPacks a collection of paths to existing packs
     * @param enable        {@code true} if validation is enabled
     * @param assetIndex    the identifier for the asset index, generally Minecraft's current major version
     * @param assetsDir     the directory in which to find vanilla assets and indexes
     */
    @SuppressWarnings({"resource", "ObjectAllocationInLoop"})
    public ExistingFileHelper(Collection<Path> existingPacks, boolean enable, @Nullable final String assetIndex, @Nullable final File assetsDir) {
        List<PackResources> candidateClientResources = new ArrayList<>();
        List<PackResources> candidateServerResources = new ArrayList<>();
        candidateClientResources.add(new VanillaPackResources(ClientPackSource.BUILT_IN, "minecraft", "realms"));
        if (assetIndex != null && assetsDir != null) {
            candidateClientResources.add(new DefaultClientPackResources(ClientPackSource.BUILT_IN, new AssetIndex(assetsDir, assetIndex)));
        }
        candidateServerResources.add(new VanillaPackResources(ServerPacksSource.BUILT_IN_METADATA, "minecraft"));
        for (Path existing : existingPacks) {
            File file = existing.toFile();
            PackResources pack = file.isDirectory() ? new FolderPackResources(file) : new FilePackResources(file);
            candidateClientResources.add(pack);
            candidateServerResources.add(pack);
        }
        this.clientResources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, candidateClientResources);
        this.serverData = new MultiPackResourceManager(PackType.SERVER_DATA, candidateServerResources);
        this.enable = enable;
    }

    private static ResourceLocation getLocation(ResourceLocation base, String suffix, String prefix) {
        return new ResourceLocation(base.getNamespace(), prefix + "/" + base.getPath() + suffix);
    }

    /**
     * Check if a given resource exists in the known resource packs.
     *
     * @param loc      the complete location of the resource, e.g.
     *                 {@code "minecraft:textures/block/stone.png"}
     * @param packType the type of resources to check
     * @return {@code true} if the resource exists in any pack, {@code false}
     * otherwise
     */
    public boolean exists(ResourceLocation loc, PackType packType) {
        if (!this.enable) {
            return true;
        }
        return this.generated.get(packType).contains(loc) || this.getManager(packType).hasResource(loc);
    }

    /**
     * Check if a given resource exists in the known resource packs. This is a
     * convenience method to avoid repeating type/prefix/suffix and instead use the
     * common definitions in {@link ResourceType}, or a custom {@link IResourceType}
     * definition.
     *
     * @param loc  the base location of the resource, e.g.
     *             {@code "minecraft:block/stone"}
     * @param type a {@link IResourceType} describing how to form the path to the
     *             resource
     * @return {@code true} if the resource exists in any pack, {@code false}
     * otherwise
     */
    public boolean exists(ResourceLocation loc, IResourceType type) {
        return this.exists(getLocation(loc, type.getSuffix(), type.getPrefix()), type.getPackType());
    }

    /**
     * Check if a given resource exists in the known resource packs.
     *
     * @param loc        the base location of the resource, e.g.
     *                   {@code "minecraft:block/stone"}
     * @param packType   the type of resources to check
     * @param pathSuffix a string to append after the path, e.g. {@code ".json"}
     * @param pathPrefix a string to append before the path, before a slash, e.g.
     *                   {@code "models"}
     * @return {@code true} if the resource exists in any pack, {@code false}
     * otherwise
     */
    public boolean exists(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) {
        return this.exists(getLocation(loc, pathSuffix, pathPrefix), packType);
    }

    private ResourceManager getManager(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? this.clientResources : this.serverData;
    }

    @VisibleForTesting
    public Resource getResource(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) throws IOException {
        return this.getResource(getLocation(loc, pathSuffix, pathPrefix), packType);
    }

    @VisibleForTesting
    public Resource getResource(ResourceLocation loc, PackType packType) throws IOException {
        return this.getManager(packType).getResource(loc);
    }

    /**
     * @return {@code true} if validation is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return this.enable;
    }

    /**
     * Track the existence of a generated file. This is a convenience method to
     * avoid repeating type/prefix/suffix and instead use the common definitions in
     * {@link ResourceType}, or a custom {@link IResourceType} definition.
     * <p>
     * This should be called by data providers immediately when a new data object is
     * created, i.e. not during
     * {@link DataProvider#run(net.minecraft.data.HashCache) run} but instead
     * when the "builder" (or whatever intermediate object) is created, such as a
     * {@link ModelBuilder}.
     * <p>
     * This represents a <em>promise</em> to generate the file later, since other
     * datagen may rely on this file existing.
     *
     * @param loc  the base location of the resource, e.g.
     *             {@code "minecraft:block/stone"}
     * @param type a {@link IResourceType} describing how to form the path to the
     *             resource
     */
    public void trackGenerated(ResourceLocation loc, IResourceType type) {
        this.generated.put(type.getPackType(), getLocation(loc, type.getSuffix(), type.getPrefix()));
    }

    /**
     * Track the existence of a generated file.
     * <p>
     * This should be called by data providers immediately when a new data object is
     * created, i.e. not during
     * {@link DataProvider#run(HashCache) run} but instead
     * when the "builder" (or whatever intermediate object) is created, such as a
     * {@link ModelBuilder}.
     * <p>
     * This represents a <em>promise</em> to generate the file later, since other
     * datagen may rely on this file existing.
     *
     * @param loc        the base location of the resource, e.g.
     *                   {@code "minecraft:block/stone"}
     * @param packType   the type of resources to check
     * @param pathSuffix a string to append after the path, e.g. {@code ".json"}
     * @param pathPrefix a string to append before the path, before a slash, e.g.
     *                   {@code "models"}
     */
    public void trackGenerated(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) {
        this.generated.put(packType, getLocation(loc, pathSuffix, pathPrefix));
    }

    public interface IResourceType {

        PackType getPackType();

        String getPrefix();

        String getSuffix();
    }

    public static class ResourceType implements IResourceType {

        final PackType packType;
        final String prefix;
        final String suffix;

        public ResourceType(PackType type, String suffix, String prefix) {
            this.packType = type;
            this.suffix = suffix;
            this.prefix = prefix;
        }

        @Override
        public PackType getPackType() {
            return this.packType;
        }

        @Override
        public String getPrefix() {
            return this.prefix;
        }

        @Override
        public String getSuffix() {
            return this.suffix;
        }
    }
}

