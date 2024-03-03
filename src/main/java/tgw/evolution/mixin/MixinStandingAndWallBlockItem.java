package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StandingAndWallBlockItem.class)
public abstract class MixinStandingAndWallBlockItem extends BlockItem {

    @Shadow @Final protected Block wallBlock;

    public MixinStandingAndWallBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState state = null;
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState stateForDir = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (stateForDir != null && stateForDir.canSurvive_(level, x, y, z)) {
                    state = stateForDir;
                    break;
                }
            }
        }
        return state != null && level.isUnobstructed_(state, x, y, z, null) ? state : null;
    }
}
