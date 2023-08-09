package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.math.IRandom;

public interface PatchBlockRenderDispatcher {

    default boolean renderBatched(BlockState state, int x, int y, int z, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder, boolean checkSides, IRandom random, IModelData modelData) {
        throw new AbstractMethodError();
    }

    default void renderBreakingTexture(BlockState state, int x, int y, int z, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder) {
        throw new AbstractMethodError();
    }

    default boolean renderLiquid(int x, int y, int z, BlockAndTintGetter level, VertexConsumer builder, BlockState state, FluidState fluidState) {
        throw new AbstractMethodError();
    }
}
