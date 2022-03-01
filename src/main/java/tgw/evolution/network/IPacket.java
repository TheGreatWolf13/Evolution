package tgw.evolution.network;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public interface IPacket {

    static boolean checkSide(IPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide() != packet.getDestinationSide()) {
            Evolution.warn("Received {} on {}!",
                           packet.getClass().getSimpleName(),
                           packet.getDestinationSide().isClient() ? LogicalSide.SERVER : LogicalSide.CLIENT);
            return false;
        }
        return true;
    }

    LogicalSide getDestinationSide();
}
