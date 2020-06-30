package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface IChunkStorages extends IChunkStorage {
	
	/**
	 * Get the {@link World} containing this instance's chunk.
	 *
	 * @return The World
	 */
	World getWorld();
	
	/**
	 * Get the {@link ChunkPos} of this instance's chunk.
	 *
	 * @return The chunk position
	 */
	ChunkPos getChunkPos();
}
