package tgw.evolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionResources;

public final class EvolutionOverlays {

    static {
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.CROSSHAIR_ELEMENT, "Crosshair", EvolutionOverlays::crosshair);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.POTION_ICONS_ELEMENT, "Potion", EvolutionOverlays::potion);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "Temperature", EvolutionOverlays::temperature);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, "Player Health", EvolutionOverlays::health);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "Food and Thirst", EvolutionOverlays::foodAndThirst);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, "Stamina", EvolutionOverlays::stamina);
    }

    private EvolutionOverlays() {
    }

    private static void crosshair(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            gui.setBlitOffset(-90);
            ClientRenderer.getInstance().renderCrosshair(matrices, partialTicks, width, height);
        }
    }

    private static void foodAndThirst(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.getInstance().renderFoodAndThirst(matrices, width, height);
        }
    }

    private static void health(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.getInstance().renderHealth(matrices, width, height);
        }
    }

    public static void init() {
        Evolution.info("Registered Evolution HUD Overlays");
    }

    private static void potion(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui) {
            ClientRenderer.getInstance().renderPotionIcons(matrices, partialTicks, width, height);
        }
    }

    private static void stamina(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.getInstance().renderStamina(matrices, width, height);
        }
    }

    private static void temperature(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.getInstance().renderTemperature(matrices, width, height);
        }
    }
}
