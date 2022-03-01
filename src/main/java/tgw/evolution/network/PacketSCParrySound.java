package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionSounds;

import java.util.function.Supplier;

public class PacketSCParrySound implements IPacket {

    private final boolean success;

    public PacketSCParrySound(boolean success) {
        this.success = success;
    }

    public static PacketSCParrySound decode(FriendlyByteBuf buffer) {
        return new PacketSCParrySound(buffer.readBoolean());
    }

    public static void encode(PacketSCParrySound packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
    }

    public static void handle(PacketSCParrySound packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                SoundEvent sound;
                if (packet.success) {
                    sound = EvolutionSounds.PARRY_SUCCESS.get();
                }
                else {
                    sound = EvolutionSounds.PARRY_FAIL.get();
                }
                Evolution.PROXY.getClientPlayer().playSound(sound, 0.4f, 0.8F + Evolution.PROXY.getClientLevel().random.nextFloat() * 0.4F);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
