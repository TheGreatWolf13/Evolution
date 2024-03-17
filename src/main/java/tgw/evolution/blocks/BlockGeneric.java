package tgw.evolution.blocks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.util.constants.HarvestLevel;

/**
 * Represents a generic block with no special behaviour.
 */
public abstract class BlockGeneric extends Block {

    public BlockGeneric(Properties properties) {
        super(properties);
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.HAND;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, int x, int y, int z, LivingEntity entity) {
        return this instanceof IClimbable climbable && climbable.isClimbable(state, level, x, y, z, entity);
    }

    public boolean preventsShortAttacking(Level level, int x, int y, int z, BlockState state, Player player) {
        return false;
    }
}
