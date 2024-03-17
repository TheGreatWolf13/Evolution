package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionDamage;

/**
 * Represents a block that is, in some way or another, subject to the laws of Physics.
 */
public abstract class BlockPhysics extends BlockGeneric implements IPhysics {

    public BlockPhysics(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, this instanceof ICollisionBlock collisionBlock ? collisionBlock.getSlowdownTop(state) : 1.0f, EvolutionDamage.FALL);
    }

    @Override
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        if (!level.isClientSide && this instanceof IPoppable poppable) {
            poppable.popLogic(level, x, y, z);
        }
    }
}
