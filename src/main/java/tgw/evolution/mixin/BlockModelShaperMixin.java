package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvModelDataManager;

@Mixin(BlockModelShaper.class)
public abstract class BlockModelShaperMixin {

    @Shadow public abstract BakedModel getBlockModel(BlockState pState);

    /**
     * @author TheGreatWolf
     * @reason Use evolution ModelManager.
     */
    @Overwrite
    public TextureAtlasSprite getTexture(BlockState state, Level level, BlockPos pos) {
        IModelData data = EvModelDataManager.getModelData((ClientLevel) level, pos.getX(), pos.getY(), pos.getZ());
        BakedModel model = this.getBlockModel(state);
        return model.getParticleIcon(model.getModelData(level, pos, state, data == null ? EmptyModelData.INSTANCE : data));
    }
}
