package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface PatchEntityRenderer<T extends Entity> {

    default int getBlockLightLevel_(T entity, int x, int y, int z) {
        if (entity.isOnFire()) {
            return 0xFF_00FF;
        }
        LevelLightEngine lightEngine = entity.level.getLightEngine();
        long pos = BlockPos.asLong(x, y, z);
        int bl = lightEngine.getLayerListener(LightLayer.BLOCK).getLightValue_(pos);
        int r = bl;
        int g = bl;
        int b = bl;
        return r | g << 4 | b << 20;
    }

    default int getSkyLightLevel_(T entity, int x, int y, int z) {
        return entity.level.getBrightness_(LightLayer.SKY, BlockPos.asLong(x, y, z));
    }
}
