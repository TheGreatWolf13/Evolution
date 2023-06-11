package tgw.evolution.mixin;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public interface ClipContextAccessor {

    @Accessor
    CollisionContext getCollisionContext();

    @Accessor
    @Mutable
    void setBlock(ClipContext.Block block);

    @Accessor
    @Mutable
    void setFluid(ClipContext.Fluid fluid);
}
