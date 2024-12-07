package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.chunk.LevelRenderer;

import java.util.Map;

@Mixin(HeightMapRenderer.class)
public abstract class MixinHeightMapRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract Vector3f getColor(Heightmap.Types pTypes);

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer, avoid allocations
     */
    @Overwrite
    public void render(PoseStack matrices, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        LevelAccessor level = this.minecraft.level;
        assert level != null;
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        int posX = Mth.floor(camX);
        int posZ = Mth.floor(camZ);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = -2; i <= 2; ++i) {
            int secX = SectionPos.blockToSectionCoord(posX + 16 * i);
            for (int j = -2; j <= 2; ++j) {
                ChunkAccess chunk = level.getChunk(secX, SectionPos.blockToSectionCoord(posZ + 16 * j));
                for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
                    Heightmap.Types heightmap = entry.getKey();
                    ChunkPos pos = chunk.getPos();
                    float r = 0;
                    float g = 0;
                    float b = 0;
                    switch (heightmap) {
                        case WORLD_SURFACE_WG -> {
                            r = 1;
                            g = 1;
                            b = 0;
                        }
                        case OCEAN_FLOOR_WG -> {
                            r = 1;
                            g = 0;
                            b = 1;
                        }
                        case WORLD_SURFACE -> {
                            r = 0;
                            g = 0.7f;
                            b = 0;
                        }
                        case OCEAN_FLOOR -> {
                            r = 0;
                            g = 0;
                            b = 0.5f;
                        }
                        case MOTION_BLOCKING -> {
                            r = 0;
                            g = 0.3f;
                            b = 0.3f;
                        }
                        case MOTION_BLOCKING_NO_LEAVES -> {
                            r = 0;
                            g = 0.5f;
                            b = 0.5f;
                        }
                    }
                    for (int dx = 0; dx < 16; ++dx) {
                        int x = SectionPos.sectionToBlockCoord(pos.x, dx);
                        for (int dz = 0; dz < 16; ++dz) {
                            int z = SectionPos.sectionToBlockCoord(pos.z, dz);
                            double y = level.getHeight(heightmap, x, z) + heightmap.ordinal() * 0.093_75F - camY;
                            LevelRenderer.addChainedFilledBoxVertices(builder, x + 0.25F - camX, y, z + 0.25F - camZ,
                                                                      x + 0.75F - camX, y + 0.093_75F, (z + 0.75F) - camZ,
                                                                      r, g, b, 1.0F);
                        }
                    }
                }
            }
        }
        tesselator.end();
        RenderSystem.enableTexture();
    }
}
