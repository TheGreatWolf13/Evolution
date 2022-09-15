package tgw.evolution.events;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;

import java.util.random.RandomGenerator;

public class ChunkEvents {

    private static final int RANGE = 8;
    private static int tick = 200;

    private static void doChunkActions(Level level, ChunkPos chunkPos) {
        Integer[] amount = {0};
        Integer[] delta = {0};
        CapabilityChunkStorage.getChunkStorage(level.getChunk(chunkPos.x, chunkPos.z)).ifPresent(chunkStorages -> {
            if (chunkStorages.getElementStored(EnumStorage.NITROGEN) < 10_000) {
                if (chunkStorages.removeElement(EnumStorage.GAS_NITROGEN, 2)) {
                    chunkStorages.addElement(EnumStorage.NITROGEN, 4);
                }
            }
            amount[0] = Math.min(chunkStorages.getElementStored(EnumStorage.ORGANIC) / 4, 250);
            if (chunkStorages.removeElement(EnumStorage.OXYGEN, amount[0])) {
                chunkStorages.removeElement(EnumStorage.ORGANIC, amount[0]);
                chunkStorages.addElement(EnumStorage.CARBON_DIOXIDE, amount[0]);
                chunkStorages.addElement(EnumStorage.WATER, amount[0]);
            }
            for (ChunkPos pos : getNeighbours(chunkPos)) {
//                if (level.getChunkSource().isEntityTickingChunk(pos)) {
//                    //noinspection ObjectAllocationInLoop
//                    CapabilityChunkStorage.getChunkStorage(level.getChunk(pos.x, pos.z)).ifPresent(storage -> {
//                        delta[0] = chunkStorages.getElementStored(EnumStorage.GAS_NITROGEN) - storage.getElementStored(EnumStorage.GAS_NITROGEN);
//                        if (delta[0] > 0) {
//                            if (chunkStorages.removeElement(EnumStorage.GAS_NITROGEN, delta[0] / 4)) {
//                                storage.addElement(EnumStorage.GAS_NITROGEN, delta[0] / 4);
//                            }
//                        }
//                        delta[0] = chunkStorages.getElementStored(EnumStorage.OXYGEN) - storage.getElementStored(EnumStorage.OXYGEN);
//                        if (delta[0] > 0) {
//                            if (chunkStorages.removeElement(EnumStorage.OXYGEN, delta[0] / 4)) {
//                                storage.addElement(EnumStorage.OXYGEN, delta[0] / 4);
//                            }
//                        }
//                        delta[0] = chunkStorages.getElementStored(EnumStorage.CARBON_DIOXIDE) - storage.getElementStored(EnumStorage
//                        .CARBON_DIOXIDE);
//                        if (delta[0] > 0) {
//                            if (chunkStorages.removeElement(EnumStorage.CARBON_DIOXIDE, delta[0] / 4)) {
//                                storage.addElement(EnumStorage.CARBON_DIOXIDE, delta[0] / 4);
//                            }
//                        }
//                    });
//                }
            }
        });
    }

    private static ChunkPos[] getNeighbours(ChunkPos chunkPos) {
        return new ChunkPos[]{new ChunkPos(chunkPos.x + 1, chunkPos.z),
                              new ChunkPos(chunkPos.x - 1, chunkPos.z),
                              new ChunkPos(chunkPos.x, chunkPos.z + 1),
                              new ChunkPos(chunkPos.x, chunkPos.z - 1)};
    }

    private static ChunkPos getRandom(int chunkX, int chunkZ, RandomGenerator random) {
        int i = random.nextInt(RANGE * 2 + 1) - RANGE;
        int j = random.nextInt(RANGE * 2 + 1) - RANGE;
        return new ChunkPos(chunkX + i, chunkZ + j);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER) {
            return;
        }
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (tick-- == 0) {
            tick = 200;
            ChunkPos chunkPos = getRandom(event.player.chunkPosition().x, event.player.chunkPosition().z, event.player.level.random);
//            if (event.player.level.getChunkSource().isEntityTickingChunk(chunkPos)) {
//                doChunkActions(event.player.level, chunkPos);
//            }
        }
    }
}
