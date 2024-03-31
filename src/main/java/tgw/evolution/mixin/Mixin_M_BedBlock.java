package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(BedBlock.class)
public abstract class Mixin_M_BedBlock extends HorizontalDirectionalBlock implements EntityBlock {

    @Shadow @Final public static EnumProperty<BedPart> PART;
    @Shadow @Final public static BooleanProperty OCCUPIED;
    @Shadow @Final protected static VoxelShape EAST_SHAPE;
    @Shadow @Final protected static VoxelShape WEST_SHAPE;
    @Shadow @Final protected static VoxelShape SOUTH_SHAPE;
    @Shadow @Final protected static VoxelShape NORTH_SHAPE;

    public Mixin_M_BedBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean canSetSpawn(Level level) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static Direction getConnectedDirection(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public long getSeed_(BlockState state, int x, int y, int z) {
        int seedX = x;
        int seedZ = z;
        if (state.getValue(PART) != BedPart.HEAD) {
            Direction facing = state.getValue(FACING);
            seedX += facing.getStepX();
            seedZ += facing.getStepZ();
        }
        return Mth.getSeed(seedX, y, seedZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (getConnectedDirection(state).getOpposite()) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> EAST_SHAPE;
        };
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        if (!level.isClientSide && player.isCreative()) {
            BedPart bedPart = state.getValue(PART);
            if (bedPart == BedPart.FOOT) {
                Direction otherDir = getNeighbourDirection(bedPart, state.getValue(FACING));
                int otherX = x + otherDir.getStepX();
                int otherZ = z + otherDir.getStepZ();
                BlockState otherState = level.getBlockState_(otherX, y, otherZ);
                if (otherState.is(this) && otherState.getValue(PART) == BedPart.HEAD) {
                    level.setBlock_(otherX, y, otherZ, Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.NO_NEIGHBOR_DROPS);
                    level.levelEvent_(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherX, y, otherZ, Block.getId(otherState));
                }
            }
        }
        return super.playerWillDestroy_(level, x, y, z, state, player, face, hitX, hitY, hitZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        if (from == getNeighbourDirection(state.getValue(PART), state.getValue(FACING))) {
            return fromState.is(this) && fromState.getValue(PART) != state.getValue(PART) ?
                   state.setValue(OCCUPIED, fromState.getValue(OCCUPIED)) :
                   Blocks.AIR.defaultBlockState();
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (state.getValue(PART) != BedPart.HEAD) {
            Direction facing = state.getValue(FACING);
            x += facing.getStepX();
            z += facing.getStepZ();
            state = level.getBlockState_(x, y, z);
            if (!state.is(this)) {
                return InteractionResult.CONSUME;
            }
        }
        if (!canSetSpawn(level)) {
            level.removeBlock_(x, y, z, false);
            Direction offset = state.getValue(FACING).getOpposite();
            int otherX = x + offset.getStepX();
            int otherZ = z + offset.getStepZ();
            if (level.getBlockState_(otherX, y, otherZ).is(this)) {
                level.removeBlock_(otherX, y, otherZ, false);
            }
            level.explode(null, DamageSource.badRespawnPointExplosion(), null, x + 0.5, y + 0.5, z + 0.5, 5.0F, true,
                          Explosion.BlockInteraction.DESTROY);
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(OCCUPIED)) {
            if (!this.kickVillagerOutOfBed(level, new BlockPos(x, y, z))) {
                player.displayClientMessage(new TranslatableComponent("block.minecraft.bed.occupied"), true);
            }
            return InteractionResult.SUCCESS;
        }
        PatchEither<Player.BedSleepingProblem, Unit> either = (PatchEither) player.startSleepInBed(new BlockPos(x, y, z));
        if (either.isLeft()) {
            player.displayClientMessage(either.getLeft().getMessage(), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Shadow
    protected abstract boolean kickVillagerOutOfBed(Level level, BlockPos blockPos);
}
