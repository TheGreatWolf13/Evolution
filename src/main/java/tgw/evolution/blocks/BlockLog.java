package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
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
import tgw.evolution.util.EnumWoodNames;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.OriginMutableBlockPos;

import java.util.Random;

public class BlockLog extends BlockXYZAxis {

    public static final BooleanProperty TREE = EvolutionBlockStateProperties.TREE;
    private final EnumWoodNames variant;

    public BlockLog(EnumWoodNames variant) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(8F, 2F).sound(SoundType.WOOD).harvestLevel(HarvestLevel.STONE), variant.getMass());
        this.variant = variant;
        this.setDefaultState(this.getDefaultState().with(TREE, false));
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public SoundEvent breakSound() {
        return SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
    }

    @Override
    public int beamSize() {
        return 8;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    protected boolean canSustainWeight(BlockState state) {
        return state.get(AXIS) != Axis.Y && super.canSustainWeight(state);
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
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.get(TREE)) {
            BlockPos up = pos.up();
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                state.updateNeighbors(worldIn, up.offset(dir), 3);
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos blockPos, Random random) {
        OriginMutableBlockPos pos = new OriginMutableBlockPos(blockPos);
        if (!worldIn.isRemote) {
            if (!state.get(TREE)) {
                super.tick(state, worldIn, blockPos, random);
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
                if (worldIn.getBlockState(pos.down().getPos()).getBlock() instanceof BlockLog && !worldIn.getBlockState(pos.getPos()).get(TREE)) {
                    worldIn.setBlockState(pos.reset().getPos(), state.with(TREE, false), 3);
                }
                else if (worldIn.getBlockState(pos.up().getPos()).getBlock() instanceof BlockLog && !worldIn.getBlockState(pos.getPos()).get(TREE)) {
                    worldIn.setBlockState(pos.reset().getPos(), state.with(TREE, false), 3);
                }
                else if (!BlockUtils.isTrunkSustained(worldIn, pos) && BlockUtils.isReplaceable(worldIn.getBlockState(pos.up().getPos()))) {
                    worldIn.setBlockState(pos.reset().getPos(), state.with(TREE, false), 3);
                }
            }
        }
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.WOOD_COLLAPSE.get();
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        //        if (!worldIn.isRemote) {
        //            if (worldIn.getBlockState(pos.up()).getBlock() instanceof BlockLog && worldIn.getBlockState(pos.up()).get(TREE)) {
        //                PlayerEntity player = worldIn.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 25, false);
        //                Direction direction = Direction.byHorizontalIndex(worldIn.getRandom().nextInt(4));
        //                if (player != null) {
        //                    direction = player.getHorizontalFacing();
        //                }
        //                FallingEvents.chopEvent(worldIn, worldIn.getBlockState(pos.up()), pos.up(), direction);
        //            }
        //        }
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(TREE);
        super.fillStateContainer(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).with(TREE, false);
    }
}
