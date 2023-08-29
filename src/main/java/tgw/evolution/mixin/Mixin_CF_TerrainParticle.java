package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockDryGrass;
import tgw.evolution.blocks.BlockGrass;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchTerrainParticle;
import tgw.evolution.util.physics.Physics;

@Mixin(TerrainParticle.class)
public abstract class Mixin_CF_TerrainParticle extends TextureSheetParticle implements PatchTerrainParticle {

    @Shadow @Final @DeleteField private BlockPos pos;
    @Unique private int posX;
    @Unique private int posY;
    @Unique private int posZ;
    @Mutable @Shadow @Final @RestoreFinal private float uo;
    @Mutable @Shadow @Final @RestoreFinal private float vo;

    @ModifyConstructor
    public Mixin_CF_TerrainParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, BlockState state) {
        super(level, x, y, z, vx, vy, vz);
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
        this.gravity = (float) Physics.getRestLocalGravity(level, y, z);
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.posX = Mth.floor(x);
        this.posY = Mth.floor(y);
        this.posZ = Mth.floor(z);
        Block block = state.getBlock();
        if (block != Blocks.GRASS_BLOCK && !(block instanceof BlockGrass) && !(block instanceof BlockDryGrass)) {
            int color = Minecraft.getInstance().getBlockColors().getColor_(state, level, this.posX, this.posY, this.posZ, 0);
            this.rCol *= (color >> 16 & 255) / 255.0F;
            this.gCol *= (color >> 8 & 255) / 255.0F;
            this.bCol *= (color & 255) / 255.0F;
        }
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @ModifyConstructor
    public Mixin_CF_TerrainParticle(ClientLevel clientLevel,
                                    double d,
                                    double e,
                                    double f,
                                    double g,
                                    double h,
                                    double i,
                                    BlockState blockState,
                                    BlockPos blockPos) {
        super(clientLevel, d, e, f, g, h, i);
        Evolution.warn("Deprecated constructor called by {}", Thread.currentThread().getStackTrace()[2]);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        return i == 0 && this.level.hasChunkAt(this.posX, this.posZ) ? EvLevelRenderer.getLightColor(this.level, this.posX, this.posY, this.posZ) : i;
    }

    @Override
    public void setBlockPos(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd += this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }
}
