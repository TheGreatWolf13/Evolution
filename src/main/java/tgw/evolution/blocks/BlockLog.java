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
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.*;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.AXIS;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLog extends BlockXYZAxis {

    private final WoodVariant variant;

    public BlockLog(WoodVariant variant) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(8.0F, 2.0F).sound(SoundType.WOOD).harvestLevel(HarvestLevel.STONE),
              variant.getMass());
        this.variant = variant;
        this.setDefaultState(this.getDefaultState().with(TREE, false));
    }

    @Override
    public boolean beamCondition(BlockState checking, BlockState state) {
        return state.get(AXIS) == checking.get(AXIS);
    }

    @Override
    public Direction[] beamDirections(BlockState state) {
        return new Direction[]{MathHelper.getNegativeAxis(state.get(AXIS)), MathHelper.getPositiveAxis(state.get(AXIS))};
    }

    @Override
    public int beamSize() {
        return 8;
    }

    @Override
    public SoundEvent breakSound() {
        return SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
    }

    @Override
    protected boolean canSustainWeight(BlockState state) {
        return state.get(AXIS) != Axis.Y && super.canSustainWeight(state);
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.WOOD_COLLAPSE.get();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(TREE);
        super.fillStateContainer(builder);
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
        return super.getStateForPlacement(context).with(TREE, false);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.get(TREE)) {
            BlockPos up = pos.up();
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                state.updateNeighbors(world, up.offset(dir), BlockFlags.NOTIFY_AND_UPDATE);
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        //        if (!world.isRemote) {
        //            if (world.getBlockState(pos.up()).getBlock() instanceof BlockLog && world.getBlockState(pos.up()).get(TREE)) {
        //                PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 25, false);
        //                Direction direction = Direction.byHorizontalIndex(world.getRandom().nextInt(4));
        //                if (player != null) {
        //                    direction = player.getHorizontalFacing();
        //                }
        //                FallingEvents.chopEvent(world, world.getBlockState(pos.up()), pos.up(), direction);
        //            }
        //        }
    }

    @Override
    public void tick(BlockState state, World world, BlockPos blockPos, Random random) {
        OriginMutableBlockPos pos = new OriginMutableBlockPos(blockPos);
        if (!world.isRemote) {
            if (!state.get(TREE)) {
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
                if (world.getBlockState(pos.down().getPos()).getBlock() instanceof BlockLog && !world.getBlockState(pos.getPos()).get(TREE)) {
                    world.setBlockState(pos.reset().getPos(), state.with(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
                }
                else if (world.getBlockState(pos.up().getPos()).getBlock() instanceof BlockLog && !world.getBlockState(pos.getPos()).get(TREE)) {
                    world.setBlockState(pos.reset().getPos(), state.with(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
                }
                else if (!BlockUtils.isTrunkSustained(world, pos) && BlockUtils.isReplaceable(world.getBlockState(pos.up().getPos()))) {
                    world.setBlockState(pos.reset().getPos(), state.with(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
                }
            }
        }
    }
}
