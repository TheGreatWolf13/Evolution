package tgw.evolution.patches;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.IRandom;

import javax.annotation.Nullable;

public interface PatchBakedModel {

    default IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default TextureAtlasSprite getParticleIcon(IModelData data) {
        throw new AbstractMethodError();

    }

    default OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        throw new AbstractMethodError();
    }
}
