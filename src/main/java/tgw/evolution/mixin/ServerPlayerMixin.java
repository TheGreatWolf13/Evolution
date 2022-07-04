package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.util.damage.DamageSourceEv;

import java.util.function.Consumer;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    private int spawnInvulnerableTime;

    public ServerPlayerMixin(Level level, BlockPos pos, float spawnAngle, GameProfile profile) {
        super(level, pos, spawnAngle, profile);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to overcome respawnInvulnerabilityTime
     */
    @Overwrite
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.spawnInvulnerableTime > 0) {
            boolean checkForImmunity = !source.isBypassInvul() &&
                                       !(source instanceof DamageSourceEv evDamSource && evDamSource.getType() == EvolutionDamage.Type.IMPACT);
            if (checkForImmunity) {
                return false;
            }
        }
        if (source instanceof EntityDamageSource) {
            Entity entity = source.getEntity();
            if (entity instanceof Player player && !this.canHarmPlayer(player)) {
                return false;
            }
            if (entity instanceof AbstractArrow arrow) {
                Entity owner = arrow.getOwner();
                if (owner instanceof Player player && !this.canHarmPlayer(player)) {
                    return false;
                }
            }
        }
        return super.hurt(source, amount);
    }

    @ModifyArg(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat" +
                                                                              "(Lnet/minecraft/resources/ResourceLocation;)V", ordinal = 0), index
            = 0)
    private ResourceLocation modifyAwardKillScore0(ResourceLocation resLoc) {
        return EvolutionStats.PLAYER_KILLS;
    }

    @ModifyArg(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat" +
                                                                              "(Lnet/minecraft/resources/ResourceLocation;)V", ordinal = 1), index
            = 0)
    private ResourceLocation modifyAwardKillScore1(ResourceLocation resLoc) {
        return EvolutionStats.MOB_KILLS;
    }

    @ModifyArg(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE",
            target =
            "Lnet/minecraft/server/level/ServerPlayer;awardStat" +
            "(Lnet/minecraft/resources/ResourceLocation;)V", ordinal = 0), index = 0)
    private ResourceLocation modifyDrop(ResourceLocation resLoc) {
        return EvolutionStats.ITEMS_DROPPED;
    }

    @ModifyArg(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;ifRight" +
                                                                               "(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"
            , ordinal = 0), index = 0)
    private Consumer<? super Unit> modifyStartSleepInBed(Consumer<? super Unit> consumer) {
        return unit -> {
            this.awardStat(EvolutionStats.TIMES_SLEPT);
            CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayer) (Object) this);
        };
    }
}