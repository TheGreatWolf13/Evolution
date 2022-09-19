package tgw.evolution.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.gui.ScreenDisplayEffects;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.math.MathHelper;

public class RunnableAddingEffect implements Runnable {

    private boolean active;
    private float alpha;
    private @Nullable Font font;
    private @Nullable ClientEffectInstance instance;
    private @Nullable PoseStack matrices;
    private @Nullable TextureAtlasSprite sprite;
    private float x;
    private float y;

    public void discard() {
        this.active = false;
        this.sprite = null;
        this.matrices = null;
        this.instance = null;
        this.font = null;
    }

    @Override
    public void run() {
        if (this.active) {
            assert this.instance != null;
            assert this.matrices != null;
            assert this.sprite != null;
            assert this.font != null;
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            if (this.instance.isAmbient()) {
                ClientRenderer.floatBlit(this.matrices, this.x, this.y, 180, 180, 24, 24, -80);
            }
            else {
                ClientRenderer.floatBlit(this.matrices, this.x, this.y, 156, 180, 24, 24, -80);
            }
            RenderSystem.setShaderTexture(0, this.sprite.atlas().location());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            ClientRenderer.floatBlit(this.matrices, this.x + 3, this.y + 3, -79, 18, 18, this.sprite);
            if (this.instance.getAmplifier() != 0) {
                this.matrices.pushPose();
                this.matrices.scale(0.5f, 0.5f, 0.5f);
                this.font.drawShadow(this.matrices, MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(this.instance) + 1),
                                     (this.x + 3) * 2, (this.y + 17) * 2, 0xffff_ffff);
                this.matrices.popPose();
            }
        }
    }

    public void set(PoseStack matrices, float x, float y, float alpha, ClientEffectInstance instance, TextureAtlasSprite sprite, Font font) {
        this.active = true;
        this.matrices = matrices;
        this.x = x;
        this.y = y;
        this.alpha = alpha;
        this.instance = instance;
        this.sprite = sprite;
        this.font = font;
    }
}
