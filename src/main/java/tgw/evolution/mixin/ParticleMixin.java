package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

@Mixin(Particle.class)
public abstract class ParticleMixin {

    @Shadow @Final protected ClientLevel level;
    @Shadow protected double x;
    @Shadow protected double y;
    @Shadow protected double z;

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer, delay BlockPos allocation
     */
    @Overwrite
    protected int getLightColor(float partialTick) {
        int x = Mth.floor(this.x);
        int z = Mth.floor(this.z);
        return this.level.hasChunkAt(x, z) ? EvLevelRenderer.getLightColor(this.level, new BlockPos(x, Mth.floor(this.y), z)) : 0;
    }
}
