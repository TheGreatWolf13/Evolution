package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.LvlEvent;

public interface PatchLevelAccessor {

    default void blockUpdated_(int x, int y, int z, Block block) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("unused")
    default DifficultyInstance getCurrentDifficultyAt_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("unused")
    default void levelEvent_(@Nullable Player player, @LvlEvent int event, int x, int y, int z, int data) {
        throw new AbstractMethodError();
    }

    default void levelEvent_(@Nullable Player player, @LvlEvent int event, BlockPos pos, int data) {
        this.levelEvent_(player, event, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    default void levelEvent_(@LvlEvent int event, int x, int y, int z, int data) {
        this.levelEvent_(null, event, x, y, z, data);
    }

    default void levelEvent_(@LvlEvent int event, BlockPos pos, int data) {
        this.levelEvent_(null, event, pos.getX(), pos.getY(), pos.getZ(), data);
    }
}
