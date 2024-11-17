package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.network.PacketSCCustomEntity;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.math.Vec3d;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class MixinServerEntity {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private Vec3 ap;
    @Shadow @Final private Consumer<Packet<?>> broadcast;
    @Shadow @Final private Entity entity;
    @Shadow private List<Entity> lastPassengers;
    @Shadow @Final private ServerLevel level;
    @Shadow private int teleportDelay;
    @Shadow private int tickCount;
    @Shadow @Final private boolean trackDelta;
    @Shadow @Final private int updateInterval;
    @Shadow private boolean wasOnGround;
    @Shadow private boolean wasRiding;
    @Shadow private int xRotp;
    @Shadow private long xp;
    @Shadow private int yHeadRotp;
    @Shadow private int yRotp;
    @Shadow private long yp;
    @Shadow private long zp;

    @Shadow
    protected abstract void broadcastAndSend(Packet<?> pPacket);

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerEntity;ap:Lnet/minecraft/world/phys/Vec3;",
            opcode = Opcodes.PUTFIELD))
    private void onInit(ServerEntity instance, Vec3 value) {
        this.ap = new Vec3d();
    }

    /**
     * @author TheGreatWolf
     * @reason Fix Vec3d leak.
     */
    @Overwrite
    public void sendChanges() {
        List<Entity> list = this.entity.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            this.lastPassengers = list;
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity instanceof ItemFrame itemFrame && this.tickCount % 10 == 0) {
            ItemStack stack = itemFrame.getItem();
            Integer mapId = MapItem.getMapId(stack);
            MapItemSavedData data = MapItem.getSavedData(stack, this.level);
            if (data != null) {
                assert mapId != null;
                for (ServerPlayer serverplayer : this.level.players()) {
                    data.tickCarriedBy(serverplayer, stack);
                    Packet<?> packet = data.getUpdatePacket(mapId, serverplayer);
                    if (packet != null) {
                        serverplayer.connection.send(packet);
                    }
                }
            }
            this.sendDirtyEntityData();
        }
        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            if (this.entity.isPassenger()) {
                int yaw = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int pitch = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                if (Math.abs(yaw - this.yRotp) >= 1 || Math.abs(pitch - this.xRotp) >= 1) {
                    this.broadcast.accept(
                            new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte) yaw, (byte) pitch, this.entity.isOnGround()));
                    this.yRotp = yaw;
                    this.xRotp = pitch;
                }
                this.updateSentPos();
                this.sendDirtyEntityData();
                this.wasRiding = true;
            }
            else {
                ++this.teleportDelay;
                int yaw = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int pitch = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                Vec3 deltaPos = this.entity.position().subtract(ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp));
                boolean isDeltaSignif = deltaPos.lengthSqr() >= 7.629_394_5E-6F;
                Packet<?> packet = null;
                boolean shouldUpdatePos = isDeltaSignif || this.entity.hasImpulse || this.tickCount % 60 == 0;
                boolean shouldUpdateRot = Math.abs(yaw - this.yRotp) >= 1 || Math.abs(pitch - this.xRotp) >= 1;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    long i = ClientboundMoveEntityPacket.entityToPacket(deltaPos.x);
                    long j = ClientboundMoveEntityPacket.entityToPacket(deltaPos.y);
                    long k = ClientboundMoveEntityPacket.entityToPacket(deltaPos.z);
                    boolean outsideRange = i < Short.MIN_VALUE ||
                                           i > Short.MAX_VALUE ||
                                           j < Short.MIN_VALUE ||
                                           j > Short.MAX_VALUE ||
                                           k < Short.MIN_VALUE ||
                                           k > Short.MAX_VALUE;
                    if (!outsideRange && this.teleportDelay <= 400 && !this.wasRiding && this.wasOnGround == this.entity.isOnGround()) {
                        if ((!shouldUpdatePos || !shouldUpdateRot) && !(this.entity instanceof AbstractArrow)) {
                            if (shouldUpdatePos) {
                                packet = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short) (int) i, (short) (int) j,
                                                                             (short) (int) k, this.entity.isOnGround());
                            }
                            else if (shouldUpdateRot) {
                                packet = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte) yaw, (byte) pitch,
                                                                             this.entity.isOnGround());
                            }
                        }
                        else {
                            packet = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short) (int) i, (short) (int) j,
                                                                            (short) (int) k, (byte) yaw, (byte) pitch, this.entity.isOnGround());
                        }
                    }
                    else {
                        this.wasOnGround = this.entity.isOnGround();
                        this.teleportDelay = 0;
                        packet = new ClientboundTeleportEntityPacket(this.entity);
                    }
                }

                if ((this.trackDelta ||
                     this.entity.hasImpulse ||
                     this.entity instanceof LivingEntity living && living.isFallFlying()) && this.tickCount > 0) {
                    Vec3 velocity = this.entity.getDeltaMovement();
                    double delta = velocity.distanceToSqr(this.ap);
                    if (delta > 1.0E-7D || delta > 0 && velocity.lengthSqr() == 0) {
                        ((Vec3d) this.ap).set(velocity);
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                    }
                }
                if (packet != null) {
                    this.broadcast.accept(packet);
                }
                this.sendDirtyEntityData();
                if (shouldUpdatePos) {
                    this.updateSentPos();
                }
                if (shouldUpdateRot) {
                    this.yRotp = yaw;
                    this.xRotp = pitch;
                }
                this.wasRiding = false;
            }
            int yawHead = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
            if (Math.abs(yawHead - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte) yawHead));
                this.yHeadRotp = yawHead;
            }
            this.entity.hasImpulse = false;
        }
        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
            this.entity.hurtMarked = false;
        }
    }

    @Shadow
    protected abstract void sendDirtyEntityData();

    /**
     * @author TheGreatWolf
     * @reason Fix Vec3d leak.
     */
    @Overwrite
    public void sendPairingData(Consumer<Packet<?>> consumer) {
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", this.entity);
        }
        Packet<?> packet = this.entity.getAddEntityPacket();
        this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
        consumer.accept(packet);
        if (!this.entity.getEntityData().isEmpty()) {
            consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.entity.getEntityData(), true));
        }
        boolean trackMovement = this.trackDelta;
        if (this.entity instanceof LivingEntity living) {
            Collection<AttributeInstance> collection = living.getAttributes().getSyncableAttributes();
            if (!collection.isEmpty()) {
                consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), collection));
            }
            if (living.isFallFlying()) {
                trackMovement = true;
            }
        }
        ((Vec3d) this.ap).set(this.entity.getDeltaMovement());
        if (trackMovement && !(packet instanceof ClientboundAddMobPacket || packet instanceof PacketSCCustomEntity)) {
            consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
        }
        if (this.entity instanceof LivingEntity living) {
            OList<Pair<EquipmentSlot, ItemStack>> list = null;
            for (EquipmentSlot slot : AdditionalSlotType.SLOTS) {
                ItemStack stack = living.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    if (list == null) {
                        list = new OArrayList<>();
                    }
                    //noinspection ObjectAllocationInLoop
                    list.add(Pair.of(slot, stack.copy()));
                }
            }
            if (list != null) {
                consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
            }
            R2OMap<MobEffect, MobEffectInstance> effects = (R2OMap<MobEffect, MobEffectInstance>) living.getActiveEffectsMap();
            for (long it = effects.beginIteration(); effects.hasNextIteration(it); it = effects.nextEntry(it)) {
                //noinspection ObjectAllocationInLoop
                consumer.accept(new ClientboundUpdateMobEffectPacket(this.entity.getId(), effects.getIterationValue(it)));
            }
        }
        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity.isPassenger()) {
            assert this.entity.getVehicle() != null;
            consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }
        if (this.entity instanceof Mob mob) {
            if (mob.isLeashed()) {
                consumer.accept(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
            }
        }
    }

    @Shadow
    protected abstract void updateSentPos();
}
