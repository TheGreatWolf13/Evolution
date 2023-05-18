package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ILivingEntityPatch<Villager> {

    public VillagerMixin(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public double getBaseHealth() {
        return 100;
    }

    @Override
    public double getBaseMass() {
        return 70;
    }

    @Override
    public double getBaseWalkForce() {
        return 5_000 * SI.NEWTON;
    }

    @Override
    public @Nullable HitboxEntity<Villager> getHitboxes() {
        return LegacyEntityHitboxes.VILLAGER.get((Villager) (Object) this);
    }
}
