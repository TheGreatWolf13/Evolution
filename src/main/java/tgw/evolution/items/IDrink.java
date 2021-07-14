package tgw.evolution.items;

public interface IDrink extends IConsumable {

    /**
     * @return The amount of thirst this item will cure.
     */
    int getThirst();
}
