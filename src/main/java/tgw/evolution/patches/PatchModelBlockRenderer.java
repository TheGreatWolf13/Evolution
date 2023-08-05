package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.client.models.data.IModelData;

import java.util.Random;

public interface PatchModelBlockRenderer {

    default boolean tesselateBlock(BlockAndTintGetter level,
                                   BakedModel model,
                                   BlockState state,
                                   BlockPos pos,
                                   PoseStack matrices,
                                   VertexConsumer consumer,
                                   boolean checkSides,
                                   Random random,
                                   long seed,
                                   int packedOverlay,
                                   IModelData modelData) {
        throw new AbstractMethodError();
    }
}
