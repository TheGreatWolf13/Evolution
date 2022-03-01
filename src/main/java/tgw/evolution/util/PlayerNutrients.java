package tgw.evolution.util;

public enum PlayerNutrients {

    //Base data:
    //      Volume of blood in human body: 5L
    //      Stored Calories in player hunger bar: 3000kcal
    //      Base consumption per day (sedentary): 2500kcal
    //      Each hunger drumstick represents 300kcal and each half drumstick represents 150kcal
    //      Water consumption: 2.5L/day
    //      Overeating causes tiredness, increases temperature and dizziness
    //      Excess calories are stored as fat
    //      1 to 3 days for nutrients to be absorbed.
    ;
    private final float baseAdherence;
    private final int id;
    private final int lowerLimit;
    private final String name;
    private final String unit;
    private final int upperLimit;

    PlayerNutrients(int id, String name, float baseAdherence, int lowerLimit, int upperLimit, String unit) {
        this.id = id;
        this.name = name;
        this.baseAdherence = baseAdherence;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.unit = unit;
    }

    public float getBaseAdherence() {
        return this.baseAdherence;
    }

    public int getId() {
        return this.id;
    }

    public int getLowerLimit() {
        return this.lowerLimit;
    }

    public String getName() {
        return this.name;
    }

    public String getUnit() {
        return this.unit;
    }

    public int getUpperLimit() {
        return this.upperLimit;
    }
}
