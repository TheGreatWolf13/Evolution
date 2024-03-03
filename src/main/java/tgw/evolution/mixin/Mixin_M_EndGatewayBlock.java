package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(EndGatewayBlock.class)
public abstract class Mixin_M_EndGatewayBlock extends BaseEntityBlock {

    public Mixin_M_EndGatewayBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (level.getBlockEntity_(x, y, z) instanceof TheEndGatewayBlockEntity tile) {
            int amount = tile.getParticleAmount();
            for (int j = 0; j < amount; ++j) {
                double px = x + random.nextDouble();
                double py = y + random.nextDouble();
                double pz = z + random.nextDouble();
                double vx = (random.nextDouble() - 0.5) * 0.5;
                double vy = (random.nextDouble() - 0.5) * 0.5;
                double vz = (random.nextDouble() - 0.5) * 0.5;
                int l = random.nextInt(2) * 2 - 1;
                if (random.nextBoolean()) {
                    pz = z + 0.5 + 0.25 * l;
                    vz = random.nextFloat() * 2.0F * l;
                }
                else {
                    px = x + 0.5 + 0.25 * l;
                    vx = random.nextFloat() * 2.0F * l;
                }
                level.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
            }
        }
    }
}
