package tgw.evolution.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.util.ConfigHelper;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

public class PacketCSSyncServerConfig implements IPacket {

    private final byte[] data;
    private final String filename;

    public PacketCSSyncServerConfig(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public static PacketCSSyncServerConfig decode(FriendlyByteBuf buffer) {
        return new PacketCSSyncServerConfig(buffer.readUtf(), buffer.readByteArray());
    }

    public static void encode(PacketCSSyncServerConfig packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.filename);
        buffer.writeByteArray(packet.data);
    }

    public static void handle(PacketCSSyncServerConfig packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (!player.hasPermissions(player.server.getOperatorUserPermissionLevel())) {
                    Evolution.warn("{} tried to update server config without operator status", player.getName().getString());
                    return;
                }
                Evolution.debug("Received server config sync from player: {}", player.getName().getString());
                ModConfig config = ConfigHelper.getModConfig(packet.filename);
                if (config != null) {
                    CommentedConfig data = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(packet.data));
                    config.getConfigData().putAll(data);
                    ConfigHelper.resetCache(config);
                    EvolutionNetwork.INSTANCE.send(PacketDistributor.ALL.with(() -> null),
                                                   new PacketSCSyncServerConfig(packet.filename, packet.data));
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
