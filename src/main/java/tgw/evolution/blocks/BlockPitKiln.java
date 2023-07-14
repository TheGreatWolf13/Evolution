package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemClayMolded;
import tgw.evolution.items.ItemLog;
import tgw.evolution.util.math.DirectionDiagonal;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class BlockPitKiln extends BlockGeneric implements IReplaceable, EntityBlock {

    public BlockPitKiln() {
        super(Properties.of(Material.GRASS).noDrops().randomTicks().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_0_16, 0));
    }

    public static boolean canBurn(BlockGetter level, BlockPos pos) {
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            if (!checkDirection(level, pos, dir)) {
                return false;
            }
        }
        return level.getBlockState(pos.above()).getBlock() == EvolutionBlocks.FIRE;
    }

    private static boolean checkDirection(BlockGetter level, BlockPos pos, Direction direction) {
        return BlockUtils.hasSolidSide(level, pos.relative(direction), direction.getOpposite());
    }

    private static InteractionResult manageStack(TEPitKiln tile, ItemStack handStack, Player player, DirectionDiagonal direction) {
        ItemStack stack = tile.getStack(direction);
        if (stack.isEmpty() &&
            !tile.isSingle() &&
            handStack.getItem() instanceof ItemClayMolded clayMolded &&
            !clayMolded.single) {
            tile.setStack(handStack, direction);
            tile.setChanged();
            return InteractionResult.SUCCESS;
        }
        if (!stack.isEmpty()) {
            Level level = tile.getLevel();
            assert level != null;
            if (!level.isClientSide && !player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, tile.getBlockPos(), stack);
            }
            tile.setStack(ItemStack.EMPTY, direction);
            tile.setChanged();
            tile.checkEmpty();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return;
        }
        int layers = state.getValue(LAYERS_0_16);
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers - 1));
            ItemStack stack = new ItemStack(EvolutionItems.STRAW);
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, pos, stack);
            }
            return;
        }
        TEPitKiln tile = (TEPitKiln) level.getBlockEntity(pos);
        level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers - 1));
        assert tile != null;
        ItemStack stack = tile.getLogStack(layers - 9);
        tile.setLog(layers - 9, (byte) -1);
        tile.setChanged();
        if (!player.getInventory().add(stack)) {
            BlockUtils.dropItemStack(level, pos, stack);
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_0_16);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hit, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(EvolutionItems.STRAW);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6f;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        //TODO implementation
        return Shapes.block();
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        if (state.getValue(LAYERS_0_16) == 0) {
            return SoundType.STONE;
        }
        if (state.getValue(LAYERS_0_16) <= 8) {
            return SoundType.GRASS;
        }
        return SoundType.WOOD;
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return side == Direction.UP && state.getValue(LAYERS_0_16) == 16;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LAYERS_0_16) == 16;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(LAYERS_0_16) < 13;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEPitKiln(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TEPitKiln te = (TEPitKiln) level.getBlockEntity(pos);
            assert te != null;
            te.onRemoved();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        TEPitKiln tile = (TEPitKiln) level.getBlockEntity(pos);
        assert tile != null;
        if (canBurn(level, pos)) {
            if (level.getDayTime() > tile.getTimeStart() + 8L * Time.TICKS_PER_HOUR) {
                level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, 0));
                tile.finish();
            }
        }
        else {
            tile.reset();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int layers = state.getValue(LAYERS_0_16);
        TEPitKiln tile = (TEPitKiln) level.getBlockEntity(pos);
        if (tile == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (layers == 0) {
            if (stack.getItem() == EvolutionItems.STRAW && !tile.hasFinished()) {
                level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(player, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            if (tile.isSingle()) {
                return manageStack(tile, stack, player, DirectionDiagonal.NORTH_WEST);
            }
            int x = MathHelper.getIndex(2, 0, 16, (hit.getLocation().x - pos.getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (hit.getLocation().z - pos.getZ()) * 16);
            return manageStack(tile, stack, player, MathHelper.DIAGONALS[z][x]);
        }
        if (layers < 8) {
            if (stack.getItem() == EvolutionItems.STRAW) {
                level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers + 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(player, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (layers != 16 && stack.getItem() instanceof ItemLog) {
            level.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers + 1));
            tile.setLog(layers - 8, ((ItemLog) stack.getItem()).variant.getId());
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            level.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
