package tgw.evolution.patches;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.client.models.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public interface PatchBakedModel {

    default IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state, IModelData modelData) {
        throw new AbstractMethodError();
    }

    default TextureAtlasSprite getParticleIcon(IModelData data) {
        throw new AbstractMethodError();

    }

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
        throw new AbstractMethodError();
    }
}
