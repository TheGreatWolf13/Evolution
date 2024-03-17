package tgw.evolution.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchLevel;
import tgw.evolution.util.math.Vec3d;

import java.util.Set;

@Mixin(TeleportCommand.class)
public abstract class MixinTeleportCommand {

    @Shadow @Final private static SimpleCommandExceptionType INVALID_POSITION;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static void performTeleport(CommandSourceStack sourceStack, Entity entity, ServerLevel level, double x0, double y0, double z0, Set<ClientboundPlayerPositionPacket.RelativeArgument> args, float yaw, float pitch, @Nullable TeleportCommand.LookAt lookAt) throws CommandSyntaxException {
        int x = Mth.floor(x0);
        int y = Mth.floor(y0);
        int z = Mth.floor(z0);
        if (!PatchLevel.isInSpawnableBounds_(x, y, z)) {
            throw INVALID_POSITION.create();
        }
        float wrappedYaw = Mth.wrapDegrees(yaw);
        float wrappedPitch = Mth.wrapDegrees(pitch);
        if (entity instanceof ServerPlayer player) {
            long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            level.getChunkSource().addRegionTicket_(TicketType.POST_TELEPORT, chunkPos, 1, entity.getId());
            entity.stopRiding();
            if (player.isSleeping()) {
                player.stopSleepInBed(true, true);
            }
            if (level == entity.level) {
                player.connection.teleport(x, y, z, wrappedYaw, wrappedPitch, args);
            }
            else {
                player.teleportTo(level, x, y, z, wrappedYaw, wrappedPitch);
            }
            entity.setYHeadRot(wrappedYaw);
        }
        else {
            float k = Mth.clamp(wrappedPitch, -90.0F, 90.0F);
            if (level == entity.level) {
                entity.moveTo(x, y, z, wrappedYaw, k);
                entity.setYHeadRot(wrappedYaw);
            }
            else {
                entity.unRide();
                Entity oldEntity = entity;
                entity = entity.getType().create(level);
                if (entity == null) {
                    return;
                }
                entity.restoreFrom(oldEntity);
                entity.moveTo(x, y, z, wrappedYaw, k);
                entity.setYHeadRot(wrappedYaw);
                oldEntity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                level.addDuringTeleport(entity);
            }
        }
        if (lookAt != null) {
            lookAt.perform(sourceStack, entity);
        }
        if (!(entity instanceof LivingEntity living) || !living.isFallFlying()) {
            entity.setDeltaMovement(((Vec3d) entity.getDeltaMovement()).multiplyMutable(1, 0, 1));
            entity.setOnGround(true);
        }
        if (entity instanceof PathfinderMob mob) {
            mob.getNavigation().stop();
        }
    }
}
