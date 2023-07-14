package tgw.evolution.entities;

import it.unimi.dsi.fastutil.floats.FloatListIterator;
import net.minecraft.nbt.*;
import tgw.evolution.util.collection.lists.FArrayList;
import tgw.evolution.util.collection.lists.FList;

public class EffectHelper {

    private final FList suggestedAbsorptions = new FArrayList();
    /**
     * Bit 0: canSprint; <br>
     * Bit 1: canRegen; <br>
     */
    private byte flags;
    private float hungerMod;
    private float maxAbsorption;
    private double tempMod;
    private float thirstMod;

    public float addAbsorptionSuggestion(float amount) {
        if (amount > this.maxAbsorption) {
            float delta = amount - this.maxAbsorption;
            this.maxAbsorption = amount;
            this.suggestedAbsorptions.add(amount);
            return delta;
        }
        return 0;
    }

    public boolean canRegen() {
        return (this.flags & 2) != 0;
    }

    public boolean canSprint() {
        return (this.flags & 1) != 0;
    }

    public void fromNBT(CompoundTag tag) {
        this.suggestedAbsorptions.clear();
        if (tag.contains("SuggestedAbsorptions", Tag.TAG_LIST)) {
            ListTag list = tag.getList("SuggestedAbsorptions", Tag.TAG_FLOAT);
            float max = 0;
            for (int i = 0, l = list.size(); i < l; i++) {
                float f = ((NumericTag) list.get(i)).getAsFloat();
                if (f > max) {
                    max = f;
                }
                this.suggestedAbsorptions.add(f);
            }
            this.maxAbsorption = max;
        }
    }

    public float getHungerMod() {
        return this.hungerMod;
    }

    public double getTemperatureMod() {
        return this.tempMod;
    }

    public float getThirstMod() {
        return this.thirstMod;
    }

    public float removeAbsorptionSuggestion(float amount) {
        for (FloatListIterator it = this.suggestedAbsorptions.iterator(); it.hasNext(); ) {
            if (it.nextFloat() == amount) {
                it.remove();
                break;
            }
        }
        if (amount == this.maxAbsorption) {
            this.maxAbsorption = 0;
            for (int i = 0, l = this.suggestedAbsorptions.size(); i < l; i++) {
                float f = this.suggestedAbsorptions.getFloat(i);
                if (f > this.maxAbsorption) {
                    this.maxAbsorption = f;
                }
            }
        }
        return this.maxAbsorption;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.suggestedAbsorptions.isEmpty()) {
            ListTag list = new ListTag();
            for (int i = 0, l = this.suggestedAbsorptions.size(); i < l; i++) {
                list.add(FloatTag.valueOf(this.suggestedAbsorptions.getFloat(i)));
            }
            tag.put("SuggestedAbsorptions", list);
        }
        return tag;
    }

    public void setCanRegen(boolean canRegen) {
        if (canRegen) {
            this.flags |= 2;
        }
        else {
            this.flags &= ~2;
        }
    }

    public void setCanSprint(boolean canSprint) {
        if (canSprint) {
            this.flags |= 1;
        }
        else {
            this.flags &= ~1;
        }
    }

    public void setHungerMod(float hunger) {
        this.hungerMod = hunger;
    }

    public void setTemperatureMod(double temp) {
        this.tempMod = temp;
    }

    public void setThirstMod(float thirst) {
        this.thirstMod = thirst;
    }
}
