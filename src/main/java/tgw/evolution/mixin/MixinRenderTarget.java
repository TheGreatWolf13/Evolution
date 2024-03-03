package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void _blitToScreen(int width, int height, boolean disableBlend) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, width, height);
        if (disableBlend) {
            GlStateManager._disableBlend();
        }
        ShaderInstance shader = Minecraft.getInstance().gameRenderer.blitShader;
        shader.setSampler("DiffuseSampler", this.colorTextureId);
        Matrix4f ortho = Matrix4f.orthographic(width, -height, 1_000.0F, 3_000.0F);
        RenderSystem.setProjectionMatrix(ortho);
        if (shader.MODEL_VIEW_MATRIX != null) {
            //Pass the translation matrix directly (0, 0, -2000)
            shader.MODEL_VIEW_MATRIX.setMat4x4(1, 0, 0, 0,
                                               0, 1, 0, 0,
                                               0, 0, 1, 0,
                                               0, 0, -2_000, 1);
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(ortho);
        }
        shader.apply();
        float u = this.viewWidth / (float) this.width;
        float v = this.viewHeight / (float) this.height;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(0, height, 0).uv(0, 0).color(255, 255, 255, 255).endVertex();
        builder.vertex(width, height, 0).uv(u, 0).color(255, 255, 255, 255).endVertex();
        builder.vertex(width, 0, 0).uv(u, v).color(255, 255, 255, 255).endVertex();
        builder.vertex(0, 0, 0).uv(0.0F, v).color(255, 255, 255, 255).endVertex();
        builder.end();
        BufferUploader._endInternal(builder);
        shader.clear();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }

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
