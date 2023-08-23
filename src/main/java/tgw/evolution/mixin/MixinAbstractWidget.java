package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.gui.widgets.Area;
import tgw.evolution.patches.PatchAbstractWidget;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget extends GuiComponent implements PatchAbstractWidget, Widget, GuiEventListener, NarratableEntry {

    @Shadow @Final public static ResourceLocation WIDGETS_LOCATION;
    @Shadow public boolean active;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow protected float alpha;
    @Shadow protected int height;
    @Shadow protected int width;
    @Unique private @Nullable AbstractWidget parent;
    @Unique private @Nullable Screen screen;

    @Override
    public void childRequestedUpdate() {
    }

    @Override
    public void focusOnParent() {
        if (this.screen != null) {
            this.screen.setFocused(this);
        }
        if (this.parent instanceof Area a) {
            a.setFocusOnParent((AbstractWidget) (Object) this);
            a.focusOnParent();
        }
    }

    @Shadow
    public abstract Component getMessage();

    @Override
    public @Nullable AbstractWidget getParent() {
        return this.parent;
    }

    @Override
    public @Nullable Screen getScreen() {
        return this.screen;
    }

    @Shadow
    protected abstract int getYImage(boolean pIsHovered);

    @Shadow
    public abstract boolean isHoveredOrFocused();

    @Shadow
    protected abstract void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY);

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
            int delta = messageWidth - widthThatFits;
            double mult = 0.51;
            if (delta < 2) {
                delta = 2;
                mult = 1;
            }
            double e = Math.sin(Mth.HALF_PI * Math.cos(d)) * mult;
            GUIUtils.enableScissor(this.x + 2, this.y + 2, this.x + this.width - 3, this.y + this.height - 2);
            drawCenteredString(matrices, font, message, this.x + this.width / 2 - (int) (e * delta), this.y + (this.height - 8) / 2, fgColor);
            GUIUtils.disableScissor();
        }
        else {
            drawCenteredString(matrices, font, message, this.x + this.width / 2, this.y + (this.height - 8) / 2, fgColor);
        }
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setParent(@Nullable AbstractWidget parent) {
        this.parent = parent;
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        this.screen = screen;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }
}
