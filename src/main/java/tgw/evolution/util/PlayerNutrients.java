package tgw.evolution.util;

public enum PlayerNutrients {
    //Alcohol               //Calories = 7kcal/g (29kJ/g)
    //Carbohydrates         //Calories = 4kcal/g (17kJ/g)       130g/day
    //Fat                   //Calories = 9kcal/g (37kJ/g), cold resistance              65g/day (???)
    //Fibre                 //Not absorbed (gastro-intestinal health), Increase absorption of Calcium, Magnesium and Iron       38g/day
    //Protein               //Build body                        55g/day
    //Minerals
    //      Calcium         //Muscles, Circulatory, Digestive, BONES
    //      Iodine          //Thyroid hormones
    //      Iron            //Hemoglobin                        14.8mg/day
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
    private final String name;
    private final int id;
    private final float baseAdherence;
    private final int lowerLimit;
    private final int upperLimit;
    private final String unit;

    PlayerNutrients(int id, String name, float baseAdherence, int lowerLimit, int upperLimit, String unit) {
        this.id = id;
        this.name = name;
        this.baseAdherence = baseAdherence;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.unit = unit;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public float getBaseAdherence() {
        return this.baseAdherence;
    }

    public int getLowerLimit() {
        return this.lowerLimit;
    }

    public int getUpperLimit() {
        return this.upperLimit;
    }

    public String getUnit() {
        return this.unit;
    }
}
