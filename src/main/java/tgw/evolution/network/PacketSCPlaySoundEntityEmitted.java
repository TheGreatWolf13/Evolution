package tgw.evolution.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.client.audio.SoundEntityEmitted;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketSCPlaySoundEntityEmitted implements IPacket {
    @Nonnull
    private final SoundCategory category;
    private final int entityId;
    private final float pitch;
    @Nonnull
    private final String sound;
    private final float volume;

    public PacketSCPlaySoundEntityEmitted(int entityId, @Nonnull String sound, @Nonnull SoundCategory category, float volume, float pitch) {
        this.entityId = entityId;
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static PacketSCPlaySoundEntityEmitted decode(PacketBuffer buffer) {
        int entityId = buffer.readInt();
        String sound = buffer.readString();
        SoundCategory category = buffer.readEnumValue(SoundCategory.class);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        return new PacketSCPlaySoundEntityEmitted(entityId, sound, category, volume, pitch);
    }

    public static void encode(PacketSCPlaySoundEntityEmitted packet, PacketBuffer buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeString(packet.sound);
        buffer.writeEnumValue(packet.category);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
    }

    public static void handle(PacketSCPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                Entity entity = mc.world.getEntityByID(packet.entityId);
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(packet.sound));
                if (entity != null && sound != null) {
                    Minecraft.getInstance()
                             .getSoundHandler()
                             .play(new SoundEntityEmitted(entity, sound, packet.category, packet.volume, packet.pitch));
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
