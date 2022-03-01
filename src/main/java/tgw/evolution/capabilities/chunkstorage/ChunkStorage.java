package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.nbt.LongArrayTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public class ChunkStorage implements IChunkStorage, INBTSerializable<LongArrayTag> {

    protected final int capacity;
    private final ChunkPos chunkPos;
    private final Level level;
    protected int carbonDioxide;
    protected int gasNitrogen;
    protected int methane;
    protected int nitrogen;
    protected int oxygen;
    protected int phosphorus;
    protected int potassium;
    protected int water;

    public ChunkStorage(int capacity, Level level, ChunkPos chunkPos) {
        this.capacity = capacity;
        this.level = level;
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
            case NITROGEN -> {
                elementReceived = Math.min(this.capacity - this.nitrogen, amount);
                this.nitrogen += elementReceived;
            }
            case PHOSPHORUS -> {
                elementReceived = Math.min(this.capacity - this.phosphorus, amount);
                this.phosphorus += elementReceived;
            }
            case POTASSIUM -> {
                elementReceived = Math.min(this.capacity - this.potassium, amount);
                this.potassium += elementReceived;
            }
            case WATER -> {
                elementReceived = Math.min(this.capacity - this.water, amount);
                this.water += elementReceived;
            }
            case CARBON_DIOXIDE -> {
                elementReceived = Math.min(this.capacity - this.carbonDioxide, amount);
                this.carbonDioxide += elementReceived;
            }
            case OXYGEN -> {
                elementReceived = Math.min(this.capacity - this.oxygen, amount);
                this.oxygen += elementReceived;
            }
            case GAS_NITROGEN -> {
                elementReceived = Math.min(this.capacity - this.gasNitrogen, amount);
                this.gasNitrogen += elementReceived;
            }
            case ORGANIC -> {
                elementReceived = Math.min(this.capacity - this.methane, amount);
                this.methane += elementReceived;
            }
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
    public void deserializeNBT(LongArrayTag nbt) {
        this.nitrogen = nbt.get(EnumStorage.NITROGEN.getId()).getAsInt();
        this.phosphorus = nbt.get(EnumStorage.PHOSPHORUS.getId()).getAsInt();
        this.potassium = nbt.get(EnumStorage.POTASSIUM.getId()).getAsInt();
        this.water = nbt.get(EnumStorage.WATER.getId()).getAsInt();
        this.carbonDioxide = nbt.get(EnumStorage.CARBON_DIOXIDE.getId()).getAsInt();
        this.oxygen = nbt.get(EnumStorage.OXYGEN.getId()).getAsInt();
        this.gasNitrogen = nbt.get(EnumStorage.GAS_NITROGEN.getId()).getAsInt();
        this.methane = nbt.get(EnumStorage.ORGANIC.getId()).getAsInt();
    }

    @Override
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    @Override
    public int getElementStored(EnumStorage element) {
        return switch (element) {
            case NITROGEN -> this.nitrogen;
            case PHOSPHORUS -> this.phosphorus;
            case POTASSIUM -> this.potassium;
            case WATER -> this.water;
            case CARBON_DIOXIDE -> this.carbonDioxide;
            case OXYGEN -> this.oxygen;
            case GAS_NITROGEN -> this.gasNitrogen;
            case ORGANIC -> this.methane;
        };
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    protected void onElementChanged() {
        if (this.level.isClientSide) {
            return;
        }
//        if (this.level.getChunkSource().isEntityTickingChunk(this.chunkPos)) {
//            LevelChunk chunk = this.level.getChunk(this.chunkPos.x, this.chunkPos.z);
//            chunk.markUnsaved();
//            EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new PacketSCUpdateChunkStorage(this));
//        }
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
        for (Map.Entry<EnumStorage, Integer> entry : elementsAndAmounts.entrySet()) {
            switch (entry.getKey()) {
                case NITROGEN -> {
                    if (this.nitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case CARBON_DIOXIDE -> {
                    if (this.carbonDioxide - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case GAS_NITROGEN -> {
                    if (this.gasNitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case OXYGEN -> {
                    if (this.oxygen - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case PHOSPHORUS -> {
                    if (this.phosphorus - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case POTASSIUM -> {
                    if (this.potassium - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case WATER -> {
                    if (this.water - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
                case ORGANIC -> {
                    if (this.methane - entry.getValue() >= 0) {
                        continue;
                    }
                    success = false;
                }
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
    public LongArrayTag serializeNBT() {
        return new LongArrayTag(new long[]{this.getElementStored(EnumStorage.NITROGEN),
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
            case NITROGEN -> this.nitrogen = amount;
            case PHOSPHORUS -> this.phosphorus = amount;
            case POTASSIUM -> this.potassium = amount;
            case WATER -> this.water = amount;
            case CARBON_DIOXIDE -> this.carbonDioxide = amount;
            case OXYGEN -> this.oxygen = amount;
            case GAS_NITROGEN -> this.gasNitrogen = amount;
            case ORGANIC -> this.methane = amount;
        }
        this.onElementChanged();
    }
}
