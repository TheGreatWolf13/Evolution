package tgw.evolution.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DISTANCE_0_7;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLeaves extends BlockGeneric implements IReplaceable {

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
        return state.isAir() || block == EvolutionBlocks.FIRE || material.isLiquid() || material.isReplaceable();
    }

    /**
     * Checks whether this block should fall.
     */
    private static void checkFallable(Level level, int x, int y, int z) {
        if (canFallThrough(level.getBlockState_(x, y - 1, z)) && y >= 0) {
            if (!level.isClientSide) {
                fall(level, x, y, z);
            }
        }
    }

    /**
     * Called when this block should fall.
     */
    private static void fall(Level level, int x, int y, int z) {
        BlockState state = level.getBlockState_(x, y, z);
        EntityFallingWeight entity = new EntityFallingWeight(level, x + 0.5, y, z + 0.5, state,
                                                             state instanceof IPhysics physics ? physics.getMass(level, x, y, z, state) : 500);
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
    private static BlockState updateDistance(BlockState state, BlockGetter level, int x, int y, int z) {
        int i = 7;
        for (Direction direction : DirectionUtil.ALL) {
            i = Math.min(i, getDistance(level.getBlockStateAtSide(x, y, z, direction)) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.setValue(DISTANCE_0_7, i);
    }

    @Override
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
    public float getFrictionCoefficient(BlockState state) {
        return 0.2f;
    }

    @Override
    public int getLightBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        return 1;
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        return updateDistance(this.defaultBlockState().setValue(TREE, false), level, x, y, z);
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
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(new BlockPos(x, y, z), this, 2);
        }
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            BlockUtils.dropResources(state, level, x, y, z);
            level.removeBlock_(x, y, z, false);
        }
    }

    @Override
    public boolean shouldCull(BlockGetter level,
                              BlockState state,
                              int x,
                              int y,
                              int z,
                              BlockState adjacentState,
                              int adjX,
                              int adjY,
                              int adjZ,
                              Direction face) {
        if (!Minecraft.useFancyGraphics()) {
            return adjacentState.getBlock() == this;
        }
        int leavesCulling = EvolutionConfig.CLIENT.leavesCulling.get();
        if (leavesCulling == 0) {
            return false;
        }
        for (int i = 1; i <= leavesCulling; i++) {
            BlockState s = level.getBlockStateAtSide(x, y, z, face, i);
            if (!(s.getBlock() == this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        level.setBlockAndUpdate_(x, y, z, updateDistance(state, level, x, y, z));
        if (!state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            checkFallable(level, x, y, z);
        }
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        int i = getDistance(fromState) + 1;
        if (i != 1 || state.getValue(DISTANCE_0_7) != i) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        return state;
    }
}
