package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.util.math.MathHelper;

public class PacketSCAddEffect implements Packet<ClientGamePacketListener> {

    public final ClientEffectInstance instance;
    public final Logic logic;

    public PacketSCAddEffect(MobEffectInstance instance, Logic logic) {
        this.logic = logic;
        this.instance = new ClientEffectInstance(instance);
    }

    public PacketSCAddEffect(FriendlyByteBuf buf) {
        this.logic = buf.readEnum(Logic.class);
        MobEffect effect = MobEffect.byId(buf.readVarInt());
        assert effect != null;
        this.instance = new ClientEffectInstance(effect);
        ClientEffectInstance instance = this.instance;
        boolean hasHiddenInstance = true;
        while (hasHiddenInstance) {
            instance.setAmplifier(buf.readByte());
            instance.setDuration(buf.readVarInt());
            byte flags = buf.readByte();
            instance.setAmbient((flags & 1) != 0);
            instance.setInfinite((flags & 2) != 0);
            instance.setShowIcon((flags & 4) != 0);
            if ((flags & 8) != 0) {
                //noinspection ObjectAllocationInLoop
                ClientEffectInstance hidden = new ClientEffectInstance(effect);
                instance.setHiddenInstance(hidden);
                instance = hidden;
            }
            else {
                hasHiddenInstance = false;
            }
        }
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleAddEffect(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.logic);
        buf.writeVarInt(MobEffect.getId(this.instance.getEffect()));
        ClientEffectInstance instance = this.instance;
        boolean hasHiddenInstance = true;
        while (hasHiddenInstance) {
            buf.writeByte(instance.getAmplifier());
            buf.writeVarInt(instance.getDuration());
            ClientEffectInstance hidden = instance.getHiddenInstance();
            hasHiddenInstance = hidden != null;
            buf.writeByte(MathHelper.makeFlags(instance.isAmbient(), instance.isInfinite(), instance.isShowIcon(), hasHiddenInstance));
            instance = hidden;
        }
    }

    public enum Logic {
        ADD,
        REPLACE,
        UPDATE
    }
}
