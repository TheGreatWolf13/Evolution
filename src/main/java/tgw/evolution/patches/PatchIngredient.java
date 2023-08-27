package tgw.evolution.patches;

public interface PatchIngredient {

    default boolean isSimple() {
        throw new AbstractMethodError();
    }
}
