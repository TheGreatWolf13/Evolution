package tgw.evolution.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.IWorldGenerationReader;
import tgw.evolution.blocks.BlockDryGrass;
import tgw.evolution.blocks.BlockGrass;
import tgw.evolution.blocks.IRockVariant;

import java.util.function.Consumer;

public final class TreeUtils {

    private TreeUtils() {
    }

    public static void iterateBlocks(int range, BlockPos center, Consumer<BlockPos.Mutable> action) {
        BlockPos.Mutable targetPos = new BlockPos.Mutable();
        int y = -range;
        while (y <= range) {
            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    targetPos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    action.accept(targetPos);
                }
            }
            y++;
        }
    }

    public static void setDirtAt(IWorldGenerationReader reader, BlockPos pos) {
        if (reader instanceof IWorld) {
            IWorld world = (IWorld) reader;
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockGrass || state.getBlock() instanceof BlockDryGrass) {
                world.setBlock(pos, ((IRockVariant) state.getBlock()).getVariant().getDirt().defaultBlockState(), BlockFlags.UPDATE_NEIGHBORS);
            }
        }
    }
}
