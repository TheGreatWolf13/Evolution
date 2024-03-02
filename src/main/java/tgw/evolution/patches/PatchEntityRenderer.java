package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import tgw.evolution.client.renderer.ambient.DynamicLights;

public interface PatchEntityRenderer<T extends Entity> {

    default int getBlockLightLevel_(T entity, int x, int y, int z) {
        if (entity.isOnFire()) {
            return DynamicLights.FULL_LIGHTMAP_NO_SKY;
        }
        int bl = entity.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue_(BlockPos.asLong(x, y, z));
        int r = bl & 31;
        int g = bl >>> 5 & 31;
        int b = bl >>> 10 & 31;
        return r | g << 5 | b << 20;
    }

    default int getSkyLightLevel_(T entity, int x, int y, int z) {
        return entity.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue_(BlockPos.asLong(x, y, z));
    }
}
