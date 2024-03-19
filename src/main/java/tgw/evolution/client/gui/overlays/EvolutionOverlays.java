package tgw.evolution.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.util.Blending;
import tgw.evolution.init.EvolutionResources;

public final class EvolutionOverlays {

    public static final ResourceLocation CROSSHAIR = Evolution.getResource("crosshair");
    public static final ResourceLocation DEATH = Evolution.getResource("death");
    public static final ResourceLocation EFFECTS = Evolution.getResource("effects");
    public static final ResourceLocation FOOD_AND_THIRST = Evolution.getResource("food_and_thirst");
    public static final ResourceLocation HEALTH = Evolution.getResource("health");
    public static final ResourceLocation STAMINA = Evolution.getResource("stamina");
    public static final ResourceLocation TEMPERATURE = Evolution.getResource("temperature");

    private EvolutionOverlays() {
    }

    private static void crosshair(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, EvolutionResources.GUI_ICONS);
        gui.setBlitOffset(-90);
        ClientRenderer.getInstance().renderCrosshair(matrices, partialTicks, width, height);
    }

    private static void death(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        ClientRenderer.getInstance().renderDeathOverlay(matrices, partialTicks, width, height);
    }

    private static void effects(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        ClientRenderer.getInstance().renderEffectIcons(matrices, partialTicks, width, height);
    }

    private static void foodAndThirst(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, EvolutionResources.GUI_ICONS);
        ClientRenderer.getInstance().renderFoodAndThirst(matrices, width, height);
    }

    private static void health(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, EvolutionResources.GUI_ICONS);
        ClientRenderer.getInstance().renderHealth(matrices, width, height);
    }

    public static void register() {
        Overlays.registerHudOverlayAbove(VanillaOverlays.HOTBAR, CROSSHAIR, EvolutionOverlays::crosshair);
        Overlays.registerHudOverlayBelow(VanillaOverlays.AIR_LEVEL, HEALTH, EvolutionOverlays::health);
        Overlays.registerHudOverlayAbove(HEALTH, STAMINA, EvolutionOverlays::stamina);
        Overlays.registerHudOverlayBelow(VanillaOverlays.AIR_LEVEL, FOOD_AND_THIRST, EvolutionOverlays::foodAndThirst);
        Overlays.registerHudOverlayAbove(VanillaOverlays.FPS_GRAPH, EFFECTS, EvolutionOverlays::effects);
        Overlays.registerHudOverlayBelow(VanillaOverlays.JUMP_BAR, TEMPERATURE, EvolutionOverlays::temperature);
        Overlays.registerGameOverlayBottom(DEATH, EvolutionOverlays::death);
        Evolution.info("Registered Evolution Overlays");
    }

    private static void stamina(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, EvolutionResources.GUI_ICONS);
        ClientRenderer.getInstance().renderStamina(matrices, width, height);
    }

    private static void temperature(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, EvolutionResources.GUI_ICONS);
        ClientRenderer.getInstance().renderTemperature(matrices, width, height);
    }
}
