package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SlimeBlock.class)
public abstract class Mixin_M_SlimeBlock extends HalfTransparentBlock {

    public Mixin_M_SlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        throw new AbstractMethodError();
    }

    @Override
    public void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        double vy = Math.abs(velocity.y);
        if (vy < 0.1 && !entity.isSteppingCarefully()) {
            double e = 0.4 + vy * 0.2;
            entity.setDeltaMovement(velocity.x * e, velocity.y, velocity.z * e);
        }
        super.stepOn_(level, x, y, z, state, entity);
    }
}
