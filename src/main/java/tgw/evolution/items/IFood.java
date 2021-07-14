package tgw.evolution.items;

public interface IFood extends IConsumable {

    /**
     * @return The amount of hunger this item will cure.
     */
    int getHunger();
}
