package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCSChangeBlock implements IPacket {

    private final Direction direction;
    private final boolean isInside;
    private final BlockPos pos;
    private final Vec3 vec;

    public PacketCSChangeBlock(BlockHitResult result) {
        this.pos = result.getBlockPos();
        this.vec = result.getLocation();
        this.direction = result.getDirection();
        this.isInside = result.isInside();
    }

    public PacketCSChangeBlock(Vec3 vec, Direction direction, BlockPos pos, boolean isInside) {
        this.direction = direction;
        this.isInside = isInside;
        this.pos = pos;
        this.vec = vec;
    }

    public static PacketCSChangeBlock decode(FriendlyByteBuf buffer) {
        return new PacketCSChangeBlock(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                                       buffer.readEnum(Direction.class),
                                       buffer.readBlockPos(),
                                       buffer.readBoolean());
    }

    public static void encode(PacketCSChangeBlock packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.vec.x);
        buffer.writeDouble(packet.vec.y);
        buffer.writeDouble(packet.vec.z);
        buffer.writeEnum(packet.direction);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.isInside);
    }

    public static void handle(PacketCSChangeBlock packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Player player = context.get().getSender();
                Item item = player.getMainHandItem().getItem();
                if (item instanceof BlockItem blockItem) {
                    Level level = player.level;
                    BlockHitResult result = new BlockHitResult(packet.vec, packet.direction, packet.pos, packet.isInside);
                    UseOnContext itemContext = new UseOnContext(player, InteractionHand.MAIN_HAND, result);
                    BlockPlaceContext blockContext = new BlockPlaceContext(itemContext);
                    BlockState state = blockItem.getBlock().getStateForPlacement(blockContext);
                    if (state.getBlock() instanceof ButtonBlock) {
                        return;
                    }
                    level.setBlockAndUpdate(packet.pos, state);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
