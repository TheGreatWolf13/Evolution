package tgw.evolution.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.util.hitbox.HitboxZombie;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends MonsterEntity implements INeckPosition {

    public ZombieEntityMixin(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
    }

    @Override
    public float getCameraYOffset() {
        return 4 / 16.0f;
    }

    @Override
    public float getCameraZOffset() {
        return 4 / 16.0f;
    }

    @Override
    public Vector3d getNeckPoint() {
        return HitboxZombie.NECK_STANDING;
    }
}
