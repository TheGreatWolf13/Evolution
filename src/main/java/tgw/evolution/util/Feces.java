package tgw.evolution.util;

public class Feces {

    private int nitrogen;
    private int organic;
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

    public void add(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD -> this.organic += value;
            case NITROGEN -> this.nitrogen += value;
            case PHOSPHORUS -> this.phosphorus += value;
            case POTASSIUM -> this.potassium += value;
        }
    }

    public int get(EnumFoodNutrients nutrient) {
        return switch (nutrient) {
            case FOOD -> this.organic;
            case NITROGEN -> this.nitrogen;
            case PHOSPHORUS -> this.phosphorus;
            case POTASSIUM -> this.potassium;
        };
    }

    public void set(EnumFoodNutrients nutrient, int value) {
        switch (nutrient) {
            case FOOD -> this.organic = value;
            case NITROGEN -> this.nitrogen = value;
            case PHOSPHORUS -> this.phosphorus = value;
            case POTASSIUM -> this.potassium = value;
        }
    }

    @Override
    public String toString() {
        return this.organic + " Organic / " + this.nitrogen + " N / " + this.potassium + " K / " + this.phosphorus + " P";
    }
}
