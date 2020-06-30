package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCUpdateChunkStorage;

import java.util.Map;

public class ChunkStorages extends ChunkStorage implements IChunkStorages, INBTSerializable<IntArrayNBT> {

    /**
     * The {@link World} containing this instance's chunk.
     */
    private final World world;

    /**
     * The {@link ChunkPos} of this instance's chunk.
     */
    private final ChunkPos chunkPos;

    public ChunkStorages(int capacity, World world, ChunkPos chunkPos, int initialAmount) {
        super(capacity);
        this.world = world;
        this.chunkPos = chunkPos;
        //TODO Make the quantity of nutrients change according to the biome
        //		for (Biome biome : chunk.getBiomes()) {
        //			Evolution.LOGGER.debug("Chunk at " + chunkPos + " contains " + biome.getDisplayName());
        //		}
        this.nitrogen = initialAmount;
        this.phosphorus = initialAmount;
        this.potassium = initialAmount;
        this.water = initialAmount;
        this.carbonDioxide = 10000;
        this.oxygen = 210000;
        this.gasNitrogen = 780000;
        this.methane = 0;
    }

    @Override
    public IntArrayNBT serializeNBT() {
        return new IntArrayNBT(new int[]{this.getElementStored(EnumStorage.NITROGEN),
                                         this.getElementStored(EnumStorage.PHOSPHORUS),
                                         this.getElementStored(EnumStorage.POTASSIUM),
                                         this.getElementStored(EnumStorage.WATER),
                                         this.getElementStored(EnumStorage.CARBON_DIOXIDE),
                                         this.getElementStored(EnumStorage.OXYGEN),
                                         this.getElementStored(EnumStorage.GAS_NITROGEN),
                                         this.getElementStored(EnumStorage.ORGANIC)});
    }

    @Override
    public void deserializeNBT(IntArrayNBT nbt) {
        this.nitrogen = nbt.get(EnumStorage.NITROGEN.getId()).getInt();
        this.phosphorus = nbt.get(EnumStorage.PHOSPHORUS.getId()).getInt();
        this.potassium = nbt.get(EnumStorage.POTASSIUM.getId()).getInt();
        this.water = nbt.get(EnumStorage.WATER.getId()).getInt();
        this.carbonDioxide = nbt.get(EnumStorage.CARBON_DIOXIDE.getId()).getInt();
        this.oxygen = nbt.get(EnumStorage.OXYGEN.getId()).getInt();
        this.gasNitrogen = nbt.get(EnumStorage.GAS_NITROGEN.getId()).getInt();
        this.methane = nbt.get(EnumStorage.ORGANIC.getId()).getInt();
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    @Override
    public int addElement(EnumStorage element, int amount) {
        int elementReceived = super.addElement(element, amount);
        if (elementReceived != 0) {
            this.onElementChanged();
        }
        return elementReceived;
    }

    @Override
    public boolean removeElement(EnumStorage element, int amount) {
        boolean elementExtracted = super.removeElement(element, amount);
        if (elementExtracted) {
            this.onElementChanged();
        }
        return elementExtracted;
    }

    @Override
    public boolean removeMany(Map<EnumStorage, Integer> elementsAndAmounts) {
        boolean success = super.removeMany(elementsAndAmounts);
        if (success) {
            this.onElementChanged();
        }
        return success;
    }

    /**
     * Set the element value. For internal use only.
     *
     * @param amount The new energy value
     */
    public void setElement(EnumStorage element, int amount) {
        switch (element) {
            case NITROGEN:
                this.nitrogen = amount;
                break;
            case PHOSPHORUS:
                this.phosphorus = amount;
                break;
            case POTASSIUM:
                this.potassium = amount;
                break;
            case WATER:
                this.water = amount;
                break;
            case CARBON_DIOXIDE:
                this.carbonDioxide = amount;
                break;
            case OXYGEN:
                this.oxygen = amount;
                break;
            case GAS_NITROGEN:
                this.gasNitrogen = amount;
                break;
            case ORGANIC:
                this.methane = amount;
                break;
        }
        this.onElementChanged();
    }

    /**
     * Called when the element value changes.
     */
    protected void onElementChanged() {
        if (this.world.isRemote) {
            return;
        }
        if (this.world.getChunkProvider().isChunkLoaded(this.chunkPos)) {
            Chunk chunk = this.world.getChunk(this.chunkPos.x, this.chunkPos.z);
            chunk.markDirty();
            EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new PacketSCUpdateChunkStorage(this));
        }
    }
}
