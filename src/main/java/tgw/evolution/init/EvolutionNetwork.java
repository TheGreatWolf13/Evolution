package tgw.evolution.init;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tgw.evolution.Evolution;
import tgw.evolution.network.*;

import java.util.function.Supplier;

public class EvolutionNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(Evolution.location("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int id;

    private static int increaseId() {
        id++;
        return id;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(increaseId(), PacketSCUpdateChunkStorage.class, PacketSCUpdateChunkStorage::encode, PacketSCUpdateChunkStorage::decode, PacketSCUpdateChunkStorage::handle);
        INSTANCE.registerMessage(increaseId(), PacketSCHandAnimation.class, PacketSCHandAnimation::encode, PacketSCHandAnimation::decode, PacketSCHandAnimation::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSOpenExtendedInventory.class, PacketCSOpenExtendedInventory::encode, PacketCSOpenExtendedInventory::decode, PacketCSOpenExtendedInventory::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSOffhandAttack.class, PacketCSOffhandAttack::encode, PacketCSOffhandAttack::decode, PacketCSOffhandAttack::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSSetKnappingType.class, PacketCSSetKnappingType::encode, PacketCSSetKnappingType::decode, PacketCSSetKnappingType::handle);
        INSTANCE.registerMessage(increaseId(), PacketSCOpenKnappingGui.class, PacketSCOpenKnappingGui::encode, PacketSCOpenKnappingGui::decode, PacketSCOpenKnappingGui::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSSetProne.class, PacketCSSetProne::encode, PacketCSSetProne::decode, PacketCSSetProne::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSReduceHitbox.class, PacketCSReduceHitbox::encode, PacketCSReduceHitbox::decode, PacketCSReduceHitbox::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSUpdatePuzzle.class, PacketCSUpdatePuzzle::encode, PacketCSUpdatePuzzle::decode, PacketCSUpdatePuzzle::handle);
        INSTANCE.registerMessage(increaseId(), PacketSCOpenMoldingGui.class, PacketSCOpenMoldingGui::encode, PacketSCOpenMoldingGui::decode, PacketSCOpenMoldingGui::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSSetMoldingType.class, PacketCSSetMoldingType::encode, PacketCSSetMoldingType::decode, PacketCSSetMoldingType::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSUpdateSchematicBlock.class, PacketCSUpdateSchematicBlock::encode, PacketCSUpdateSchematicBlock::decode, PacketCSUpdateSchematicBlock::handle);
    }

    public static boolean checkSide(Supplier<NetworkEvent.Context> context, PacketAbstract packet) {
        if (context.get().getDirection().getReceptionSide() != packet.destinationSide) {
            Evolution.LOGGER.warn("Received {} on {}!", packet.getClass().getName(), packet.destinationSide.isClient() ? LogicalSide.SERVER : LogicalSide.CLIENT);
            return false;
        }
        return true;
    }
}
