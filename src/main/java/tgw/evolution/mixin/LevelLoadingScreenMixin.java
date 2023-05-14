package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.util.math.ColorABGR;
import tgw.evolution.util.math.ColorARGB;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin extends Screen {

    private static final int NULL_STATUS_COLOR = ColorABGR.pack(0, 0, 0, 0xFF);
    private static final int DEFAULT_STATUS_COLOR = ColorARGB.pack(0, 0x11, 0xFF, 0xFF);
    @Shadow
    @Final
    private static Object2IntMap<ChunkStatus> COLORS;
    @Nullable
    private static Reference2IntOpenHashMap<ChunkStatus> STATUS_TO_COLOR_FAST;

    protected LevelLoadingScreenMixin(Component text) {
        super(text);
    }

    private static void addRect(Matrix4f matrix, VertexConsumer buffer, int x1, int y1, int x2, int y2, int color) {
        buffer.vertex(matrix, x1, y2, 0).color(color).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(color).endVertex();
        buffer.vertex(matrix, x2, y1, 0).color(color).endVertex();
        buffer.vertex(matrix, x1, y1, 0).color(color).endVertex();
    }

    /**
     * @author TheGreatWolf
     * @reason Use faster collections.
     */
    @Overwrite
    public static void renderChunks(PoseStack matrices, StoringChunkProgressListener tracker, int mapX, int mapY, int mapScale, int mapPadding) {
        if (STATUS_TO_COLOR_FAST == null) {
            STATUS_TO_COLOR_FAST = new Reference2IntOpenHashMap<>(COLORS.size());
            STATUS_TO_COLOR_FAST.put(null, NULL_STATUS_COLOR);
            for (Object2IntMap.Entry<ChunkStatus> entry : COLORS.object2IntEntrySet()) {
                STATUS_TO_COLOR_FAST.put(entry.getKey(), ColorARGB.toABGR(entry.getIntValue(), 0xFF));
            }
        }
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
            addRect(matrix, buffer, mapX - radius, mapY - radius, mapX - radius + 1, mapY + radius, DEFAULT_STATUS_COLOR);
            addRect(matrix, buffer, mapX + radius - 1, mapY - radius, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
            addRect(matrix, buffer, mapX - radius, mapY - radius, mapX + radius, mapY - radius + 1, DEFAULT_STATUS_COLOR);
            addRect(matrix, buffer, mapX - radius, mapY + radius - 1, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
        }
        ChunkStatus prevStatus = null;
        int prevColor = NULL_STATUS_COLOR;
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
}
