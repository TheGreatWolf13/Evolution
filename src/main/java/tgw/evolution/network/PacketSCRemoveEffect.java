package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCRemoveEffect implements Packet<ClientGamePacketListener> {

    public final @Nullable MobEffect effect;

    public PacketSCRemoveEffect(MobEffect effect) {
        this.effect = effect;
    }

    public PacketSCRemoveEffect(FriendlyByteBuf buf) {
        this.effect = MobEffect.byId(buf.readVarInt());
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleRemoveEffect(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        assert this.effect != null;
        buf.writeVarInt(MobEffect.getId(this.effect));
    }
}
