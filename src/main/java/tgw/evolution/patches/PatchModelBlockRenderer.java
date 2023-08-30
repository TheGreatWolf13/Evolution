package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.util.math.IRandom;

public interface PatchModelBlockRenderer {

    default boolean tesselateBlock(BlockAndTintGetter level, BakedModel model, BlockState state, int x, int y, int z, PoseStack matrices, VertexConsumer consumer, boolean checkSides, IRandom random, long seed, int packedOverlay) {
        throw new AbstractMethodError();
    }
}
