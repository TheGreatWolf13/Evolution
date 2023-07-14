package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gui.GUIUtils;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget extends GuiComponent {

    @Shadow @Final public static ResourceLocation WIDGETS_LOCATION;
    @Shadow public boolean active;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow protected float alpha;
    @Shadow protected int height;
    @Shadow protected int width;

    @Shadow
    public abstract Component getMessage();

    @Shadow
    protected abstract int getYImage(boolean pIsHovered);

    @Shadow
    public abstract boolean isHoveredOrFocused();

    @Shadow
    protected abstract void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY);

    /**
     * @author TheGreatWolf
     * @reason Make text that is too long slide
     */
    @Overwrite
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(matrices, minecraft, mouseX, mouseY);
        int fgColor = (this.active ? 0xFF_FFFF : 0xA0_A0A0) | Mth.ceil(this.alpha * 255.0F) << 24;
        Component message = this.getMessage();
        int messageWidth = font.width(message);
        int widthThatFits = this.width - 4;
        if (messageWidth > widthThatFits) {
            double d = Util.getMillis() / 1_000.0;
            double e = Math.sin(Mth.HALF_PI * Math.cos(d));
            int delta = messageWidth - widthThatFits;
            GUIUtils.enableScissor(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2);
            drawCenteredString(matrices, font, message, this.x + this.width / 2 - (int) (e * delta), this.y + (this.height - 8) / 2, fgColor);
            GUIUtils.disableScissor();
        }
        else {
            drawCenteredString(matrices, font, message, this.x + this.width / 2, this.y + (this.height - 8) / 2, fgColor);
        }
    }
}
