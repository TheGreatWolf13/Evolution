package tgw.evolution.patches;

import net.minecraft.world.level.block.state.BlockState;
import org.intellij.lang.annotations.MagicConstant;
import tgw.evolution.util.constants.HarvestLevel;

public interface IBlockPatch {

    float getFrictionCoefficient(BlockState state);

    @MagicConstant(valuesFromClass = HarvestLevel.class)
    int getHarvestLevel(BlockState state);
}
