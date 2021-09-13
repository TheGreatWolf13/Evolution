package tgw.evolution.network;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;

import java.util.function.Supplier;

public class PacketCSUpdateSchematicBlock implements IPacket {

    private final TESchematic.UpdateCommand command;
    private final boolean ignoresEntities;
    private final float integrity;
    private final Mirror mirror;
    private final SchematicMode mode;
    private final String name;
    private final Rotation rotation;
    private final BlockPos schematicPos;
    private final long seed;
    private final boolean showAir;
    private final boolean showBB;
    private final BlockPos size;
    private final BlockPos tilePos;

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

    public static PacketCSUpdateSchematicBlock decode(PacketBuffer buffer) {
        return new PacketCSUpdateSchematicBlock(buffer.readBlockPos(),
                                                buffer.readEnum(TESchematic.UpdateCommand.class),
                                                buffer.readEnum(SchematicMode.class),
                                                buffer.readUtf(),
                                                buffer.readBlockPos(),
                                                buffer.readBlockPos(),
                                                buffer.readEnum(Mirror.class),
                                                buffer.readEnum(Rotation.class),
                                                buffer.readBoolean(),
                                                buffer.readBoolean(),
                                                buffer.readBoolean(),
                                                buffer.readFloat(),
                                                buffer.readLong());
    }

    public static void encode(PacketCSUpdateSchematicBlock packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.tilePos);
        buffer.writeEnum(packet.command);
        buffer.writeEnum(packet.mode);
        buffer.writeUtf(packet.name);
        buffer.writeBlockPos(packet.schematicPos);
        buffer.writeBlockPos(packet.size);
        buffer.writeEnum(packet.mirror);
        buffer.writeEnum(packet.rotation);
        buffer.writeBoolean(packet.ignoresEntities);
        buffer.writeBoolean(packet.showAir);
        buffer.writeBoolean(packet.showBB);
        buffer.writeFloat(packet.integrity);
        buffer.writeLong(packet.seed);
    }

    public static void handle(PacketCSUpdateSchematicBlock packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            PlayerEntity player = context.get().getSender();
            if (player.canUseGameMasterBlocks()) {
                BlockPos tilePos = packet.tilePos;
                BlockState state = player.level.getBlockState(tilePos);
                TileEntity tile = player.level.getBlockEntity(tilePos);
                if (tile instanceof TESchematic) {
                    TESchematic teSchematic = (TESchematic) tile;
                    teSchematic.setMode(packet.mode);
                    teSchematic.setName(packet.name);
                    teSchematic.setSchematicPos(packet.schematicPos);
                    teSchematic.setSize(packet.size);
                    teSchematic.setMirror(packet.mirror);
                    teSchematic.setRotation(packet.rotation);
                    teSchematic.setIgnoresEntities(packet.ignoresEntities);
                    teSchematic.setShowAir(packet.showAir);
                    teSchematic.setShowBoundingBox(packet.showBB);
                    teSchematic.setIntegrity(packet.integrity);
                    teSchematic.setSeed(packet.seed);
                    if (teSchematic.hasName()) {
                        String s = teSchematic.getName();
                        if (packet.command == TESchematic.UpdateCommand.SAVE_AREA) {
                            if (teSchematic.save()) {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.save_success", s), false);
                            }
                            else {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.save_failure", s), false);
                            }
                        }
                        else if (packet.command == TESchematic.UpdateCommand.LOAD_AREA) {
                            if (!teSchematic.isStructureLoadable()) {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.load_not_found", s), false);
                            }
                            else if (teSchematic.load((ServerWorld) context.get().getSender().level)) {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.load_success", s), false);
                            }
                            else {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.load_prepare", s), false);
                            }
                        }
                        else if (packet.command == TESchematic.UpdateCommand.SCAN_AREA) {
                            if (teSchematic.detectSize()) {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.size_success", s), false);
                            }
                            else {
                                player.displayClientMessage(new TranslationTextComponent("structure_block.size_failure"), false);
                            }
                        }
                    }
                    else {
                        player.displayClientMessage(new TranslationTextComponent("structure_block.invalid_structure_name", packet.name), false);
                    }
                    teSchematic.setChanged();
                    player.level.sendBlockUpdated(tilePos, state, state, 3);
                }
            }
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
