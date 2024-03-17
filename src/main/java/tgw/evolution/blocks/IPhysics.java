package tgw.evolution.blocks;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents any block that has physics, from reacting to gravity, to popping, to structural integrity
 */
public interface IPhysics {

    default BlockState getStateForPhysicsChange(BlockState state) {
        return state;
    }
}
