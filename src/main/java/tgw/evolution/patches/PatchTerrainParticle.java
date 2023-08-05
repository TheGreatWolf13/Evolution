package tgw.evolution.patches;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public interface PatchTerrainParticle {

    @Contract(value = "_, _, _, _, _, _, _, _, _, _, _ -> new")
    static TerrainParticle create(ClientLevel level,
                                  double x,
                                  double y,
                                  double z,
                                  double vx,
                                  double vy,
                                  double vz,
                                  BlockState state,
                                  int posX,
                                  int posY,
                                  int posZ) {
        TerrainParticle particle = new TerrainParticle(level, x, y, z, vx, vy, vz, state);
        particle.setBlockPos(posX, posY, posZ);
        if (!state.is(Blocks.GRASS_BLOCK)) {
            int color = Minecraft.getInstance().getBlockColors().getColor(state, level, new BlockPos(posX, posY, posZ), 0);
            particle.setColor(0.6f * (color >> 16 & 255) / 255.0F, 0.6f * (color >> 8 & 255) / 255.0F, 0.6f * (color & 255) / 255.0F);
        }
        return particle;
    }

    default void setBlockPos(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
