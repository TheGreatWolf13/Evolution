package tgw.evolution.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketCSPlaySoundEntityEmitted implements IPacket {

    @Nonnull
    private final SoundCategory category;
    private final int entityId;
    private final float pitch;
    @Nonnull
    private final String sound;
    private final float volume;

    public PacketCSPlaySoundEntityEmitted(@Nonnull Entity entity,
                                          @Nonnull SoundEvent sound,
                                          @Nonnull SoundCategory category,
                                          float volume,
                                          float pitch) {
        this(entity.getId(), sound.getRegistryName().toString(), category, volume, pitch);
    }

    private PacketCSPlaySoundEntityEmitted(int entityId, @Nonnull String sound, @Nonnull SoundCategory category, float volume, float pitch) {
        this.entityId = entityId;
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static PacketCSPlaySoundEntityEmitted decode(PacketBuffer buffer) {
        int entityId = buffer.readVarInt();
        String sound = buffer.readUtf();
        SoundCategory category = buffer.readEnum(SoundCategory.class);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        return new PacketCSPlaySoundEntityEmitted(entityId, sound, category, volume, pitch);
    }

    public static void encode(PacketCSPlaySoundEntityEmitted packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeUtf(packet.sound);
        buffer.writeEnum(packet.category);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
    }

    public static void handle(PacketCSPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
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
