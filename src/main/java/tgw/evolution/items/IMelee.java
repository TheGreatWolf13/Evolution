package tgw.evolution.items;

public interface IMelee {

    float setReach();

    default float getReach() {
        return this.setReach() - 5f;
    }

    float getAttackDamage();

    float getAttackSpeed();
}
