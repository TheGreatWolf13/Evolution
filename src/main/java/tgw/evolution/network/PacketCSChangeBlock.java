//package tgw.evolution.network;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.protocol.game.ServerGamePacketListener;
//import net.minecraft.world.phys.BlockHitResult;
//import tgw.evolution.patches.PatchServerPacketListener;
//
//public class PacketCSChangeBlock implements Packet<ServerGamePacketListener> {
//
//    public final Direction direction;
//    public final double hitX;
//    public final double hitY;
//    public final double hitZ;
//    public final boolean isInside;
//    public final long pos;
//
//    public PacketCSChangeBlock(BlockHitResult result) {
//        this.pos = BlockPos.asLong(result.posX(), result.posY(), result.posZ());
//        this.hitX = result.x();
//        this.hitY = result.y();
//        this.hitZ = result.z();
//        this.direction = result.getDirection();
//        this.isInside = result.isInside();
//    }
//
//    public PacketCSChangeBlock(FriendlyByteBuf buf) {
//        this.hitX = buf.readDouble();
//        this.hitY = buf.readDouble();
//        this.hitZ = buf.readDouble();
//        this.direction = buf.readEnum(Direction.class);
//        this.pos = buf.readLong();
//        this.isInside = buf.readBoolean();
//    }
//
//    @Override
//    public void handle(ServerGamePacketListener listener) {
//        ((PatchServerPacketListener) listener).handleChangeBlock(this);
//    }
//
//    @Override
//    public void write(FriendlyByteBuf buf) {
//        buf.writeDouble(this.hitX);
//        buf.writeDouble(this.hitY);
//        buf.writeDouble(this.hitZ);
//        buf.writeEnum(this.direction);
//        buf.writeLong(this.pos);
//        buf.writeBoolean(this.isInside);
//    }
//}
