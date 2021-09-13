package tgw.evolution.entities;

public interface IEntityProperties {

    /**
     * @return The entity mass in kg.
     */
    double getBaseMass();

    float getFrictionModifier();

    /**
     * @return Controls the deceleration because of the motion of your legs.
     */
    double getLegSlowdown();
}
