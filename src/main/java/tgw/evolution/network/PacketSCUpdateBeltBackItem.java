package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class PacketSCUpdateBeltBackItem implements Packet<ClientGamePacketListener> {

    public final boolean back;
    public final int entityId;
    public final ItemStack stack;

    public PacketSCUpdateBeltBackItem(Entity entity, boolean back, ItemStack stack) {
        this.stack = stack;
        this.back = back;
        this.entityId = entity.getId();
    }

    public PacketSCUpdateBeltBackItem(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.back = buf.readBoolean();
        this.stack = buf.readItem();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleUpdateBeltBackItem(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.back);
        buf.writeItem(this.stack);
    }
}
