package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCAddEffect implements IPacket {

    private final ClientEffectInstance instance;
    private final Logic logic;

    public PacketSCAddEffect(EffectInstance instance, Logic logic) {
        this.logic = logic;
        this.instance = new ClientEffectInstance(instance);
    }

    private PacketSCAddEffect(ClientEffectInstance instance, Logic logic) {
        this.instance = instance;
        this.logic = logic;
    }

    public static PacketSCAddEffect decode(PacketBuffer buffer) {
        Effect effect = Effect.byId(buffer.readVarInt());
        boolean hasHiddenInstance = true;
        ClientEffectInstance instance = new ClientEffectInstance(effect);
        ClientEffectInstance returnInstance = instance;
        Logic logic = null;
        while (hasHiddenInstance) {
            instance.setAmplifier(buffer.readByte());
            instance.setDuration(buffer.readVarInt());
            byte flag = buffer.readByte();
            logic = Logic.byId(flag & 0b11);
            instance.setAmbient((flag & 4) != 0);
            instance.setInfinite((flag & 8) != 0);
            instance.setShowIcon((flag & 16) != 0);
            hasHiddenInstance = (flag & 32) != 0;
            if (hasHiddenInstance) {
                //noinspection ObjectAllocationInLoop
                instance.setHiddenInstance(new ClientEffectInstance(effect));
                instance = instance.getHiddenInstance();
            }
        }
        return new PacketSCAddEffect(returnInstance, logic);
    }

    public static void encode(PacketSCAddEffect packet, PacketBuffer buffer) {
        buffer.writeVarInt(Effect.getId(packet.instance.getEffect()));
        ClientEffectInstance instance = packet.instance;
        boolean hasHiddenInstance = true;
        while (hasHiddenInstance) {
            byte flag = packet.logic.getFlag();
            buffer.writeByte(instance.getAmplifier());
            buffer.writeVarInt(instance.getDuration());
            flag |= (instance.isAmbient() ? 1 : 0) << 2;
            flag |= (instance.isInfinite() ? 1 : 0) << 3;
            flag |= (instance.isShowIcon() ? 1 : 0) << 4;
            hasHiddenInstance = instance.hasHiddenInstance();
            flag |= (hasHiddenInstance ? 1 : 0) << 5;
            buffer.writeByte(flag);
            instance = instance.getHiddenInstance();
        }
    }

    public static void handle(PacketSCAddEffect packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().onPotionAdded(packet.instance, packet.logic));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }

    public enum Logic {
        ADD,
        REPLACE,
        UPDATE;

        public static Logic byId(int id) {
            switch (id) {
                case 0:
                    return ADD;
                case 1:
                    return REPLACE;
                case 2:
                    return UPDATE;
            }
            throw new IllegalStateException("Unknown Logic Id: " + id);
        }

        public byte getFlag() {
            switch (this) {
                case ADD:
                    return 0;
                case REPLACE:
                    return 1;
                case UPDATE:
                    return 2;
            }
            throw new IllegalStateException("Unknown Logic: " + this);
        }
    }
}
