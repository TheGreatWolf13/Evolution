package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.client.models.data.IModelData;

import java.util.Random;

public interface PatchBlockRenderDispatcher {

    default boolean renderBatched(BlockState state,
                                  BlockPos pos,
                                  BlockAndTintGetter level,
                                  PoseStack matrices,
                                  VertexConsumer builder,
                                  boolean checkSides,
                                  Random random,
                                  IModelData modelData) {
        return false;
    }
}
