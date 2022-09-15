package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.Mob;
import tgw.evolution.util.hitbox.hms.HMEntity;

public interface HRMob<T extends Mob, M extends HMEntity<T>> extends HRLivingEntity<T, M> {
    
}
