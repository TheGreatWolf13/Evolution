package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.Hitbox;

public class AdvancedEntityHitResult extends EntityHitResult {

    private final @Nullable Hitbox hitbox;

    public AdvancedEntityHitResult(Entity entity, double x, double y, double z, @Nullable Hitbox hitbox) {
        super(entity);
        this.hitbox = hitbox;
        this.set(x, y, z);
    }

    public @Nullable Hitbox getHitbox() {
        return this.hitbox;
    }
}
