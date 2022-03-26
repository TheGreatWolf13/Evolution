package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin {

    @Shadow
    private int indexCount;
    @Shadow
    private VertexFormat.IndexType indexType;
    @Shadow
    private VertexFormat.Mode mode;

    @Shadow
    public static void unbind() {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void unbindVertexArray() {
        throw new AbstractMethodError();
    }

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations
     */
    @Overwrite
    public void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader) {
        if (this.indexCount != 0) {
            RenderSystem.assertOnRenderThread();
            BufferUploader.reset();
            for (int i = 0; i < 12; ++i) {
                int j = RenderSystem.getShaderTexture(i);
                //Avoid allocating fixed name strings
                shader.setSampler(RenderHelper.SAMPLER_NAMES[i], j);
            }
            if (shader.MODEL_VIEW_MATRIX != null) {
                shader.MODEL_VIEW_MATRIX.set(modelViewMatrix);
            }
            if (shader.PROJECTION_MATRIX != null) {
                shader.PROJECTION_MATRIX.set(projectionMatrix);
            }
            if (shader.INVERSE_VIEW_ROTATION_MATRIX != null) {
                shader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
            }
            if (shader.COLOR_MODULATOR != null) {
                shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
            }
            if (shader.FOG_START != null) {
                shader.FOG_START.set(RenderSystem.getShaderFogStart());
            }
            if (shader.FOG_END != null) {
                shader.FOG_END.set(RenderSystem.getShaderFogEnd());
            }
            if (shader.FOG_COLOR != null) {
                shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
            }
            if (shader.TEXTURE_MATRIX != null) {
                shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }
            if (shader.GAME_TIME != null) {
                shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }
            if (shader.SCREEN_SIZE != null) {
                Window window = Minecraft.getInstance().getWindow();
                shader.SCREEN_SIZE.set((float) window.getWidth(), window.getHeight());
            }
            if (shader.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
                shader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }
            RenderSystem.setupShaderLights(shader);
            this.bindVertexArray();
            this.bind();
            this.getFormat().setupBufferState();
            shader.apply();
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
            shader.clear();
            this.getFormat().clearBufferState();
            unbind();
            unbindVertexArray();
        }
    }

    @Shadow
    public abstract void bind();

    @Shadow
    protected abstract void bindVertexArray();

    @Shadow
    public abstract VertexFormat getFormat();
}
