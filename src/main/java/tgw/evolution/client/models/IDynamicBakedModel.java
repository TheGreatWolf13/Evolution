package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.IRandom;

public interface IDynamicBakedModel extends BakedModel {

    @Override
    default IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state) {
        return IModelData.EMPTY;
    }

    @Override
    OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData);
}
