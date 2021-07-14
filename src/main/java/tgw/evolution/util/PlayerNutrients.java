package tgw.evolution.util;

public enum PlayerNutrients {

    //Daily recommendations:
    //      Protein: 64g/day
    //      Fiber: 30g/day
    //      Vitamin A: 900µg/day
    //      Iron: 8mg/day
    //      Sodium: 690mg/day
    //      Potassium: 3.8g/day
    //      Zinc: 14mg/day
    //      Magnesium: 400mg/day
    //      Iodine: 150µg/day
    //      Calcium: 1g/day
    //      Vitamin C: 45mg/day
    //      Vitamin B9 (Folate): 400µg/day
    //      Vitamin B12: 2.4µg/day
    //      Vitamin B6: 1.3mg/day
    //      Vitamin B3 (Niacin): 16mg/day
    //      Vitamin B2 (Riboflavin): 1.3mg/day
    //      Vitamin B1 (Thiamin): 1.2mg/day

    //
    //Base data:
    //      Volume of blood in human body: 5L
    //      Stored Calories in player hunger bar: 3000kcal
    //      Base consumption per day (sedentary): 2500kcal
    //      Each hunger drumstick represents 300kcal and each half drumstick represents 150kcal
    //      Water consumption: 2.5L/day
    //
    //Nutrient
    //      Mineral
    //          Iron
    //              Consumption: 12 mg/day
    //              Absorption: 5 ~ 10%
    //

    //Alcohol               //Calories = 7kcal/g (29kJ/g)
    //Carbohydrates         //Calories = 4kcal/g (17kJ/g)       130g/day
    //Fat                   //Calories = 9kcal/g (37kJ/g), cold resistance              65g/day (???)
    //Fibre                 //Not absorbed (gastro-intestinal health), Increase absorption of Calcium, Magnesium and Iron       38g/day
    //Protein               //Build body                        55g/day
    //Minerals
    //      Calcium         //Muscles, Circulatory, Digestive, BONES
    //      Iodine          //Thyroid hormones
    //      Iron            //Hemoglobin                        15 mg/day
    //      Magnesium       //Catalyst for DNA and RNA making
    //      Phosphorus      //Bones, energy, DNA, RNA
    //      Potassium       //Electrolyte (nerves and heart)
    //      Sodium          //Electrolyte (osmotic equilibrium)
    //Vitamins
    //      A               //Immune System, Vision             700µg/day
    //      B               //Some shit about cell metabolism
    //      C               //Immune System, Repair Tissues     40mg/day
    //      D               //Increases absorption of Calcium, Magnesium and Phosphorus
    //      K               //Blood coagulation, binding of Calcium to bones
    //    CARBOHYDRATES(0, "carbohydrates", 1f),
    //    FAT(1, "fat", 0.6f),
    //    FIBRE(2, "fibre", 0.08f),
    //    PROTEIN(3, "protein", 0.89f),
    //    CALCIUM(4, "calcium", 0.31f),
    //    IODINE(5, "iodine", 1f),
    //    IRON(6, "iron", 0.9f),
    //    MAGNESIUM(7, "magnesium", 0.43f),
    //    PHOSPHORUS(8, "phosphorus", 0.87f),
    //    POTASSIUM(9, "potassium", 0.08f),
    //    SODIUM(10, "sodium", 0.3f),
    //    VITAMIN_A(11, "vitamin_a", 0.46f, 700, 3000, "µg"),
    //    VITAMIN_B(12, "vitamin_b", 0.75f),
    //    VITAMIN_C(13, "vitamin_c", 0.51f),
    //    VITAMIN_D(14, "vitamin_d", 1f),
    //    VITAMIN_K(15, "vitamin_k", 1f),
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
