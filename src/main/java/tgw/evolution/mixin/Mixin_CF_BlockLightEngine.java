package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightEngine;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;

@Mixin(BlockLightEngine.class)
public abstract class Mixin_CF_BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {

    @Unique private final boolean isClientSide;
    @Shadow @Final @DeleteField private BlockPos.MutableBlockPos pos;

    @ModifyConstructor
    public Mixin_CF_BlockLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
        this.isClientSide = lightChunkGetter.getLevel() instanceof Level level && level.isClientSide;
    }

    @Overwrite
    private int getLightEmission(long pos) {
        int dynamicLight = 0;
        if (this.isClientSide) {
            dynamicLight = ClientEvents.getInstance().getDynamicLights().get(pos);
            if (dynamicLight == 15) {
                return 15;
            }
        }
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockGetter blockGetter = this.chunkSource.getChunkForLighting(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        return blockGetter != null ? Math.max(blockGetter.getLightEmission_(x, y, z), dynamicLight) : dynamicLight;
    }

    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
    }

    @Override
    public void onBlockEmissionIncrease_(long pos, int lightEmission) {
        this.storage.runAllUpdates();
        this.checkEdge(Long.MAX_VALUE, pos, 15 - lightEmission, true);
    }
}
