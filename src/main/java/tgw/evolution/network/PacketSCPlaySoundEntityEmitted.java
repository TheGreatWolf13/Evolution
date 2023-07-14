package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCPlaySoundEntityEmitted implements Packet<ClientGamePacketListener> {

    public final SoundSource category;
    public final int entityId;
    public final float pitch;
    public final ResourceLocation sound;
    public final float volume;

    public PacketSCPlaySoundEntityEmitted(Entity entity, ResourceLocation sound, SoundSource category, float volume, float pitch) {
        this.entityId = entity.getId();
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public PacketSCPlaySoundEntityEmitted(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.sound = buf.readResourceLocation();
        this.category = buf.readEnum(SoundSource.class);
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handlePlaySoundEntityEmitted(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeResourceLocation(this.sound);
        buf.writeEnum(this.category);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
    }
}
