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

/**
 * Overlays to render on top of the screen. <p>
 * There are two types of overlays: Hud overlays and game overlays. <p>
 * Hud overlays are simply elements of the hud, such as health bar, hunger bar, elements that are not diegetic, that do not exist on the world, and
 * as such, NOT affected by shaders. <p>
 * Game overlays, on the other way, represent elements that are diegetic, that is, elements that do exist in the world, and as such, are affected
 * by shaders.
 **/
public final class Overlays {

    private static final RList<Entry> HUD_OVERLAYS = new RArrayList<>();
    private static final RList<Entry> GAME_OVERLAYS = new RArrayList<>();

    private Overlays() {}

    public static void enableGameOverlay(ResourceLocation name, boolean enable) {
        enableOverlay(name, enable, GAME_OVERLAYS);
    }

    public static void enableHudOverlay(ResourceLocation name, boolean enable) {
        enableOverlay(name, enable, HUD_OVERLAYS);
    }

    private static void enableOverlay(ResourceLocation name, boolean enable, RList<Entry> list) {
        Entry entry = find(name, list);
        if (entry != null) {
            entry.enabled = enable;
        }
        else {
            Evolution.warn("Tried to enable / disable an unregistered overlay: {}", name);
        }
    }

    @Nullable
    private static Entry find(ResourceLocation name, RList<Entry> list) {
        for (int i = 0, len = list.size(); i < len; i++) {
            if (list.get(i).name.equals(name)) {
                return list.get(i);
            }
        }
        return null;
    }

    /**
     * Above as in rendering order, actual placement in the list is just below (renders after 'other').
     */
    public static void registerGameOverlayAbove(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, other, name, overlay, GAME_OVERLAYS);
    }

    /**
     * Below as in rendering order, actual placement in the list is just above (renders before 'other').
     */
    public static void registerGameOverlayBelow(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, other, name, overlay, GAME_OVERLAYS);
    }

    /**
     * Bottom as in rendering order, actual placement in the list is in the beginning (renders first).
     */
    public static void registerGameOverlayBottom(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, null, name, overlay, GAME_OVERLAYS);
    }

    /**
     * Top as in rendering order, actual placement in the list is in the end (renders last).
     */
    public static void registerGameOverlayTop(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, null, name, overlay, GAME_OVERLAYS);
    }

    /**
     * Above as in rendering order, actual placement in the list is just below (renders after 'other').
     */
    public static void registerHudOverlayAbove(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, other, name, overlay, HUD_OVERLAYS);
    }

    /**
     * Below as in rendering order, actual placement in the list is just above (renders before 'other').
     */
    public static void registerHudOverlayBelow(ResourceLocation other, ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, other, name, overlay, HUD_OVERLAYS);
    }

    /**
     * Bottom as in rendering order, actual placement in the list is in the beginning (renders first).
     */
    public static void registerHudOverlayBottom(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(-1, null, name, overlay, HUD_OVERLAYS);
    }

    /**
     * Top as in rendering order, actual placement in the list is in the end (renders last).
     */
    public static void registerHudOverlayTop(ResourceLocation name, IGuiOverlay overlay) {
        registerOverlay(1, null, name, overlay, HUD_OVERLAYS);
    }

    private static void registerOverlay(int sort, @Nullable ResourceLocation other, ResourceLocation name, IGuiOverlay overlay, RList<Entry> list) {
        Entry entry = find(name, list);
        if (entry != null) {
            throw new IllegalStateException("Tried to register duplicate overlay with registry name: " + entry.name);
        }
        int insertAt = list.size();
        if (other == null) {
            if (sort < 0) {
                insertAt = 0;
            }
        }
        else {
            for (int i = 0, len = list.size(); i < len; i++) {
                if (list.get(i).name.equals(other)) {
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
        list.add(insertAt, entry);
    }

    private static void renderAll(Minecraft mc,
                                  EvolutionGui gui,
                                  PoseStack matrices,
                                  float partialTicks,
                                  int screenWidth,
                                  int screenHeight,
                                  RList<Entry> list) {
        ProfilerFiller profiler = mc.getProfiler();
        for (int i = 0, l = list.size(); i < l; i++) {
            Entry entry = list.get(i);
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

    public static void renderAllGame(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int screenWidth, int screenHeight) {
        renderAll(mc, gui, matrices, partialTicks, screenWidth, screenHeight, GAME_OVERLAYS);
    }

    public static void renderAllHud(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int screenWidth, int screenHeight) {
        renderAll(mc, gui, matrices, partialTicks, screenWidth, screenHeight, HUD_OVERLAYS);
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
