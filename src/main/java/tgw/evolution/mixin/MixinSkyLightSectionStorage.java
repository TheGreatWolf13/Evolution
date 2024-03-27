package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SkyLightSectionStorage.class)
public abstract class MixinSkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {

    public MixinSkyLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, SkyLightSectionStorage.SkyDataLayerStorageMap dataLayerStorageMap) {
        super(lightLayer, lightChunkGetter, dataLayerStorageMap);
    }

    @Shadow
    private static DataLayer repeatFirstLayer(DataLayer dataLayer) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public DataLayer createDataLayer(long pos) {
        DataLayer dataLayer;
        synchronized (this.queuedSections) {
            dataLayer = this.queuedSections.get(pos);
        }
        if (dataLayer != null) {
            return dataLayer;
        }
        long m = SectionPos.offset(pos, Direction.UP);
        int i = this.updatingSectionData.topSections.get(SectionPos.getZeroNode(pos));
        if (i != this.updatingSectionData.currentLowestY && SectionPos.y(m) < i) {
            DataLayer dataLayer2;
            while ((dataLayer2 = this.getDataLayer(m, true)) == null) {
                m = SectionPos.offset(m, Direction.UP);
            }
            return repeatFirstLayer(dataLayer2);
        }
        return new DataLayer();
    }
}
