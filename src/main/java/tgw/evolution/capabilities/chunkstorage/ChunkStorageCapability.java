package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.SerializableCapabilityProvider;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCUpdateChunkStorage;
import tgw.evolution.util.InjectionUtil;

import java.util.Map;

public final class ChunkStorageCapability {

    @CapabilityInject(IChunkStorage.class)
    public static final Capability<IChunkStorage> CHUNK_STORAGE_CAPABILITY = InjectionUtil.Null();
    public static final Direction DEFAULT_FACING = null;
    public static final int DEFAULT_CAPACITY = 1_000_000;
    private static final ResourceLocation ID = Evolution.getResource("storage");

    private ChunkStorageCapability() {
    }

    public static void add(Chunk chunk, EnumStorage storage, int value) {
        getChunkStorage(chunk).map(chunkStorages -> {
            chunkStorages.addElement(storage, value);
            return true;
        }).orElseGet(() -> false);
    }

    public static void addElements(Chunk chunk, Map<EnumStorage, Integer> map) {
        getChunkStorage(chunk).map(chunkStorages -> {
            chunkStorages.addMany(map);
            return true;
        }).orElseGet(() -> false);
    }

    public static boolean contains(Chunk chunk, EnumStorage storage, int value) {
        boolean[] bool = {false};
        getChunkStorage(chunk).map(chunkStorages -> {
            bool[0] = chunkStorages.getElementStored(storage) - value >= 0;
            return true;
        }).orElseGet(() -> false);
        return bool[0];
    }

    public static LazyOptional<IChunkStorage> getChunkStorage(Chunk chunk) {
        return chunk.getCapability(CHUNK_STORAGE_CAPABILITY, DEFAULT_FACING);
    }

    public static LazyOptional<IChunkStorage> getChunkStorage(World world, ChunkPos chunkPos) {
        return getChunkStorage(world.getChunk(chunkPos.x, chunkPos.z));
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IChunkStorage.class, new Capability.IStorage<IChunkStorage>() {

            @Override
            public void readNBT(Capability<IChunkStorage> capability, IChunkStorage instance, Direction side, INBT nbt) {
                if (instance == null) {
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                }
                ((ChunkStorage) instance).setElement(EnumStorage.NITROGEN, ((IntArrayNBT) nbt).get(EnumStorage.NITROGEN.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.PHOSPHORUS, ((IntArrayNBT) nbt).get(EnumStorage.PHOSPHORUS.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.POTASSIUM, ((IntArrayNBT) nbt).get(EnumStorage.POTASSIUM.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.WATER, ((IntArrayNBT) nbt).get(EnumStorage.WATER.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.CARBON_DIOXIDE,
                                                     ((IntArrayNBT) nbt).get(EnumStorage.CARBON_DIOXIDE.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.OXYGEN, ((IntArrayNBT) nbt).get(EnumStorage.OXYGEN.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.GAS_NITROGEN, ((IntArrayNBT) nbt).get(EnumStorage.GAS_NITROGEN.getId()).getInt());
                ((ChunkStorage) instance).setElement(EnumStorage.ORGANIC, ((IntArrayNBT) nbt).get(EnumStorage.ORGANIC.getId()).getInt());
            }

            @Override
            public INBT writeNBT(Capability<IChunkStorage> capability, IChunkStorage instance, Direction side) {
                return new IntArrayNBT(new int[]{instance.getElementStored(EnumStorage.NITROGEN),
                                                 instance.getElementStored(EnumStorage.PHOSPHORUS),
                                                 instance.getElementStored(EnumStorage.POTASSIUM),
                                                 instance.getElementStored(EnumStorage.WATER),
                                                 instance.getElementStored(EnumStorage.CARBON_DIOXIDE),
                                                 instance.getElementStored(EnumStorage.OXYGEN),
                                                 instance.getElementStored(EnumStorage.GAS_NITROGEN),
                                                 instance.getElementStored(EnumStorage.ORGANIC)});
            }
        }, () -> {
            throw new IllegalStateException("Could not register Capability ChunkStorage");
        });
    }

    public static boolean remove(Chunk chunk, EnumStorage storage, int value) {
        boolean[] bool = {false};
        getChunkStorage(chunk).map(chunkStorages -> {
            bool[0] = chunkStorages.removeElement(storage, value);
            return true;
        }).orElseGet(() -> false);
        return bool[0];
    }

    public static boolean removeElements(Chunk chunk, Map<EnumStorage, Integer> map) {
        boolean[] bool = {false};
        getChunkStorage(chunk).map(chunkStorages -> {
            bool[0] = chunkStorages.removeMany(map);
            return true;
        }).orElseGet(() -> false);
        return bool[0];
    }

    @Mod.EventBusSubscriber(modid = Evolution.MODID)
    private static final class EventHandler {

        @SubscribeEvent
        public static void attachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
            Chunk chunk = event.getObject();
            IChunkStorage chunkStorages = new ChunkStorage(DEFAULT_CAPACITY, chunk.getWorld(), chunk.getPos());
            event.addCapability(ID, new SerializableCapabilityProvider<>(CHUNK_STORAGE_CAPABILITY, DEFAULT_FACING, chunkStorages));
        }

        @SubscribeEvent
        public static void chunkWatch(ChunkWatchEvent.Watch event) {
            ServerPlayerEntity player = event.getPlayer();
            if (player == null) {
                return;
            }
            getChunkStorage(event.getWorld(), event.getPos()).ifPresent(chunkStorages -> EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(
                    () -> player), new PacketSCUpdateChunkStorage(chunkStorages)));
        }
    }
}
