package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.IRandom;

import java.util.List;
import java.util.Random;

@Mixin(BuiltInModel.class)
public abstract class MixinBuiltInModel implements BakedModel {
    
    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        Evolution.deprecatedMethod();
        return List.of();
    }

    @Override
    public OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        return OList.emptyList();
    }
}
