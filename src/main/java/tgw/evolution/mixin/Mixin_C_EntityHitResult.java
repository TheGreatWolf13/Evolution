package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchEntityHitResult;

@Mixin(EntityHitResult.class)
public abstract class Mixin_C_EntityHitResult extends HitResult implements PatchEntityHitResult {

    @Mutable @Shadow @Final @RestoreFinal private Entity entity;

    @ModifyConstructor
    public Mixin_C_EntityHitResult(Entity entity, Vec3 v) {
        super(Vec3.ZERO);
        Evolution.deprecatedConstructor();
        this.set(v.x, v.y, v.z);
        this.entity = entity;
    }

    @ModifyConstructor
    public Mixin_C_EntityHitResult(Entity entity) {
        super(Vec3.ZERO);
        Vec3 position = entity.position();
        this.set(position.x, position.y, position.z);
        this.entity = entity;
    }
}
