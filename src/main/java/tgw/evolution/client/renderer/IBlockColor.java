package tgw.evolution.client.renderer;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public interface IBlockColor extends BlockColor {

    @Override
    default int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        if (pos == null) {
            return this.getColor_(state, level, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, data);
        }
        return this.getColor_(state, level, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    int getColor_(BlockState state, @Nullable BlockAndTintGetter level, int x, int y, int z, int data);
}
