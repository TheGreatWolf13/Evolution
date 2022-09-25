package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.blocks.ICollisionBlock;
import tgw.evolution.init.EvolutionAttributes;

import java.util.function.Supplier;

public class PacketCSCollision implements IPacket {

    private final Direction.Axis axis;
    private final long packedPos;
    private final double speed;

    public PacketCSCollision(BlockPos pos, double speed, Direction.Axis axis) {
        this(pos.asLong(), speed, axis);
    }

    private PacketCSCollision(long packedPos, double speed, Direction.Axis axis) {
        this.packedPos = packedPos;
        this.speed = speed;
        this.axis = axis;
    }

    public static PacketCSCollision decode(FriendlyByteBuf buffer) {
        return new PacketCSCollision(buffer.readVarLong(), buffer.readDouble(), buffer.readEnum(Direction.Axis.class));
    }

    public static void encode(PacketCSCollision packet, FriendlyByteBuf buffer) {
        buffer.writeVarLong(packet.packedPos);
        buffer.writeDouble(packet.speed);
        buffer.writeEnum(packet.axis);
    }

    public static void handle(PacketCSCollision packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                Level level = player.level;
                BlockPos pos = BlockPos.of(packet.packedPos);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof ICollisionBlock collisionBlock) {
                    double mass = player.getAttributeValue(EvolutionAttributes.MASS.get());
                    collisionBlock.collision(level, pos, player, packet.speed, mass, packet.axis);
                }
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
