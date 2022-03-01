package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCFixRotation implements IPacket {

    private final int entityId;
    private final byte xRot;
    private final byte yHeadRot;
    private final byte yRot;

    private PacketSCFixRotation(int entityId, byte xRot, byte yRot, byte yHeadRot) {
        this.yRot = yRot;
        this.xRot = xRot;
        this.yHeadRot = yHeadRot;
        this.entityId = entityId;
    }

    public PacketSCFixRotation(Entity entity) {
        this.entityId = entity.getId();
        this.xRot = (byte) Mth.floor(entity.getXRot() * 256.0f / 360.0f);
        this.yRot = (byte) Mth.floor(entity.getYRot() * 256.0f / 360.0f);
        this.yHeadRot = (byte) Mth.floor(entity.getYHeadRot() * 256.0f / 360.0f);
    }

    public static PacketSCFixRotation decode(FriendlyByteBuf buffer) {
        return new PacketSCFixRotation(buffer.readVarInt(), buffer.readByte(), buffer.readByte(), buffer.readByte());
    }

    public static void encode(PacketSCFixRotation packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeByte(packet.xRot);
        buffer.writeByte(packet.yRot);
        buffer.writeByte(packet.yHeadRot);
    }

    public static void handle(PacketSCFixRotation packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Level level = Evolution.PROXY.getClientLevel();
                Entity entity = level.getEntity(packet.entityId);
                if (entity != null) {
                    float yRot = (packet.yRot * 360) / 256.0F;
                    float xRot = (packet.xRot * 360) / 256.0f;
                    entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), yRot, xRot, 3, false);
                    float yHeadRot = (packet.yHeadRot * 360) / 256.0f;
                    entity.lerpHeadTo(yHeadRot, 3);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
