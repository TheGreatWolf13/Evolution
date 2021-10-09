package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.XoRoShiRoRandom;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();
    @Shadow
    @Final
    private ItemColors itemColors;

    private static void addVertexDataTemperature(IVertexBuilder builder,
                                                 MatrixStack.Entry entry,
                                                 BakedQuad quad,
                                                 float red,
                                                 float green,
                                                 float blue,
                                                 float alpha,
                                                 int light,
                                                 int overlay) {
        int[] vertices = quad.getVertices();
        Vector3i faceNormal = quad.getDirection().getNormal();
        Vector3f normal = new Vector3f(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        IMatrix4fPatch pose = MathHelper.getExtendedMatrix(entry.pose());
        normal.transform(entry.normal());
        int intSize = DefaultVertexFormats.BLOCK.getIntegerSize();
        int vertexCount = vertices.length / intSize;
        try (MemoryStack memory = MemoryStack.stackPush()) {
            ByteBuffer buffer = memory.malloc(DefaultVertexFormats.BLOCK.getVertexSize());
            IntBuffer intBuffer = buffer.asIntBuffer();
            for (int vertex = 0; vertex < vertexCount; ++vertex) {
                intBuffer.clear();
                intBuffer.put(vertices, vertex * 8, 8);
                float f = buffer.getFloat(0);
                float f1 = buffer.getFloat(4);
                float f2 = buffer.getFloat(8);
                float r = (buffer.get(12) & 255) / 255.0F;
                float g = (buffer.get(13) & 255) / 255.0F;
                float b = (buffer.get(14) & 255) / 255.0F;
                float a = (buffer.get(15) & 255) / 255.0F;
                int lightmapCoord = builder.applyBakedLighting(quad.getTintIndex() == 0 ? light : 0xf0_00f0, buffer);
                float u = buffer.getFloat(16);
                float v = buffer.getFloat(20);
                float x = pose.transformVecX(f, f1, f2);
                float y = pose.transformVecY(f, f1, f2);
                float z = pose.transformVecZ(f, f1, f2);
                builder.applyBakedNormals(normal, buffer, entry.normal());
                float cr = r * red;
                float cg = g * green;
                float cb = b * blue;
                float ca = a * alpha;
                builder.vertex(x, y, z, cr, cg, cb, ca, u, v, overlay, lightmapCoord, normal.x(), normal.y(), normal.z());
            }
        }
    }

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    public void renderModelLists(IBakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, IVertexBuilder builder) {
        XoRoShiRoRandom random = this.random;
        for (Direction direction : DirectionUtil.ALL) {
            this.renderQuadList(matrices, builder, model.getQuads(null, direction, random.setSeedAndReturn(42L)), stack, light, overlay);
        }
        this.renderQuadList(matrices, builder, model.getQuads(null, null, random.setSeedAndReturn(42L)), stack, light, overlay);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to implement a new method to calculate item colors.
     */
    @Overwrite
    public void renderQuadList(MatrixStack matrices, IVertexBuilder builder, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean notEmpty = !stack.isEmpty();
        MatrixStack.Entry entry = matrices.last();
        for (BakedQuad quad : quads) {
            int color = 0xffff_ffff;
            if (notEmpty && quad.isTinted()) {
                color = this.itemColors.getColor(stack, quad.getTintIndex());
            }
            float a = (color >> 24 & 255) / 255.0f;
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            if (stack.getItem() instanceof IItemTemperature) {
                addVertexDataTemperature(builder, entry, quad, r, g, b, a, light, overlay);
            }
            else {
                builder.addVertexData(entry, quad, r, g, b, light, overlay, true);
            }
        }
    }
}
