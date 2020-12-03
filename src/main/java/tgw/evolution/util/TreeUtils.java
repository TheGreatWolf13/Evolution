package tgw.evolution.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.IWorldGenerationReader;
import tgw.evolution.blocks.*;
import tgw.evolution.entities.misc.EntityFallingTimber;
import tgw.evolution.events.FallingEvents;
import tgw.evolution.init.EvolutionSounds;

import java.util.function.Consumer;

public final class TreeUtils {

    private TreeUtils() {
    }

    public static void iterateBlocks(int range, BlockPos center, Consumer<BlockPos.MutableBlockPos> action) {
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        int y = -range;
        while (y <= range) {
            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    targetPos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
                    action.accept(targetPos);
                }
            }
            ++y;
        }
    }

    public static void setDirtAt(IWorldGenerationReader reader, BlockPos pos) {
        if (reader instanceof IWorld) {
            IWorld world = (IWorld) reader;
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockGrass || state.getBlock() instanceof BlockDryGrass) {
                world.setBlockState(pos, ((IStoneVariant) state.getBlock()).getVariant().getDirt().getDefaultState(), 16);
            }
        }
    }

    /**
     * Spawns an EntityFallingTimber given the pre-calculated arguments.
     */
    private static void spawnFalling(World world,
                                     BlockPos pos,
                                     BlockPos base,
                                     BlockState state,
                                     BlockState newState,
                                     Direction fallingDirection,
                                     boolean isLog,
                                     int delay) {
        EntityFallingTimber entity = new EntityFallingTimber(world,
                                                             pos.getX() + 0.5,
                                                             pos.getY(),
                                                             pos.getZ() + 0.5,
                                                             state,
                                                             newState,
                                                             isLog,
                                                             (pos.getY() - base.getY()) + 0.25,
                                                             delay);
        if (FallingEvents.sound) {
            entity.playSound(EvolutionSounds.TREE_FALLING.get(), 0.25f, 1.0f);
            FallingEvents.sound = false;
        }
        entity.setMotion(entity.getMotion()
                               .add(0.25 * fallingDirection.getXOffset() * (pos.getY() - base.getY()),
                                    0,
                                    0.25 * fallingDirection.getZOffset() * (pos.getY() - base.getY())));
        world.addEntity(entity);
    }

    /**
     * Spawns an EntityFallingTimber based on the location of the original block and the chopping conditions.
     * Will assume the type of leaves.
     */
    public static void spawnFallingLeaves(World world,
                                          BlockPos.MutableBlockPos pos,
                                          BlockPos logPos,
                                          BlockPos base,
                                          BlockState state,
                                          Direction fellingDirection) {
        pos.move(Direction.DOWN);
        BlockState belowState = world.getBlockState(pos);
        boolean canFall = BlockUtils.isReplaceable(belowState) || logPos.equals(pos);
        pos.move(Direction.UP);
        if (!canFall) {
            return;
        }
        spawnFalling(world, pos, base, state, state, fellingDirection, false, 0);
    }

    /**
     * Spawns an EntityFallingTimber based on the location of the original block and the chopping conditions.
     * Will assume the type of log.
     */
    public static void spawnFallingLog(World world, BlockPos logPos, BlockPos base, Direction fallingDirection) {
        if (!(world.getBlockState(logPos).getBlock() instanceof BlockLog)) {
            return;
        }
        if (!world.getBlockState(logPos).get(BlockLog.TREE)) {
            return;
        }
        int delay = 0;
        if (world.getBlockState(logPos.offset(fallingDirection)).getBlock() instanceof BlockLog) {
            delay = 10;
        }
        BlockState state = world.getBlockState(logPos).with(BlockLog.TREE, false);
        BlockState newState = state;
        if (state.get(BlockXYZAxis.AXIS) == Direction.Axis.Y && fallingDirection.getAxis() == Direction.Axis.X) {
            newState = newState.with(BlockXYZAxis.AXIS, Direction.Axis.X);
        }
        else if (state.get(BlockXYZAxis.AXIS) == Direction.Axis.Y && fallingDirection.getAxis() == Direction.Axis.Z) {
            newState = newState.with(BlockXYZAxis.AXIS, Direction.Axis.Z);
        }
        else if (state.get(BlockXYZAxis.AXIS) == Direction.Axis.X && fallingDirection.getAxis() == Direction.Axis.X && logPos.getY() != base.getY()) {
            newState = newState.with(BlockXYZAxis.AXIS, Direction.Axis.Y);
        }
        else if (state.get(BlockXYZAxis.AXIS) == Direction.Axis.Z && fallingDirection.getAxis() == Direction.Axis.Z && logPos.getY() != base.getY()) {
            newState = newState.with(BlockXYZAxis.AXIS, Direction.Axis.Y);
        }
        spawnFalling(world, logPos, base, state, newState, fallingDirection, true, delay);
    }
}
