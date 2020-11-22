package tgw.evolution.entities;

public enum Gender {
    MALE(true),
    FEMALE(false);

    private final boolean male;

    Gender(boolean male) {
        this.male = male;
    }

    public static Gender fromBoolean(boolean gender) {
        if (gender) {
            return MALE;
        }
        return FEMALE;
    }

    public boolean toBoolean() {
        return this.male;
    }
}
