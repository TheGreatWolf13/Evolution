package tgw.evolution.client.models.tile;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.constants.RockVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BakedModelKnapping implements BakedModel {

    public static final ModelProperty<Long> PARTS = new ModelProperty<>();
    private static final Vector3f FROM = new Vector3f();
    private static final Vector3f TO = new Vector3f();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private final BakedModel baseModel;
    private final RockVariant variant;

    public BakedModelKnapping(BakedModel baseModel, RockVariant variant) {
        this.baseModel = baseModel;
        this.variant = variant;
    }

    public static ModelDataMap getEmptyIModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(PARTS, -1L);
        return builder.build();
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
        FROM.set(minX, minY, minZ);
        TO.set(maxX, maxY, maxZ);
        BlockFaceUV blockFaceUV = new BlockFaceUV(getUVs(whichFace, i, j), 0);
        BlockElementFace blockPartFace = new BlockElementFace(null, -1, "", blockFaceUV);
        return FACE_BAKERY.bakeQuad(FROM,
                                    TO,
                                    blockPartFace,
                                    sprite,
                                    whichFace,
                                    SimpleModelState.IDENTITY,
                                    null,
                                    true,
                                    TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static float[] getUVs(Direction side, int i, int j) {
        return switch (side) {
            case UP -> new float[]{2 * i, 2 * j, 2 * i + 2, 2 * j + 2};
            case DOWN -> new float[]{2 * i, 14 - 2 * j, 2 * i + 2, 16 - 2 * j};
            case NORTH -> new float[]{14 - 2 * i, 15, 16 - 2 * i, 16};
            case SOUTH -> new float[]{2 * i, 15, 2 * i + 2, 16};
            case EAST -> new float[]{14 - 2 * j, 15, 16 - 2 * j, 16};
            case WEST -> new float[]{2 * j, 15, 2 * j + 2, 16};
        };
    }

    private List<BakedQuad> getBakedQuadsFromIModelData(BlockState state,
                                                        Direction side,
                                                        Random rand,
                                                        IModelData data) {
        if (!data.hasProperty(PARTS)) {
            Evolution.error("IModelData did not have expected property PARTS");
            return this.baseModel.getQuads(state, side, rand);
        }
        long parts = data.getData(PARTS);
        return new ArrayList<>(this.getQuadsFromParts(parts, side));
    }

    @Override
    public @NotNull IModelData getModelData(BlockAndTintGetter level,
                                            BlockPos pos,
                                            BlockState state,
                                            IModelData tileData) {
        BlockEntity tile = level.getBlockEntity(pos);
        ModelDataMap modelDataMap = getEmptyIModelData();
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

    private List<BakedQuad> getQuadsFromParts(long parts, Direction side) {
        TextureAtlas blockAtlas = ForgeModelBakery.instance().getSpriteMap().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_KNAPPING[this.variant.getId()]);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
        if (side != null) {
            switch (side) {
                case UP, DOWN -> {
                    for (int j = 0; j < 8; j++) {
                        for (int i = 0; i < 8; i++) {
                            if ((parts >> (7 - j) * 8 + 7 - i & 1) != 0) {
                                builder.add(getQuadForPart(side, sprite, 2 * i, 0, 2 * j, 2 * i + 2, 1, 2 * j + 2, i, j));
                            }
                        }
                    }
                }
                case NORTH -> {
                    for (int i = 0; i < 8; i++) {
                        if ((parts >> 63 - i & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 2 * i, 0, 0, 2 * i + 2, 1, 2, i, 0));
                        }
                    }
                }
                case SOUTH -> {
                    for (int i = 0; i < 8; i++) {
                        if ((parts >> 7 - i & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 2 * i, 0, 14, 2 * i + 2, 1, 16, i, 7));
                        }
                    }
                }
                case EAST -> {
                    for (int j = 0; j < 8; j++) {
                        if ((parts >> (7 - j) * 8 & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 14, 0, 2 * j, 16, 1, 2 * j + 2, 7, j));
                        }
                    }
                }
                case WEST -> {
                    for (int j = 0; j < 8; j++) {
                        if ((parts >> (7 - j) * 8 + 7 & 1) != 0) {
                            builder.add(getQuadForPart(side, sprite, 0, 0, 2 * j, 2, 1, 2 * j + 2, 0, j));
                        }
                    }
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
