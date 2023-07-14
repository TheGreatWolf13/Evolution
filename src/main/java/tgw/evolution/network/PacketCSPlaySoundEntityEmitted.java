package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSPlaySoundEntityEmitted implements Packet<ServerGamePacketListener> {

    public final SoundSource category;
    public final float pitch;
    public final ResourceLocation sound;
    public final float volume;

    public PacketCSPlaySoundEntityEmitted(SoundEvent sound, SoundSource category, float volume, float pitch) {
        this.sound = sound.getLocation();
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public PacketCSPlaySoundEntityEmitted(FriendlyByteBuf buf) {
        this.sound = buf.readResourceLocation();
        this.category = buf.readEnum(SoundSource.class);
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handlePlaySoundEntityEmitted(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.sound);
        buf.writeEnum(this.category);
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
    }
}
