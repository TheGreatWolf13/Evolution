package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.XoRoShiRoRandom;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();
    @Shadow
    @Final
    private ItemColors itemColors;

    private static void addVertexDataTemperature(VertexConsumer builder,
                                                 PoseStack.Pose entry,
                                                 BakedQuad quad,
                                                 float red,
                                                 float green,
                                                 float blue,
                                                 float alpha,
                                                 int packedLight,
                                                 int overlay) {
        int[] vertices = quad.getVertices();
        Vec3i faceNormal = quad.getDirection().getNormal();
        IMatrix4fPatch poseMat = MathHelper.getExtendedMatrix(entry.pose());
        IMatrix3fPatch normalMat = MathHelper.getExtendedMatrix(entry.normal());
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        int vertexCount = vertices.length / 8;
        for (int vertex = 0; vertex < vertexCount; ++vertex) {
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Color : 4 byte
            int color = vertices[offset + 3];
            float r = (color >> 24 & 255) / 255.0F * red;
            float g = (color >> 16 & 255) / 255.0F * green;
            float b = (color >> 8 & 255) / 255.0F * blue;
            float a = (color & 255) / 255.0F * alpha;
            //Texture : 2 float
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            //Light : 2 short
            int l = quad.getTintIndex() == 0 ? packedLight : 0xf0_00f0;
            int bl = l & 0xFFFF;
            int sl = l >> 16 & 0xFFFF;
            int light = vertices[offset + 6];
            int blBaked = light & 0xffff;
            int slBaked = light >> 16 & 0xffff;
            bl = Math.max(bl, blBaked);
            sl = Math.max(sl, slBaked);
            int lightmapCoord = bl | sl << 16;
            //Normal : 3 byte
            int norm = vertices[offset + 7];
            byte nx = (byte) (norm & 255);
            byte ny = (byte) (norm >> 8 & 255);
            byte nz = (byte) (norm >> 16 & 255);
            if (nx != 0 || ny != 0 || nz != 0) {
                normX = nx / 127.0f;
                normY = ny / 127.0f;
                normZ = nz / 127.0f;
                float nX = normalMat.transformVecX(normX, normY, normZ);
                float nY = normalMat.transformVecY(normX, normY, normZ);
                float nZ = normalMat.transformVecZ(normX, normY, normZ);
                normX = nX;
                normY = nY;
                normZ = nZ;
            }
            builder.vertex(x, y, z, r, g, b, a, u, v, overlay, lightmapCoord, normX, normY, normZ);
        }
    }

    /**
     * @author JellySquid
     * @reason Avoid allocations
     */
    @Overwrite
    public void renderModelLists(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer builder) {
        XoRoShiRoRandom random = this.random;
        for (Direction direction : DirectionUtil.ALL) {
            List<BakedQuad> quads = model.getQuads(null, direction, random.setSeedAndReturn(42L));
            if (!quads.isEmpty()) {
                this.renderQuadList(matrices, builder, quads, stack, light, overlay);
            }
        }
        List<BakedQuad> quads = model.getQuads(null, null, random.setSeedAndReturn(42L));
        if (!quads.isEmpty()) {
            this.renderQuadList(matrices, builder, quads, stack, light, overlay);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to implement a new method to calculate item colors.
     */
    @Overwrite
    public void renderQuadList(PoseStack matrices, VertexConsumer builder, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean notEmpty = !stack.isEmpty();
        PoseStack.Pose pose = matrices.last();
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad quad = quads.get(i);
            int color = 0xffff_ffff;
            if (notEmpty && quad.isTinted()) {
                color = this.itemColors.getColor(stack, quad.getTintIndex());
            }
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            if (stack.getItem() instanceof IItemTemperature) {
                float a = (color >> 24 & 255) / 255.0f;
                addVertexDataTemperature(builder, pose, quad, r, g, b, a, light, overlay);
            }
            else {
                builder.putBulkData(pose, quad, r, g, b, light, overlay, true);
            }
        }
    }
}
