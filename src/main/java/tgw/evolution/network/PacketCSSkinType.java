package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.EvolutionClient;
import tgw.evolution.patches.PatchServerPacketListener;
import tgw.evolution.util.constants.SkinType;

public class PacketCSSkinType implements Packet<ServerGamePacketListener> {

    public final SkinType skin;

    public PacketCSSkinType(FriendlyByteBuf buf) {
        this.skin = buf.readEnum(SkinType.class);
    }

    public PacketCSSkinType() {
        this.skin = EvolutionClient.getSkinType();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSkinType(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.skin);
    }
}
