package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.models.data.IModelData;

import java.util.List;
import java.util.Random;

public interface IDynamicBakedModel extends BakedModel {

    @Override
    default IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state, IModelData modelData) {
        return modelData;
    }

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return this.getQuads(state, side, rand, IModelData.EMPTY);
    }

    @Override
    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData);
}
