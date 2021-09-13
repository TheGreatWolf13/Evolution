package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketSCPlaySoundEntityEmitted implements IPacket {
    @Nonnull
    protected final SoundCategory category;
    protected final int entityId;
    protected final float pitch;
    @Nonnull
    protected final String sound;
    protected final float volume;

    public PacketSCPlaySoundEntityEmitted(int entityId, @Nonnull String sound, @Nonnull SoundCategory category, float volume, float pitch) {
        this.entityId = entityId;
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static PacketSCPlaySoundEntityEmitted decode(PacketBuffer buffer) {
        int entityId = buffer.readVarInt();
        String sound = buffer.readUtf();
        SoundCategory category = buffer.readEnum(SoundCategory.class);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        return new PacketSCPlaySoundEntityEmitted(entityId, sound, category, volume, pitch);
    }

    public static void encode(PacketSCPlaySoundEntityEmitted packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeUtf(packet.sound);
        buffer.writeEnum(packet.category);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
    }

    public static void handle(PacketSCPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            Evolution.PACKET_HANDLER.handlePlaySoundEntityEmitted(packet, context);
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
