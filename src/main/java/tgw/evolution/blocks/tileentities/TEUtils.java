package tgw.evolution.blocks.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.util.BlockFlags;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TEUtils {

    private TEUtils() {
    }

    public static <T> void invokeIfInstance(TileEntity tile, Consumer<T> consumer) {
        invokeIfInstance(tile, consumer, false);
    }

    public static <T> void invokeIfInstance(TileEntity tile, Consumer<T> consumer, boolean showError) {
        try {
            consumer.accept((T) tile);
        }
        catch (Throwable ignored) {
            if (showError) {
                Evolution.LOGGER.warn("Error while invoking method on {} as it has failed the instance test", tile);
            }
        }
    }

    public static <T, R> R returnIfInstance(TileEntity tile, Function<T, R> function, @Nullable R orElse) {
        try {
            return function.apply((T) tile);
        }
        catch (Throwable ignored) {
            return orElse;
        }
    }

    public static void sendRenderUpdate(TileEntity tile) {
        tile.setChanged();
        World world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        BlockState state = world.getBlockState(pos);
        tile.getLevel().sendBlockUpdated(pos, state, state, BlockFlags.RERENDER);
    }
}
