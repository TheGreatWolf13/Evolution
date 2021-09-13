package tgw.evolution.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.client.audio.SoundEntityEmitted;

import java.util.function.Supplier;

public class PacketHandlerClient implements IPacketHandler {

    @Override
    public void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(packet.entityId);
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(packet.sound));
            if (entity != null && sound != null) {
                mc.getSoundManager().play(new SoundEntityEmitted(entity, sound, packet.category, packet.volume, packet.pitch));
            }
        });
    }
}
