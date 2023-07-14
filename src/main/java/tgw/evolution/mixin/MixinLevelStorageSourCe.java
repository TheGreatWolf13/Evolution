package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchLevelSummary;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;

@Mixin(LevelStorageSource.class)
public abstract class MixinLevelStorageSourCe {

    @Shadow @Final static Logger LOGGER;
    @Shadow @Final static DateTimeFormatter FORMATTER;
    @Shadow @Final Path baseDir;

    /**
     * @author TheGreatWolf
     * @reason Calculate size on disk
     */
    @SuppressWarnings("ObjectAllocationInLoop")
    @Overwrite
    public List<LevelSummary> getLevelList() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
        }
        OList<LevelSummary> summaries = new OArrayList<>();
        File[] files = this.baseDir.toFile().listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                boolean isLocked;
                try {
                    isLocked = DirectoryLock.isLocked(file.toPath());
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to read {} lock", file, exception);
                    continue;
                }
                try {
                    LevelSummary summary = this.readLevelData(file, this.levelSummaryReader(file, isLocked));
                    if (summary != null) {
                        ((PatchLevelSummary) summary).setSizeOnDisk(MathHelper.calculateSizeOnDisk(file.toPath()));
                        summaries.add(summary);
                    }
                }
                catch (OutOfMemoryError e) {
                    MemoryReserve.release();
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", file);
                    throw e;
                }
                catch (StackOverflowError e) {
                    LOGGER.error(LogUtils.FATAL_MARKER,
                                 "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.",
                                 file);
                    File levelDat = new File(file, "level.dat");
                    File levelDatOld = new File(file, "level.dat_old");
                    File levelDataCorr = new File(file, "level.dat_corrupted_" + LocalDateTime.now().format(FORMATTER));
                    Util.safeReplaceOrMoveFile(levelDat, levelDatOld, levelDataCorr, true);
                    throw e;
                }
            }
        }
        return summaries;
    }

    @Shadow
    abstract BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File pSaveDir, boolean pLocked);

    @Shadow
    @Nullable
    abstract <T> T readLevelData(File pSaveDir, BiFunction<File, DataFixer, T> pLevelDatReader);
}
