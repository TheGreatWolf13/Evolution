package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchPoiManager;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(PoiManager.class)
public abstract class Mixin_M_PoiManager extends SectionStorage<PoiSection> implements PatchPoiManager {

    public Mixin_M_PoiManager(Path path, Function<Runnable, Codec<PoiSection>> function, Function<Runnable, PoiSection> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, LevelHeightAccessor levelHeightAccessor) {
        super(path, function, function2, dataFixer, dataFixTypes, bl, levelHeightAccessor);
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static boolean mayHavePoi(LevelChunkSection section) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Unique
    private static void updateFromSection_(LevelChunkSection section, int secX, int secY, int secZ, BiConsumer<BlockPos, PoiType> biConsumer) {
        int minX = secX << 4;
        int minY = secY << 4;
        int minZ = secZ << 4;
        for (int dz = 0; dz < 16; ++dz) {
            for (int dy = 0; dy < 16; ++dy) {
                for (int dx = 0; dx < 16; ++dx) {
                    BlockState blockState = section.getBlockState(dx, dy, dz);
                    Optional<PoiType> type = PoiType.forState(blockState);
                    if (type.isPresent()) {
                        //noinspection ObjectAllocationInLoop
                        biConsumer.accept(new BlockPos(minX + dx, minY + dy, minZ + dz), type.get());
                    }
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void add(BlockPos pos, PoiType poi) {
        Evolution.deprecatedMethod();
        this.add_(pos.getX(), pos.getY(), pos.getZ(), poi);
    }

    @Override
    public void add_(int x, int y, int z, PoiType poi) {
        this.getOrCreate(SectionPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z)))
            .add_(x, y, z, poi);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void checkConsistencyWithBlocks(ChunkPos chunkPos, LevelChunkSection section) {
        int secX = chunkPos.x;
        int secY = SectionPos.blockToSectionCoord(section.bottomBlockY());
        int secZ = chunkPos.z;
        PoiSection poiSection = (PoiSection) this.getOrLoad_(SectionPos.asLong(secX, secY, secZ));
        if (poiSection != null) {
            poiSection.refresh(biConsumer -> {
                if (mayHavePoi(section)) {
                    updateFromSection_(section, secX, secY, secZ, biConsumer);
                }
            });
        }
        else {
            if (mayHavePoi(section)) {
                PoiSection poiSec = this.getOrCreate(SectionPos.asLong(secX, secY, secZ));
                updateFromSection_(section, secX, secY, secZ, (pos, poiType) -> poiSec.add_(pos.getX(), pos.getY(), pos.getZ(), poiType));
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void remove(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.remove_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void remove_(int x, int y, int z) {
        Optional<PoiSection> section = this.getOrLoad(
                SectionPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z)));
        if (section.isPresent()) {
            section.get().remove_(x, y, z);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void updateFromSection(LevelChunkSection levelChunkSection, SectionPos sectionPos, BiConsumer<BlockPos, PoiType> biConsumer) {
        throw new AbstractMethodError();
    }
}
