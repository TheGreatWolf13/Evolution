package tgw.evolution.entities;

import net.minecraft.nbt.CompoundNBT;

public class AnimalFoodWaterController {
    private final EntityGenericAnimal animal;
    /**
     * Each unit of food represents 10 g.
     */
    private short food;
    private short processedFood;
    private int foodConsumption;
    /**
     * Each unit of water represents 10 mL
     */
    private short water;
    private short processedWater;
    private int waterConsumption;

    public AnimalFoodWaterController(EntityGenericAnimal animal) {
        this.animal = animal;
    }

    public void readFromNBT(CompoundNBT nbt) {
        this.food = nbt.getShort("Food");
        this.processedFood = nbt.getShort("ProcFood");
        this.water = nbt.getShort("Water");
        this.processedWater = nbt.getShort("ProcWater");
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putShort("Food", this.food);
        nbt.putShort("ProcFood", this.processedFood);
        nbt.putShort("Water", this.water);
        nbt.putShort("ProcWater", this.processedWater);
    }

    public void tick() {
        this.consumeFood();
        this.consumeWater();
    }

    private void consumeFood() {
        if (this.food < this.foodConsumption) {
            this.starve();
            return;
        }
        this.food -= this.foodConsumption;
        this.processedFood += this.foodConsumption;
    }

    private void consumeWater() {
        if (this.water < this.waterConsumption) {
            this.dehydrate();
            return;
        }
        this.water -= this.waterConsumption;
        this.processedWater += this.waterConsumption;
    }

    private void starve() {
        //TODO
    }

    private void dehydrate() {
        //TODO
    }
}
