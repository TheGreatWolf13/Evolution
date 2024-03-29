package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchLevelSummary;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;

@Mixin(LevelStorageSource.class)
public abstract class Mixin_M_LevelStorageSource {

    @Shadow @Final static Logger LOGGER;
    @Shadow @Final static DateTimeFormatter FORMATTER;
    @Shadow @Final Path baseDir;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static BiFunction<File, DataFixer, PrimaryLevelData> getLevelData(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig, Lifecycle lifecycle) {
        return (file, dataFixer) -> {
            try {
                CompoundTag dataTag = NbtIo.readCompressed(file).getCompound("Data");
                CompoundTag playerTag = NBTHelper.getCompound(dataTag, "Player");
                dataTag.remove("Player");
                int i = NBTHelper.getIntOrElse(dataTag, "DataVersion", -1);
                WorldGenSettings worldGenSettings = NBTHelper.parseWorldGenSettings((RegistryOps<Tag>) dynamicOps, dataTag, LOGGER);
                LevelVersion levelVersion = NBTHelper.parseLevelVersion(dataTag);
                LevelSettings levelSettings = NBTHelper.parseLevelSettings(dataTag, dataPackConfig);
                return NBTHelper.parsePrimaryLevelData(dataTag, dataFixer, i, playerTag, levelSettings, levelVersion, worldGenSettings, lifecycle);
            }
            catch (Exception e) {
                LOGGER.error("Exception reading {}", file, e);
                //noinspection ReturnOfNull
                return null;
            }
        };
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static <T> Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<T> dynamic, DataFixer dataFixer, int i) {
        throw new AbstractMethodError();
    }

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
                    LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.", file);
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
    abstract @Nullable <T> T readLevelData(File pSaveDir, BiFunction<File, DataFixer, T> pLevelDatReader);
}
