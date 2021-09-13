package tgw.evolution.events;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.entities.misc.EntityPlayerCorpse;

import javax.annotation.Nullable;

public class WorldEvents {

    @Nullable
    public static BlockPos findSpawn(World world, int posX, int posZ) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable(posX, 0, posZ);
        Biome biome = world.getBiome(mutablePos);
        BlockState biomeSurfaceState = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
        Chunk chunk = world.getChunk(posX >> 4, posZ >> 4);
        int i = chunk.getHeight(Heightmap.Type.MOTION_BLOCKING, posX & 15, posZ & 15);
        if (i < 0) {
            return null;
        }
        if (chunk.getHeight(Heightmap.Type.WORLD_SURFACE, posX & 15, posZ & 15) > chunk.getHeight(Heightmap.Type.OCEAN_FLOOR, posX & 15, posZ & 15)) {
            return null;
        }
        for (int j = i + 1; j >= 0; --j) {
            mutablePos.set(posX, j, posZ);
            BlockState stateAtWorld = world.getBlockState(mutablePos);
            if (!stateAtWorld.getFluidState().isEmpty()) {
                break;
            }
            if (BlockUtils.compareVanillaBlockStates(biomeSurfaceState, stateAtWorld)) {
                return mutablePos.above().immutable();
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            MinecraftServer server = world.getServer();
            if (server.getWorldData().getGameType() != GameType.CREATIVE) {
                world.getGameRules().getRule(GameRules.RULE_REDUCEDDEBUGINFO).set(true, server);
            }
        }
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        EntityPlayerCorpse.setProfileCache(server.getProfileCache());
        EntityPlayerCorpse.setSessionService(server.getSessionService());
    }
}
