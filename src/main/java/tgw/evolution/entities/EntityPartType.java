package tgw.evolution.entities;

public enum EntityPartType {
    HEAD("head", 10),
    NECK("neck", 10),
    ;

    private final String name;
    private final float damageMult;

    EntityPartType(String name, float damageMult) {
        this.name = name;
        this.damageMult = damageMult;
    }

    public String getName() {
        return this.name;
    }

    public float getDamageMult() {
        return this.damageMult;
    }
}
