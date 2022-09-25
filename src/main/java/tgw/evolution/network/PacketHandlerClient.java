package tgw.evolution.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.ConfigHelper;

import java.io.ByteArrayInputStream;

public class PacketHandlerClient implements IPacketHandler {

    @Override
    public void handleMultiplayerPause(boolean paused) {
        Minecraft mc = Minecraft.getInstance();
        boolean wasPaused = ((IMinecraftPatch) mc).isMultiplayerPaused();
        if (wasPaused == paused) {
            return;
        }
        LocalPlayer player = mc.player;
        assert player != null;
        if (paused) {
            player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_PAUSE_INFO, false);
            Evolution.info("Pausing Client due to Multiplayer Pause");
        }
        else {
            player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_RESUME_INFO, false);
            Evolution.info("Resuming Client due to Multiplayer Resume");
        }
        ((IMinecraftPatch) mc).setMultiplayerPaused(paused);
    }

    @Override
    public void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Entity entity = Evolution.PROXY.getClientLevel().getEntity(packet.entityId);
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(packet.sound));
            if (entity != null && sound != null) {
                Minecraft.getInstance().getSoundManager().play(new SoundEntityEmitted(entity, sound, packet.category, packet.volume, packet.pitch));
            }
        });
    }

    @Override
    public void handleSyncServerConfig(String filename, byte[] data) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Evolution.info("Received config sync from server");
            ModConfig config = ConfigHelper.getModConfig(filename);
            if (config != null) {
                CommentedConfig commentedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data));
                ConfigHelper.setConfigData(config, commentedConfig);
                ConfigHelper.fireEvent(config, new ModConfigEvent.Reloading(config));
            }
        }
    }
}
