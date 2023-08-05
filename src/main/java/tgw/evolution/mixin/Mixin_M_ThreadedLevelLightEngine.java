package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.collection.lists.LList;

import java.util.function.IntSupplier;

@Mixin(ThreadedLevelLightEngine.class)
public abstract class Mixin_M_ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {

    public Mixin_M_ThreadedLevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
        super(lightChunkGetter, bl, bl2);
    }

    @Shadow
    protected abstract void addTask(int i, int j, ThreadedLevelLightEngine.TaskType taskType, Runnable runnable);

    @Shadow
    protected abstract void addTask(int i,
                                    int j,
                                    IntSupplier intSupplier,
                                    ThreadedLevelLightEngine.TaskType taskType,
                                    Runnable runnable);

    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.checkBlock_(pos.asLong());
    }

    @Override
    public void checkBlock_(long pos) {
        this.addTask(SectionPos.blockToSectionCoord(BlockPos.getX(pos)), SectionPos.blockToSectionCoord(BlockPos.getZ(pos)),
                     ThreadedLevelLightEngine.TaskType.POST_UPDATE, () -> super.checkBlock_(pos));
    }

    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        this.enableLightSources_(chunkPos.x, chunkPos.z, bl);
    }

    @Override
    public void enableLightSources_(int secX, int secZ, boolean bl) {
        this.addTask(secX, secZ, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, () -> super.enableLightSources_(secX, secZ, bl));
    }

    @Overwrite
    protected void method_17312(ChunkAccess chunk, ChunkPos chunkPos, boolean isLighted) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < chunk.getSectionsCount(); ++i) {
            LevelChunkSection section = sections[i];
            if (!section.hasOnlyAir()) {
                int secY = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                super.updateSectionStatus_sec(chunkPos.x, secY, chunkPos.z, false);
            }
        }
        super.enableLightSources_(chunkPos.x, chunkPos.z, true);
        if (!isLighted) {
            LList lights = chunk.getLights_();
            for (int i = 0, len = lights.size(); i < len; ++i) {
                long pos = lights.getLong(i);
                super.onBlockEmissionIncrease_(pos, chunk.getLightEmission_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos)));
            }
        }
    }

    @Overwrite
    protected void method_20388(ChunkPos chunkPos) {
        super.retainData(chunkPos, false);
        super.enableLightSources_(chunkPos.x, chunkPos.z, false);
        int x = chunkPos.x;
        int z = chunkPos.z;
        for (int y = this.getMinLightSection(), len = this.getMaxLightSection(); y < len; ++y) {
            super.queueSectionData_(LightLayer.BLOCK, x, y, z, null, true);
            super.queueSectionData_(LightLayer.SKY, x, y, z, null, true);
        }
        for (int y = this.levelHeightAccessor.getMinSection(), len = this.levelHeightAccessor.getMaxSection(); y < len; ++y) {
            super.updateSectionStatus_sec(x, y, z, true);
        }
    }

    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
    }

    @Override
    public void onBlockEmissionIncrease_(long pos, int lightEmission) {
        throw Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void queueSectionData_(LightLayer lightLayer, int secX, int secY, int secZ, @Nullable DataLayer dataLayer, boolean bl) {
        this.addTask(secX, secZ, () -> {
            return 0;
        }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, () -> {
            super.queueSectionData_(lightLayer, secX, secY, secZ, dataLayer, bl);
        });
    }

    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_sec(secPos.x(), secPos.y(), secPos.z(), hasOnlyAir);
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean hasOnlyAir) {
        this.addTask(secX, secZ, () -> {
            return 0;
        }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, () -> {
            super.updateSectionStatus_sec(secX, secY, secZ, hasOnlyAir);
        });
    }
}
