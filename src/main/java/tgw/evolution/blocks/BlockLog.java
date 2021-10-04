package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.*;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.AXIS;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLog extends BlockXYZAxis {

    private final WoodVariant variant;

    public BlockLog(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(8.0F, 2.0F).sound(SoundType.WOOD).harvestLevel(HarvestLevel.STONE), variant.getMass());
        this.variant = variant;
        this.registerDefaultState(this.defaultBlockState().setValue(TREE, false));
    }

    @Override
    public boolean beamCondition(BlockState checking, BlockState state) {
        return state.getValue(AXIS) == checking.getValue(AXIS);
    }

    @Override
    public Direction[] beamDirections(BlockState state) {
        return new Direction[]{MathHelper.getNegativeAxis(state.getValue(AXIS)), MathHelper.getPositiveAxis(state.getValue(AXIS))};
    }

    @Override
    public int beamSize() {
        return 8;
    }

    @Override
    public SoundEvent breakSound() {
        return EvolutionSounds.WOOD_BREAK.get();
    }

    @Override
    protected boolean canSustainWeight(BlockState state) {
        return state.getValue(AXIS) != Axis.Y && super.canSustainWeight(state);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(TREE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.WOOD_COLLAPSE.get();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.62f;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(TREE, false);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(TREE)) {
            BlockPos up = pos.above();
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                state.updateNeighbourShapes(world, up.relative(dir), BlockFlags.NOTIFY_AND_UPDATE);
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos blockPos, Random random) {
        OriginMutableBlockPos pos = new OriginMutableBlockPos(blockPos);
        if (!state.getValue(TREE)) {
            super.tick(state, world, blockPos, random);
        }
        else {
            //                if (!BlockUtils.isTrunkSustained(worldIn, pos)) {
            //                    pos.reset();
            //                    PlayerEntity player = worldIn.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 25, false);
            //                    Direction direction = Direction.byHorizontalIndex(random.nextInt(4));
            //                    if (player != null) {
            //                        direction = player.getHorizontalFacing();
            //                    }
            //                    FallingEvents.chopEvent(worldIn, worldIn.getBlockState(pos.up().getPos()), pos.getPos(), direction);
            //                }
            pos.reset();
            if (world.getBlockState(pos.down().getPos()).getBlock() instanceof BlockLog && !world.getBlockState(pos.getPos()).getValue(TREE)) {
                world.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
            }
            else if (world.getBlockState(pos.up().getPos()).getBlock() instanceof BlockLog && !world.getBlockState(pos.getPos()).getValue(TREE)) {
                world.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
            }
            else if (!BlockUtils.isTrunkSustained(world, pos) && BlockUtils.isReplaceable(world.getBlockState(pos.up().getPos()))) {
                world.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
            }
        }
    }
}
