package tgw.evolution.util;

public class Feces {

    private int organic;
    private int nitrogen;
    private int phosphorus;
    private int potassium;

    public Feces(int organic, int nitrogen, int phosphorus, int potassium) {
        this.organic = Math.max(organic, 0);
        this.nitrogen = Math.max(nitrogen, 0);
        this.phosphorus = Math.max(phosphorus, 0);
        this.potassium = Math.max(potassium, 0);
    }

    public Feces() {
        this(0, 0, 0, 0);
    }

    public void set(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD:
                this.organic = value;
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
                return this.organic;
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
                this.organic += value;
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
        return this.organic + " Organic / " + this.nitrogen + " N / " + this.potassium + " K / " + this.phosphorus + " P";
    }
}
