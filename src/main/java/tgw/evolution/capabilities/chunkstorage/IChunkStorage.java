package tgw.evolution.capabilities.chunkstorage;

import java.util.Map;

public interface IChunkStorage {

    /**
     * Adds an element to the storage. Returns quantity of that element that was accepted.
     */
    int addElement(EnumStorage element, int amount);

    /**
     * Adds a set of elements and their respective amounts to the storage.
     */
    void addMany(Map<EnumStorage, Integer> elements);

    /**
     * Removes an element from the storage. Returns TRUE if succeeded, FALSE otherwise.
     */
    boolean removeElement(EnumStorage element, int amount);

    /**
     * Removes a set of elements and their respective amounts from the storage. Returns TRUE if succeeded, FALSE otherwise.
     */
    boolean removeMany(Map<EnumStorage, Integer> elements);

    /**
     * Returns the amount of energy currently stored.
     */
    int getElementStored(EnumStorage element);

}