package tgw.evolution.patches;

public interface PatchRecipeCollection {

    default int getRecipeAmount(boolean onlyCraftable) {
        throw new AbstractMethodError();
    }
}
