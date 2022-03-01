package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.Evolution;
import tgw.evolution.util.constants.BlockFlags;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TEUtils {

    private TEUtils() {
    }

    public static <T> void invokeIfInstance(BlockEntity tile, Consumer<T> consumer) {
        invokeIfInstance(tile, consumer, false);
    }

    public static <T> void invokeIfInstance(BlockEntity tile, Consumer<T> consumer, boolean showError) {
        try {
            consumer.accept((T) tile);
        }
        catch (Throwable ignored) {
            if (showError) {
                Evolution.warn("Error while invoking method on {} as it has failed the instance test", tile);
            }
        }
    }

    public static <T, R> R returnIfInstance(BlockEntity tile, Function<T, R> function, @Nullable R orElse) {
        try {
            return function.apply((T) tile);
        }
        catch (Throwable ignored) {
            return orElse;
        }
    }

    public static void sendRenderUpdate(BlockEntity tile) {
        tile.setChanged();
        Level world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        BlockState state = world.getBlockState(pos);
        tile.getLevel().sendBlockUpdated(pos, state, state, BlockFlags.RERENDER);
    }
}
