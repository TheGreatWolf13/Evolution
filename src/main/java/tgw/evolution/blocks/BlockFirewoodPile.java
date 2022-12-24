package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemFirewood;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.math.MathHelper;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.FIREWOOD_COUNT;

public class BlockFirewoodPile extends BlockMass implements IReplaceable, EntityBlock {

    public BlockFirewoodPile() {
        super(Properties.of(Material.WOOD).strength(-1, 2.0F).sound(SoundType.WOOD), 400);
        this.registerDefaultState(this.defaultBlockState().setValue(FIREWOOD_COUNT, 1).setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return;
        }
        if (Math.abs(pos.getX() + 0.5 - player.getX()) < 1.75 &&
            Math.abs(pos.getY() - player.getY()) < 1.75 &&
            Math.abs(pos.getZ() + 0.5 - player.getZ()) < 1.75) {
            TEFirewoodPile tile = (TEFirewoodPile) level.getBlockEntity(pos);
            assert tile != null;
            ItemStack stack = new ItemStack(tile.removeLastFirewood());
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, pos, stack);
            }
            else {
                level.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                                ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1) * 2);
            }
            if (state.getValue(FIREWOOD_COUNT) == 1) {
                level.removeBlock(pos, false);
                return;
            }
            level.setBlockAndUpdate(pos, state.setValue(FIREWOOD_COUNT, state.getValue(FIREWOOD_COUNT) - 1));
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(FIREWOOD_COUNT) < 16) {
            ItemStack stack = context.getItemInHand();
            if (stack.getItem() instanceof ItemFirewood) {
                return true;
            }
        }
        return super.canBeReplaced(state, context);
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
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (state.getValue(FIREWOOD_COUNT) == 16 || BlockUtils.isReplaceable(level.getBlockState(pos.above()))) &&
               BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FIREWOOD_COUNT, DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        TEFirewoodPile tile = (TEFirewoodPile) level.getBlockEntity(pos);
        assert tile != null;
        if (target instanceof BlockHitResult hit) {
            Vec3 location = hit.getLocation();
            int y = (int) ((location.y - pos.getY() + 0.001) * 16) / 4;
            Direction stateDirection = state.getValue(DIRECTION_HORIZONTAL);
            int x;
            if (stateDirection.getAxis() == Direction.Axis.Z) {
                x = (int) ((location.x - pos.getX() + 0.001) * 16) / 4;
            }
            else {
                x = (int) ((location.z - pos.getZ() + 0.001) * 16) / 4;
            }
            Direction hitDirection = hit.getDirection();
            if (hitDirection == Direction.UP) {
                y--;
            }
            else if (hitDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE && hitDirection.getAxis() != stateDirection.getAxis()) {
                x--;
            }
            if (stateDirection == Direction.SOUTH || stateDirection == Direction.WEST) {
                x = 3 - x;
            }
            return new ItemStack(tile.getFirewoodAt(4 * y + x));
        }
        return new ItemStack(tile.getFirewoodAt(0));
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        NonNullList<ItemStack> drops = NonNullList.withSize(state.getValue(FIREWOOD_COUNT), ItemStack.EMPTY);
        TEFirewoodPile tile = (TEFirewoodPile) level.getBlockEntity(pos);
        assert tile != null;
        for (int i = 0; i < drops.size(); i++) {
            //noinspection ObjectAllocationInLoop
            drops.set(i, new ItemStack(tile.getFirewoodAt(i)));
        }
        return drops;
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
        return HarvestLevel.UNBREAKABLE;
    }

    @Override
    public int getMass(BlockState state) {
        return state.getValue(FIREWOOD_COUNT) * this.getBaseMass() / 16;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int logCount = state.getValue(FIREWOOD_COUNT);
        if (logCount == 16) {
            return Shapes.block();
        }
        VoxelShape shape = Shapes.empty();
        if (logCount >= 12) {
            shape = EvolutionShapes.LOG_PILE[12];
        }
        else if (logCount >= 8) {
            shape = EvolutionShapes.LOG_PILE[8];
        }
        else if (logCount >= 4) {
            shape = EvolutionShapes.LOG_PILE[4];
        }
        return MathHelper.union(shape,
                                MathHelper.rotateShape(Direction.NORTH, state.getValue(DIRECTION_HORIZONTAL), EvolutionShapes.LOG_PILE[logCount]));
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(FIREWOOD_COUNT) < 13;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                for (ItemStack stack : this.getDrops(level, pos, state)) {
                    popResource(level, pos, stack);
                }
                level.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEFirewoodPile(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }
}
