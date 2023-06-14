package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.GameTestDebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.patches.IDebugRendererPatch;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin implements IDebugRendererPatch {

    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
    @Shadow
    @Final
    public GameTestDebugRenderer gameTestDebugRenderer;
    @Shadow
    @Final
    public DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    @Shadow
    private boolean renderChunkborder;
    private boolean renderHeightmap;

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public static void renderFilledBox(double minX,
                                       double minY,
                                       double minZ,
                                       double maxX,
                                       double maxY,
                                       double maxZ,
                                       float r,
                                       float g,
                                       float b,
                                       float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        EvLevelRenderer.addChainedFilledBoxVertices(builder, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);
        tesselator.end();
    }

    /**
     * @author TheGreatWolf
     * @reason Active some debug renderers
     */
    @Overwrite
    public void render(PoseStack matrices, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ) {
        if (!Minecraft.getInstance().showOnlyReducedInfo()) {
            if (this.renderChunkborder) {
                this.chunkBorderRenderer.render(matrices, bufferSource, camX, camY, camZ);
            }
            if (this.renderHeightmap) {
                this.heightMapRenderer.render(matrices, bufferSource, camX, camY, camZ);
            }
        }
//        this.gameTestDebugRenderer.render(matrices, bufferSource, camX, camY, camZ);
    }

    @Override
    public void setRenderHeightmap(boolean render) {
        this.renderHeightmap = render;
    }
}

