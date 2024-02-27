package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchLevelLightEngine;
import tgw.evolution.util.constants.Lightlayer;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.StarLightEngine;
import tgw.evolution.world.lighting.StarLightInterface;
import tgw.evolution.world.lighting.WorldUtil;

@Mixin(LevelLightEngine.class)
public abstract class Mixin_CF_LevelLightEngine implements PatchLevelLightEngine, LightEventListener {

    @Unique protected final Long2ObjectOpenHashMap<SWMRNibbleArray[]> blockLightMap;
    @Unique protected final Long2ObjectOpenHashMap<SWMRNibbleArray[]> skyLightMap;
    @Unique private final StarLightInterface lightEngine;
    @Mutable @Shadow @Final @RestoreFinal protected LevelHeightAccessor levelHeightAccessor;
    @Shadow @Final @DeleteField private LayerLightEngine<?, ?> blockEngine;
    @Shadow @Final @DeleteField private LayerLightEngine<?, ?> skyEngine;

    @DummyConstructor
    public Mixin_CF_LevelLightEngine() {
        //This will never run, it is not compiled to the class' bytecode.
        this.lightEngine = new StarLightInterface(null, false, false, (LevelLightEngine) (Object) this);
        this.blockLightMap = new Long2ObjectOpenHashMap<>();
        this.skyLightMap = new Long2ObjectOpenHashMap<>();
    }

    @ModifyConstructor
    public Mixin_CF_LevelLightEngine(LightChunkGetter chunkGetter, boolean hasBL, boolean hasSL) {
        this.blockLightMap = new Long2ObjectOpenHashMap<>();
        this.skyLightMap = new Long2ObjectOpenHashMap<>();
        this.levelHeightAccessor = chunkGetter.getLevel();
        if (chunkGetter.getLevel() instanceof Level) {
            this.lightEngine = new StarLightInterface(chunkGetter, hasSL, hasBL, (LevelLightEngine) (Object) this);
        }
        else {
            this.lightEngine = new StarLightInterface(null, hasSL, hasBL, (LevelLightEngine) (Object) this);
        }
    }

    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.checkBlock_(pos.asLong());
    }

    @Override
    public void checkBlock_(long pos) {
        this.lightEngine.blockChange(pos);
    }

    @Override
    public void clientChunkLoad(ChunkPos pos, LevelChunk chunk) {
        if (((Object) this).getClass() != LevelLightEngine.class) {
            throw new IllegalStateException("This hook is for the CLIENT ONLY");
        }
        long key = pos.toLong();
        SWMRNibbleArray[] blockNibbles = this.blockLightMap.get(key);
        SWMRNibbleArray[] skyNibbles = this.skyLightMap.get(key);
        if (blockNibbles != null) {
            chunk.setBlockNibbles(blockNibbles);
        }
        if (skyNibbles != null) {
            chunk.setSkyNibbles(skyNibbles);
        }
    }

    @Override
    public void clientRemoveLightData(int chunkX, int chunkZ) {
        if (((Object) this).getClass() != LevelLightEngine.class) {
            throw new IllegalStateException("This hook is for the CLIENT ONLY");
        }
        long pos = ChunkPos.asLong(chunkX, chunkZ);
        this.blockLightMap.remove(pos);
        this.skyLightMap.remove(pos);
    }

    @Override
    public void clientUpdateLight(LightLayer lightType, int secX, int secY, int secZ, byte @Nullable [] nibble, boolean trustEdges) {
        if (((Object) this).getClass() != LevelLightEngine.class) {
            throw new IllegalStateException("This hook is for the CLIENT ONLY");
        }
        // data storage changed with new light impl
        ChunkAccess chunk = this.getLightEngine().getAnyChunkNow(secX, secZ);
        Level level = this.lightEngine.getLevel();
        assert level != null;
        switch (lightType) {
            case BLOCK: {
                long key = ChunkPos.asLong(secX, secZ);
                SWMRNibbleArray[] blockNibbles = this.blockLightMap.get(key);
                if (blockNibbles == null) {
                    blockNibbles = StarLightEngine.getFilledEmptyLight(level);
                    this.blockLightMap.put(key, blockNibbles);
                }
                blockNibbles[secY - WorldUtil.getMinLightSection(level)] = SWMRNibbleArray.fromVanilla(nibble);
                if (chunk != null) {
                    chunk.setBlockNibbles(blockNibbles);
                    assert this.lightEngine.getLightAccess() != null;
                    this.lightEngine.getLightAccess().onLightUpdate_(LightLayer.BLOCK, secX, secY, secZ);
                }
                break;
            }
            case SKY: {
                long key = ChunkPos.asLong(secX, secZ);
                SWMRNibbleArray[] skyNibbles = this.skyLightMap.get(key);
                if (skyNibbles == null) {
                    skyNibbles = StarLightEngine.getFilledEmptyLight(level);
                    this.skyLightMap.put(key, skyNibbles);
                }
                skyNibbles[secY - WorldUtil.getMinLightSection(level)] = SWMRNibbleArray.fromVanilla(nibble);
                if (chunk != null) {
                    chunk.setSkyNibbles(skyNibbles);
                    assert this.lightEngine.getLightAccess() != null;
                    this.lightEngine.getLightAccess().onLightUpdate_(LightLayer.SKY, secX, secY, secZ);
                }
                break;
            }
        }
    }

    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        //Do nothing
    }

    @Override
    public int getColoredBrightness(@Lightlayer int layer, long pos) {
//        return switch (layer) {
//            case Lightlayer.SKY -> this.skyEngine != null ? this.skyEngine.getLightValue_(pos) : 0;
//            case Lightlayer.RED -> this.blockEngine != null ? this.blockEngine.getLightValue_(pos) : 0;
//            default -> throw new IllegalArgumentException("Unknown layer");
//        };
        return 0;
    }

    @Overwrite
    public String getDebugData(LightLayer lightLayer, SectionPos sectionPos) {
        return "n/a";
    }

    @Overwrite
    public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
        return lightLayer == LightLayer.BLOCK ? this.lightEngine.getBlockReader() : this.lightEngine.getSkyReader();
    }

    @Override
    public StarLightInterface getLightEngine() {
        return this.lightEngine;
    }

    @Overwrite
    public int getRawBrightness(BlockPos pos, int reducedSkyLight) {
        Evolution.deprecatedMethod();
        return this.getRawBrightness_(pos.asLong(), reducedSkyLight);
    }

    @Override
    public int getRawBrightness_(long pos, int reducedSkyLight) {
        return this.lightEngine.getRawBrightness(pos, reducedSkyLight);
    }

    @Override
    @Overwrite
    public boolean hasLightWork() {
        return this.lightEngine.hasUpdates();
    }

    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        //No op
    }

    @Overwrite
    public void queueSectionData(LightLayer lightLayer, SectionPos secPos, @Nullable DataLayer dataLayer, boolean trustEdges) {
        Evolution.deprecatedMethod();
        //Do nothing
    }

    @Overwrite
    public void retainData(ChunkPos chunkPos, boolean retainData) {
        //Not used
    }

    @Override
    @Overwrite
    public int runUpdates(int limit, boolean bl, boolean bl2) {
        final boolean hadUpdates = this.hasLightWork();
        this.lightEngine.propagateChanges();
        return hadUpdates ? 1 : 0;
    }

    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean notReady) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_sec(secPos.x(), secPos.y(), secPos.z(), notReady);
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean notReady) {
        this.lightEngine.sectionChange(secX, secY, secZ, notReady);
    }
}
