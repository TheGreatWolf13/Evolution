package tgw.evolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionResources;

public final class EvolutionOverlays {

    public static final IIngameOverlay CROSSHAIR_OVERLAY = OverlayRegistry.registerOverlayBottom("Crosshair", EvolutionOverlays::crosshair);
    public static final IIngameOverlay POTION_OVERLAY = OverlayRegistry.registerOverlayBottom("Potion", EvolutionOverlays::potion);
    public static final IIngameOverlay TEMPERATURE_OVERLAY = OverlayRegistry.registerOverlayBottom("Temperature", EvolutionOverlays::temperature);
    public static final IIngameOverlay HEALTH_OVERLAY = OverlayRegistry.registerOverlayBottom("Player Health", EvolutionOverlays::health);
    public static final IIngameOverlay FOOD_THIRST_OVERLAY = OverlayRegistry.registerOverlayBottom("Food Thirst", EvolutionOverlays::foodAndThirst);

    private EvolutionOverlays() {
    }

    private static void crosshair(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            gui.setBlitOffset(-90);
            ClientRenderer.instance.renderCrosshair(matrices, partialTicks, width, height);
        }
    }

    private static void foodAndThirst(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.instance.renderFoodAndThirst(matrices, width, height);
        }
    }

    private static void health(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.instance.renderHealth(matrices, width, height);
        }
    }

    public static void init() {
        Evolution.info("Registered Evolution HUD Overlays");
    }

    private static void potion(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui) {
            ClientRenderer.instance.renderPotionIcons(matrices, partialTicks, width, height);
        }
    }

    private static void temperature(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false, EvolutionResources.GUI_ICONS);
            ClientRenderer.instance.renderTemperature(matrices, width, height);
        }
    }
}
