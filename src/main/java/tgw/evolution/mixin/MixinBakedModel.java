package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.patches.PatchBakedModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mixin(BakedModel.class)
public interface MixinBakedModel extends PatchBakedModel {

    @Override
    default IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state, IModelData modelData) {
        return modelData;
    }

    @Override
    default TextureAtlasSprite getParticleIcon(IModelData data) {
        return this.getParticleIcon();
    }

    @Shadow
    TextureAtlasSprite getParticleIcon();

    @Shadow
    List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random);
}
