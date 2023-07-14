//package tgw.evolution.entities.util;
//
//import net.minecraft.nbt.CompoundTag;
//import tgw.evolution.entities.EntityGenericAnimal;
//
//public class AnimalFoodWaterController {
//    private final EntityGenericAnimal animal;
//    /**
//     * Each unit of food represents 10 g.
//     */
//    private short food;
//    private int foodConsumption;
//    private short processedFood;
//    private short processedWater;
//    /**
//     * Each unit of water represents 10 mL
//     */
//    private short water;
//    private int waterConsumption;
//
//    public AnimalFoodWaterController(EntityGenericAnimal animal) {
//        this.animal = animal;
//    }
//
//    private void consumeFood() {
//        if (this.food < this.foodConsumption) {
//            this.starve();
//            return;
//        }
//        this.food -= this.foodConsumption;
//        this.processedFood += this.foodConsumption;
//    }
//
//    private void consumeWater() {
//        if (this.water < this.waterConsumption) {
//            this.dehydrate();
//            return;
//        }
//        this.water -= this.waterConsumption;
//        this.processedWater += this.waterConsumption;
//    }
//
//    private void dehydrate() {
//        //TODO
//    }
//
//    public void readFromNBT(CompoundTag tag) {
//        this.food = tag.getShort("Food");
//        this.processedFood = tag.getShort("ProcFood");
//        this.water = tag.getShort("Water");
//        this.processedWater = tag.getShort("ProcWater");
//    }
//
//    private void starve() {
//        //TODO
//    }
//
//    public void tick() {
//        this.consumeFood();
//        this.consumeWater();
//    }
//
//    public void writeToNBT(CompoundTag tag) {
//        tag.putShort("Food", this.food);
//        tag.putShort("ProcFood", this.processedFood);
//        tag.putShort("Water", this.water);
//        tag.putShort("ProcWater", this.processedWater);
//    }
//}
