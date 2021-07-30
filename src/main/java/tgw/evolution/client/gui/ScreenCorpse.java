package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.corpse.ContainerCorpse;

@OnlyIn(Dist.CLIENT)
public class ScreenCorpse extends ContainerScreen<ContainerCorpse> {

    public ScreenCorpse(ContainerCorpse screenContainer, PlayerInventory inv, ITextComponent title) {
        super(screenContainer, inv, title);
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_CORPSE);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GUIUtils.drawCenteredString(this.font, this.container.getCorpse().getName().getFormattedText(), this.xSize, 6);
        GUIUtils.drawCenteredString(this.font, this.playerInventory.getDisplayName().getFormattedText(), this.xSize, this.ySize - 94);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
