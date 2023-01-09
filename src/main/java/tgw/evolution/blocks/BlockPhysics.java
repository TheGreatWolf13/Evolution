package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionDamage;

import java.util.Random;

public abstract class BlockPhysics extends BlockGeneric implements IPhysics {

    public BlockPhysics(Properties properties) {
        super(properties);
    }

    public static void updateWeight(LevelAccessor level, BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        for (int y = pos.getY() - 1; y >= 0; y--) {
            BlockState down = BlockUtils.getBlockState(level, x, y, z);
            if (BlockUtils.isReplaceable(down)) {
                BlockUtils.scheduleBlockTick(level, x, y + 1, z);
                BlockUtils.scheduleBlockTick(level, x, y, z);
                return;
            }
            if (down.getBlock() instanceof BlockStone || down.getBlock() == Blocks.BEDROCK) {
                return;
            }
        }
    }

    public final void checkPhysics(Level level, BlockPos pos) {
        if (this.pops() && this.popLogic(level, pos)) {
            return;
        }
        if (this.fallable() && this.fallLogic(level, pos)) {
            return;
        }
//        if (this.shouldFall(level, pos)) {
//            if (this.hasBeams()) {
//                DirectionToIntMap beams = this.checkBeams(level, pos, false);
//                if (beams.isEmpty()) {
//                    if (this.specialCondition(level, pos)) {
//                        return;
//                    }
//                    this.fall(level, pos);
//                    return;
//                }
//                if (BlockUtils.hasMass(level.getBlockState(pos.above()))) {
//                    if (!this.canSustainWeight(level.getBlockState(pos))) {
//                        this.fall(level, pos);
//                        return;
//                    }
//                    Axis beamAxis = BlockUtils.getSmallestBeam(beams);
//                    if (beamAxis == null) {
//                        if (this.specialCondition(level, pos)) {
//                            return;
//                        }
//                        this.fall(level, pos);
//                        return;
//                    }
//                    this.checkWeight(level, pos, beams, beamAxis, false);
//                    return;
//                }
//                return;
//            }
//            this.fall(level, pos);
//            return;
//        }
        if (this.slopes()) {
            this.slopeLogic(level, pos);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance,
                               this instanceof ICollisionBlock collisionBlock ? collisionBlock.getSlowdownTop(state) : 1.0f,
                               EvolutionDamage.FALL);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.85F;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (this.pops()) {
                this.popLogic(level, pos);
            }
//            if (pos.getX() == fromPos.getX() && pos.getZ() == fromPos.getZ() && pos.getY() + 1 == fromPos.getY()) {
//                updateWeight(level, pos);
//            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockUtils.scheduleBlockTick(level, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockUtils.updateSlopingBlocks(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        this.checkPhysics(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState fromState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos fromPos) {
        BlockUtils.scheduleBlockTick(level, pos.getX(), pos.getY(), pos.getZ());
        return super.updateShape(state, direction, fromState, level, pos, fromPos);
    }
}