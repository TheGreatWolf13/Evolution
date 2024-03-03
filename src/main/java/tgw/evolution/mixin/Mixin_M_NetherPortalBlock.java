package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(NetherPortalBlock.class)
public abstract class Mixin_M_NetherPortalBlock extends Block {

    @Shadow @Final public static EnumProperty<Direction.Axis> AXIS;
    @Shadow @Final protected static VoxelShape Z_AXIS_AABB;
    @Shadow @Final protected static VoxelShape X_AXIS_AABB;

    public Mixin_M_NetherPortalBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 4; ++i) {
            double px = x + random.nextDouble();
            double py = y + random.nextDouble();
            double pz = z + random.nextDouble();
            double vx = (random.nextFloat() - 0.5) * 0.5;
            double vy = (random.nextFloat() - 0.5) * 0.5;
            double vz = (random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState_(x - 1, y, z).is(this) && !level.getBlockState_(x + 1, y, z).is(this)) {
                px = x + 0.5 + 0.25 * k;
                vx = random.nextFloat() * 2.0F * k;
            }
            else {
                pz = z + 0.5 + 0.25 * k;
                vz = random.nextFloat() * 2.0F * k;
            }
            level.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
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
        if (state.getValue(AXIS) == Direction.Axis.Z) {
            return Z_AXIS_AABB;
        }
        return X_AXIS_AABB;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.dimensionType().natural() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) &&
            random.nextInt(2_000) < level.getDifficulty().getId()) {
            while (level.getBlockState_(x, y, z).is(this)) {
                --y;
            }
            if (level.getBlockState_(x, y, z).isValidSpawn_(level, x, y, z, EntityType.ZOMBIFIED_PIGLIN)) {
                Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(level, null, null, null, new BlockPos(x, y + 1, z), MobSpawnType.STRUCTURE, false,
                                                                  false);
                if (entity != null) {
                    entity.setPortalCooldown();
                }
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
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
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
        Direction.Axis axis = from.getAxis();
        Direction.Axis axis2 = state.getValue(AXIS);
        boolean bl = axis2 != axis && axis.isHorizontal();
        return !bl && !fromState.is(this) && !new PortalShape(level, new BlockPos(x, y, z), axis2).isComplete() ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
