package tgw.evolution.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.world.EvWorldDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class WorldEvents {

    @Nullable
    private static BlockPos findSpawnChunk(World world, int chunkPosStartX, int chunkPosStartZ) {
        int chunkPosEndX = chunkPosStartX + 15;
        int chunkPosEndZ = chunkPosStartZ + 15;
        for (int i = chunkPosStartX; i <= chunkPosEndX; ++i) {
            for (int j = chunkPosStartZ; j <= chunkPosEndZ; ++j) {
                BlockPos pos = findSpawn(world, i, j);
                if (pos != null) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Nullable
    public static BlockPos findSpawn(World world, int posX, int posZ) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(posX, 0, posZ);
        Biome biome = world.getBiome(mutablePos);
        BlockState biomeSurfaceState = biome.getSurfaceBuilderConfig().getTop();
        Chunk chunk = world.getChunk(posX >> 4, posZ >> 4);
        int i = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING, posX & 15, posZ & 15);
        if (i < 0) {
            return null;
        }
        if (chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, posX & 15, posZ & 15) > chunk.getTopBlockY(Heightmap.Type.OCEAN_FLOOR, posX & 15, posZ & 15)) {
            return null;
        }
        for (int j = i + 1; j >= 0; --j) {
            mutablePos.setPos(posX, j, posZ);
            BlockState stateAtWorld = world.getBlockState(mutablePos);
            if (!stateAtWorld.getFluidState().isEmpty()) {
                break;
            }
            if (BlockUtils.compareVanillaBlockStates(biomeSurfaceState, stateAtWorld)) {
                return mutablePos.up().toImmutable();
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().getWorld().getWorldInfo().getGameType() != GameType.CREATIVE) {
            event.getWorld().getWorld().getGameRules().get(GameRules.REDUCED_DEBUG_INFO).set(true, event.getWorld().getWorld().getServer());
        }
    }

    @SubscribeEvent
    public void createSpawn(WorldEvent.CreateSpawnPosition event) {
        World world = event.getWorld().getWorld();
        WorldInfo worldInfo = world.getWorldInfo();
        if (!(worldInfo.getGenerator() instanceof EvWorldDefault)) {
            return;
        }
        event.setCanceled(true);
        Dimension dimension = world.getDimension();
        AbstractChunkProvider chunkProvider = world.getChunkProvider();
        if (!dimension.canRespawnHere()) {
            worldInfo.setSpawn(BlockPos.ZERO.up(chunkProvider.getChunkGenerator().getGroundHeight()));
        }
        else {
            BiomeProvider biomeprovider = chunkProvider.getChunkGenerator().getBiomeProvider();
            List<Biome> list = biomeprovider.getBiomesToSpawnIn();
            Random random = new Random(world.getSeed());
            BlockPos biomePosition = biomeprovider.findBiomePosition(0, 0, 256, list, random);
            ChunkPos chunkpos = biomePosition == null ? new ChunkPos(0, 0) : new ChunkPos(biomePosition);
            if (biomePosition == null) {
                Evolution.LOGGER.warn("Unable to find spawn biome");
            }
            BlockPos spawnPos = chunkpos.asBlockPos().add(8, chunkProvider.getChunkGenerator().getGroundHeight(), 8);
            worldInfo.setSpawn(spawnPos);
            int xPosIncrement = 0;
            int zPosIncrement = 0;
            int i = 0;
            int j = -1;
            for (int l = 0; l < 1024; ++l) {
                if (xPosIncrement > -16 && xPosIncrement <= 16 && zPosIncrement > -16 && zPosIncrement <= 16) {
                    BlockPos blockpos1 = findSpawnChunk(world, chunkpos.x + xPosIncrement << 4, chunkpos.z + zPosIncrement << 4);
                    if (blockpos1 != null) {
                        worldInfo.setSpawn(blockpos1);
                        break;
                    }
                }
                if (xPosIncrement == zPosIncrement || xPosIncrement < 0 && xPosIncrement == -zPosIncrement || xPosIncrement > 0 && xPosIncrement == 1 - zPosIncrement) {
                    int temp = i;
                    i = -j;
                    j = temp;
                }
                xPosIncrement += i;
                zPosIncrement += j;
            }
        }
    }
}
