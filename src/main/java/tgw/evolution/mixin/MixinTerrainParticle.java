package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

@Mixin(TerrainParticle.class)
public abstract class MixinTerrainParticle extends TextureSheetParticle {

    @Shadow @Final private BlockPos pos;

    public MixinTerrainParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        return i == 0 && this.level.hasChunkAt(this.pos) ? EvLevelRenderer.getLightColor(this.level, this.pos) : i;
    }
}
