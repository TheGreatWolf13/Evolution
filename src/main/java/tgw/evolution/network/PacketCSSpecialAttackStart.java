package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;

import java.util.function.Supplier;

public class PacketCSSpecialAttackStart implements IPacket {

    private final IMelee.IAttackType type;

    public PacketCSSpecialAttackStart(IMelee.IAttackType type) {
        this.type = type;
    }

    public static PacketCSSpecialAttackStart decode(FriendlyByteBuf buf) {
        return new PacketCSSpecialAttackStart(IMelee.IAttackType.decode(buf));
    }

    public static void encode(PacketCSSpecialAttackStart packet, FriendlyByteBuf buf) {
        packet.type.encode(buf);
    }

    public static void handle(PacketCSSpecialAttackStart packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                ((ILivingEntityPatch) player).startSpecialAttack(packet.type);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
