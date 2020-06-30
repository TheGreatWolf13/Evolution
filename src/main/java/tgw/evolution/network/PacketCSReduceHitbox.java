package tgw.evolution.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSReduceHitbox extends PacketAbstract {

    public static final EntitySize SIZE = EntitySize.flexible(0.6F, 0.6F);

    public PacketCSReduceHitbox() {
        super(LogicalSide.SERVER);
    }

    public static void encode(PacketCSReduceHitbox packet, PacketBuffer buffer) {
    }

    public static PacketCSReduceHitbox decode(PacketBuffer buffer) {
        return new PacketCSReduceHitbox();
    }

    public static void handle(PacketCSReduceHitbox packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> ObfuscationReflectionHelper.setPrivateValue(Entity.class, context.get().getSender(), SIZE, "field_77280_f"));
            context.get().setPacketHandled(true);
        }
    }
}
