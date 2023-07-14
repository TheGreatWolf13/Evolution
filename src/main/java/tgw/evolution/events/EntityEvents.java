package tgw.evolution.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCAddEffect;
import tgw.evolution.network.PacketSCFixRotation;
import tgw.evolution.network.PacketSCRemoveEffect;
import tgw.evolution.patches.PatchServerPlayer;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.Temperature;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.math.Metric;
import tgw.evolution.util.physics.WindVector;

import java.util.Map;
import java.util.UUID;

public final class EntityEvents {

    public static final WindVector WIND = new WindVector();
    public static final Map<UUID, SkinType> SKIN_TYPE = new O2OHashMap<>();
    private static final double[] LAST_TEMPERATURES = new double[20];

    private EntityEvents() {
    }

    public static void onEffectAdded(LivingEntity entity, @Nullable MobEffectInstance oldInstance, MobEffectInstance newInstance) {
        if (entity instanceof ServerPlayer player) {
            if (oldInstance == null) {
                player.connection.send(new PacketSCAddEffect(newInstance, PacketSCAddEffect.Logic.ADD));
            }
            else {
                MobEffectInstance newEffect = new MobEffectInstance(oldInstance);
                newEffect.update(newInstance);
                boolean isSame = oldInstance.getAmplifier() == newEffect.getAmplifier();
                if (isSame) {
                    player.connection.send(new PacketSCAddEffect(newEffect, PacketSCAddEffect.Logic.UPDATE));
                }
                else {
                    player.connection.send(new PacketSCAddEffect(newEffect, PacketSCAddEffect.Logic.REPLACE));
                }
            }
        }
    }

    public static void onEffectExpired(LivingEntity entity, @Nullable MobEffectInstance effect) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (effect != null) {
            player.connection.send(new PacketSCRemoveEffect(effect.getEffect()));
        }
    }

    public static void onPlayerLogin(Player player) {
        Level level = player.level;
        if (!level.isClientSide && player instanceof ServerPlayer p) {
            for (Player otherPlayer : level.players()) {
                if (otherPlayer != player) {
                    //noinspection ObjectAllocationInLoop
                    p.connection.send(new PacketSCFixRotation(otherPlayer));
                }
            }
            ((ServerLevel) level).getChunkSource().broadcast(player, new PacketSCFixRotation(player));
        }
    }

    public static void onPlayerTickEnd(Player player) {
        ProfilerFiller profiler = player.level.getProfiler();
        profiler.push("postTick");
        profiler.push("stats");
        player.awardStat(EvolutionStats.TIME_PLAYED);
        if (player.getPose() == Pose.CROUCHING) {
            player.setSprinting(false);
            player.awardStat(EvolutionStats.TIME_SNEAKING);
        }
        if (!player.isSleeping()) {
            if (player.isAlive()) {
                player.awardStat(EvolutionStats.TIME_SINCE_LAST_REST);
            }
        }
        else {
            PlayerHelper.takeStat(player, Stats.CUSTOM.get(EvolutionStats.TIME_SINCE_LAST_REST));
        }
        if (player.isAlive()) {
            player.awardStat(EvolutionStats.TIME_SINCE_LAST_DEATH);
        }
        profiler.popPush("status");
        //Handles Status Updates
        if (!player.level.isClientSide) {
            long time = player.level.getDayTime();
            try (Temperature temperature = Temperature.getInstance((ServerLevel) player.level, player.getX(), player.getY(), player.getZ(),
                                                                   time)) {
                LAST_TEMPERATURES[(int) (time % 20)] = Temperature.K2C(temperature.getAmbientBasedTemperature());
            }
            catch (Exception e) {
                Evolution.warn("An exception was thrown while calculating temperature!");
            }
            if (player.isCrouching() && time % 20 == 0) {
                double sum = 0;
                for (double lastTemperature : LAST_TEMPERATURES) {
                    sum += lastTemperature;
                }
                Evolution.info("Average Temperature at {} is {}\u00B0C", time, Metric.TWO_PLACES.format(sum / 20.0));
            }
            //Ticks Player systems
            if (!player.isCreative() && !player.isSpectator()) {
                ServerPlayer sPlayer = (ServerPlayer) player;
                PatchServerPlayer patch = (PatchServerPlayer) player;
                profiler.push("thirst");
                patch.getThirstStats().tick(sPlayer);
                profiler.popPush("health");
                patch.getHealthStats().tick(sPlayer);
                profiler.popPush("hunger");
                patch.getHungerStats().tick(sPlayer);
                profiler.popPush("temperature");
                patch.getTemperatureStats().tick(sPlayer);
                profiler.pop();
            }
        }
        profiler.popPush("water");
        if (!player.level.isClientSide) {
            //Put off torches in Water
            if (player.isEyeInFluid(FluidTags.WATER)) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                boolean torch = false;
                if (mainHand.getItem() == EvolutionItems.TORCH) {
                    int count = mainHand.getCount();
                    player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EvolutionItems.TORCH_UNLIT, count));
                    torch = true;
                }
                if (offHand.getItem() == EvolutionItems.TORCH) {
                    int count = offHand.getCount();
                    player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(EvolutionItems.TORCH_UNLIT, count));
                    torch = true;
                }
                if (torch) {
                    player.level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F,
                                           2.6F + (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.8F);
                }
            }
        }
        profiler.pop();
        profiler.pop();
    }

    public static void onPlayerTickStart(Player player) {
        ProfilerFiller profiler = player.level.getProfiler();
        profiler.push("preTick");
        profiler.push("reach");
        AttributeInstance reachDist = player.getAttribute(EvolutionAttributes.REACH_DISTANCE);
        assert reachDist != null;
        if (player.isCreative()) {
            reachDist.setBaseValue(8);
        }
        else {
            reachDist.setBaseValue(PlayerHelper.REACH_DISTANCE);
        }
        profiler.pop();
        profiler.pop();
    }
}
