package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface PatchLiquidBlockRenderer {

    default boolean tesselate(BlockAndTintGetter level, int x, int y, int z, VertexConsumer builder, BlockState state, FluidState fluid) {
        throw new AbstractMethodError();
    }
}
