package tgw.evolution.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.PatchMinecraft;

public class PacketHandlerClient implements IPacketHandler {

    @Override
    public void handleMultiplayerPause(boolean paused) {
        Minecraft mc = Minecraft.getInstance();
        boolean wasPaused = ((PatchMinecraft) mc).isMultiplayerPaused();
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
        ((PatchMinecraft) mc).setMultiplayerPaused(paused);
    }

    @Override
    public void handleSyncServerConfig(String filename, byte[] data) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Evolution.info("Received config sync from server");
//            ModConfig config = ConfigHelper.getModConfig(filename);
//            if (config != null) {
//                CommentedConfig commentedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data));
//                ConfigHelper.setConfigData(config, commentedConfig);
//                ConfigHelper.fireEvent(config, new ModConfigEvent.Reloading(config));
//            }
        }
    }
}
