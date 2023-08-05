package tgw.evolution.patches.obj;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IStateArgumentPredicate<A> {

    boolean test(BlockState state, BlockGetter level, int x, int y, int z, A arg);
}
