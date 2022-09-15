package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IClipContextPatch;

@Mixin(ClipContext.class)
public abstract class ClipContextMixin implements IClipContextPatch {

    @Mutable
    @Shadow
    @Final
    private ClipContext.Block block;

    @Mutable
    @Shadow
    @Final
    private CollisionContext collisionContext;

    @Mutable
    @Shadow
    @Final
    private ClipContext.Fluid fluid;

    @Mutable
    @Shadow
    @Final
    private Vec3 from;

    @Mutable
    @Shadow
    @Final
    private Vec3 to;

    @Override
    public void setBlock(ClipContext.Block block) {
        this.block = block;
    }

    @Override
    public void setEntity(@Nullable Entity entity) {
        this.collisionContext = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
    }

    @Override
    public void setFluid(ClipContext.Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public void setFrom(Vec3 from) {
        this.from = from;
    }

    @Override
    public void setTo(Vec3 to) {
        this.to = to;
    }
}
