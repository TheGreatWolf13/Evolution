package tgw.evolution.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftPatch;

import java.util.function.Supplier;

public class PacketHandlerClient implements IPacketHandler {

    @Override
    public void handleMultiplayerPause(boolean paused) {
        boolean wasPaused = ((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused();
        if (wasPaused == paused) {
            return;
        }
        if (paused) {
            Minecraft.getInstance().player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_PAUSE_INFO, false);
            Evolution.LOGGER.info("Pausing Client due to Multiplayer Pause");
        }
        else {
            Minecraft.getInstance().player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_RESUME_INFO, false);
            Evolution.LOGGER.info("Resuming Client due to Multiplayer Resume");
        }
        ((IMinecraftPatch) Minecraft.getInstance()).setMultiplayerPaused(paused);
    }

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
