package tgw.evolution.network;

import net.minecraftforge.fml.LogicalSide;

public abstract class PacketAbstract {

    public LogicalSide destinationSide;

    public PacketAbstract(LogicalSide destinationSide) {
        this.destinationSide = destinationSide;
    }
}
