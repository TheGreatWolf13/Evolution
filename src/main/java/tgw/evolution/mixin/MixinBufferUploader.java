package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL15C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;

import java.nio.ByteBuffer;

@Mixin(BufferUploader.class)
public abstract class MixinBufferUploader {

    @Shadow private static int lastIndexBufferObject;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private static void _end(ByteBuffer buffer,
                             VertexFormat.Mode mode,
                             VertexFormat format,
                             int vertexCount,
                             VertexFormat.IndexType indexType,
                             int indexCount,
                             boolean sequentialIndex) {
        RenderSystem.assertOnRenderThread();
        buffer.clear();
        if (vertexCount > 0) {
            int i = vertexCount * format.getVertexSize();
            updateVertexSetup(format);
            buffer.position(0);
            buffer.limit(i);
            GlStateManager._glBufferData(GL15C.GL_ARRAY_BUFFER, buffer, GL15C.GL_DYNAMIC_DRAW);
            int j;
            if (sequentialIndex) {
                RenderSystem.AutoStorageIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(mode, indexCount);
                int k = sequentialBuffer.name();
                if (k != lastIndexBufferObject) {
                    GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, k);
                    lastIndexBufferObject = k;
                }
                j = sequentialBuffer.type().asGLType;
            }
            else {
                int i1 = format.getOrCreateIndexBufferObject();
                if (i1 != lastIndexBufferObject) {
                    GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, i1);
                    lastIndexBufferObject = i1;
                }
                buffer.position(i);
                buffer.limit(i + indexCount * indexType.bytes);
                GlStateManager._glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15C.GL_DYNAMIC_DRAW);
                j = indexType.asGLType;
            }
            ShaderInstance shader = RenderSystem.getShader();
            for (int j1 = 0; j1 < 8; ++j1) {
                int l = RenderSystem.getShaderTexture(j1);
                //Avoid allocating fixed name strings
                shader.setSampler(RenderHelper.SAMPLER_NAMES[j1], l);
            }
            if (shader.MODEL_VIEW_MATRIX != null) {
                shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            }
            if (shader.PROJECTION_MATRIX != null) {
                shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
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
            if (shader.FOG_SHAPE != null) {
                shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
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
            if (shader.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
                shader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }
            RenderSystem.setupShaderLights(shader);
            shader.apply();
            GlStateManager._drawElements(mode.asGLMode, indexCount, j, 0L);
            shader.clear();
            buffer.position(0);
        }
    }

    @Shadow
    private static void updateVertexSetup(VertexFormat pFormat) {
        throw new AbstractMethodError();
    }
}
