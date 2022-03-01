package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import tgw.evolution.Evolution;

import java.util.Map;

public final class CapabilityChunkStorage {

    public static final Capability<IChunkStorage> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Direction DEFAULT_FACING = null;
    public static final int DEFAULT_CAPACITY = 1_000_000;
    private static final ResourceLocation ID = Evolution.getResource("storage");

    private CapabilityChunkStorage() {
    }

    public static void add(LevelChunk chunk, EnumStorage storage, int value) {
        getChunkStorage(chunk).ifPresent(chunkStorages -> chunkStorages.addElement(storage, value));
    }

    public static void addElements(LevelChunk chunk, Map<EnumStorage, Integer> map) {
        getChunkStorage(chunk).ifPresent(chunkStorages -> chunkStorages.addMany(map));
    }

    public static boolean contains(LevelChunk chunk, EnumStorage storage, int value) {
        boolean[] bool = {false};
        getChunkStorage(chunk).ifPresent(chunkStorages -> bool[0] = chunkStorages.getElementStored(storage) - value >= 0);
        return bool[0];
    }

    public static LazyOptional<IChunkStorage> getChunkStorage(LevelChunk chunk) {
        return chunk.getCapability(INSTANCE, DEFAULT_FACING);
    }

    public static LazyOptional<IChunkStorage> getChunkStorage(Level level, ChunkPos chunkPos) {
        return getChunkStorage(level.getChunk(chunkPos.x, chunkPos.z));
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IChunkStorage.class);
//        CapabilityManager.INSTANCE.register(IChunkStorage.class, new Capability.IStorage<IChunkStorage>() {
//
//            @Override
//            public void readNBT(Capability<IChunkStorage> capability, IChunkStorage instance, Direction side, INBT nbt) {
//                if (instance == null) {
//                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
//                }
//                ((ChunkStorage) instance).setElement(EnumStorage.NITROGEN, ((IntArrayNBT) nbt).get(EnumStorage.NITROGEN.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.PHOSPHORUS, ((IntArrayNBT) nbt).get(EnumStorage.PHOSPHORUS.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.POTASSIUM, ((IntArrayNBT) nbt).get(EnumStorage.POTASSIUM.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.WATER, ((IntArrayNBT) nbt).get(EnumStorage.WATER.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.CARBON_DIOXIDE,
//                                                     ((IntArrayNBT) nbt).get(EnumStorage.CARBON_DIOXIDE.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.OXYGEN, ((IntArrayNBT) nbt).get(EnumStorage.OXYGEN.getId()).getAsInt());
//                ((ChunkStorage) instance).setElement(EnumStorage.GAS_NITROGEN, ((IntArrayNBT) nbt).get(EnumStorage.GAS_NITROGEN.getId()).getAsInt
//                ());
//                ((ChunkStorage) instance).setElement(EnumStorage.ORGANIC, ((IntArrayNBT) nbt).get(EnumStorage.ORGANIC.getId()).getAsInt());
//            }
//
//            @Override
//            public INBT writeNBT(Capability<IChunkStorage> capability, IChunkStorage instance, Direction side) {
//                return new IntArrayNBT(new int[]{instance.getElementStored(EnumStorage.NITROGEN),
//                                                 instance.getElementStored(EnumStorage.PHOSPHORUS),
//                                                 instance.getElementStored(EnumStorage.POTASSIUM),
//                                                 instance.getElementStored(EnumStorage.WATER),
//                                                 instance.getElementStored(EnumStorage.CARBON_DIOXIDE),
//                                                 instance.getElementStored(EnumStorage.OXYGEN),
//                                                 instance.getElementStored(EnumStorage.GAS_NITROGEN),
//                                                 instance.getElementStored(EnumStorage.ORGANIC)});
//            }
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityChunkStorage");
//        });
    }

    public static boolean remove(LevelChunk chunk, EnumStorage storage, int value) {
        boolean[] bool = {false};
        getChunkStorage(chunk).ifPresent(chunkStorages -> bool[0] = chunkStorages.removeElement(storage, value));
        return bool[0];
    }

    public static boolean removeElements(LevelChunk chunk, Map<EnumStorage, Integer> map) {
        boolean[] bool = {false};
        getChunkStorage(chunk).ifPresent(chunkStorages -> bool[0] = chunkStorages.removeMany(map));
        return bool[0];
    }

//    @Mod.EventBusSubscriber(modid = Evolution.MODID)
//    private static final class EventHandler {
//
//        @SubscribeEvent
//        public static void attachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
//            Chunk chunk = event.getObject();
//            IChunkStorage chunkStorages = new ChunkStorage(DEFAULT_CAPACITY, chunk.getLevel(), chunk.getPos());
//            event.addCapability(ID, new SerializableCapabilityProvider<>(INSTANCE, DEFAULT_FACING, chunkStorages));
//        }
//
//        @SubscribeEvent
//        public static void chunkWatch(ChunkWatchEvent.Watch event) {
//            ServerPlayerEntity player = event.getPlayer();
//            if (player == null) {
//                return;
//            }
//            getChunkStorage(event.getWorld(), event.getPos()).ifPresent(chunkStorages -> EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER
//            .with(
//                    () -> player), new PacketSCUpdateChunkStorage(chunkStorages)));
//        }
//    }
}
