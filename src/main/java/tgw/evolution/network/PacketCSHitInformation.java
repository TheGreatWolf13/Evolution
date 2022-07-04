package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.init.EvolutionDamage;

import java.util.function.Supplier;

public class PacketCSHitInformation implements IPacket {

    private final int victimId;

    public PacketCSHitInformation(Entity victim) {
        this(victim.getId());
    }

    private PacketCSHitInformation(int victimId) {
        this.victimId = victimId;
    }

    public static PacketCSHitInformation decode(FriendlyByteBuf buffer) {
        return new PacketCSHitInformation(buffer.readVarInt());
    }

    public static void encode(PacketCSHitInformation packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.victimId);
    }

    public static void handle(PacketCSHitInformation packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                Entity victim = player.level.getEntity(packet.victimId);
                if (victim != null) {
                    victim.hurt(EvolutionDamage.DUMMY, 0);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}