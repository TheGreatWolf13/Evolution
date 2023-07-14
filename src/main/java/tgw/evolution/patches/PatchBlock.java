package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.HarvestLevel;

public interface PatchBlock {

    default ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        throw new AbstractMethodError();
    }

    /**
     * Common Values: <br>
     * 0.55 = Sand; <br>
     * 0.57 = Gravel; <br>
     * 0.60 = Clay, Glass, Grass; <br>
     * 0.61 = Dry Grass; <br>
     * 0.63 = Dirt; <br>
     * 0.70 = Wood; <br>
     * 0.73 = MAXIMUM WALK SPEED; <br>
     * 0.80 = Stone; <br>
     * 0.95 = MAXIMUM SPRINT SPEED; <br>
     */
    default float getFrictionCoefficient(BlockState state) {
        throw new AbstractMethodError();
    }

    default @HarvestLevel int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        throw new AbstractMethodError();
    }

    default boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return false;
    }

    default boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.is(BlockTags.CLIMBABLE);
    }

    default boolean shouldCull(BlockGetter level, BlockState state, BlockPos pos, BlockState adjacentState, BlockPos adjacentPos, Direction face) {
        return state.skipRendering(adjacentState, face);
    }
}
