package tgw.evolution.capabilities.chunkstorage;

import java.util.Map;

public class ChunkStorage implements IChunkStorage {

    protected final int capacity;
    protected int nitrogen;
    protected int phosphorus;
    protected int potassium;
    protected int water;
    protected int carbonDioxide;
    protected int oxygen;
    protected int gasNitrogen;
    protected int methane;

    public ChunkStorage(int capacity) {
        this.capacity = capacity;
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
                return elementReceived;
            case PHOSPHORUS:
                elementReceived = Math.min(this.capacity - this.phosphorus, amount);
                this.phosphorus += elementReceived;
                return elementReceived;
            case POTASSIUM:
                elementReceived = Math.min(this.capacity - this.potassium, amount);
                this.potassium += elementReceived;
                return elementReceived;
            case WATER:
                elementReceived = Math.min(this.capacity - this.water, amount);
                this.water += elementReceived;
                return elementReceived;
            case CARBON_DIOXIDE:
                elementReceived = Math.min(this.capacity - this.carbonDioxide, amount);
                this.carbonDioxide += elementReceived;
                return elementReceived;
            case OXYGEN:
                elementReceived = Math.min(this.capacity - this.oxygen, amount);
                this.oxygen += elementReceived;
                return elementReceived;
            case GAS_NITROGEN:
                elementReceived = Math.min(this.capacity - this.gasNitrogen, amount);
                this.gasNitrogen += elementReceived;
                return elementReceived;
            case ORGANIC:
                elementReceived = Math.min(this.capacity - this.methane, amount);
                this.methane += elementReceived;
                return elementReceived;
        }
        return elementReceived;
    }

    @Override
    public boolean removeElement(EnumStorage element, int amount) {
        switch (element) {
            case NITROGEN:
                if (this.nitrogen - amount >= 0) {
                    this.nitrogen -= amount;
                    return true;
                }
                return false;
            case PHOSPHORUS:
                if (this.phosphorus - amount >= 0) {
                    this.phosphorus -= amount;
                    return true;
                }
                return false;
            case POTASSIUM:
                if (this.potassium - amount >= 0) {
                    this.potassium -= amount;
                    return true;
                }
                return false;
            case WATER:
                if (this.water - amount >= 0) {
                    this.water -= amount;
                    return true;
                }
                return false;
            case CARBON_DIOXIDE:
                if (this.carbonDioxide - amount >= 0) {
                    this.carbonDioxide -= amount;
                    return true;
                }
                return false;
            case OXYGEN:
                if (this.oxygen - amount >= 0) {
                    this.oxygen -= amount;
                    return true;
                }
                return false;
            case GAS_NITROGEN:
                if (this.gasNitrogen - amount >= 0) {
                    this.gasNitrogen -= amount;
                    return true;
                }
                return false;
            case ORGANIC:
                if (this.methane - amount >= 0) {
                    this.methane -= amount;
                    return true;
                }
                return false;
        }
        return false;
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
    public boolean removeMany(Map<EnumStorage, Integer> elementsAndAmounts) {
        for (Map.Entry<EnumStorage, Integer> entry : elementsAndAmounts.entrySet()) {
            switch (entry.getKey()) {
                case NITROGEN:
                    if (this.nitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case CARBON_DIOXIDE:
                    if (this.carbonDioxide - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case GAS_NITROGEN:
                    if (this.gasNitrogen - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case OXYGEN:
                    if (this.oxygen - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case PHOSPHORUS:
                    if (this.phosphorus - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case POTASSIUM:
                    if (this.potassium - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case WATER:
                    if (this.water - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
                case ORGANIC:
                    if (this.methane - entry.getValue() >= 0) {
                        continue;
                    }
                    return false;
            }
        }
        for (Map.Entry<EnumStorage, Integer> entry : elementsAndAmounts.entrySet()) {
            this.removeElement(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    public void addMany(Map<EnumStorage, Integer> elements) {
        for (Map.Entry<EnumStorage, Integer> entry : elements.entrySet()) {
            this.addElement(entry.getKey(), entry.getValue());
        }
    }
}
