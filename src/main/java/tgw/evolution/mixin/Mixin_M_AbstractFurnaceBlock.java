package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(AbstractFurnaceBlock.class)
public abstract class Mixin_M_AbstractFurnaceBlock extends BaseEntityBlock {

    public Mixin_M_AbstractFurnaceBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity_(x, y, z) instanceof AbstractFurnaceBlockEntity tile) {
                if (level instanceof ServerLevel l) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Containers.dropContents(level, pos, tile);
                    tile.getRecipesToAwardAndPopExperience(l, Vec3.atCenterOf(pos));
                }
                level.updateNeighbourForOutputSignal_(x, y, z, this);
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
    }

    @Shadow
    protected abstract void openContainer(Level level, BlockPos blockPos, Player player);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        this.openContainer(level, new BlockPos(x, y, z), player);
        return InteractionResult.CONSUME;
    }
}
