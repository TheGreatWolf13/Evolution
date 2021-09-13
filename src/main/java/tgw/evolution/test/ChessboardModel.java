package tgw.evolution.test;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
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
    public ItemOverrideList getOverrides() {
        return this.chessboardItemOverrideList;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseChessboardModel.getParticleIcon();
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
    public ItemCameraTransforms getTransforms() {
        return this.baseChessboardModel.getTransforms();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseChessboardModel.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseChessboardModel.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseChessboardModel.useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
