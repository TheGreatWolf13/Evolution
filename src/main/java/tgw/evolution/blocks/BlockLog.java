package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.AXIS;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLog extends BlockXYZAxis {

    private final WoodVariant variant;

    public BlockLog(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(8.0F, 2.0F).sound(SoundType.WOOD), variant.getMass());
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
        return state.getValue(AXIS) != Direction.Axis.Y && super.canSustainWeight(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TREE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.WOOD_COLLAPSE.get();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.62f;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevel.STONE;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return state.setValue(TREE, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(TREE)) {
            BlockPos up = pos.above();
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                state.updateNeighbourShapes(level, up.relative(dir), BlockFlags.NOTIFY_AND_UPDATE);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos blockPos, Random random) {
//        OriginMutableBlockPos pos = new OriginMutableBlockPos(blockPos);
        if (!state.getValue(TREE)) {
            super.tick(state, level, blockPos, random);
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
//            pos.reset();
//            if (level.getBlockState(pos.down().getPos()).getBlock() instanceof BlockLog && !level.getBlockState(pos.getPos()).getValue(TREE)) {
//                level.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
//            }
//            else if (level.getBlockState(pos.up().getPos()).getBlock() instanceof BlockLog && !level.getBlockState(pos.getPos()).getValue(TREE)) {
//                level.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
//            }
//            else if (!BlockUtils.isTrunkSustained(level, pos) && BlockUtils.isReplaceable(level.getBlockState(pos.up().getPos()))) {
//                level.setBlock(pos.reset().getPos(), state.setValue(TREE, false), BlockFlags.NOTIFY_AND_UPDATE);
//            }
        }
    }
}
