package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class BlockPitKiln extends BlockGeneric implements IReplaceable, EntityBlock {

    public BlockPitKiln() {
        super(Properties.of(Material.GRASS).noDrops().randomTicks().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_0_16, 0));
    }

    public static boolean canBurn(BlockGetter level, int x, int y, int z) {
        if (level.getBlockState_(x, y + 1, z).getBlock() != EvolutionBlocks.FIRE) {
            return false;
        }
        if (!BlockUtils.hasSolidFace(level, x + 1, y, z, Direction.WEST)) {
            return false;
        }
        if (!BlockUtils.hasSolidFace(level, x - 1, y, z, Direction.EAST)) {
            return false;
        }
        if (!BlockUtils.hasSolidFace(level, x, y, z + 1, Direction.NORTH)) {
            return false;
        }
        return BlockUtils.hasSolidFace(level, x, y, z - 1, Direction.SOUTH);
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
                BlockPos pos = tile.getBlockPos();
                BlockUtils.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
            tile.setStack(ItemStack.EMPTY, direction);
            tile.setChanged();
            tile.checkEmpty();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        int layers = state.getValue(LAYERS_0_16);
        if (layers == 0) {
            return InteractionResult.PASS;
        }
        if (layers <= 8) {
            level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_0_16, layers - 1));
            ItemStack stack = new ItemStack(EvolutionItems.STRAW);
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, x, y, z, stack);
            }
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity_(x, y, z) instanceof TEPitKiln tile) {
            level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_0_16, layers - 1));
            ItemStack stack = tile.getLogStack(layers - 9);
            tile.setLog(layers - 9, (byte) -1);
            tile.setChanged();
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, x, y, z, stack);
            }
        }
        return InteractionResult.PASS;
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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_0_16);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hit, BlockGetter level, int x, int y, int z, Player player) {
        return new ItemStack(EvolutionItems.STRAW);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6f;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
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
    public boolean isFireSource(BlockState state, LevelReader level, int x, int y, int z, Direction side) {
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
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity_(x, y, z) instanceof TEPitKiln tile) {
                tile.onRemoved();
            }
        }
        super.onRemove_(state, level, x, y, z, newState, isMoving);
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getBlockEntity_(x, y, z) instanceof TEPitKiln tile) {
            if (canBurn(level, x, y, z)) {
                if (level.getDayTime() > tile.getTimeStart() + 8L * Time.TICKS_PER_HOUR) {
                    level.setBlockAndUpdate_(x, y, z, state.setValue(LAYERS_0_16, 0));
                    tile.finish();
                }
            }
            else {
                tile.reset();
            }
        }
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hit) {
        int layers = state.getValue(LAYERS_0_16);
        if (level.getBlockEntity_(x, y, z) instanceof TEPitKiln tile) {
            ItemStack stack = player.getItemInHand(hand);
            if (layers == 0) {
                if (stack.getItem() == EvolutionItems.STRAW && !tile.hasFinished()) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_0_16, 1));
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
                if (tile.isSingle()) {
                    return manageStack(tile, stack, player, DirectionDiagonal.NORTH_WEST);
                }
                int partX = MathHelper.getIndex(2, 0, 16, (hit.getLocation().x - x) * 16);
                int partZ = MathHelper.getIndex(2, 0, 16, (hit.getLocation().z - z) * 16);
                return manageStack(tile, stack, player, MathHelper.DIAGONALS[partZ][partX]);
            }
            if (layers < 8) {
                if (stack.getItem() == EvolutionItems.STRAW) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_0_16, layers + 1));
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            if (layers != 16 && stack.getItem() instanceof ItemLog) {
                level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_0_16, layers + 1));
                tile.setLog(layers - 8, ((ItemLog) stack.getItem()).variant.getId());
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}
