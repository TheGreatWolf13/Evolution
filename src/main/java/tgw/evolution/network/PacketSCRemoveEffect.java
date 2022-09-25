package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCRemoveEffect implements IPacket {

    private final MobEffect effect;

    public PacketSCRemoveEffect(MobEffect effect) {
        this.effect = effect;
    }

    public static PacketSCRemoveEffect decode(FriendlyByteBuf buffer) {
        MobEffect effect = MobEffect.byId(buffer.readVarInt());
        return new PacketSCRemoveEffect(effect);
    }

    public static void encode(PacketSCRemoveEffect packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(MobEffect.getId(packet.effect));
    }

    public static void handle(PacketSCRemoveEffect packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ClientEvents.removePotionEffect(packet.effect));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
