package tgw.evolution.client.models.tile;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.Patterns;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.RockVariant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BakedModelKnapping implements IBakedModel {

    public static final ModelProperty<Long> PARTS = new ModelProperty<>();
    private static final Vector3f FROM = new Vector3f();
    private static final Vector3f TO = new Vector3f();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private final IBakedModel baseModel;
    private final RockVariant variant;

    public BakedModelKnapping(IBakedModel baseModel, RockVariant variant) {
        this.baseModel = baseModel;
        this.variant = variant;
    }

    public static ModelDataMap getEmptyIModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(PARTS, -1L);
        return builder.build();
    }

    private static BakedQuad getQuadForPart(@Nonnull Direction whichFace,
                                            TextureAtlasSprite sprite,
                                            float minX,
                                            float minY,
                                            float minZ,
                                            float maxX,
                                            float maxY,
                                            float maxZ,
                                            int i,
                                            int j) {
        FROM.set(minX, minY, minZ);
        TO.set(maxX, maxY, maxZ);
        BlockFaceUV blockFaceUV = new BlockFaceUV(getUVs(whichFace, i, j), 0);
        BlockPartFace blockPartFace = new BlockPartFace(null, -1, "", blockFaceUV);
        return FACE_BAKERY.bakeQuad(FROM,
                                    TO,
                                    blockPartFace,
                                    sprite,
                                    whichFace,
                                    SimpleModelTransform.IDENTITY,
                                    null,
                                    true,
                                    TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static float[] getUVs(Direction side, int i, int j) {
        switch (side) {
            case UP: {
                return new float[]{2 * i, 2 * j, 2 * i + 2, 2 * j + 2};
            }
            case DOWN: {
                return new float[]{2 * i, 14 - 2 * j, 2 * i + 2, 16 - 2 * j};
            }
            case NORTH: {
                return new float[]{14 - 2 * i, 15, 16 - 2 * i, 16};
            }
            case SOUTH: {
                return new float[]{2 * i, 15, 2 * i + 2, 16};
            }
            case EAST: {
                return new float[]{14 - 2 * j, 15, 16 - 2 * j, 16};
            }
            case WEST: {
                return new float[]{2 * j, 15, 2 * j + 2, 16};
            }
        }
        throw new IllegalStateException("Unknown direction: " + side);
    }

    private List<BakedQuad> getBakedQuadsFromIModelData(@Nullable BlockState state,
                                                        @Nullable Direction side,
                                                        @Nonnull Random rand,
                                                        @Nonnull IModelData data) {
        if (!data.hasProperty(PARTS)) {
            Evolution.LOGGER.error("IModelData did not have expected property PARTS");
            return this.baseModel.getQuads(state, side, rand);
        }
        long parts = data.getData(PARTS);
        return new LinkedList<>(this.getQuadsFromParts(parts, side));
    }

    @Override
    @Nonnull
    public IModelData getModelData(@Nonnull IBlockDisplayReader world,
                                   @Nonnull BlockPos pos,
                                   @Nonnull BlockState state,
                                   @Nonnull IModelData tileData) {
        TileEntity tile = world.getBlockEntity(pos);
        ModelDataMap modelDataMap = getEmptyIModelData();
        if (tile instanceof TEKnapping) {
            modelDataMap.setData(PARTS, ((TEKnapping) tile).getParts());
        }
        else {
            modelDataMap.setData(PARTS, Patterns.MATRIX_TRUE);
        }
        return modelDataMap;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.baseModel.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random) {
        throw new AssertionError("IBakedModel::getQuads should never be called, only IForgeBakedModel::getQuads");
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return this.getBakedQuadsFromIModelData(state, side, rand, extraData);
    }

    private List<BakedQuad> getQuadsFromParts(long parts, Direction side) {
        AtlasTexture blockAtlas = ModelLoader.instance().getSpriteMap().getAtlas(AtlasTexture.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_KNAPPING[this.variant.getId()]);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
        if (side != null) {
            switch (side) {
                case UP:
                case DOWN: {
                    for (int j = 0; j < 8; j++) {
                        for (int i = 0; i < 8; i++) {
                            if ((parts >> (7 - j) * 8 + 7 - i & 1) != 0) {
                                builder.add(getQuadForPart(side, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                            }
                        }
                    }
                    break;
                }
                case NORTH: {
                    for (int i = 0; i < 8; i++) {
                        if ((parts >> 63 - i & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 2 * i, 0, 0, 2 * i + 2, 1, 2, i, 0));
                        }
                    }
                    break;
                }
                case SOUTH: {
                    for (int i = 0; i < 8; i++) {
                        if ((parts >> 7 - i & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 2 * i, 0, 14, 2 * i + 2, 1, 16, i, 7));
                        }
                    }
                    break;
                }
                case EAST: {
                    for (int j = 0; j < 8; j++) {
                        if ((parts >> (7 - j) * 8 & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 14, 0, 2 * j, 16, 1, 2 * j + 2, 7, j));
                        }
                    }
                    break;
                }
                case WEST: {
                    for (int j = 0; j < 8; j++) {
                        if ((parts >> (7 - j) * 8 + 7 & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 0, 0, 2 * j, 2, 1, 2 * j + 2, 0, j));
                        }
                    }
                    break;
                }
            }
        }
        else {
            for (int j = 0; j < 8; j++) {
                for (int i = 0; i < 8; i++) {
                    if ((parts >> (7 - j) * 8 + 7 - i & 1) != 0) {
                        if (j > 0 && (parts >> (8 - j) * 8 + 7 - i & 1) == 0) {
                            builder.add(getQuadForPart(Direction.NORTH, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        if (j < 7 && (parts >> (6 - j) * 8 + 7 - i & 1) == 0) {
                            builder.add(getQuadForPart(Direction.SOUTH, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        if (i > 0 && (parts >> (7 - j) * 8 + 8 - i & 1) == 0) {
                            builder.add(getQuadForPart(Direction.WEST, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        if (i < 7 && (parts >> (7 - j) * 8 + 6 - i & 1) == 0) {
                            builder.add(getQuadForPart(Direction.EAST, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return this.baseModel.usesBlockLight();
    }
}
