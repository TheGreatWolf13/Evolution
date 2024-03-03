package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchPoiSection;
import tgw.evolution.util.math.BlockPosUtil;

import java.util.Map;
import java.util.Set;

@Mixin(PoiSection.class)
public abstract class MixinPoiSection implements PatchPoiSection {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Map<PoiType, Set<PoiRecord>> byType;
    @Shadow @Final private Short2ObjectMap<PoiRecord> records;
    @Shadow @Final private Runnable setDirty;

    @Shadow
    protected abstract boolean add(PoiRecord poiRecord);

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
        if (this.add(new PoiRecord(new BlockPos(x, y, z), poi, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ [{}, {}, {}]", poi, x, y, z);
            this.setDirty.run();
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
        PoiRecord poiRecord = this.records.remove(BlockPosUtil.sectionRelativePos(x, y, z));
        if (poiRecord == null) {
            LOGGER.error("POI data mismatch: never registered at [{}, {}, {}]", x, y, z);
        }
        else {
            this.byType.get(poiRecord.getPoiType()).remove(poiRecord);
            LOGGER.debug("Removed POI of type {} @ {}", poiRecord.getPoiType(), poiRecord.getPos());
            this.setDirty.run();
        }
    }
}
