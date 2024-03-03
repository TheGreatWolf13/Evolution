package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchPoiManager;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

@Mixin(PoiManager.class)
public abstract class MixinPoiManager extends SectionStorage<PoiSection> implements PatchPoiManager {

    public MixinPoiManager(Path path,
                           Function<Runnable, Codec<PoiSection>> function,
                           Function<Runnable, PoiSection> function2,
                           DataFixer dataFixer,
                           DataFixTypes dataFixTypes,
                           boolean bl,
                           LevelHeightAccessor levelHeightAccessor) {
        super(path, function, function2, dataFixer, dataFixTypes, bl, levelHeightAccessor);
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
}
