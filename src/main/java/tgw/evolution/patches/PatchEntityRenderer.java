package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;

public interface PatchEntityRenderer<T extends Entity> {

    default int getBlockLightLevel_(T entity, int x, int y, int z) {
        if (entity.isOnFire()) {
            return 0xF0_00FF;
        }
        int bl = entity.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue_(BlockPos.asLong(x, y, z));
        int r = bl & 0xF;
        int g = bl >>> 5 & 0xF;
        int b = bl >>> 10 & 0xF;
        return r | g << 4 | b << 20;
    }

    default int getSkyLightLevel_(T entity, int x, int y, int z) {
        return entity.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue_(BlockPos.asLong(x, y, z));
    }
}
