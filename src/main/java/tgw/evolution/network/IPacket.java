package tgw.evolution.network;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

public interface IPacket {

    static boolean checkSide(IPacket packet, NetworkEvent.Context context) {
        if (context.getDirection().getReceptionSide() != packet.getDestinationSide()) {
            Evolution.warn("Received {} on {}!",
                           packet.getClass().getSimpleName(),
                           packet.getDestinationSide().isClient() ? LogicalSide.SERVER : LogicalSide.CLIENT);
            return false;
        }
        return true;
    }

    LogicalSide getDestinationSide();
}
