package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchRenderTarget;

@Mixin(RenderTarget.class)
public abstract class MixinRenderTarget implements PatchRenderTarget {

    @Shadow public int frameBufferId;
    @Shadow public int height;
    @Shadow @Final public boolean useDepth;
    @Shadow public int viewHeight;
    @Shadow public int viewWidth;
    @Shadow public int width;
    @Shadow protected int colorTextureId;
    @Shadow protected int depthBufferId;
    @Unique private boolean stencilEnabled;

    @Shadow
    public abstract void checkStatus();

    @Shadow
    public abstract void clear(boolean bl);

    /**
     * @author TheGreatWolf
     * @reason Create stencil buffers
     */
    @Overwrite
    public void createBuffers(int width, int height, boolean cleanError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= i && height > 0 && height <= i) {
            this.viewWidth = width;
            this.viewHeight = height;
            this.width = width;
            this.height = height;
            this.frameBufferId = GlStateManager.glGenFramebuffers();
            this.colorTextureId = TextureUtil.generateTextureId();
            if (this.useDepth) {
                this.depthBufferId = TextureUtil.generateTextureId();
                GlStateManager._bindTexture(this.depthBufferId);
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, 0);
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                if (this.stencilEnabled) {
                    GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, GL30.GL_DEPTH_STENCIL,
                                               GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
                }
                else {
                    GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, this.width, this.height, 0, GL11.GL_DEPTH_COMPONENT,
                                               GL11.GL_FLOAT, null);
                }
            }
            this.setFilterMode(GL11.GL_NEAREST);
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.colorTextureId, 0);
            if (this.useDepth) {
                if (this.stencilEnabled) {
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                                                           this.depthBufferId, 0);
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D,
                                                           this.depthBufferId, 0);
                }
                else {
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthBufferId, 0);
                }
            }
            this.checkStatus();
            this.clear(cleanError);
            this.unbindRead();
        }
        else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
        }
    }

    /**
     * Attempts to enable 8 bits of stencil buffer on this FrameBuffer.
     * This is to prevent the default cause where graphics cards do not support stencil bits.
     * <b>Make sure to call this on the main render thread!</b>
     */
    @Override
    public void enableStencil() {
        if (this.stencilEnabled) {
            return;
        }
        this.stencilEnabled = true;
        this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
    }

    /**
     * Returns wither or not this FBO has been successfully initialized with stencil bits.
     */
    @Override
    public boolean isStencilEnabled() {
        return this.stencilEnabled;
    }

    @Shadow
    public abstract void resize(int i, int j, boolean bl);

    @Shadow
    public abstract void setFilterMode(int i);

    @Shadow
    public abstract void unbindRead();
}
