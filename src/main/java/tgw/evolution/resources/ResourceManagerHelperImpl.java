package tgw.evolution.resources;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.*;
import java.util.function.Consumer;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
    private static final Map<PackType, ResourceManagerHelperImpl> REGISTRY_MAP = new EnumMap<>(PackType.class);
    private static final OSet<Pair<String, ModPackResources>> BUILTIN_RESOURCE_PACKS = new OHashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);

    private final OSet<ResourceLocation> addedListenerIds = new OHashSet<>();
    private final Set<IKeyedReloadListener> addedListeners = new LinkedHashSet<>();

    public static ResourceManagerHelperImpl get(@Nullable PackType type) {
        ResourceManagerHelperImpl r = REGISTRY_MAP.get(type);
        if (r == null) {
            r = new ResourceManagerHelperImpl();
            REGISTRY_MAP.put(type, r);
        }
        return r;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    public static void registerBuiltinResourcePacks(PackType resourceType, Consumer<Pack> consumer, Pack.PackConstructor factory) {
        // Loop through each registered built-in resource packs and add them if valid.
        for (long it = BUILTIN_RESOURCE_PACKS.beginIteration(); BUILTIN_RESOURCE_PACKS.hasNextIteration(it); it = BUILTIN_RESOURCE_PACKS.nextEntry(it)) {
            Pair<String, ModPackResources> e = BUILTIN_RESOURCE_PACKS.getIteration(it);
            ModPackResources pack = e.getSecond();
            // Add the built-in pack only if namespaces for the specified resource type are present.
            if (!pack.getNamespaces(resourceType).isEmpty()) {
                // Make the resource pack profile for built-in pack, should never be always enabled.
                Pack profile = Pack.create(e.getFirst(), pack.getActivationType() == PackActivationType.ALWAYS_ENABLED, e::getSecond, factory, Pack.Position.TOP, new BuiltinModResourcePackSource(pack.getModMetadata().getId()));
                if (profile != null) {
                    consumer.accept(profile);
                }
            }
        }
    }

    public static OList<PreparableReloadListener> sort(@Nullable PackType type, List<PreparableReloadListener> listeners) {
        ResourceManagerHelperImpl instance = get(type);
        OList<PreparableReloadListener> mutable = new OArrayList<>(listeners);
        instance.sort(mutable);
        mutable.trim();
        return mutable.view();
    }

    @Override
    public void registerReloadListener(IKeyedReloadListener listener) {
        if (!this.addedListenerIds.add(listener.getKey())) {
            LOGGER.warn("Tried to register resource reload listener " + listener.getKey() + " twice!");
            return;
        }
        if (!this.addedListeners.add(listener)) {
            throw new RuntimeException("Listener with previously unknown ID " + listener.getKey() + " already in listener set!");
        }
    }

    protected void sort(OList<PreparableReloadListener> listeners) {
        listeners.removeAll(this.addedListeners);
        OList<IKeyedReloadListener> listenersToAdd = new OArrayList<>(this.addedListeners);
        OSet<ResourceLocation> resolvedIds = new OHashSet<>();
        for (int i = 0, len = listeners.size(); i < len; ++i) {
            if (listeners.get(i) instanceof IKeyedReloadListener k) {
                resolvedIds.add(k.getKey());
            }
        }
        int lastSize = -1;
        while (listeners.size() != lastSize) {
            lastSize = listeners.size();
            for (int i = 0, len = listenersToAdd.size(); i < len; ++i) {
                IKeyedReloadListener listener = listenersToAdd.get(i);
                if (resolvedIds.containsAll(listener.getDependencies())) {
                    resolvedIds.add(listener.getKey());
                    listeners.add(listener);
                    listenersToAdd.remove(i--);
                }
            }
        }
        for (int i = 0, len = listenersToAdd.size(); i < len; ++i) {
            //noinspection ObjectAllocationInLoop
            LOGGER.warn("Could not resolve dependencies for listener: " + listenersToAdd.get(i).getKey() + "!");
        }
    }
}
