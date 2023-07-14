package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSChangeBlock implements Packet<ServerGamePacketListener> {

    public final Direction direction;
    public final boolean isInside;
    public final BlockPos pos;
    public final Vec3 vec;

    public PacketCSChangeBlock(BlockHitResult result) {
        this.pos = result.getBlockPos();
        this.vec = result.getLocation();
        this.direction = result.getDirection();
        this.isInside = result.isInside();
    }

    public PacketCSChangeBlock(FriendlyByteBuf buf) {
        this.vec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.direction = buf.readEnum(Direction.class);
        this.pos = buf.readBlockPos();
        this.isInside = buf.readBoolean();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleChangeBlock(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.vec.x);
        buf.writeDouble(this.vec.y);
        buf.writeDouble(this.vec.z);
        buf.writeEnum(this.direction);
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.isInside);
    }
}
