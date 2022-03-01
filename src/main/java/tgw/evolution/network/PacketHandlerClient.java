package tgw.evolution.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.ConfigHelper;

import java.io.ByteArrayInputStream;
import java.util.Optional;
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
            Evolution.info("Pausing Client due to Multiplayer Pause");
        }
        else {
            Minecraft.getInstance().player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_RESUME_INFO, false);
            Evolution.info("Resuming Client due to Multiplayer Resume");
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

    @Override
    public void handleSyncServerConfig(String filename, byte[] data) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Evolution.info("Received config sync from server");
            Optional.ofNullable(ConfigHelper.getModConfig(filename)).ifPresent(config -> {
                CommentedConfig commentedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data));
                ConfigHelper.setConfigData(config, commentedConfig);
                ConfigHelper.fireEvent(config, new ModConfigEvent.Reloading(config));
            });
        }
    }
}
