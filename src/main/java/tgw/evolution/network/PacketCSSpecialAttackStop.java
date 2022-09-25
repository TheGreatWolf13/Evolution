package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;

import java.util.function.Supplier;

public class PacketCSSpecialAttackStop implements IPacket {

    private final IMelee.StopReason reason;

    public PacketCSSpecialAttackStop(IMelee.StopReason reason) {
        this.reason = reason;
    }

    public static PacketCSSpecialAttackStop decode(FriendlyByteBuf buf) {
        return new PacketCSSpecialAttackStop(buf.readEnum(IMelee.StopReason.class));
    }

    public static void encode(PacketCSSpecialAttackStop packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.reason);
    }

    public static void handle(PacketCSSpecialAttackStop packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                ((ILivingEntityPatch) player).stopSpecialAttack(packet.reason);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
