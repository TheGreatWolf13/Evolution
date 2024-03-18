package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.ATTACHED;
import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockClimbingHook extends BlockGeneric implements IReplaceable, IRopeSupport {

    public BlockClimbingHook() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.0f).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(ATTACHED, false));
    }

    public static void checkSides(BlockState state, Level level, int x, int y, int z) {
        //todo
        BlockPos pos = new BlockPos(x, y, z);
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        BlockState stateTest = level.getBlockState(pos.relative(direction));
        if (stateTest.getBlock() == EvolutionBlocks.ROPE_GROUND) {
            if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                return;
            }
        }
        else if (BlockUtils.isReplaceable(stateTest)) {
            stateTest = level.getBlockState(pos.relative(direction).below());
            if (stateTest.getBlock() == EvolutionBlocks.ROPE) {
                if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                    return;
                }
            }
        }
        level.setBlockAndUpdate(pos, state.setValue(ATTACHED, false));
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
//        int x = hitResult.posX();
//        int y = hitResult.posY();
//        int z = hitResult.posZ();
//        if (!level.isClientSide) {
//            int rope = this.removeRope(state, level, x, y, z);
//            BlockUtils.dropItemStack(level, x, y, z, new ItemStack(EvolutionItems.ROPE, rope));
//        }
//        level.removeBlock(x, y, z, true);
//        dropResources(state, level, x, y, z);
//        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSupport(BlockState state, Direction direction) {
        return state.getValue(DIRECTION_HORIZONTAL) == direction;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL, ATTACHED);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, int x, int y, int z, Player player) {
        return new ItemStack(EvolutionItems.CLIMBING_HOOK);
    }

    @Override
    public int getRopeLength() {
        return 8;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH -> EvolutionShapes.HOOK_NORTH;
            case EAST -> EvolutionShapes.HOOK_EAST;
            case SOUTH -> EvolutionShapes.HOOK_SOUTH;
            case WEST -> EvolutionShapes.HOOK_WEST;
            case UP, DOWN -> throw new IllegalStateException("Invalid horizontal direction " + state.getValue(DIRECTION_HORIZONTAL));
        };
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(DIRECTION_HORIZONTAL, mirror.mirror(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive_(level, x, y, z)) {
                BlockPos pos = new BlockPos(x, y, z);
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
            checkSides(state, level, x, y, z);
        }
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !isMoving && state.getBlock() != newState.getBlock()) {
//            BlockUtils.scheduleBlockTick(level, pos.below().relative(state.getValue(DIRECTION_HORIZONTAL)));
        }
    }

//    public int removeRope(BlockState state, Level level, int x, int y, int z) {
//        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
//        mutablePos.set(pos);
//        Direction movement = direction;
//        OList<BlockPos> toRemove = new OArrayList<>();
//        int count = 0;
//        for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
//            mutablePos.move(movement);
//            BlockState temp = level.getBlockState(mutablePos);
//            if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE_GROUND) {
//                if (temp.getValue(DIRECTION_HORIZONTAL) == movement.getOpposite()) {
//                    count++;
//                    toRemove.add(mutablePos.immutable());
//                    continue;
//                }
//                break;
//            }
//            if (movement != Direction.DOWN && BlockUtils.isReplaceable(temp)) {
//                movement = Direction.DOWN;
//                mutablePos.move(Direction.DOWN);
//                temp = level.getBlockState(mutablePos);
//                if (temp.getBlock() == EvolutionBlocks.ROPE) {
//                    if (temp.getValue(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
//                        count++;
//                        toRemove.add(mutablePos.immutable());
//                        continue;
//                    }
//                }
//                break;
//            }
//            if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE) {
//                if (temp.getValue(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
//                    count++;
//                    toRemove.add(mutablePos.immutable());
//                    continue;
//                }
//            }
//            break;
//        }
//        for (int i = toRemove.size() - 1; i >= 0; i--) {
//            level.removeBlock(toRemove.get(i), false);
//        }
//        return count;
//    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        checkSides(state, level, x, y, z);
    }
}
