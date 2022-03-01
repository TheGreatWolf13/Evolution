package tgw.evolution.util;

public class FoodNutrients {

    private int food;
    private int nitrogen;
    private int phosphorus;
    private int potassium;

    public FoodNutrients(int food, int phosphorus, int nitrogen, int potassium) {
        this.food = Math.max(food, 0);
        this.phosphorus = Math.max(phosphorus, 0);
        this.potassium = Math.max(potassium, 0);
        this.nitrogen = Math.max(nitrogen, 0);
    }

    public FoodNutrients() {
        this(0, 0, 0, 0);
    }

    public void add(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD -> this.food += value;
            case NITROGEN -> this.nitrogen += value;
            case PHOSPHORUS -> this.phosphorus += value;
            case POTASSIUM -> this.potassium += value;
        }
    }

    public int get(EnumFoodNutrients nutrient) {
        return switch (nutrient) {
            case FOOD -> this.food;
            case NITROGEN -> this.nitrogen;
            case PHOSPHORUS -> this.phosphorus;
            case POTASSIUM -> this.potassium;
        };
    }

    public void set(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD -> this.food = value;
            case NITROGEN -> this.nitrogen = value;
            case PHOSPHORUS -> this.phosphorus = value;
            case POTASSIUM -> this.potassium = value;
        }
    }

    @Override
    public String toString() {
        return this.food + " Food / " + this.nitrogen + " N / " + this.potassium + " K / " + this.phosphorus + " P";
    }
}
