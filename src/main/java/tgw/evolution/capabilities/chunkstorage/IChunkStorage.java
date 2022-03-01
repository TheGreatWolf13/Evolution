package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

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
     * Get the {@link ChunkPos} of this instance's chunk.
     *
     * @return The chunk position
     */
    ChunkPos getChunkPos();

    /**
     * Returns the amount of energy currently stored.
     */
    int getElementStored(EnumStorage element);

    /**
     * Get the {@link Level} containing this instance's chunk.
     *
     * @return The World
     */
    Level getLevel();

    /**
     * Removes an element from the storage. Returns {@code true} if succeeded, {@code false} otherwise.
     */
    boolean removeElement(EnumStorage element, int amount);

    /**
     * Removes a set of elements and their respective amounts from the storage. Returns TRUE if succeeded, FALSE otherwise.
     */
    boolean removeMany(Map<EnumStorage, Integer> elements);
}