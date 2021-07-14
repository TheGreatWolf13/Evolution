package tgw.evolution.test;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ChessboardModel implements IBakedModel {

    public static final ModelResourceLocation MODEL_RESOURCE_LOCATION = new ModelResourceLocation("evolution:mbe15_item_chessboard_registry_name",
                                                                                                  "inventory");
    private final IBakedModel baseChessboardModel;
    private final ChessboardItemOverrideList chessboardItemOverrideList;

    public ChessboardModel(IBakedModel baseChessboardModel) {
        this.baseChessboardModel = baseChessboardModel;
        this.chessboardItemOverrideList = new ChessboardItemOverrideList();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.baseChessboardModel.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IEnviromentBlockReader world,
                                   @Nonnull BlockPos pos,
                                   @Nonnull BlockState state,
                                   @Nonnull IModelData tileData) {
        throw new AssertionError("ChessboardModel::getModelData should never be called");
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.chessboardItemOverrideList;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseChessboardModel.getParticleTexture();
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        throw new AssertionError("ChessboardModel::getQuads(IModelData) should never be called");
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.baseChessboardModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.baseChessboardModel.isAmbientOcclusion();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.baseChessboardModel.isBuiltInRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseChessboardModel.isGui3d();
    }
}
