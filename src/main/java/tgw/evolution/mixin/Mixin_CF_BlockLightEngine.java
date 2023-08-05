package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;

@Mixin(BlockLightEngine.class)
public abstract class Mixin_CF_BlockLightEngine
        extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {

    @Shadow @Final @DeleteField private BlockPos.MutableBlockPos pos;

    @DummyConstructor
    public Mixin_CF_BlockLightEngine(LightChunkGetter lightChunkGetter,
                                     LightLayer lightLayer,
                                     BlockLightSectionStorage layerLightSectionStorage) {
        super(lightChunkGetter, lightLayer, layerLightSectionStorage);
    }

    @ModifyConstructor
    public Mixin_CF_BlockLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
    }

    @Overwrite
    private int getLightEmission(long pos) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockGetter blockGetter = this.chunkSource.getChunkForLighting(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        return blockGetter != null ? blockGetter.getLightEmission_(x, y, z) : 0;
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
