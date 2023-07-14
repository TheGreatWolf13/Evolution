package tgw.evolution.entities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.network.PacketSCCustomEntity;

public interface IEntityPacket<T extends Entity & IEntityPacket<T>> {

    void readAdditionalSyncData(FriendlyByteBuf buf);

    /**
     * DO NOT OVERRIDE.
     */
    default void readData(T entity, PacketSCCustomEntity<T> packet) {
        entity.setId(packet.id);
        entity.setPacketCoordinates(packet.x, packet.y, packet.z);
        entity.moveTo(packet.x, packet.y, packet.z);
        entity.setXRot(packet.xRot);
        entity.setYRot(packet.yRot);
        entity.setUUID(packet.uuid);
        FriendlyByteBuf buf = packet.getBuffer();
        if (entity instanceof LivingEntity living) {
            living.setYHeadRot(buf.readByte() * 360.0f / 256);
        }
        this.readAdditionalSyncData(buf);
    }

    void writeAdditionalSyncData(FriendlyByteBuf buf);

    /**
     * DO NOT OVERRIDE.
     */
    default void writeData(T entity, FriendlyByteBuf buf) {
        if (entity instanceof LivingEntity living) {
            buf.writeByte(Mth.floor(living.getYHeadRot() * 256.0F / 360.0F));
        }
        this.writeAdditionalSyncData(buf);
    }
}
