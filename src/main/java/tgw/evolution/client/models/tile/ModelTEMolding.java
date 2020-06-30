//package tgw.evolution.client.models.tile;
//
//import net.minecraft.block.BlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.Vector3f;
//import net.minecraft.client.renderer.model.BakedQuad;
//import net.minecraft.client.renderer.model.ItemCameraTransforms;
//import net.minecraft.client.renderer.model.ItemOverrideList;
//import net.minecraft.client.renderer.model.ItemTransformVec3f;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.Vec3d;
//import net.minecraftforge.client.model.data.IDynamicBakedModel;
//import net.minecraftforge.client.model.data.IModelData;
//import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
//import tgw.evolution.Evolution;
//import tgw.evolution.blocks.tileentities.TEMolding;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//public class ModelTEMolding implements IDynamicBakedModel {
//
//    private final VertexFormat format;
//    private final ItemCameraTransforms transforms = this.getAllTransforms();
//
//    public ModelTEMolding(VertexFormat format) {
//        this.format = format;
//    }
//
//    private static Vec3d v(double x, double y, double z) {
//        return new Vec3d(x, y, z);
//    }
//
//    private TextureAtlasSprite getTexture() {
//        String name = Evolution.MODID + ":block/clay";
//        return Minecraft.getInstance().getTextureMap().getAtlasSprite(name);
//    }
//
//    public ItemCameraTransforms getAllTransforms() {
//        ItemTransformVec3f tpLeft = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
//        ItemTransformVec3f tpRight = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
//        ItemTransformVec3f fpLeft = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
//        ItemTransformVec3f fpRight = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
//        ItemTransformVec3f head = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
//        ItemTransformVec3f gui = this.getTransform(ItemCameraTransforms.TransformType.GUI);
//        ItemTransformVec3f ground = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
//        ItemTransformVec3f fixed = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
//        return new ItemCameraTransforms(tpLeft, tpRight, fpLeft, fpRight, head, gui, ground, fixed);
//    }
//
//    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
//        for (int e = 0; e < this.format.getElementCount(); e++) {
//            switch (this.format.getElement(e).getUsage()) {
//                case POSITION:
//                    builder.put(e, (float) x, (float) y, (float) z, 1.0f);
//                    break;
//                case COLOR:
//                    builder.put(e, r, g, b, 1.0f);
//                    break;
//                case UV:
//                    if (this.format.getElement(e).getIndex() == 0) {
//                        u = sprite.getInterpolatedU(u);
//                        v = sprite.getInterpolatedV(v);
//                        builder.put(e, u, v, 0f, 1f);
//                        break;
//                    }
//                case NORMAL:
//                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
//                    break;
//                default:
//                    builder.put(e);
//                    break;
//            }
//        }
//    }
//
//    private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type) {
//        if (type.equals(ItemCameraTransforms.TransformType.GUI)) {
//            return new ItemTransformVec3f(new Vector3f(200, 50, 100), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
//        }
//        return ItemTransformVec3f.DEFAULT;
//    }
//
//    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
//        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();
//        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(this.format);
//        builder.setTexture(sprite);
//        this.putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f);
//        return builder.build();
//    }
//
//    @Nonnull
//    @Override
//    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
//        boolean[][][] matrices = extraData.getData(TEMolding.MATRICES);
//        if (side == null || matrices == null) {
//            return Collections.emptyList();
//        }
//        switch (side) {
//            case UP:
//                return this.up(matrices);
//            case DOWN:
//                break;
//            case NORTH:
//                break;
//            case SOUTH:
//                break;
//            case WEST:
//                break;
//            case EAST:
//                break;
//        }
//        return Collections.emptyList();
//    }
//
//    private List<BakedQuad> up(boolean[][][] tensor) {
//        List<BakedQuad> quads = new ArrayList<>();
//        for (int y = 0; y < 5; y++) {
//            if (tensor[y] == null) {
//                break;
//            }
//            for (int i = 0; i < 5; i++) {
//                for (int j = 0; j < 5; j++) {
//                    if (tensor[y][i][j]) {
//                        this.tryRenderUp(quads, y, i, j, tensor);
//                    }
//                }
//            }
//        }
//        return quads;
//    }
//
//    private void tryRenderUp(List<BakedQuad> quads, int y, int i, int j, boolean[][][] tensor) {
//        if (y == 4) {
//            quads.add(this.createUp(i, y, j));
//            return;
//        }
//        if (tensor[y + 1] == null) {
//            quads.add(this.createUp(i, y, j));
//            return;
//        }
//        if (!tensor[y + 1][i][j]) {
//            quads.add(this.createUp(i, y, j));
//        }
//    }
//
//    private BakedQuad createUp(int i, int y, int j) {
//        Vec3d v1 = v((i + 0.03125) * 0.1875, (y + 1) * 0.1875, (j + 0.03125) * 0.1875);
//        Vec3d v2 = v((i + 0.03125) * 0.1875, (y + 1) * 0.1875, (j + 1.03125) * 0.1875);
//        Vec3d v3 = v((i + 1.03125) * 0.1875, (y + 1) * 0.1875, (j + 1.03125) * 0.1875);
//        Vec3d v4 = v((i + 1.03125) * 0.1875, (y + 1) * 0.1875, (j + 0.03125) * 0.1875);
//        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();
//        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(this.format);
//        TextureAtlasSprite texture = this.getTexture();
//        builder.setTexture(texture);
//        this.putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, texture, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, texture, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, texture, 1.0f, 1.0f, 1.0f);
//        this.putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, texture, 1.0f, 1.0f, 1.0f);
//        return builder.build();
//    }
//
//    @Override
//    public boolean isAmbientOcclusion() {
//        return true;
//    }
//
//    @Override
//    public boolean isGui3d() {
//        return false;
//    }
//
//    @Override
//    public boolean isBuiltInRenderer() {
//        return false;
//    }
//
//    @Override
//    public TextureAtlasSprite getParticleTexture() {
//        return this.getTexture();
//    }
//
//    @Override
//    public ItemOverrideList getOverrides() {
//        return ItemOverrideList.EMPTY;
//    }
//
//    @Override
//    public ItemCameraTransforms getItemCameraTransforms() {
//        return this.transforms;
//    }
//}
