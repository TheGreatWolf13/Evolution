package tgw.evolution.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;

import javax.annotation.Nullable;

public final class Overlays {

    private static final RList<Entry> OVERLAYS = new RArrayList<>();

    private Overlays() {}

    public static void enableOverlay(ResourceLocation name, boolean enable) {
        Entry entry = find(name);
        if (entry != null) {
            entry.enabled = enable;
        }
        else {
            Evolution.warn("Tried to enable / disable an unregistered overlay: {}", name);
        }
    }

    @Nullable
    private static Entry find(ResourceLocation name) {
        for (int i = 0, len = OVERLAYS.size(); i < len; i++) {
            if (OVERLAYS.get(i).name.equals(name)) {
                return OVERLAYS.get(i);
            }
        }
        return null;
    }

    private static void registerOverlay(int sort, @Nullable ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        Entry entry = find(name);
        if (entry != null) {
            throw new IllegalStateException("Tried to register duplicate overlay with registry name: " + entry.name);
        }
        int insertAt = OVERLAYS.size();
        if (other == null) {
            if (sort < 0) {
                insertAt = 0;
            }
        }
        else {
            for (int i = 0, len = OVERLAYS.size(); i < len; i++) {
                if (OVERLAYS.get(i).name.equals(other)) {
                    if (sort < 0) {
                        insertAt = i;
                    }
                    else {
                        insertAt = i + 1;
                    }
                    break;
                }
            }
        }
        entry = new Entry(name, overlay);
        OVERLAYS.add(insertAt, entry);
    }

    /**
     * Above as in rendering order, actual placement in the list is just below (renders after 'other').
     */
    public static void registerOverlayAbove(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, other, name, overlay);
    }

    /**
     * Below as in rendering order, actual placement in the list is just above (renders before 'other').
     */
    public static void registerOverlayBelow(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, other, name, overlay);
    }

    /**
     * Bottom as in rendering order, actual placement in the list is in the beginning (renders first).
     */
    public static void registerOverlayBottom(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, null, name, overlay);
    }

    /**
     * Top as in rendering order, actual placement in the list is in the end (renders last).
     */
    public static void registerOverlayTop(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, null, name, overlay);
    }

    public static void renderAll(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int screenWidth, int screenHeight) {
        ProfilerFiller profiler = mc.getProfiler();
        for (int i = 0, l = OVERLAYS.size(); i < l; i++) {
            Entry entry = OVERLAYS.get(i);
            if (!entry.enabled) {
                continue;
            }
            profiler.push(entry.name.getNamespace());
            profiler.push(entry.name.getPath());
            try {
                entry.overlay.render(mc, gui, matrices, partialTicks, screenWidth, screenHeight);
            }
            catch (Exception e) {
                Evolution.error("Error rendering overlay {}: {}", entry.name, e);
            }
            profiler.pop();
            profiler.pop();
        }
    }

    public static class Entry {
        private final ResourceLocation name;
        private final IGuiOverlay overlay;
        private boolean enabled = true;

        public Entry(ResourceLocation name, IGuiOverlay overlay) {
            this.name = name;
            this.overlay = overlay;
        }
    }
}
