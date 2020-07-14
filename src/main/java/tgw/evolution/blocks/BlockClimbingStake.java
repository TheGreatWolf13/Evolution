package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemHammer;
import tgw.evolution.util.EvolutionStyles;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockClimbingStake extends Block implements IReplaceable, IRopeSupport {

    public static final BooleanProperty ROPE_NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty ROPE_SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty ROPE_EAST = BlockStateProperties.EAST;
    public static final BooleanProperty ROPE_WEST = BlockStateProperties.WEST;
    public static final BooleanProperty ROPE_DOWN = BlockStateProperties.DOWN;
    public static final Map<Direction, BooleanProperty> DIRECTION_TO_PROPERTY = SixWayBlock.FACING_TO_PROPERTY_MAP.entrySet()
                                                                                                                  .stream()
                                                                                                                  .filter(entry -> entry.getKey() != Direction.UP)
                                                                                                                  .collect(Util.toMapCollector());
    public static final BooleanProperty HIT = EvolutionBlockStateProperties.HIT;
    public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;
    public static final ITextComponent TEXT_HIT = new TranslationTextComponent("evolution.actionbar.hit_stake").setStyle(EvolutionStyles.WHITE);
    public static final ITextComponent TEXT_ROPE = new TranslationTextComponent("evolution.tooltip.rope").setStyle(EvolutionStyles.INFO);
    public BlockClimbingStake() {
        super(Properties.create(Material.IRON)
                        .sound(SoundType.METAL)
                        .hardnessAndResistance(0f)
                        .doesNotBlockMovement()
                        .harvestLevel(HarvestLevel.UNBREAKABLE));
        this.setDefaultState(this.getDefaultState()
                                 .with(ROPE_EAST, false)
                                 .with(ROPE_NORTH, false)
                                 .with(ROPE_SOUTH, false)
                                 .with(ROPE_WEST, false)
                                 .with(ROPE_DOWN, false)
                                 .with(HIT, false)
                                 .with(FACING, Direction.DOWN));
    }

    public static boolean canGoDown(World world, BlockPos pos) {
        BlockPos posDown = pos.down();
        return BlockUtils.isReplaceable(world.getBlockState(posDown));
    }

    public static void checkSides(BlockState state, World world, BlockPos pos) {
        for (Map.Entry<Direction, BooleanProperty> entry : DIRECTION_TO_PROPERTY.entrySet()) {
            Direction direction = entry.getKey();
            if (state.get(entry.getValue())) {
                BlockState stateTest = world.getBlockState(pos.offset(direction));
                if (direction == Direction.DOWN) {
                    if (stateTest.getBlock() != EvolutionBlocks.ROPE.get()) {
                        world.setBlockState(pos, state.with(ROPE_DOWN, false));
                    }
                    continue;
                }
                if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    if (stateTest.get(BlockRopeGround.ORIGIN).getOpposite() == direction) {
                        continue;
                    }
                }
                else if (BlockUtils.isReplaceable(stateTest)) {
                    stateTest = world.getBlockState(pos.offset(direction).down());
                    if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (stateTest.get(BlockRope.DIRECTION).getOpposite() == direction) {
                            continue;
                        }
                    }
                }
                world.setBlockState(pos, state.with(entry.getValue(), false));
            }
        }
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return false;
    }

    @Override
    public boolean canSupport(BlockState state, Direction direction) {
        if (direction == Direction.UP) {
            return false;
        }
        return state.get(DIRECTION_TO_PROPERTY.get(direction));
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote && !isMoving && state.getBlock() != newState.getBlock()) {
            if (state.get(FACING) == Direction.DOWN) {
                BlockPos downPos = pos.down();
                for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                    if (state.get(DIRECTION_TO_PROPERTY.get(direction))) {
                        BlockUtils.scheduleBlockTick(worldIn, downPos.offset(direction), 2);
                    }
                }
            }
        }
    }

    public int removeRope(BlockState state, World world, BlockPos pos) {
        List<BlockPos> toRemove = new ArrayList<>();
        int count = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        BlockState temp;
        if (state.get(FACING) != Direction.DOWN) {
            for (int removingRopes = 1; removingRopes <= this.getRopeLength(); removingRopes++) {
                mutablePos.move(Direction.DOWN);
                temp = world.getBlockState(mutablePos);
                if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (temp.get(BlockRope.DIRECTION) == state.get(FACING)) {
                        count++;
                        toRemove.add(mutablePos.toImmutable());
                        continue;
                    }
                }
                break;
            }
            for (BlockPos removing : toRemove) {
                world.removeBlock(removing, false);
            }
            return count;
        }
        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
            mutablePos.setPos(pos);
            if (state.get(DIRECTION_TO_PROPERTY.get(direction))) {
                Direction movement = direction;
                for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
                    mutablePos.move(movement);
                    temp = world.getBlockState(mutablePos);
                    if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                        if (temp.get(BlockRopeGround.ORIGIN) == movement.getOpposite()) {
                            count++;
                            toRemove.add(mutablePos.toImmutable());
                            continue;
                        }
                        break;
                    }
                    if (movement != Direction.DOWN && BlockUtils.isReplaceable(temp)) {
                        movement = Direction.DOWN;
                        mutablePos.move(Direction.DOWN);
                        temp = world.getBlockState(mutablePos);
                        if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                            if (temp.get(BlockRope.DIRECTION) == direction.getOpposite()) {
                                count++;
                                toRemove.add(mutablePos.toImmutable());
                                continue;
                            }
                        }
                        break;
                    }
                    if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (temp.get(BlockRope.DIRECTION) == direction.getOpposite()) {
                            count++;
                            toRemove.add(mutablePos.toImmutable());
                            continue;
                        }
                    }
                    break;
                }
            }
        }
        MathHelper.iterateReverse(toRemove, removing -> world.removeBlock(removing, false));
        return count;
    }

    @Override
    public int getRopeLength() {
        return 12;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (player.onGround && player.getHeldItemMainhand().getItem() instanceof ItemHammer) {
            if (!state.get(HIT)) {
                worldIn.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 1f, 1f);
                worldIn.setBlockState(pos, state.with(HIT, true));
                player.getHeldItemMainhand().damageItem(1, player, playerEntity -> playerEntity.sendBreakAnimation(Hand.MAIN_HAND));
            }
            return;
        }
        if (!worldIn.isRemote) {
            int rope = this.removeRope(state, worldIn, pos);
            while (rope > 64) {
                //noinspection ObjectAllocationInLoop
                BlockUtils.dropItemStack(worldIn, pos, new ItemStack(EvolutionItems.rope.get(), 64));
                rope -= 64;
            }
            BlockUtils.dropItemStack(worldIn, pos, new ItemStack(EvolutionItems.rope.get(), rope));
        }
        worldIn.removeBlock(pos, true);
        spawnDrops(state, worldIn, pos);
        worldIn.playSound(player, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 1f, 1f);
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            checkSides(state, worldIn, pos);
        }
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    public int tryPlaceRopes(World world, BlockPos pos, Direction movement, Direction support, int count) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        Direction currentMovement = movement;
        int ropeCount = 0;
        for (int distance = 1; distance <= this.getRopeLength(); distance++) {
            if (ropeCount == count) {
                return count;
            }
            mutablePos.move(currentMovement);
            BlockState stateTemp = world.getBlockState(mutablePos);
            if (!BlockUtils.isReplaceable(stateTemp)) {
                return ropeCount;
            }
            if (currentMovement == Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (stateTemp.get(BlockRope.DIRECTION) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                if (!((IReplaceable) stateTemp.getBlock()).canBeReplacedByRope(stateTemp)) {
                    return ropeCount;
                }
            }
            if (currentMovement != Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                if (stateTemp.get(BlockRopeGround.ORIGIN) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && canGoDown(world, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = world.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (stateTemp.get(BlockRope.DIRECTION) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    return ropeCount;
                }
                if (stateTemp.getBlock() instanceof IReplaceable) {
                    BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(stateTemp));
                }
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(BlockRope.DIRECTION, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(stateTemp));
            }
            if (currentMovement == Direction.DOWN) {
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(BlockRope.DIRECTION, support));
                ropeCount++;
                continue;
            }
            world.setBlockState(mutablePos, EvolutionBlocks.GROUND_ROPE.get().getDefaultState().with(BlockRopeGround.ORIGIN, support));
            ropeCount++;
        }
        return ropeCount;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (state.get(FACING) != Direction.DOWN) {
            return BlockWallTorch.SHAPES.get(state.get(FACING).getOpposite());
        }
        return EvolutionHitBoxes.TORCH;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HIT, ROPE_DOWN, ROPE_EAST, ROPE_NORTH, ROPE_SOUTH, ROPE_WEST);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack heldStack = player.getHeldItem(handIn);
        if (heldStack.getItem() == EvolutionItems.rope.get()) {
            if (!state.get(HIT)) {
                player.sendStatusMessage(TEXT_HIT, true);
                return false;
            }
            int count = heldStack.getCount();
            Direction movement = state.get(FACING) != Direction.DOWN ? Direction.DOWN : player.getHorizontalFacing();
            Direction support = state.get(FACING) == Direction.DOWN ? player.getHorizontalFacing().getOpposite() : state.get(FACING);
            Direction ropeDir = movement == Direction.DOWN ? Direction.DOWN : player.getHorizontalFacing();
            boolean before = state.get(DIRECTION_TO_PROPERTY.get(ropeDir));
            worldIn.setBlockState(pos, state.with(DIRECTION_TO_PROPERTY.get(ropeDir), true));
            int shrink = this.tryPlaceRopes(worldIn, pos, movement, support, count);
            heldStack.shrink(shrink);
            if (shrink > 0) {
                return true;
            }
            worldIn.setBlockState(pos, state.with(DIRECTION_TO_PROPERTY.get(ropeDir), before));
            return false;
        }
        return false;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos posOffset = pos.offset(facing);
        BlockState stateFace = worldIn.getBlockState(posOffset);
        return Block.hasSolidSide(stateFace, worldIn, posOffset, facing.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
                return;
            }
            checkSides(state, worldIn, pos);
        }
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction face = context.getFace().getOpposite();
        if (face == Direction.UP) {
            face = Direction.DOWN;
        }
        return this.getDefaultState().with(FACING, face);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TEXT_ROPE);
    }
}
