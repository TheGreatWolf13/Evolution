package tgw.evolution.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import tgw.evolution.client.gui.EvolutionGui;

public interface IGuiOverlay {

    void render(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height);
}
