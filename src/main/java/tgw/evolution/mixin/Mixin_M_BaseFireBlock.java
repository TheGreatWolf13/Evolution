package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public abstract class Mixin_M_BaseFireBlock extends Block {

    @Shadow @Final protected static VoxelShape DOWN_AABB;

    public Mixin_M_BaseFireBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean inPortalDimension(Level level) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return DOWN_AABB;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            if (inPortalDimension(level)) {
                Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, new BlockPos(x, y, z), Direction.Axis.X);
                if (optional.isPresent()) {
                    optional.get().createPortalBlocks();
                    return;
                }
            }
            if (!state.canSurvive_(level, x, y, z)) {
                level.removeBlock(new BlockPos(x, y, z), false);
            }
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        if (!level.isClientSide()) {
            level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, new BlockPos(x, y, z), 0);
        }
        super.playerWillDestroy_(level, x, y, z, state, player);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void spawnDestroyParticles_(Level level, Player player, int x, int y, int z, BlockState state) {
    }
}
