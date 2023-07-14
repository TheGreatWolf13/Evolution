package tgw.evolution.resources;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ModPackResources extends AbstractPackResources implements IModResourcePack {

    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
    private static final String RES_PREFIX = PackType.CLIENT_RESOURCES.getDirectory() + "/";
    private static final String DATA_PREFIX = PackType.SERVER_DATA.getDirectory() + "/";
    private final PackActivationType activationType;
    private final List<Path> basePaths;
    private final @Nullable AutoCloseable closer;
    private final ModMetadata modInfo;
    private final String name;
    private final Map<PackType, Set<String>> namespaces;
    private final PackType type;

    private ModPackResources(String name,
                             ModMetadata modInfo,
                             List<Path> paths,
                             PackType type,
                             @Nullable AutoCloseable closer,
                             PackActivationType activationType) {
        //noinspection ConstantConditions
        super(null);
        this.name = name;
        this.modInfo = modInfo;
        this.basePaths = paths;
        this.type = type;
        this.closer = closer;
        this.activationType = activationType;
        this.namespaces = readNamespaces(paths, modInfo.getId());
    }

    public static @Nullable ModPackResources create(String name,
                                                    ModContainer mod,
                                                    @Nullable String subPath,
                                                    PackType type,
                                                    PackActivationType activationType) {
        List<Path> rootPaths = mod.getRootPaths();
        List<Path> paths;
        if (subPath == null) {
            paths = rootPaths;
        }
        else {
            paths = new ArrayList<>(rootPaths.size());
            for (Path path : rootPaths) {
                path = path.toAbsolutePath().normalize();
                Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();

                if (!childPath.startsWith(path) || !exists(childPath)) {
                    continue;
                }

                paths.add(childPath);
            }
        }
        if (paths.isEmpty()) {
            return null;
        }
        //noinspection resource
        ModPackResources ret = new ModPackResources(name, mod.getMetadata(), paths, type, null, activationType);
        return ret.getNamespaces(type).isEmpty() ? null : ret;
    }

    private static boolean exists(Path path) {
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
    }

    private static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
        Map<PackType, Set<String>> ret = new EnumMap<>(PackType.class);
        for (PackType type : PackType.values()) {
            Set<String> namespaces = null;
            for (Path path : paths) {
                Path dir = path.resolve(type.getDirectory());
                if (!Files.isDirectory(dir)) {
                    continue;
                }
                String separator = path.getFileSystem().getSeparator();
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                    for (Path p : ds) {
                        if (!Files.isDirectory(p)) {
                            continue;
                        }
                        String s = p.getFileName().toString();
                        // s may contain trailing slashes, remove them
                        s = s.replace(separator, "");
                        //noinspection ObjectAllocationInLoop
                        if (!RESOURCE_PACK_PATH.matcher(s).matches()) {
                            Evolution.warn("Ignored invalid namespace: {} in mod ID {}", s, modId);
                            continue;
                        }
                        if (namespaces == null) {
                            namespaces = new HashSet<>();
                        }
                        namespaces.add(s);
                    }
                }
                catch (IOException e) {
                    //noinspection ObjectAllocationInLoop
                    Evolution.warn("getNamespaces in mod " + modId + " failed!", e);
                }
            }
            ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
        }
        return ret;
    }

    @Override
    public void close() {
        if (this.closer != null) {
            try {
                this.closer.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public PackActivationType getActivationType() {
        return this.activationType;
    }

    @Override
    public ModMetadata getModMetadata() {
        return this.modInfo;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.namespaces.getOrDefault(type, Collections.emptySet());
    }

    private @Nullable Path getPath(String filename) {
        if (this.hasAbsentNs(filename)) {
            return null;
        }
        for (Path basePath : this.basePaths) {
            Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();

            if (childPath.startsWith(basePath) && exists(childPath)) {
                return childPath;
            }
        }
        return null;
    }

    @Override
    protected InputStream getResource(String filename) throws IOException {
        InputStream stream;
        Path path = this.getPath(filename);
        if (path != null && Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }
        stream = ModResourcePackUtil.openDefault(this.modInfo, this.type, filename);
        if (stream != null) {
            return stream;
        }
        // ReloadableResourceManagerImpl gets away with FileNotFoundException.
        throw new FileNotFoundException("\"" + filename + "\" in mod \"" + this.modInfo.getId() + "\"");
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, int depth, Predicate<String> predicate) {
        if (!this.namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
            return Collections.emptyList();
        }
        List<ResourceLocation> ids = new ArrayList<>();
        for (Path basePath : this.basePaths) {
            String separator = basePath.getFileSystem().getSeparator();
            Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (!exists(searchPath)) {
                continue;
            }
            try {
                //noinspection ObjectAllocationInLoop
                Files.walkFileTree(searchPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String fileName = file.getFileName().toString();
                        if (!fileName.endsWith(".mcmeta")
                            && predicate.test(fileName)) {
                            try {
                                ids.add(new ResourceLocation(namespace, nsPath.relativize(file).toString().replace(separator, "/")));
                            }
                            catch (ResourceLocationException e) {
                                Evolution.error(e.getMessage());
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch (IOException e) {
                //noinspection ObjectAllocationInLoop
                Evolution.warn("findResources at " + path + " in namespace " + namespace + ", mod " + this.modInfo.getId() + " failed!", e);
            }
        }
        return ids;
    }

    private boolean hasAbsentNs(String filename) {
        int prefixLen;
        PackType type;
        if (filename.startsWith(RES_PREFIX)) {
            prefixLen = RES_PREFIX.length();
            type = PackType.CLIENT_RESOURCES;
        }
        else if (filename.startsWith(DATA_PREFIX)) {
            prefixLen = DATA_PREFIX.length();
            type = PackType.SERVER_DATA;
        }
        else {
            return false;
        }
        int nsEnd = filename.indexOf('/', prefixLen);
        if (nsEnd < 0) {
            return false;
        }
        return !this.namespaces.get(type).contains(filename.substring(prefixLen, nsEnd));
    }

    @Override
    protected boolean hasResource(String filename) {
        if (ModResourcePackUtil.containsDefault(filename)) {
            return true;
        }
        Path path = this.getPath(filename);
        return path != null && Files.isRegularFile(path);
    }
}
