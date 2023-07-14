package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSUpdateBeltBackItem implements Packet<ServerGamePacketListener> {

    public final boolean back;
    public final ItemStack stack;

    public PacketCSUpdateBeltBackItem(ItemStack stack, boolean back) {
        this.stack = stack;
        this.back = back;
    }

    public PacketCSUpdateBeltBackItem(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.back = buf.readBoolean();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleUpdateBeltBackItem(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeBoolean(this.back);
    }
}
