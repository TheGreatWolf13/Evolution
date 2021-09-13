package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    private int spawnInvulnerableTime;

    public ServerPlayerEntityMixin(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
        super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
    }

    @Override
    @Shadow
    public abstract boolean canHarmPlayer(PlayerEntity p_96122_1_);

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
                                       !(source instanceof DamageSourceEv && ((DamageSourceEv) source).getType() == EvolutionDamage.Type.IMPACT);
            if (checkForImmunity) {
                return false;
            }
        }
        if (source instanceof EntityDamageSource) {
            Entity entity = source.getEntity();
            if (entity instanceof PlayerEntity && !this.canHarmPlayer((PlayerEntity) entity)) {
                return false;
            }
            if (entity instanceof AbstractArrowEntity) {
                AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity) entity;
                Entity entity1 = abstractarrowentity.getOwner();
                if (entity1 instanceof PlayerEntity && !this.canHarmPlayer((PlayerEntity) entity1)) {
                    return false;
                }
            }
        }
        return super.hurt(source, amount);
    }

    @ModifyArg(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;awardStat" +
                                                                              "(Lnet/minecraft/util/ResourceLocation;)V", ordinal = 0), index = 0)
    private ResourceLocation modifyAwardKillScore0(ResourceLocation resLoc) {
        return EvolutionStats.PLAYER_KILLS;
    }

    @ModifyArg(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;awardStat" +
                                                                              "(Lnet/minecraft/util/ResourceLocation;)V", ordinal = 1), index = 0)
    private ResourceLocation modifyAwardKillScore1(ResourceLocation resLoc) {
        return EvolutionStats.MOB_KILLS;
    }

    @ModifyArg(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;awardStat" +
                                                                    "(Lnet/minecraft/util/ResourceLocation;)V", ordinal = 0), index = 0)
    private ResourceLocation modifyDrop(ResourceLocation resLoc) {
        return EvolutionStats.ITEMS_DROPPED;
    }

    @ModifyArg(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;ifRight" +
                                                                               "(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"
            , ordinal = 0), index = 0)
    private Consumer<? super Unit> modifyStartSleepInBed(Consumer<? super Unit> consumer) {
        return unit -> {
            this.awardStat(EvolutionStats.TIMES_SLEPT);
            CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayerEntity) (Object) this);
        };
    }
}
