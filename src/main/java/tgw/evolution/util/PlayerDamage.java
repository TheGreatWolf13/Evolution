package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionEffects;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public abstract class PlayerDamage {

    private static final Predicate<Entity> PREDICATE = EntityPredicates.CAN_AI_TARGET.and(e -> e != null && e.canBeCollidedWith() && e instanceof LivingEntity && !(e instanceof FakePlayer));
    private static final Random RAND = new Random();

    @Nullable
    public static EntityRayTraceResult rayTraceEntities(Entity shooter, Vec3d startVec, Vec3d endVec, AxisAlignedBB boundingBox, double distanceSquared) {
        World world = shooter.world;
        double range = distanceSquared;
        Entity entity = null;
        Vec3d vec3d = null;
        for (Entity entityInBoundingBox : world.getEntitiesInAABBexcluding(shooter, boundingBox, PREDICATE)) {
            AxisAlignedBB axisalignedbb = entityInBoundingBox.getBoundingBox();
            Optional<Vec3d> optional = axisalignedbb.rayTrace(startVec, endVec);
            if (axisalignedbb.contains(startVec)) {
                if (range >= 0.0D) {
                    entity = entityInBoundingBox;
                    vec3d = optional.orElse(startVec);
                    range = 0.0D;
                }
            }
            else if (optional.isPresent()) {
                Vec3d hitResult = optional.get();
                double actualDistanceSquared = startVec.squareDistanceTo(hitResult);
                if (actualDistanceSquared < range || range == 0.0D) {
                    if (entityInBoundingBox.getLowestRidingEntity() == shooter.getLowestRidingEntity() && !entityInBoundingBox.canRiderInteract()) {
                        if (range == 0.0D) {
                            entity = entityInBoundingBox;
                            vec3d = hitResult;
                        }
                    }
                    else {
                        entity = entityInBoundingBox;
                        vec3d = hitResult;
                        range = actualDistanceSquared;
                    }
                }
            }
        }
        if (entity == null) {
            return null;
        }
        return new EntityRayTraceResult(entity, vec3d);
    }

    @Nullable
    public static EquipmentSlotType getPartByPosition(double y, PlayerEntity player) {
        Evolution.LOGGER.debug("y = " + y);
        Evolution.LOGGER.debug("posY = " + player.posY);
        double yRelativistic = y - player.posY;
        Evolution.LOGGER.debug("yRelativistic = " + yRelativistic);
        if (player.isSneaking()) {
            if (yRelativistic <= 0.25) {
                return EquipmentSlotType.FEET;
            }
            if (MathHelper.rangeInclusive(yRelativistic, 0.25, 0.625)) {
                return EquipmentSlotType.LEGS;
            }
            if (MathHelper.rangeInclusive(yRelativistic, 0.625, 1.125)) {
                return EquipmentSlotType.CHEST;
            }
            if (yRelativistic >= 1.125) {
                return EquipmentSlotType.HEAD;
            }
            return null;
        }
        if (yRelativistic <= 0.375) {
            return EquipmentSlotType.FEET;
        }
        if (MathHelper.rangeInclusive(yRelativistic, 0.375, 0.875)) {
            return EquipmentSlotType.LEGS;
        }
        if (MathHelper.rangeInclusive(yRelativistic, 0.875, 1.5)) {
            return EquipmentSlotType.CHEST;
        }
        if (yRelativistic >= 1.5) {
            return EquipmentSlotType.HEAD;
        }
        return null;
    }

    public static float getHitMultiplier(EquipmentSlotType type, PlayerEntity player, float damage) {
        if (type == null) {
            return 1;
        }
        switch (type) {
            case HEAD:
                headHit(player, damage, 1.75f);
                return 1.75f;
            case CHEST:
                return 1.25f;
            case LEGS:
                return 1f;
            case FEET:
                return 0.5f;
        }
        return 1;
    }

    public static float getProjectileModifier(EquipmentSlotType type) {
        if (type == null) {
            return 1;
        }
        switch (type) {
            case HEAD:
                return 2.0f;
            case CHEST:
                return 1.5f;
            case LEGS:
                return 1f;
            case FEET:
                return 0.75f;
        }
        return 1;
    }

    public static void headHit(PlayerEntity player, float damage, float multiplier) {
        float strength = MathHelper.relativize(damage * multiplier, 0, 20);
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(Effects.NAUSEA, (int) (300 * strength), 0, true, false, true));
        }
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, (int) (100 * strength), strength > 0.8f ? 1 : 0, true, false, true));
        }
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(EvolutionEffects.DIZZINESS.get(), (int) (400 * strength), strength > 0.8f ? 1 : 0, true, false, true));
        }
    }
}
