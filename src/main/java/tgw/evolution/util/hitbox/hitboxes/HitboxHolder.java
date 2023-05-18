package tgw.evolution.util.hitbox.hitboxes;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class HitboxHolder<E extends Entity> {

    private final Supplier<HitboxEntity<E>> supplier;
    private @Nullable HitboxEntity<E> client;
    private @Nullable HitboxEntity<E> server;

    public HitboxHolder(Supplier<HitboxEntity<E>> supplier) {
        this.supplier = supplier;
    }

    public HitboxEntity<E> get(E entity) {
        if (entity.level.isClientSide) {
            if (this.client == null) {
                this.client = this.supplier.get();
            }
            return this.client;
        }
        if (this.server == null) {
            this.server = this.supplier.get();
        }
        return this.server;
    }
}
