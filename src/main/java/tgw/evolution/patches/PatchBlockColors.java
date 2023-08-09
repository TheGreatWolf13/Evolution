package tgw.evolution.patches;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface PatchBlockColors {

    default int getColor_(BlockState state, @Nullable BlockAndTintGetter level, int x, int y, int z, int data) {
        throw new AbstractMethodError();
    }

    default int getColor_(BlockState state, @Nullable BlockAndTintGetter level, int data) {
        return this.getColor_(state, level, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, data);
    }

    default void register(BlockColor blockColor, Block block) {
        throw new AbstractMethodError();
    }
}
