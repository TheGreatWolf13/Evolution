package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(BaseFireBlock.class)
public abstract class Mixin_M_BaseFireBlock extends Block {

    @Shadow @Final protected static VoxelShape DOWN_AABB;

    public Mixin_M_BaseFireBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean inPortalDimension(Level level) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        double d;
        double e;
        double f;
        if (!this.canBurn(stateBelow) && !stateBelow.isFaceSturdy_(level, x, y - 1, z, Direction.UP)) {
            if (this.canBurn(level.getBlockState_(x - 1, y, z))) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextDouble() * 0.1, y + random.nextDouble(), z + random.nextDouble(), 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x + 1, y, z))) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, (x + 1) - random.nextDouble() * 0.1, y + random.nextDouble(), z + random.nextDouble(), 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y, z - 1))) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextDouble(), y + random.nextDouble(), z + random.nextDouble() * 0.1, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y, z + 1))) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextDouble(), y + random.nextDouble(), (z + 1) - random.nextDouble() * 0.1, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y + 1, z))) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextDouble(), (y + 1) - random.nextDouble() * 0.1, z + random.nextDouble(), 0, 0, 0);
                }
            }
        }
        else {
            for (int i = 0; i < 3; ++i) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextDouble(), y + random.nextDouble() * 0.5 + 0.5, z + random.nextDouble(), 0, 0, 0);
            }
        }
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
        return DOWN_AABB;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            if (inPortalDimension(level)) {
                Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, new BlockPos(x, y, z), Direction.Axis.X);
                if (optional.isPresent()) {
                    optional.get().createPortalBlocks();
                    return;
                }
            }
            if (!state.canSurvive_(level, x, y, z)) {
                level.removeBlock_(x, y, z, false);
            }
        }
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
        if (!level.isClientSide()) {
            level.levelEvent_(LevelEvent.SOUND_EXTINGUISH_FIRE, x, y, z, 0);
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
    public void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void spawnDestroyParticles_(Level level, Player player, int x, int y, int z, BlockState state) {
    }

    @Shadow
    protected abstract boolean canBurn(BlockState blockState);
}
