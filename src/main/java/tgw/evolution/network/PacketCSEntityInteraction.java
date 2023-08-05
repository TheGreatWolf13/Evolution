package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSEntityInteraction implements Packet<ServerGamePacketListener> {

    public final int entityId;
    public final InteractionHand hand;
    public final float hitX;
    public final float hitY;
    public final float hitZ;
    public final boolean secondaryAction;

    public PacketCSEntityInteraction(Entity entity, boolean secondaryAction, InteractionHand hand, double hitX, double hitY, double hitZ) {
        this.entityId = entity.getId();
        this.secondaryAction = secondaryAction;
        this.hand = hand;
        this.hitX = (float) hitX;
        this.hitY = (float) hitY;
        this.hitZ = (float) hitZ;
    }

    public PacketCSEntityInteraction(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.secondaryAction = buf.readBoolean();
        this.hand = buf.readEnum(InteractionHand.class);
        this.hitX = buf.readFloat();
        this.hitY = buf.readFloat();
        this.hitZ = buf.readFloat();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleEntityInteraction(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.secondaryAction);
        buf.writeEnum(this.hand);
        buf.writeFloat(this.hitX);
        buf.writeFloat(this.hitY);
        buf.writeFloat(this.hitZ);
    }
}
