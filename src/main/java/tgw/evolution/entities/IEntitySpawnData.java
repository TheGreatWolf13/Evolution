package tgw.evolution.entities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import tgw.evolution.network.PacketSCCustomEntity;

public interface IEntitySpawnData {

    static <T extends Entity & IEntitySpawnData> void readData(T entity, PacketSCCustomEntity<T> packet) {
        entity.setId(packet.id);
        entity.setPacketCoordinates(packet.x, packet.y, packet.z);
        entity.moveTo(packet.x, packet.y, packet.z);
        entity.setXRot(packet.xRot);
        entity.setYRot(packet.yRot);
        entity.setUUID(packet.uuid);
        packet.data.read(entity);
    }

    EntityData getSpawnData();

    abstract class LivingData<T extends LivingEntity & IEntitySpawnData> extends EntityData<T> {

        private final byte yHeadRot;

        public LivingData(T living) {
            this.yHeadRot = (byte) Mth.floor(living.getYHeadRot() * 256.0F / 360.0F);
        }

        public LivingData(FriendlyByteBuf buf) {
            this.yHeadRot = buf.readByte();
        }

        @Override
        @MustBeInvokedByOverriders
        public void read(T entity) {
            entity.setYHeadRot(this.yHeadRot * 360.0f / 256);
        }

        @Override
        @MustBeInvokedByOverriders
        public void writeToBuffer(FriendlyByteBuf buf) {
            buf.writeByte(this.yHeadRot);
        }
    }

    abstract class EntityData<T extends Entity & IEntitySpawnData> {

        public abstract void read(T entity);

        public abstract void writeToBuffer(FriendlyByteBuf buf);
    }
}
