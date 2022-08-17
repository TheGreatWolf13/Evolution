package tgw.evolution.blocks;

import net.minecraft.world.level.block.state.BlockState;

import static tgw.evolution.init.EvolutionBStates.ATM;

public interface IAir {

    default int getAtmProperty(BlockState state) {
        return state.getValue(ATM);
    }
}
