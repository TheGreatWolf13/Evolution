package tgw.evolution.util;

public class FoodNutrients {

    private int food;
    private int phosphorus;
    private int nitrogen;
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

    public void set(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD:
                this.food = value;
                break;
            case NITROGEN:
                this.nitrogen = value;
                break;
            case PHOSPHORUS:
                this.phosphorus = value;
                break;
            case POTASSIUM:
                this.potassium = value;
                break;
        }
    }

    public int get(EnumFoodNutrients nutrient) {
        switch (nutrient) {
            case FOOD:
                return this.food;
            case NITROGEN:
                return this.nitrogen;
            case PHOSPHORUS:
                return this.phosphorus;
            case POTASSIUM:
                return this.potassium;
        }
        return 0;
    }

    public void add(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD:
                this.food += value;
                break;
            case NITROGEN:
                this.nitrogen += value;
                break;
            case PHOSPHORUS:
                this.phosphorus += value;
                break;
            case POTASSIUM:
                this.potassium += value;
                break;
        }
    }

    @Override
    public String toString() {
        return this.food + " Food / " + this.nitrogen + " N / " + this.potassium + " K / " + this.phosphorus + " P";
    }
}
