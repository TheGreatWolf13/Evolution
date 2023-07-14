package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.Hitbox;

public class AdvancedEntityHitResult extends EntityHitResult {

    private final @Nullable Hitbox hitbox;

    public AdvancedEntityHitResult(Entity entity, Vec3 hitVec, @Nullable Hitbox hitbox) {
        super(entity, hitVec);
        this.hitbox = hitbox;
    }

    public @Nullable Hitbox getHitbox() {
        return this.hitbox;
    }
}
