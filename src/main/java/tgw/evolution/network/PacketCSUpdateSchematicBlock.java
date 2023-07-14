package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;
import tgw.evolution.patches.PatchServerPacketListener;
import tgw.evolution.util.math.MathHelper;

public class PacketCSUpdateSchematicBlock implements Packet<ServerGamePacketListener> {

    public final TESchematic.UpdateCommand command;
    public final boolean ignoresEntities;
    public final float integrity;
    public final Mirror mirror;
    public final SchematicMode mode;
    public final String name;
    public final Rotation rotation;
    public final BlockPos schematicPos;
    public final long seed;
    public final boolean showAir;
    public final boolean showBB;
    public final BlockPos size;
    public final BlockPos tilePos;

    public PacketCSUpdateSchematicBlock(BlockPos tilePos,
                                        TESchematic.UpdateCommand command,
                                        SchematicMode mode,
                                        String name,
                                        BlockPos schematicPos,
                                        BlockPos size,
                                        Mirror mirror,
                                        Rotation rotation,
                                        boolean ignoresEntities,
                                        boolean showAir,
                                        boolean showBB,
                                        float integrity,
                                        long seed) {
        this.tilePos = tilePos;
        this.command = command;
        this.mode = mode;
        this.name = name;
        this.schematicPos = schematicPos;
        this.size = size;
        this.mirror = mirror;
        this.rotation = rotation;
        this.ignoresEntities = ignoresEntities;
        this.showAir = showAir;
        this.showBB = showBB;
        this.integrity = integrity;
        this.seed = seed;
    }

    public PacketCSUpdateSchematicBlock(FriendlyByteBuf buf) {
        this.tilePos = buf.readBlockPos();
        this.command = buf.readEnum(TESchematic.UpdateCommand.class);
        this.mode = buf.readEnum(SchematicMode.class);
        this.name = buf.readUtf();
        this.schematicPos = buf.readBlockPos();
        this.size = buf.readBlockPos();
        this.mirror = buf.readEnum(Mirror.class);
        this.rotation = buf.readEnum(Rotation.class);
        byte flags = buf.readByte();
        this.ignoresEntities = (flags & 1) != 0;
        this.showAir = (flags & 2) != 0;
        this.showBB = (flags & 4) != 0;
        this.integrity = buf.readFloat();
        this.seed = buf.readLong();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleUpdateSchematicBlock(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.tilePos);
        buf.writeEnum(this.command);
        buf.writeEnum(this.mode);
        buf.writeUtf(this.name);
        buf.writeBlockPos(this.schematicPos);
        buf.writeBlockPos(this.size);
        buf.writeEnum(this.mirror);
        buf.writeEnum(this.rotation);
        buf.writeByte(MathHelper.makeFlags(this.ignoresEntities, this.showAir, this.showBB));
        buf.writeFloat(this.integrity);
        buf.writeLong(this.seed);
    }
}
