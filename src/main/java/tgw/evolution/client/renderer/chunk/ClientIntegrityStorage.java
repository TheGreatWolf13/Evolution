package tgw.evolution.client.renderer.chunk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IFillable;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;

public class ClientIntegrityStorage {

    private final L2OMap<byte @Nullable []> integrity = new L2OHashMap<>();
    private final L2OMap<byte @Nullable []> loadFactor = new L2OHashMap<>();
    private final L2OMap<byte @Nullable []> stability = new L2OHashMap<>();

    private static int getColour(int value, int min, int max) {
        if (max == 255) {
            return 0xff00_00ff;
        }
        value = Mth.clamp(value, min, max);
        float delta = (float) (value - min) / (max - min);
        if (Float.isNaN(delta)) {
            return 0xffff_ffff;
        }
        int r = (int) ((delta <= 0.5 ? delta * 2 : 1) * 255);
        int g = (int) ((delta >= 0.5 ? (1 - delta) * 2 : 1) * 255);
        return 0xff << 24 | r << 16 | g << 8;
    }

    public void clear() {
        this.integrity.clear();
        this.loadFactor.clear();
        this.stability.clear();
    }

    public void put(long pos, byte[] loadArray, byte[] integrityArray, byte[] stabilityArray) {
        this.integrity.put(pos, integrityArray);
        this.loadFactor.put(pos, loadArray);
        this.stability.put(pos, stabilityArray);
    }

    public void remove(long pos) {
        this.integrity.remove(pos);
        this.loadFactor.remove(pos);
        this.stability.remove(pos);
    }

    public void render(BlockGetter level, PoseStack matrices, MultiBufferSource buffer, float camX, float camY, float camZ) {
        L2OMap<byte @Nullable []> loadFactor = this.loadFactor;
        L2OMap<byte @Nullable []> integrity = this.integrity;
        L2OMap<byte @Nullable []> stability = this.stability;
        for (L2OMap.Entry<byte[]> e = loadFactor.fastEntries(); e != null; e = loadFactor.fastEntries()) {
            long pos = e.key();
            int x0 = SectionPos.sectionToBlockCoord(SectionPos.x(pos));
            if (Math.abs(camX - (x0 + 8)) > 32) {
                continue;
            }
            int y0 = SectionPos.sectionToBlockCoord(SectionPos.y(pos));
            if (Math.abs(camY - (y0 + 8)) > 32) {
                continue;
            }
            int z0 = SectionPos.sectionToBlockCoord(SectionPos.z(pos));
            if (Math.abs(camZ - (z0 + 8)) > 32) {
                continue;
            }
            int endX = x0 + 16;
            int endY = y0 + 16;
            int endZ = z0 + 16;
            byte[] loadF = e.value();
            byte[] integ = integrity.get(pos);
            byte[] stab = stability.get(pos);
            assert integ != null;
            assert stab != null;
            VertexConsumer lines = buffer.getBuffer(RenderType.lines());
            for (int x = x0; x < endX; ++x) {
                for (int y = y0; y < endY; ++y) {
                    for (int z = z0; z < endZ; ++z) {
                        BlockState state = level.getBlockState_(x, y, z);
                        Block block = state.getBlock();
                        if (block instanceof IFillable) {
                            int index = x - x0 + (z - z0) * 16 + (y - y0) * 16 * 16;
                            int i = Byte.toUnsignedInt(integ[index]);
                            int colour = getColour(Byte.toUnsignedInt(loadF[index]), 0, i);
                            EvLevelRenderer.renderLineBox(matrices, lines, x - camX, y - camY, z - camZ, x + 1 - camX, y + 1 - camY, z + 1 - camZ, (colour >> 16 & 0xFF) / 255.0f, (colour >> 8 & 0xFF) / 255.0f, (colour & 0xFF) / 255.0f, (colour >> 24 & 0xFF) / 255.0f);
                            if (colour != 0xffff_ffff && (stab[(x - x0 >> 3) + (z - z0) * 2 + (y - y0) * 2 * 16] & 1 << (x - x0 & 7)) != 0) {
                                EvLevelRenderer.renderLineBox(matrices, lines, x - camX + 0.45, y - camY, z - camZ, x + 1 - camX - 0.45, y + 1 - camY, z + 1 - camZ, 0.0f, 0.0f, 1.0f, 1.0f);
                                EvLevelRenderer.renderLineBox(matrices, lines, x - camX, y - camY + 0.45, z - camZ, x + 1 - camX, y + 1 - camY - 0.45, z + 1 - camZ, 0.0f, 0.0f, 1.0f, 1.0f);
                                EvLevelRenderer.renderLineBox(matrices, lines, x - camX, y - camY, z - camZ + 0.45, x + 1 - camX, y + 1 - camY, z + 1 - camZ - 0.45, 0.0f, 0.0f, 1.0f, 1.0f);
                            }
                        }
                    }
                }
            }
        }
    }
}
