package tgw.evolution.client.models.tile;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.Patterns;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.constants.RockVariant;

import java.util.List;
import java.util.Random;

public class BakedModelKnapping implements BakedModel {

    public static final ModelProperty<Long> PARTS = new ModelProperty<>();
    private static final ThreadLocal<ModelDataMap> MODEL_DATA = ThreadLocal.withInitial(() -> {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(PARTS, -1L);
        return builder.build();
    });
    private final BakedModel baseModel;
    private final RockVariant variant;

    public BakedModelKnapping(BakedModel baseModel, RockVariant variant) {
        this.baseModel = baseModel;
        this.variant = variant;
    }

    private static BakedQuad getQuadForPart(Direction whichFace,
                                            TextureAtlasSprite sprite,
                                            float minX,
                                            float minY,
                                            float minZ,
                                            float maxX,
                                            float maxY,
                                            float maxZ,
                                            int i,
                                            int j) {
        Vector3f from = RenderHelper.MODEL_FROM.get();
        from.set(minX, minY, minZ);
        Vector3f to = RenderHelper.MODEL_TO.get();
        to.set(maxX, maxY, maxZ);
        BlockFaceUV blockFaceUV = RenderHelper.MODEL_FACE_UV.get();
        blockFaceUV.uvs = getUVs(whichFace, i, j);
        blockFaceUV.rotation = 0;
        return RenderHelper.MODEL_FACE_BAKERY.bakeQuad(from, to, RenderHelper.MODEL_FACE.get(), sprite, whichFace, SimpleModelState.IDENTITY, null,
                                                       true, TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static float[] getUVs(Direction side, int i, int j) {
        float[] uv = RenderHelper.MODEL_UV.get();
        return switch (side) {
            case UP -> {
                uv[0] = 2 * i;
                uv[1] = 2 * j;
                uv[2] = 2 * i + 2;
                uv[3] = 2 * j + 2;
                yield uv;
            }
            case DOWN -> {
                uv[0] = 2 * i;
                uv[1] = 14 - 2 * j;
                uv[2] = 2 * i + 2;
                uv[3] = 16 - 2 * j;
                yield uv;
            }
            case NORTH -> {
                uv[0] = 14 - 2 * i;
                uv[1] = 15;
                uv[2] = 16 - 2 * i;
                uv[3] = 16;
                yield uv;
            }
            case SOUTH -> {
                uv[0] = 2 * i;
                uv[1] = 15;
                uv[2] = 2 * i + 2;
                uv[3] = 16;
                yield uv;
            }
            case EAST -> {
                uv[0] = 14 - 2 * j;
                uv[1] = 15;
                uv[2] = 16 - 2 * j;
                uv[3] = 16;
                yield uv;
            }
            case WEST -> {
                uv[0] = 2 * j;
                uv[1] = 15;
                uv[2] = 2 * j + 2;
                uv[3] = 16;
                yield uv;
            }
        };
    }

    private List<BakedQuad> getBakedQuadsFromIModelData(@Nullable BlockState state,
                                                        @Nullable Direction side,
                                                        Random rand,
                                                        IModelData data) {
        if (!data.hasProperty(PARTS)) {
            Evolution.error("IModelData did not have expected property PARTS");
            return this.baseModel.getQuads(state, side, rand);
        }
        //noinspection ConstantConditions
        long parts = data.getData(PARTS);
        return new RArrayList<>(this.getQuadsFromParts(parts, side));
    }

    @Override
    public @NotNull IModelData getModelData(BlockAndTintGetter level,
                                            BlockPos pos,
                                            BlockState state,
                                            IModelData tileData) {
        BlockEntity tile = level.getBlockEntity(pos);
        ModelDataMap modelDataMap = MODEL_DATA.get();
        if (tile instanceof TEKnapping teKnapping) {
            modelDataMap.setData(PARTS, teKnapping.getParts());
        }
        else {
            modelDataMap.setData(PARTS, Patterns.MATRIX_TRUE);
        }
        return modelDataMap;
    }

    @Override
    public ItemOverrides getOverrides() {
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
        return this.getBakedQuadsFromIModelData(state, side, rand, extraData);
    }

    private List<BakedQuad> getQuadsFromParts(long parts, @Nullable Direction side) {
        assert ForgeModelBakery.instance() != null;
        TextureAtlas blockAtlas = ForgeModelBakery.instance().getSpriteMap().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_KNAPPING[this.variant.getId()]);
        List<BakedQuad> quads = RenderHelper.MODEL_QUAD_HOLDER.get();
        quads.clear();
        if (side != null) {
            switch (side) {
                case UP, DOWN -> {
                    for (int j = 0; j < 8; j++) {
                        for (int i = 0; i < 8; i++) {
                            if ((parts & 1L << 8 * j + i) != 0) {
                                quads.add(getQuadForPart(side, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                            }
                        }
                    }
                }
                case NORTH -> {
                    for (int i = 0; i < 8; i++) {
                        //j = 0
                        if ((parts & 1L << i) != 0) {
                            quads.add(getQuadForPart(side, sprite, 2 * i, 0, 0, 2 * i + 2, 1, 2, i, 0));
                        }
                    }
                }
                case SOUTH -> {
                    for (int i = 0; i < 8; i++) {
                        //j = 7
                        if ((parts & 1L << 56 + i) != 0) {
                            quads.add(getQuadForPart(side, sprite, 2 * i, 0, 14, 2 * i + 2, 1, 16, i, 7));
                        }
                    }
                }
                case EAST -> {
                    for (int j = 0; j < 8; j++) {
                        //i = 7
                        if ((parts & 1L << 8 * j + 7) != 0) {
                            quads.add(getQuadForPart(side, sprite, 14, 0, 2 * j, 16, 1, 2 * j + 2, 7, j));
                        }
                    }
                }
                case WEST -> {
                    for (int j = 0; j < 8; j++) {
                        //i = 0
                        if ((parts & 1L << 8 * j) != 0) {
                            quads.add(getQuadForPart(side, sprite, 0, 0, 2 * j, 2, 1, 2 * j + 2, 0, j));
                        }
                    }
                }
            }
        }
        else {
            for (int j = 0; j < 8; j++) {
                for (int i = 0; i < 8; i++) {
                    int index = 8 * j + i;
                    if ((parts & 1L << index) != 0) {
                        //j - 1
                        if (j > 0 && (parts & 1L << 8 * (j - 1) + i) == 0) {
                            quads.add(getQuadForPart(Direction.NORTH, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        //j + 1
                        if (j < 7 && (parts & 1L << 8 * (j + 1) + i) == 0) {
                            quads.add(getQuadForPart(Direction.SOUTH, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        //i - 1
                        if (i > 0 && (parts & 1L << index - 1) == 0) {
                            quads.add(getQuadForPart(Direction.WEST, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                        //i + 1
                        if (i < 7 && (parts & 1L << 8 * j + i + 1) == 0) {
                            quads.add(getQuadForPart(Direction.EAST, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                        }
                    }
                }
            }
        }
        return quads;
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
