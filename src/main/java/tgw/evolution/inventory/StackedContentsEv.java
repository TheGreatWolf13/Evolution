package tgw.evolution.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.*;
import tgw.evolution.util.collection.maps.I2IHashMap;
import tgw.evolution.util.collection.maps.I2IMap;

import java.util.BitSet;
import java.util.List;

public class StackedContentsEv extends StackedContents {

    @Override
    public boolean canCraft(Recipe<?> recipe, @Nullable IntList itemIdsList, int amount) {
        throw new IllegalStateException("Invalid method: should instead call the version using BiIIArrayList!");
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable BiIIArrayList itemIdsList, int amount) {
        return new RecipePicker(recipe).tryPick(amount, itemIdsList);
    }

    public boolean canCraft(Recipe<?> pRecipe, @Nullable BiIIArrayList itemIdsList) {
        return this.canCraft(pRecipe, itemIdsList, 1);
    }

    public int getBiggestCraftableStack(Recipe<?> pRecipe, @Nullable BiIIArrayList pStackingIndexList) {
        return this.getBiggestCraftableStack(pRecipe, Integer.MAX_VALUE, pStackingIndexList);
    }

    @Override
    public int getBiggestCraftableStack(Recipe<?> recipe, int amount, @Nullable IntList itemIdsList) {
        throw new IllegalStateException("Invalid method: should instead call the version using BiIIArrayList!");
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, int amount, @Nullable BiIIArrayList itemIdsList) {
        return new RecipePicker(recipe).tryPickAll(amount, itemIdsList);
    }

    boolean has(int itemId, int count) {
        return this.contents.get(itemId) >= count;
    }

    void put(int itemId, int increment) {
        this.contents.put(itemId, this.contents.get(itemId) + increment);
    }

    void take(int itemId, int amount) {
        int i = this.contents.get(itemId);
        if (i >= amount) {
            this.contents.put(itemId, i - amount);
        }
    }

    public class RecipePicker {

        private final BitSet data;
        private final int ingredientCount;
        private final OList<Ingredient> ingredients = new OArrayList<>();
        private final int itemCount;
        private final BiIIArrayList items;
        private final IList path = new IArrayList();
        private final Recipe<?> recipe;

        public RecipePicker(Recipe<?> recipe) {
            this.recipe = recipe;
            this.ingredients.addAll(recipe.getIngredients());
            for (int i = 0; i < this.ingredients.size(); i++) {
                if (this.ingredients.get(i).isEmpty()) {
                    this.ingredients.remove(i);
                    i--;
                }
            }
            this.ingredientCount = this.ingredients.size();
            this.items = this.getUniqueAvailableIngredientItems();
            this.itemCount = this.items.size();
            this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + 2 * this.ingredientCount * this.itemCount);
            for (int i = 0; i < this.ingredients.size(); ++i) {
                Ingredient ingredient = this.ingredients.get(i);
                IntList itemIds = ingredient.getStackingIds();
                int ingredientCount = ingredient.getItems()[0].getCount();
                for (int j = 0; j < this.itemCount; ++j) {
                    if (itemIds.contains(this.items.getLeft(j)) && ingredientCount == this.items.getRight(j)) {
                        this.data.set(this.getIndex(true, j, i));
                    }
                }
            }
        }

        private boolean dfs(int amount) {
            int itemCount = this.itemCount;
            for (int i = 0; i < itemCount; i++) {
                if (StackedContentsEv.this.contents.get(this.items.getLeft(i)) >= amount * this.items.getRight(i)) {
                    this.visit(false, i);
                    while (!this.path.isEmpty()) {
                        int pathSize = this.path.size();
                        boolean hasNonPairedItem = (pathSize & 1) == 1;
                        int lastIntInPath = this.path.getInt(pathSize - 1);
                        if (!hasNonPairedItem && !this.isSatisfied(lastIntInPath)) {
                            break;
                        }
                        int i1 = hasNonPairedItem ? this.ingredientCount : itemCount;
                        for (int j = 0; j < i1; j++) {
                            if (!this.hasVisited(hasNonPairedItem, j) &&
                                this.hasConnection(hasNonPairedItem, lastIntInPath, j) &&
                                this.hasResidual(hasNonPairedItem, lastIntInPath, j)) {
                                this.visit(hasNonPairedItem, j);
                                break;
                            }
                        }
                        int newPathSize = this.path.size();
                        if (newPathSize == pathSize) {
                            this.path.removeInt(newPathSize - 1);
                        }
                    }
                    if (!this.path.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private int getIndex(boolean isIngredientPath, int stackingIndex, int pathIndex) {
            int i = isIngredientPath ? stackingIndex * this.ingredientCount + pathIndex : pathIndex * this.ingredientCount + stackingIndex;
            return this.ingredientCount + this.itemCount + this.ingredientCount + 2 * i;
        }

        private int getMinIngredientCount() {
            int i = Integer.MAX_VALUE;
            for (int i1 = 0, len0 = this.ingredients.size(); i1 < len0; i1++) {
                int j = 0;
                IntList itemIdList = this.ingredients.get(i1).getStackingIds();
                for (int k = 0, len = itemIdList.size(); k < len; k++) {
                    j = Math.max(j, StackedContentsEv.this.contents.get(itemIdList.getInt(k)));
                }
                if (i > 0) {
                    i = Math.min(i, j);
                }
            }
            return i;
        }

        private int getSatisfiedIndex(int stackingIndex) {
            return this.ingredientCount + this.itemCount + stackingIndex;
        }

        private BiIIArrayList getUniqueAvailableIngredientItems() {
            BiIIArrayList bilist = new BiIIArrayList();
            I2IMap firstIndexOfPresent = new I2IHashMap();
            firstIndexOfPresent.defaultReturnValue(-1);
            for (int i = 0, len = this.ingredients.size(); i < len; i++) {
                Ingredient ingredient = this.ingredients.get(i);
                int count = ingredient.getItems()[0].getCount();
                IntList itemIds = ingredient.getStackingIds();
                for (int j = 0, len1 = itemIds.size(); j < len1; j++) {
                    int itemId = itemIds.getInt(j);
                    int firstIndex = firstIndexOfPresent.get(itemId);
                    if (firstIndex == -1) {
                        firstIndexOfPresent.put(itemId, bilist.size());
                        bilist.add(itemId, count);
                    }
                    else {
                        if (!bilist.contains(itemId, count, firstIndex)) {
                            bilist.add(itemId, count);
                        }
                    }
                }
            }
            for (int i = 0; i < bilist.size(); i++) {
                if (!StackedContentsEv.this.has(bilist.getLeft(i), bilist.getRight(i))) {
                    bilist.removeAt(i);
                    i--;
                }
            }
            bilist.trim();
            return bilist;
        }

        private int getVisitedIndex(boolean isIngredientPath, int pathIndex) {
            return (isIngredientPath ? 0 : this.ingredientCount) + pathIndex;
        }

        private boolean hasConnection(boolean isIngredientPath, int stackingIndex, int pathIndex) {
            return this.data.get(this.getIndex(isIngredientPath, stackingIndex, pathIndex));
        }

        private boolean hasResidual(boolean isIngredientPath, int stackingIndex, int pathIndex) {
            return isIngredientPath != this.data.get(1 + this.getIndex(isIngredientPath, stackingIndex, pathIndex));
        }

        private boolean hasVisited(boolean isIngredientPath, int pathIndex) {
            return this.data.get(this.getVisitedIndex(isIngredientPath, pathIndex));
        }

        private boolean isSatisfied(int stackingIndex) {
            return this.data.get(this.getSatisfiedIndex(stackingIndex));
        }

        private void setSatisfied(int stackingId) {
            this.data.set(this.getSatisfiedIndex(stackingId));
        }

        private void toggleResidual(boolean isIngredientPath, int stackingIndex, int pathIndex) {
            this.data.flip(1 + this.getIndex(isIngredientPath, stackingIndex, pathIndex));
        }

        public boolean tryPick(int amount, @Nullable BiIIArrayList stackingIndexList) {
            if (amount <= 0) {
                return true;
            }
            int countingIngredients;
            for (countingIngredients = 0; this.dfs(amount); countingIngredients++) {
                int index = this.path.getInt(0);
                StackedContentsEv.this.take(this.items.getLeft(index), amount * this.items.getRight(index));
                int lastIndexOfPath = this.path.size() - 1;
                this.setSatisfied(this.path.getInt(lastIndexOfPath));
                for (int k = 0; k < lastIndexOfPath; k++) {
                    this.toggleResidual((k & 1) == 0, this.path.getInt(k), this.path.getInt(k + 1));
                }
                this.path.clear();
                this.data.clear(0, this.ingredientCount + this.itemCount);
            }
            boolean ingredientCountMatches = countingIngredients == this.ingredientCount;
            boolean needsExternalWriting = ingredientCountMatches && stackingIndexList != null;
            if (needsExternalWriting) {
                stackingIndexList.clear();
            }
            this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
            int l = 0;
            List<Ingredient> ingredients = this.recipe.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                if (needsExternalWriting && ingredients.get(i).isEmpty()) {
                    stackingIndexList.add(0, 0);
                }
                else {
                    for (int j = 0; j < this.itemCount; j++) {
                        if (this.hasResidual(false, l, j)) {
                            this.toggleResidual(true, j, l);
                            StackedContentsEv.this.put(this.items.getLeft(j), amount * this.items.getRight(j));
                            if (needsExternalWriting) {
                                stackingIndexList.add(this.items.getLeft(j), this.items.getRight(j));
                            }
                        }
                    }
                    ++l;
                }
            }
            return ingredientCountMatches;
        }

        public int tryPickAll(int amount, @Nullable BiIIArrayList itemIdsList) {
            int i = 0;
            int j = Math.min(amount, this.getMinIngredientCount()) + 1;
            while (true) {
                int k = (i + j) / 2;
                if (this.tryPick(k, null)) {
                    if (j - i <= 1) {
                        if (k > 0) {
                            this.tryPick(k, itemIdsList);
                        }
                        return k;
                    }
                    i = k;
                }
                else {
                    j = k;
                }
            }
        }

        private void visit(boolean isIngredientPath, int pathIndex) {
            this.data.set(this.getVisitedIndex(isIngredientPath, pathIndex));
            this.path.add(pathIndex);
        }
    }
}
