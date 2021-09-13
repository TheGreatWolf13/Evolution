package tgw.evolution.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.util.SkinType;

import java.util.function.Supplier;

public class PacketCSSkinType implements IPacket {

    private final SkinType skin;

    private PacketCSSkinType(SkinType skin) {
        this.skin = skin;
    }

    public PacketCSSkinType() {
        this.skin = Evolution.PROXY.getSkinType();
    }

    public static PacketCSSkinType decode(PacketBuffer buffer) {
        return new PacketCSSkinType(buffer.readEnum(SkinType.class));
    }

    public static void encode(PacketCSSkinType packet, PacketBuffer buffer) {
        buffer.writeEnum(packet.skin);
    }

    public static void handle(PacketCSSkinType packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                PlayerEntity player = context.get().getSender();
                EntityEvents.SKIN_TYPE.put(player.getUUID(), packet.skin);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
