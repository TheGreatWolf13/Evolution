package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSUpdatePuzzle implements Packet<ServerGamePacketListener> {

    public final ResourceLocation attachmentType;
    public final boolean checkBB;
    public final String finalState;
    public final long pos;
    public final ResourceLocation targetPool;

    public PacketCSUpdatePuzzle(long pos, ResourceLocation attachmentType, ResourceLocation targetPool, String finalState, boolean checkBB) {
        this.pos = pos;
        this.attachmentType = attachmentType;
        this.targetPool = targetPool;
        this.finalState = finalState;
        this.checkBB = checkBB;
    }

    public PacketCSUpdatePuzzle(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.attachmentType = buf.readResourceLocation();
        this.targetPool = buf.readResourceLocation();
        this.finalState = buf.readUtf();
        this.checkBB = buf.readBoolean();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleUpdatePuzzle(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeResourceLocation(this.attachmentType);
        buf.writeResourceLocation(this.targetPool);
        buf.writeUtf(this.finalState);
        buf.writeBoolean(this.checkBB);
    }
}
