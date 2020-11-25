package tgw.evolution.entities;

public enum EntityPartType {
    HEAD("head", 1.75f),
    BODY("body", 1.0f);

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
