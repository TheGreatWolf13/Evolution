package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCUpdateChunkStorage;

import java.util.Map;

public class ChunkStorage implements IChunkStorage, INBTSerializable<LongArrayNBT> {

    protected final int capacity;
    private final World world;
    private final ChunkPos chunkPos;
    protected int carbonDioxide;
    protected int gasNitrogen;
    protected int methane;
    protected int nitrogen;
    protected int oxygen;
    protected int phosphorus;
    protected int potassium;
    protected int water;

    public ChunkStorage(int capacity, World world, ChunkPos chunkPos) {
        this.capacity = capacity;
        this.world = world;
        this.chunkPos = chunkPos;
        this.nitrogen = 0;
        this.phosphorus = 0;
        this.potassium = 0;
        this.water = 0;
        this.carbonDioxide = 0;
        this.oxygen = 0;
        this.gasNitrogen = 0;
        this.methane = 0;
    }

    @Override
    public int addElement(EnumStorage element, int amount) {
        int elementReceived = 0;
        switch (element) {
            case NITROGEN:
                elementReceived = Math.min(this.capacity - this.nitrogen, amount);
                this.nitrogen += elementReceived;
                break;
            case PHOSPHORUS:
                elementReceived = Math.min(this.capacity - this.phosphorus, amount);
                this.phosphorus += elementReceived;
                break;
            case POTASSIUM:
                elementReceived = Math.min(this.capacity - this.potassium, amount);
                this.potassium += elementReceived;
                break;
            case WATER:
                elementReceived = Math.min(this.capacity - this.water, amount);
                this.water += elementReceived;
                break;
            case CARBON_DIOXIDE:
                elementReceived = Math.min(this.capacity - this.carbonDioxide, amount);
                this.carbonDioxide += elementReceived;
                break;
            case OXYGEN:
                elementReceived = Math.min(this.capacity - this.oxygen, amount);
                this.oxygen += elementReceived;
                break;
            case GAS_NITROGEN:
                elementReceived = Math.min(this.capacity - this.gasNitrogen, amount);
                this.gasNitrogen += elementReceived;
                break;
            case ORGANIC:
                elementReceived = Math.min(this.capacity - this.methane, amount);
                this.methane += elementReceived;
                break;
        }
        if (elementReceived != 0) {
            this.onElementChanged();
        }
        return elementReceived;
    }

    @Override
    public void addMany(Map<EnumStorage, Integer> elements) {
        for (Map.Entry<EnumStorage, Integer> entry : elements.entrySet()) {
            this.addElement(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deserializeNBT(LongArrayNBT nbt) {
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
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    @Override
    public int getElementStored(EnumStorage element) {
        switch (element) {
            case NITROGEN:
                return this.nitrogen;
            case PHOSPHORUS:
                return this.phosphorus;
            case POTASSIUM:
                return this.potassium;
            case WATER:
                return this.water;
            case CARBON_DIOXIDE:
                return this.carbonDioxide;
            case OXYGEN:
                return this.oxygen;
            case GAS_NITROGEN:
                return this.gasNitrogen;
            case ORGANIC:
                return this.methane;
        }
        return 0;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

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

    @Override
    public boolean removeElement(EnumStorage element, int amount) {
        boolean extracted = false;
        switch (element) {
            case NITROGEN:
                if (this.nitrogen - amount >= 0) {
                    this.nitrogen -= amount;
                    extracted = true;
                }
                break;
            case PHOSPHORUS:
                if (this.phosphorus - amount >= 0) {
                    this.phosphorus -= amount;
                    extracted = true;
                }
                break;
            case POTASSIUM:
                if (this.potassium - amount >= 0) {
                    this.potassium -= amount;
                    extracted = true;
                }
                break;
            case WATER:
                if (this.water - amount >= 0) {
                    this.water -= amount;
                    extracted = true;
                }
                break;
            case CARBON_DIOXIDE:
                if (this.carbonDioxide - amount >= 0) {
                    this.carbonDioxide -= amount;
                    extracted = true;
                }
                break;
            case OXYGEN:
                if (this.oxygen - amount >= 0) {
                    this.oxygen -= amount;
                    extracted = true;
                }
                break;
            case GAS_NITROGEN:
                if (this.gasNitrogen - amount >= 0) {
                    this.gasNitrogen -= amount;
                    extracted = true;
                }
                break;
            case ORGANIC:
                if (this.methane - amount >= 0) {
                    this.methane -= amount;
                    extracted = true;
                }
                break;
        }
        if (extracted) {
            this.onElementChanged();
        }
        return extracted;
    }

    @Override
    public boolean removeMany(Map<EnumStorage, Integer> elementsAndAmounts) {
        boolean success = true;
        outer:
        for (Map.Entry<EnumStorage, Integer> entry : elementsAndAmounts.entrySet()) {
            switch (entry.getKey()) {
                case NITROGEN:
                    if (this.nitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case CARBON_DIOXIDE:
                    if (this.carbonDioxide - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case GAS_NITROGEN:
                    if (this.gasNitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case OXYGEN:
                    if (this.oxygen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case PHOSPHORUS:
                    if (this.phosphorus - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case POTASSIUM:
                    if (this.potassium - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case WATER:
                    if (this.water - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
                case ORGANIC:
                    if (this.methane - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                    break outer;
            }
        }
        if (success) {
            for (Map.Entry<EnumStorage, Integer> entry : elementsAndAmounts.entrySet()) {
                this.removeElement(entry.getKey(), entry.getValue());
            }
            this.onElementChanged();
        }
        return success;
    }

    @Override
    public LongArrayNBT serializeNBT() {
        return new LongArrayNBT(new long[]{this.getElementStored(EnumStorage.NITROGEN),
                                           this.getElementStored(EnumStorage.PHOSPHORUS),
                                           this.getElementStored(EnumStorage.POTASSIUM),
                                           this.getElementStored(EnumStorage.WATER),
                                           this.getElementStored(EnumStorage.CARBON_DIOXIDE),
                                           this.getElementStored(EnumStorage.OXYGEN),
                                           this.getElementStored(EnumStorage.GAS_NITROGEN),
                                           this.getElementStored(EnumStorage.ORGANIC)});
    }

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
}
