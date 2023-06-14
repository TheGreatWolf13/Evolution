package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IMinecraftPatch;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    @Shadow @Final private Minecraft minecraft;

    public ClientLevelMixin(WritableLevelData pLevelData,
                            ResourceKey<Level> pDimension,
                            Holder<DimensionType> pDimensionTypeRegistration,
                            Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addAlwaysVisibleParticle(ParticleOptions particleData,
                                         double x,
                                         double y,
                                         double z,
                                         double velX,
                                         double velY,
                                         double velZ) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().listener().addParticle(particleData, false, true, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addAlwaysVisibleParticle(ParticleOptions particleData,
                                         boolean ignoreRange,
                                         double x,
                                         double y,
                                         double z,
                                         double velX,
                                         double velY,
                                         double velZ) {
        ((IMinecraftPatch) this.minecraft)
                .lvlRenderer()
                .listener()
                .addParticle(particleData, particleData.getType().getOverrideLimiter() || ignoreRange, true, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double velX, double velY, double velZ) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer()
                                          .listener()
                                          .addParticle(particleData, particleData.getType().getOverrideLimiter(), false, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void addParticle(ParticleOptions particleData,
                            boolean force,
                            double x,
                            double y,
                            double z,
                            double velX,
                            double velY,
                            double velZ) {
        ((IMinecraftPatch) this.minecraft)
                .lvlRenderer()
                .listener()
                .addParticle(particleData, particleData.getType().getOverrideLimiter() || force, false, x, y, z, velX, velY, velZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().destroyBlockProgress(breakerId, pos, progress);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    public void doAnimateTick(int posX,
                              int posY,
                              int posZ,
                              int range,
                              Random random,
                              @Nullable Block block,
                              BlockPos.MutableBlockPos pos) {
        int i = posX + this.random.nextInt(range) - this.random.nextInt(range);
        int j = posY + this.random.nextInt(range) - this.random.nextInt(range);
        int k = posZ + this.random.nextInt(range) - this.random.nextInt(range);
        pos.set(i, j, k);
        BlockState blockState = this.getBlockState(pos);
        blockState.getBlock().animateTick(blockState, this, pos, random);
        FluidState fluidState = this.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            fluidState.animateTick(this, pos, random);
            ParticleOptions dripParticle = fluidState.getDripParticle();
            if (dripParticle != null && this.random.nextInt(10) == 0) {
                boolean faceSturdy = blockState.isFaceSturdy(this, pos, Direction.DOWN);
                pos.move(Direction.DOWN);
                this.trySpawnDripParticles(pos, this.getBlockState(pos), dripParticle, faceSturdy);
                pos.move(Direction.UP);
            }
        }
        if (block == blockState.getBlock()) {
            this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, blockState), i + 0.5, j + 0.5, k + 0.5, 0, 0, 0);
        }
        if (!blockState.isCollisionShapeFullBlock(this, pos)) {
            Optional<AmbientParticleSettings> ambientParticle = this.getBiome(pos).value().getAmbientParticle();
            if (ambientParticle.isPresent()) {
                AmbientParticleSettings settings = ambientParticle.get();
                if (settings.canSpawn(this.random)) {
                    this.addParticle(settings.getOptions(), pos.getX() + this.random.nextDouble(), pos.getY() + this.random.nextDouble(),
                                     pos.getZ() + this.random.nextDouble(), 0, 0, 0);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Use Evolution dimension
     */
    @Overwrite
    public float getStarBrightness(float partialTicks) {
        if (ClientEvents.getInstance().getDimension() != null) {
            return ClientEvents.getInstance().getDimension().getSkyBrightness(partialTicks);
        }
        float timeOfDay = this.getTimeOfDay(partialTicks);
        float f = 1.0F - (Mth.cos(timeOfDay * Mth.TWO_PI) * 2.0F + 0.25F);
        f = Mth.clamp(f, 0, 1);
        return f * f * 0.5F;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void globalLevelEvent(int id, BlockPos pos, int data) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().globalLevelEvent(id, pos, data);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        try {
            ((IMinecraftPatch) this.minecraft).lvlRenderer().levelEvent(type, pos, data);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Playing level event");
            CrashReportCategory category = crash.addCategory("Level event being played");
            category.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, pos));
            //noinspection ConstantConditions
            category.setDetail("Event source", player);
            category.setDetail("Event type", type);
            category.setDetail("Event data", data);
            throw new ReportedException(crash);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().blockChanged(this, pos, oldState, newState, flags);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void setBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().setBlockDirty(pos, oldState, newState);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ) {
        ((IMinecraftPatch) this.minecraft).lvlRenderer().setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
    }

    @Shadow
    protected abstract void trySpawnDripParticles(BlockPos pBlockPos,
                                                  BlockState pBlockState,
                                                  ParticleOptions pParticleData,
                                                  boolean pShapeDownSolid);
}
