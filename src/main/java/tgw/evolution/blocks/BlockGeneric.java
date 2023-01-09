package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.util.constants.HarvestLevel;

public abstract class BlockGeneric extends Block implements IBlockPatch {

    public BlockGeneric(Properties properties) {
        super(properties);
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.HAND;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return this instanceof IClimbable climbable && climbable.isClimbable(state, level, pos, entity);
    }

    public boolean preventsShortAttacking(Level level, BlockPos pos, BlockState state, Player player) {
        return false;
    }
}
