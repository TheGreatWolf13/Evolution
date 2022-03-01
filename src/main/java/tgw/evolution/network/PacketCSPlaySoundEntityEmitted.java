package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketCSPlaySoundEntityEmitted implements IPacket {

    @Nonnull
    private final SoundSource category;
    private final int entityId;
    private final float pitch;
    @Nonnull
    private final String sound;
    private final float volume;

    public PacketCSPlaySoundEntityEmitted(@Nonnull EntityAccess entity,
                                          @Nonnull SoundEvent sound,
                                          @Nonnull SoundSource category,
                                          float volume,
                                          float pitch) {
        this(entity.getId(), sound.getRegistryName().toString(), category, volume, pitch);
    }

    private PacketCSPlaySoundEntityEmitted(int entityId, @Nonnull String sound, @Nonnull SoundSource category, float volume, float pitch) {
        this.entityId = entityId;
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static PacketCSPlaySoundEntityEmitted decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        String sound = buffer.readUtf();
        SoundSource category = buffer.readEnum(SoundSource.class);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        return new PacketCSPlaySoundEntityEmitted(entityId, sound, category, volume, pitch);
    }

    public static void encode(PacketCSPlaySoundEntityEmitted packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeUtf(packet.sound);
        buffer.writeEnum(packet.category);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
    }

    public static void handle(PacketCSPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                               new PacketSCPlaySoundEntityEmitted(packet.entityId,
                                                                                  packet.sound,
                                                                                  packet.category,
                                                                                  packet.volume,
                                                                                  packet.pitch));
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
