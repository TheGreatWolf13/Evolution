package tgw.evolution.network;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCSChangeBlock implements IPacket {

    private final BlockPos pos;
    private final Vec3d vec;
    private final Direction direction;
    private final boolean isInside;

    public PacketCSChangeBlock(BlockRayTraceResult result) {
        this.pos = result.getPos();
        this.vec = result.getHitVec();
        this.direction = result.getFace();
        this.isInside = result.isInside();
    }

    public static PacketCSChangeBlock decode(PacketBuffer buffer) {
        return new PacketCSChangeBlock(new BlockRayTraceResult(new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                                                               buffer.readEnumValue(Direction.class),
                                                               buffer.readBlockPos(),
                                                               buffer.readBoolean()));
    }

    public static void encode(PacketCSChangeBlock packet, PacketBuffer buffer) {
        buffer.writeDouble(packet.vec.x);
        buffer.writeDouble(packet.vec.y);
        buffer.writeDouble(packet.vec.z);
        buffer.writeEnumValue(packet.direction);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.isInside);
    }

    public static void handle(PacketCSChangeBlock packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                PlayerEntity player = context.get().getSender();
                Item item = player.getHeldItemMainhand().getItem();
                if (item instanceof BlockItem) {
                    World world = player.world;
                    BlockRayTraceResult result = new BlockRayTraceResult(packet.vec, packet.direction, packet.pos, packet.isInside);
                    ItemUseContext itemContext = new ItemUseContext(player, Hand.MAIN_HAND, result);
                    BlockItemUseContext blockContext = new BlockItemUseContext(itemContext);
                    BlockState state = ((BlockItem) item).getBlock().getStateForPlacement(blockContext);
                    if (state.getBlock() instanceof AbstractButtonBlock) {
                        return;
                    }
                    world.setBlockState(packet.pos, state);
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
