package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.items.IItemTemperature;
import tgw.evolution.patches.PatchVertexConsumer;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.XoRoShiRoRandom;

import java.util.Collection;
import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements IKeyedReloadListener {

    private static final List<ResourceLocation> DEPENDENCY = List.of(ReloadListernerKeys.MODELS);
    @Unique private final PoseStack matricesForGuiItems = new PoseStack();
    @Unique private final XoRoShiRoRandom random = new XoRoShiRoRandom();
    @Shadow public float blitOffset;
    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;
    @Shadow @Final private ItemColors itemColors;
    @Shadow @Final private ItemModelShaper itemModelShaper;
    @Shadow @Final private TextureManager textureManager;

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
        Matrix4f poseMat = entry.pose();
        Matrix3f normalMat = entry.normal();
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

    @Shadow
    public static VertexConsumer getCompassFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Pose pMatrixEntry) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource pBuffer,
                                                            RenderType pRenderType,
                                                            PoseStack.Pose pMatrixEntry) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static VertexConsumer getFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pIsItem, boolean pGlint) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static VertexConsumer getFoilBufferDirect(MultiBufferSource pBuffer,
                                                     RenderType pRenderType,
                                                     boolean pNoEntity,
                                                     boolean pWithGlint) {
        throw new AbstractMethodError();
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.ITEM_RENDERER;
    }

    /**
     * @author TheGreatWolf
     * @reason Optimize rendering of items in gui
     */
    @Overwrite
    public void render(ItemStack stack,
                       ItemTransforms.TransformType transformType,
                       boolean leftHand,
                       PoseStack matrices,
                       MultiBufferSource buffer,
                       int light,
                       int overlay,
                       BakedModel model) {
        if (!stack.isEmpty()) {
            matrices.pushPose();
            boolean flag = transformType == ItemTransforms.TransformType.GUI ||
                           transformType == ItemTransforms.TransformType.GROUND ||
                           transformType == ItemTransforms.TransformType.FIXED;
            if (flag) {
                if (stack.is(Items.TRIDENT)) {
                    model = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
                }
                else if (stack.is(Items.SPYGLASS)) {
                    model = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
                }
            }
            model.getTransforms().getTransform(transformType).apply(leftHand, matrices);
            matrices.translate(-0.5, -0.5, -0.5);
            if (!model.isCustomRenderer() && (!stack.is(Items.TRIDENT) || flag)) {
                boolean normal;
                if (transformType != ItemTransforms.TransformType.GUI && !transformType.firstPerson() && stack.getItem() instanceof BlockItem bi) {
                    Block block = bi.getBlock();
                    normal = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                }
                else {
                    normal = true;
                }
                RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, normal);
                VertexConsumer builder;
                if (stack.is(Items.COMPASS) && stack.hasFoil()) {
                    matrices.pushPose();
                    PoseStack.Pose pose = matrices.last();
                    if (transformType == ItemTransforms.TransformType.GUI) {
                        pose.pose().multiply(0.5F);
                    }
                    else if (transformType.firstPerson()) {
                        pose.pose().multiply(0.75F);
                    }
                    if (normal) {
                        builder = getCompassFoilBufferDirect(buffer, renderType, pose);
                    }
                    else {
                        builder = getCompassFoilBuffer(buffer, renderType, pose);
                    }
                    matrices.popPose();
                }
                else if (normal) {
                    builder = getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
                }
                else {
                    builder = getFoilBuffer(buffer, renderType, true, stack.hasFoil());
                }
                if (transformType == ItemTransforms.TransformType.GUI) {
                    Vector3f rotation = model.getTransforms().gui.rotation;
                    if (rotation.x() == 0 && rotation.y() == 0 && rotation.z() == 0) {
                        this.renderModelListsSpecial(model, stack, light, overlay, matrices, builder);
                        matrices.popPose();
                        return;
                    }
                }
                this.renderModelLists(model, stack, light, overlay, matrices, builder);
            }
            else {
                this.blockEntityRenderer.renderByItem(stack, transformType, matrices, buffer, light, overlay);
            }
            matrices.popPose();
        }
    }

    @Overwrite
    public void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.translate(i, j, 100.0F + this.blitOffset);
        internalMat.translate(8, 8, 0);
        internalMat.scale(1.0F, -1.0F, 1.0F);
        internalMat.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource builder = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flatLight = !bakedModel.usesBlockLight();
        if (flatLight) {
            Lighting.setupForFlatItems();
        }
        this.matricesForGuiItems.reset();
        this.render(itemStack, ItemTransforms.TransformType.GUI, false, this.matricesForGuiItems, builder, 0xf0_00f0, OverlayTexture.NO_OVERLAY,
                    bakedModel);
        builder.endBatch();
        RenderSystem.enableDepthTest();
        if (flatLight) {
            Lighting.setupFor3DItems();
        }
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * @author JellySquid
     * @reason Avoid allocations
     */
    @Overwrite
    private void renderModelLists(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer builder) {
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

    @Unique
    private void renderModelListsSpecial(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer builder) {
        List<BakedQuad> quads = model.getQuads(null, null, this.random.setSeedAndReturn(42L));
        if (!quads.isEmpty()) {
            this.renderQuadListSpecial(matrices, builder, quads, stack, light, overlay);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to implement a new method to calculate item colors.
     */
    @Overwrite
    private void renderQuadList(PoseStack matrices, VertexConsumer builder, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
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
                ((PatchVertexConsumer) builder).putBulkData(pose, quad, r, g, b, light, overlay, true);
            }
        }
    }

    @Unique
    private void renderQuadListSpecial(PoseStack matrices, VertexConsumer builder, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean notEmpty = !stack.isEmpty();
        PoseStack.Pose pose = matrices.last();
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad quad = quads.get(i);
            if (quad.getDirection() != Direction.SOUTH) {
                continue;
            }
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
                ((PatchVertexConsumer) builder).putBulkData(pose, quad, r, g, b, light, overlay, true);
            }
        }
    }
}
