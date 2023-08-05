package tgw.evolution.client.renderer.chunk;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.constants.LvlEvent;

import java.util.Random;

import static net.minecraft.world.level.block.LevelEvent.*;

public class LevelEventListener {

    private final Minecraft mc;
    private final L2OMap<SoundInstance> playingRecords = new L2OHashMap<>();
    private @Nullable ClientLevel level;

    public LevelEventListener(Minecraft mc) {
        this.mc = mc;
    }

    /**
     * Notifies living entities in a 3 block range of the specified {@code pos} that a record is or isn't playing nearby, dependent on the
     * specified {@code playing} parameter.
     * This is used to make parrots start or stop partying.
     */
    private static void notifyNearbyEntities(EntityGetter level, BlockPos pos, boolean playing) {
        for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3))) {
            livingentity.setRecordPlayingNearby(pos, playing);
        }
    }

    public void addParticle(ParticleOptions particle,
                            boolean force,
                            boolean decr,
                            double x,
                            double y,
                            double z,
                            double velX,
                            double velY,
                            double velZ) {
        try {
            this.addParticleInternal(particle, force, decr, x, y, z, velX, velY, velZ);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Exception while adding particle");
            CrashReportCategory category = crash.addCategory("Particle being added");
            //noinspection ConstantConditions
            category.setDetail("ID", Registry.PARTICLE_TYPE.getKey(particle.getType()));
            category.setDetail("Parameters", particle.writeToString());
            //noinspection ConstantConditions
            category.setDetail("Position", () -> CrashReportCategory.formatLocation(this.level, x, y, z));
            throw new ReportedException(crash);
        }
    }

    private <T extends ParticleOptions> void addParticle(T particle, double x, double y, double z, double velX, double velY, double velZ) {
        this.addParticle(particle, particle.getType().getOverrideLimiter(), false, x, y, z, velX, velY, velZ);
    }

    /**
     * @param force if {@code true}, the particle will be created regardless of its distance from the camera and the
     *              {@linkplain #calculateParticleLevel(boolean) calculated particle level}
     * @param decr  if {@code true}, and the {@linkplain net.minecraft.client.Options#particles particles option} is set to minimal, attempts to
     *              spawn the particle at a decreased level
     */
    private @Nullable Particle addParticleInternal(ParticleOptions particle,
                                                   boolean force,
                                                   boolean decr,
                                                   double x,
                                                   double y,
                                                   double z,
                                                   double velX,
                                                   double velY,
                                                   double velZ) {
        Camera camera = this.mc.gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            if (force) {
                return this.mc.particleEngine.createParticle(particle, x, y, z, velX, velY, velZ);
            }
            if (camera.getPosition().distanceToSqr(x, y, z) > 1_024) {
                return null;
            }
            return this.calculateParticleLevel(decr) == ParticleStatus.MINIMAL ?
                   null :
                   this.mc.particleEngine.createParticle(particle, x, y, z, velX, velY, velZ);
        }
        return null;
    }

    /**
     * Calculates the level of particles to use based on the {@linkplain net.minecraft.client.Options#particles particles
     * option} and the specified {@code decreased} parameter. This leads to randomly generating more or less particles
     * than the set option.
     *
     * @param decr if {@code true}, and the {@linkplain net.minecraft.client.Options#particles particles option} is
     *             set to minimal, has a 1 in 10 chance to return a decreased level and a further 1 in 3 chance to minimise it
     */
    private ParticleStatus calculateParticleLevel(boolean decr) {
        assert this.level != null;
        ParticleStatus particleStatus = this.mc.options.particles;
        if (decr && particleStatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
            particleStatus = ParticleStatus.DECREASED;
        }
        if (particleStatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
            return ParticleStatus.MINIMAL;
        }
        return particleStatus;
    }

    public void levelEvent(@LvlEvent int type, int x, int y, int z, int data) {
        ClientLevel level = this.level;
        assert level != null;
        Random rnd = level.random;
        switch (type) {
            case SOUND_DISPENSER_DISPENSE -> this.playLocalSound(x, y, z, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            case SOUND_DISPENSER_FAIL -> this.playLocalSound(x, y, z, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F);
            case SOUND_DISPENSER_PROJECTILE_LAUNCH -> this.playLocalSound(x, y, z, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F
            );
            case SOUND_ENDER_EYE_LAUNCH -> this.playLocalSound(x, y, z, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F);
            case SOUND_FIREWORK_SHOOT -> this.playLocalSound(x, y, z, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F);
            case SOUND_OPEN_IRON_DOOR -> this.playLocalSound(x, y, z, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F,
                                                             rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_OPEN_WOODEN_DOOR -> this.playLocalSound(x, y, z, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F,
                                                               rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_OPEN_WOODEN_TRAP_DOOR -> this.playLocalSound(x, y, z, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F,
                                                                    rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_OPEN_FENCE_GATE -> this.playLocalSound(x, y, z, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F,
                                                              rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_EXTINGUISH_FIRE -> {
                if (data == 0) {
                    this.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                                        2.6F + (rnd.nextFloat() - rnd.nextFloat()) * 0.8F);
                }
                else if (data == 1) {
                    this.playLocalSound(x, y, z, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F,
                                        1.6F + (rnd.nextFloat() - rnd.nextFloat()) * 0.4F);
                }
            }
            case SOUND_PLAY_RECORDING -> {
                if (Item.byId(data) instanceof RecordItem record) {
                    this.playStreamingMusic(record.getSound(), new BlockPos(x, y, z), record);
                }
                else {
                    this.playStreamingMusic(null, new BlockPos(x, y, z), null);
                }
            }
            case SOUND_CLOSE_IRON_DOOR -> this.playLocalSound(x, y, z, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS,
                                                              1.0F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_CLOSE_WOODEN_DOOR -> this.playLocalSound(x, y, z, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS,
                                                                1.0F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_CLOSE_WOODEN_TRAP_DOOR -> this.playLocalSound(x, y, z, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS,
                                                                     1.0F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_CLOSE_FENCE_GATE -> this.playLocalSound(x, y, z, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS,
                                                               1.0F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_GHAST_WARNING -> this.playLocalSound(x, y, z, SoundEvents.GHAST_WARN, SoundSource.HOSTILE,
                                                            10.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_GHAST_FIREBALL -> this.playLocalSound(x, y, z, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE,
                                                             10.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_DRAGON_FIREBALL -> this.playLocalSound(x, y, z, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE,
                                                              10.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_BLAZE_FIREBALL -> this.playLocalSound(x, y, z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
                                                             2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ZOMBIE_WOODEN_DOOR -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE,
                                                                 2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ZOMBIE_IRON_DOOR -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE,
                                                               2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ZOMBIE_DOOR_CRASH -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE,
                                                                2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_WITHER_BLOCK_BREAK -> this.playLocalSound(x, y, z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE,
                                                                 2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_WITHER_BOSS_SHOOT -> this.playLocalSound(x, y, z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE,
                                                                2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_BAT_LIFTOFF -> this.playLocalSound(x, y, z, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL,
                                                          0.05F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ZOMBIE_INFECTED -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE,
                                                              2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ZOMBIE_CONVERTED -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE,
                                                               2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_ANVIL_BROKEN -> this.playLocalSound(x, y, z, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS,
                                                           1.0F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_ANVIL_USED -> this.playLocalSound(x, y, z, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.1f * rnd.nextFloat() + 0.9f
            );
            case SOUND_ANVIL_LAND -> this.playLocalSound(x, y, z, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
                                                         0.3F, 0.1f * rnd.nextFloat() + 0.9f);
            case SOUND_PORTAL_TRAVEL -> this.mc.getSoundManager()
                                               .play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL,
                                                                                          0.4f * rnd.nextFloat() + 0.8f, 0.25F));
            case SOUND_CHORUS_GROW -> this.playLocalSound(x, y, z, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F);
            case SOUND_CHORUS_DEATH -> this.playLocalSound(x, y, z, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F);
            case SOUND_BREWING_STAND_BREW -> this.playLocalSound(x, y, z, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
            case SOUND_CLOSE_IRON_TRAP_DOOR -> this.playLocalSound(x, y, z, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS,
                                                                   1.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_OPEN_IRON_TRAP_DOOR -> this.playLocalSound(x, y, z, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS,
                                                                  1.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_PHANTOM_BITE -> this.playLocalSound(x, y, z, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE,
                                                           0.3F, level.random.nextFloat() * 0.1F + 0.9F);
            case SOUND_ZOMBIE_TO_DROWNED -> this.playLocalSound(x, y, z, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE,
                                                                2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_HUSK_TO_ZOMBIE -> this.playLocalSound(x, y, z, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE,
                                                             2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case SOUND_GRINDSTONE_USED -> this.playLocalSound(x, y, z, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                                                              1.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_PAGE_TURN -> this.playLocalSound(x, y, z, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS,
                                                        1.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_SMITHING_TABLE_USED -> this.playLocalSound(x, y, z, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS,
                                                                  1.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_POINTED_DRIPSTONE_LAND -> this.playLocalSound(x, y, z, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS,
                                                                     2.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_DRIP_LAVA_INTO_CAULDRON -> this.playLocalSound(x, y, z, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON,
                                                                      SoundSource.BLOCKS,
                                                                      2.0F, rnd.nextFloat() * 0.1F + 0.9F);
            case SOUND_DRIP_WATER_INTO_CAULDRON -> this.playLocalSound(x, y, z, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON,
                                                                       SoundSource.BLOCKS, 2.0F, level.random.nextFloat() * 0.1F + 0.9F);
            case SOUND_SKELETON_TO_STRAY -> this.playLocalSound(x, y, z, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE,
                                                                2.0F, 0.4f * rnd.nextFloat() + 0.8f);
            case COMPOSTER_FILL -> ComposterBlock.handleFill(level, new BlockPos(x, y, z), data > 0);
            case LAVA_FIZZ -> {
                this.playLocalSound(x, y, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.8F + 1.6f * rnd.nextFloat());
                double py = y + 1.2;
                for (int i = 0; i < 8; ++i) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + rnd.nextDouble(), py, z + rnd.nextDouble(), 0, 0, 0);
                }
            }
            case REDSTONE_TORCH_BURNOUT -> {
                this.playLocalSound(x, y, z, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 1.8F + 1.6f * rnd.nextFloat());
                for (int i = 0; i < 5; ++i) {
                    level.addParticle(ParticleTypes.SMOKE,
                                      x + rnd.nextDouble() * 0.6 + 0.2,
                                      y + rnd.nextDouble() * 0.6 + 0.2,
                                      z + rnd.nextDouble() * 0.6 + 0.2,
                                      0, 0, 0);
                }
            }
            case END_PORTAL_FRAME_FILL -> {
                this.playLocalSound(x, y, z, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                double py = y + 0.812_5;
                for (int i = 0; i < 16; ++i) {
                    level.addParticle(ParticleTypes.SMOKE,
                                      x + 5 / 16.0 + 6 / 16.0 * rnd.nextDouble(),
                                      py,
                                      z + 5 / 16.0 + 6 / 16.0 * rnd.nextDouble(),
                                      0, 0, 0);
                }
            }
            case DRIPSTONE_DRIP -> PointedDripstoneBlock.spawnDripParticle(level, new BlockPos(x, y, z), level.getBlockState_(x, y, z));
            case PARTICLES_AND_SOUND_PLANT_GROWTH -> {
                BoneMealItem.addGrowthParticles(level, new BlockPos(x, y, z), data);
                this.playLocalSound(x, y, z, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            case PARTICLES_SHOOT -> {
                Direction dir = Direction.from3DDataValue(data);
                int stepX = dir.getStepX();
                int stepY = dir.getStepY();
                int stepZ = dir.getStepZ();
                double d18 = x + stepX * 0.6 + 0.5;
                double d24 = y + stepY * 0.6 + 0.5;
                double d28 = z + stepZ * 0.6 + 0.5;
                for (int i = 0; i < 10; ++i) {
                    double intensity = rnd.nextDouble() * 0.2 + 0.01;
                    double px = d18 + stepX * 0.01 + (rnd.nextDouble() - 0.5) * stepZ * 0.5;
                    double py = d24 + stepY * 0.01 + (rnd.nextDouble() - 0.5) * stepY * 0.5;
                    double pz = d28 + stepZ * 0.01 + (rnd.nextDouble() - 0.5) * stepX * 0.5;
                    double vx = stepX * intensity + rnd.nextGaussian() * 0.01;
                    double vy = stepY * intensity + rnd.nextGaussian() * 0.01;
                    double vz = stepZ * intensity + rnd.nextGaussian() * 0.01;
                    this.addParticle(ParticleTypes.SMOKE, px, py, pz, vx, vy, vz);
                }
            }
            case PARTICLES_DESTROY_BLOCK -> {
                BlockState state = Block.stateById(data);
                if (!state.isAir()) {
                    SoundType sound = state.getSoundType();
                    this.playLocalSound(x, y, z, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
                                        sound.getPitch() * 0.8F
                    );
                }
                level.addDestroyBlockEffect_(x, y, z, state);
            }
            case PARTICLES_SPELL_POTION_SPLASH, PARTICLES_INSTANT_POTION_SPLASH -> {
                double px = x + 0.5;
                double pz = z + 0.5;
                ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION));
                for (int i = 0; i < 8; ++i) {
                    this.addParticle(option, px, y, pz, rnd.nextGaussian() * 0.15, rnd.nextDouble() * 0.2, rnd.nextGaussian() * 0.15);
                }
                float r = (data >> 16 & 255) / 255.0F;
                float g = (data >> 8 & 255) / 255.0F;
                float b = (data & 255) / 255.0F;
                ParticleOptions particleType = type == PARTICLES_INSTANT_POTION_SPLASH ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int i = 0; i < 100; ++i) {
                    float power = rnd.nextFloat() * 4.0f;
                    float angle = rnd.nextFloat() * Mth.TWO_PI;
                    double dx = Mth.cos(angle) * power;
                    double dy = 0.01 + rnd.nextDouble() * 0.5;
                    double dz = Mth.sin(angle) * power;
                    Particle particle = this.addParticleInternal(particleType, particleType.getType().getOverrideLimiter(), false,
                                                                 px + dx * 0.1, y + 0.3, pz + dz * 0.1, dx, dy, dz);
                    if (particle != null) {
                        float mult = 0.75F + rnd.nextFloat() * 0.25F;
                        particle.setColor(r * mult, g * mult, b * mult);
                        particle.setPower(power);
                    }
                }
                level.playLocalSound(px, y, pz, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, rnd.nextFloat() * 0.1F + 0.9F, false);
            }
            case PARTICLES_EYE_OF_ENDER_DEATH -> {
                double px = x + 0.5;
                double pz = z + 0.5;
                ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE));
                for (int i = 0; i < 8; ++i) {
                    this.addParticle(option, px, y, pz, rnd.nextGaussian() * 0.15, rnd.nextDouble() * 0.2, rnd.nextGaussian() * 0.15);
                }
                for (float theta = 0; theta < Mth.TWO_PI; theta += Mth.PI / 20) {
                    float cos = Mth.cos(theta);
                    float sin = Mth.sin(theta);
                    this.addParticle(ParticleTypes.PORTAL, px + cos * 5, y - 0.4, pz + sin * 5, cos * -5, 0, sin * -5);
                    this.addParticle(ParticleTypes.PORTAL, px + cos * 5, y - 0.4, pz + sin * 5, cos * -7, 0, sin * -7);
                }
            }
            case PARTICLES_MOBBLOCK_SPAWN -> {
                for (int i = 0; i < 20; ++i) {
                    double px = x - 0.5 + 2 * rnd.nextDouble();
                    double py = y - 0.5 + 2 * rnd.nextDouble();
                    double pz = z - 0.5 + 2 * rnd.nextDouble();
                    level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0, 0);
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0, 0);
                }
            }
            case PARTICLES_PLANT_GROWTH -> BoneMealItem.addGrowthParticles(level, new BlockPos(x, y, z), data);
            case PARTICLES_DRAGON_FIREBALL_SPLASH -> {
                for (int i = 0; i < 200; ++i) {
                    float power = rnd.nextFloat() * 4.0F;
                    float theta = rnd.nextFloat() * Mth.TWO_PI;
                    double dx = Mth.cos(theta) * power;
                    double dy = 0.01 + rnd.nextDouble() * 0.5;
                    double dz = Mth.sin(theta) * power;
                    Particle particle = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, false,
                                                                 x + dx * 0.1, y + 0.3, z + dz * 0.1, dx, dy, dz);
                    if (particle != null) {
                        particle.setPower(power);
                    }
                }
                if (data == 1) {
                    this.playLocalSound(x, y, z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, rnd.nextFloat() * 0.1F + 0.9F
                    );
                }
            }
            case PARTICLES_DRAGON_BLOCK_BREAK -> level.addParticle(ParticleTypes.EXPLOSION, x + 0.5, y + 0.5, z + 0.5, 0, 0, 0);
            case PARTICLES_WATER_EVAPORATING -> {
                for (int i = 0; i < 8; ++i) {
                    level.addParticle(ParticleTypes.CLOUD, x + rnd.nextDouble(), y + 1.2, z + rnd.nextDouble(), 0, 0, 0);
                }
            }
            case ANIMATION_END_GATEWAY_SPAWN -> {
                level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, x + 0.5, y + 0.5, z + 0.5, 0, 0, 0);
                this.playLocalSound(x, y, z, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0F, 0.56f * 0.28f * rnd.nextFloat());
            }
            case ANIMATION_DRAGON_SUMMON_ROAR -> this.playLocalSound(x, y, z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE,
                                                                     64.0F, 0.8F + rnd.nextFloat() * 0.3F);
            case PARTICLES_ELECTRIC_SPARK -> {
                if (data >= 0 && data < Direction.Axis.VALUES.length) {
                    ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[data], level, new BlockPos(x, y, z), 0.125,
                                                          ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
                }
                else {
                    ParticleUtils.spawnParticlesOnBlockFaces(level, new BlockPos(x, y, z), ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                }
            }
            case PARTICLES_AND_SOUND_WAX_ON -> {
                ParticleUtils.spawnParticlesOnBlockFaces(level, new BlockPos(x, y, z), ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.playLocalSound(x, y, z, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            case PARTICLES_WAX_OFF -> ParticleUtils.spawnParticlesOnBlockFaces(level, new BlockPos(x, y, z), ParticleTypes.WAX_OFF,
                                                                               UniformInt.of(3, 5));
            case PARTICLES_SCRAPE -> ParticleUtils.spawnParticlesOnBlockFaces(level, new BlockPos(x, y, z), ParticleTypes.SCRAPE,
                                                                              UniformInt.of(3, 5));
        }
    }

    private void playLocalSound(int x, int y, int z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        assert this.level != null;
        this.level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, sound, source, volume, pitch, false);
    }

    public void playStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable RecordItem disc) {
        long packed = pos.asLong();
        assert this.level != null;
        SoundInstance soundInstance = this.playingRecords.get(packed);
        if (soundInstance != null) {
            this.mc.getSoundManager().stop(soundInstance);
            this.playingRecords.remove(packed);
        }
        if (soundEvent != null) {
            if (disc != null) {
                this.mc.gui.setNowPlaying(disc.getDisplayName());
            }
            SoundInstance newSound = SimpleSoundInstance.forRecord(soundEvent, pos.getX(), pos.getY(), pos.getZ());
            this.playingRecords.put(packed, newSound);
            this.mc.getSoundManager().play(newSound);
        }
        notifyNearbyEntities(this.level, pos, soundEvent != null);
    }

    public void setLevel(@Nullable ClientLevel level) {
        this.level = level;
        if (level == null) {
            this.playingRecords.clear();
            this.playingRecords.trimCollection();
        }
    }
}
