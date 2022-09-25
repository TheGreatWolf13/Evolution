package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.util.constants.SkinType;

import java.util.function.Supplier;

public class PacketCSSkinType implements IPacket {

    private final SkinType skin;

    private PacketCSSkinType(SkinType skin) {
        this.skin = skin;
    }

    public PacketCSSkinType() {
        this.skin = Evolution.PROXY.getSkinType();
    }

    public static PacketCSSkinType decode(FriendlyByteBuf buffer) {
        return new PacketCSSkinType(buffer.readEnum(SkinType.class));
    }

    public static void encode(PacketCSSkinType packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.skin);
    }

    public static void handle(PacketCSSkinType packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                Player player = c.getSender();
                assert player != null;
                EntityEvents.SKIN_TYPE.put(player.getUUID(), packet.skin);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
