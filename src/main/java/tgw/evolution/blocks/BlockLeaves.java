package tgw.evolution.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DISTANCE_0_7;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLeaves extends BlockGeneric implements IReplaceable, IForgeShearable {

    private static final Vec3 MOTION_MULTIPLIER = new Vec3(0.5, 1, 0.5);

    public BlockLeaves() {
        super(Properties.of(Material.LEAVES).strength(0.2F, 0.2F).sound(SoundType.GRASS).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DISTANCE_0_7, 0).setValue(TREE, true));
    }

    /**
     * Returns whether this block can fall through.
     */
    public static boolean canFallThrough(BlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return state.isAir() || block == EvolutionBlocks.FIRE.get() || material.isLiquid() || material.isReplaceable();
    }

    /**
     * Called when this block should fall.
     */
    private static void fall(Level level, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getBlockState(pos));
        level.addFreshEntity(entity);
    }

    /**
     * Returns the distance between this block and a Log Block.
     */
    private static int getDistance(BlockState neighbor) {
        if (neighbor.getBlock() instanceof BlockLog) {
            return 0;
        }
        return neighbor.getBlock() instanceof BlockLeaves ? neighbor.getValue(DISTANCE_0_7) : 7;
    }

    /**
     * Updates this blockstate in accordance with the distance away from logs.
     */
    private static BlockState updateDistance(BlockState state, BlockGetter level, BlockPos pos) {
        int i = 7;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : DirectionUtil.ALL) {
            mutable.setWithOffset(pos, direction);
            i = Math.min(i, getDistance(level.getBlockState(mutable)) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.setValue(DISTANCE_0_7, i);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if (level.isRainingAt(pos.above()) && !level.getBlockState(pos.below()).canOcclude() && rand.nextInt(15) == 1) {
            double x = pos.getX() + rand.nextFloat();
            double y = pos.getY() - 0.05;
            double z = pos.getZ() + rand.nextFloat();
            level.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    /**
     * Checks whether this block should fall.
     */
    private void checkFallable(Level level, BlockPos pos) {
        if (canFallThrough(level.getBlockState(pos.below())) && pos.getY() >= 0) {
            if (level.isAreaLoaded(pos, 32)) {
                if (!level.isClientSide) {
                    fall(level, pos);
                }
            }
            else {
                BlockState state = this.defaultBlockState();
                if (level.getBlockState(pos).getBlock() == this) {
                    state = level.getBlockState(pos);
                    level.removeBlock(pos, false);
                }
                BlockPos.MutableBlockPos posDown = new BlockPos.MutableBlockPos();
                posDown.setWithOffset(pos, Direction.DOWN);
                while (canFallThrough(level.getBlockState(posDown)) && posDown.getY() > 0) {
                    posDown.move(Direction.DOWN);
                }
                if (posDown.getY() > 0) {
                    level.setBlockAndUpdate(posDown.move(Direction.UP), state);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE_0_7, TREE);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.makeStuckInBlock(state, MOTION_MULTIPLIER);
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY);
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
        return 0.2f;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateDistance(this.defaultBlockState().setValue(TREE, false), context.getLevel(), context.getClickedPos());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(TREE) && (state.getValue(DISTANCE_0_7) == 0 || state.getValue(DISTANCE_0_7) == 7);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (!Minecraft.useFancyGraphics()) {
            return adjacentState.getBlock() instanceof BlockLeaves;
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        level.setBlockAndUpdate(pos, updateDistance(state, level, pos));
        if (!state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            this.checkFallable(level, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || state.getValue(DISTANCE_0_7) != i) {
            level.scheduleTick(currentPos, this, 1);
        }
        return state;
    }
}
