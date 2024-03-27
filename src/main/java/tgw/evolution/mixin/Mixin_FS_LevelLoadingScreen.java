package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.util.collection.maps.R2IHashMap;
import tgw.evolution.util.collection.maps.R2IMap;

@Mixin(LevelLoadingScreen.class)
public abstract class Mixin_FS_LevelLoadingScreen extends Screen {

    @Final @Shadow @DeleteField private static Object2IntMap<ChunkStatus> COLORS;
    @Unique private static R2IMap<ChunkStatus> STATUS_TO_COLOR_FAST;

    protected Mixin_FS_LevelLoadingScreen(Component text) {
        super(text);
    }

    /**
     * @author TheGreatWolf
     * @reason Use faster collections.
     */
    @Overwrite
    public static void renderChunks(PoseStack matrices, StoringChunkProgressListener tracker, int mapX, int mapY, int mapScale, int mapPadding) {
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        Matrix4f matrix = matrices.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int centerSize = tracker.getFullDiameter();
        int size = tracker.getDiameter();
        int tileSize = mapScale + mapPadding;
        if (mapPadding != 0) {
            int mapRenderCenterSize = centerSize * tileSize - mapPadding;
            int radius = mapRenderCenterSize / 2 + 1;
            addRect(matrix, buffer, mapX - radius, mapY - radius, mapX - radius + 1, mapY + radius, 0xff00_11ff);
            addRect(matrix, buffer, mapX + radius - 1, mapY - radius, mapX + radius, mapY + radius, 0xff00_11ff);
            addRect(matrix, buffer, mapX - radius, mapY - radius, mapX + radius, mapY - radius + 1, 0xff00_11ff);
            addRect(matrix, buffer, mapX - radius, mapY + radius - 1, mapX + radius, mapY + radius, 0xff00_11ff);
        }
        ChunkStatus prevStatus = null;
        int prevColor = 0xff00_0000;
        int mapRenderSize = size * tileSize - mapPadding;
        int mapStartX = mapX - mapRenderSize / 2;
        int mapStartY = mapY - mapRenderSize / 2;
        for (int x = 0; x < size; ++x) {
            int tileX = mapStartX + x * tileSize;
            for (int z = 0; z < size; ++z) {
                int tileY = mapStartY + z * tileSize;
                ChunkStatus status = tracker.getStatus(x, z);
                int color;
                if (prevStatus == status) {
                    color = prevColor;
                }
                else {
                    color = STATUS_TO_COLOR_FAST.getInt(status);
                    prevStatus = status;
                    prevColor = color;
                }
                addRect(matrix, buffer, tileX, tileY, tileX + mapScale, tileY + mapScale, color);
            }
        }
        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Unique
    private static void addRect(Matrix4f matrix, VertexConsumer buffer, int x1, int y1, int x2, int y2, int color) {
        buffer.vertex(matrix, x1, y2, 0).color(color).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(color).endVertex();
        buffer.vertex(matrix, x2, y1, 0).color(color).endVertex();
        buffer.vertex(matrix, x1, y1, 0).color(color).endVertex();
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        STATUS_TO_COLOR_FAST = new R2IHashMap<>();
        STATUS_TO_COLOR_FAST.put(null, 0xff00_0000);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.EMPTY, 0xff54_5454);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.STRUCTURE_STARTS, 0xff99_9999);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.STRUCTURE_REFERENCES, 0xff91_615f);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.BIOMES, 0xff52_b280);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.NOISE, 0xffd1_d1d1);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.SURFACE, 0xff09_6872);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.CARVERS, 0xff5c_666d);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.LIQUID_CARVERS, 0xff72_3530);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.FEATURES, 0xff00_c621);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.LIGHT, 0xffcc_cccc);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.SPAWN, 0xff60_60f2);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.HEIGHTMAPS, 0xffee_eeee);
        STATUS_TO_COLOR_FAST.put(ChunkStatus.FULL, 0xffff_ffff);
    }
}
