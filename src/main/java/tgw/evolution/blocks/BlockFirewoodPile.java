package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemFirewood;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.MathHelper;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.FIREWOOD_COUNT;

public class BlockFirewoodPile extends BlockPhysics implements IReplaceable, EntityBlock, IPoppable {

    public BlockFirewoodPile() {
        super(Properties.of(Material.WOOD).strength(-1, 2.0F).sound(SoundType.WOOD).noDrops());
        this.registerDefaultState(this.defaultBlockState().setValue(FIREWOOD_COUNT, 1).setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        if (Math.abs(x + 0.5 - player.getX()) < 1.75 &&
            Math.abs(y - player.getY()) < 1.75 &&
            Math.abs(z + 0.5 - player.getZ()) < 1.75 && level.getBlockEntity_(x, y, z) instanceof TEFirewoodPile tile) {
            ItemStack stack = new ItemStack(tile.removeLastFirewood());
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, x, y, z, stack);
            }
            else {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1) * 2);
            }
            if (state.getValue(FIREWOOD_COUNT) == 1) {
                level.removeBlock_(x, y, z, false);
                return InteractionResult.SUCCESS;
            }
            level.setBlockAndUpdate_(x, y, z, state.setValue(FIREWOOD_COUNT, state.getValue(FIREWOOD_COUNT) - 1));
            return InteractionResult.SUCCESS;
        }
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
    public boolean canBeReplaced_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(FIREWOOD_COUNT) < 16) {
            if (player.getItemInHand(hand).getItem() instanceof ItemFirewood) {
                return true;
            }
        }
        return super.canBeReplaced_(state, level, x, y, z, player, hand, hitResult);
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return (state.getValue(FIREWOOD_COUNT) == 16 || BlockUtils.isReplaceable(level.getBlockState_(x, y + 1, z))) && BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FIREWOOD_COUNT, DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, int x, int y, int z, Player player) {
        if (level.getBlockEntity_(x, y, z) instanceof TEFirewoodPile tile) {
            if (target instanceof BlockHitResult hit) {
                int hitY = (int) ((hit.y() - y + 0.001) * 16) / 4;
                Direction stateDirection = state.getValue(DIRECTION_HORIZONTAL);
                int hitX;
                if (stateDirection.getAxis() == Direction.Axis.Z) {
                    hitX = (int) ((hit.x() - x + 0.001) * 16) / 4;
                }
                else {
                    hitX = (int) ((hit.z() - z + 0.001) * 16) / 4;
                }
                Direction hitDirection = hit.getDirection();
                if (hitDirection == Direction.UP) {
                    hitY--;
                }
                else if (hitDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE && hitDirection.getAxis() != stateDirection.getAxis()) {
                    hitX--;
                }
                if (stateDirection == Direction.SOUTH || stateDirection == Direction.WEST) {
                    hitX = 3 - hitX;
                }
                return new ItemStack(tile.getFirewoodAt(4 * hitY + hitX));
            }
            return new ItemStack(tile.getFirewoodAt(0));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.UNBREAKABLE;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        int logCount = state.getValue(FIREWOOD_COUNT);
        if (logCount == 16) {
            return Shapes.block();
        }
        VoxelShape shape = Shapes.empty();
        if (logCount >= 12) {
            shape = EvolutionShapes.LOG_PILE[12 - 1];
        }
        else if (logCount >= 8) {
            shape = EvolutionShapes.LOG_PILE[8 - 1];
        }
        else if (logCount >= 4) {
            shape = EvolutionShapes.LOG_PILE[4 - 1];
        }
        return MathHelper.union(shape, MathHelper.rotateShape(Direction.NORTH, state.getValue(DIRECTION_HORIZONTAL), EvolutionShapes.LOG_PILE[logCount - 1]));
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(FIREWOOD_COUNT) < 16;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEFirewoodPile(pos, state);
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            if (tile instanceof TEFirewoodPile te) {
                te.dropAll(level, x, y, z);
            }
        }
        super.onRemove_(state, level, x, y, z, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public BlockState stateForParticles(BlockState state, Level level, int x, int y, int z) {
        if (level.getBlockEntity_(x, y, z) instanceof TEFirewoodPile tile) {
            WoodVariant variant = tile.getLastVariant();
            if (variant != null) {
                return EvolutionBlocks.LOGS.get(variant).defaultBlockState();
            }
        }
        return super.stateForParticles(state, level, x, y, z);
    }
}
