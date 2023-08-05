package tgw.evolution.hooks;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import tgw.evolution.EvolutionClient;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.network.PacketCSPlayerFall;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.world.util.LevelUtils;

public final class LivingHooks {

    private LivingHooks() {
    }

    public static void calculateFallDamage(LivingEntity entity, double slowDown) {
        boolean player = false;
        if (entity instanceof Player) {
            if (!entity.level.isClientSide) {
                return;
            }
            player = true;
        }
        else if (entity.level.isClientSide) {
            return;
        }
        double velocity = entity.getDeltaMovement().y;
        if (player) {
            EvolutionClient.sendToServer(new PacketCSPlayerFall(velocity, slowDown, false));
        }
        else {
            calculateFallDamage(entity, velocity, slowDown, false);
        }
    }

    public static float calculateFallDamage(LivingEntity entity, double velocity, double distanceOfSlowDown, boolean isWater) {
        if (velocity == 0) {
            return 0;
        }
        distanceOfSlowDown += entity.intrinsicSlowdown();
        AttributeInstance massAttribute = entity.getAttribute(EvolutionAttributes.MASS);
        assert massAttribute != null;
        double baseMass = massAttribute.getBaseValue();
        double totalMass = massAttribute.getValue();
        double kineticEnergy = totalMass * velocity * velocity / 2;
        double forceOfImpact = kineticEnergy / distanceOfSlowDown;
        double area = entity.getBbWidth() * entity.getBbWidth();
        double pressureOfFall = forceOfImpact / area;
        double maxSupportedPressure = baseMass / (area * 0.035);
        double deltaPressure = Math.max(400 * pressureOfFall - maxSupportedPressure, 0);
        if (deltaPressure <= 1) {
            return 0;
        }
        float amount = (float) Math.pow(deltaPressure, 1.7) / 750_000;
        amount *= isWater ? 1.16 : 1.5;
        if (amount >= 1) {
            entity.hurt(isWater ? EvolutionDamage.WATER_IMPACT : EvolutionDamage.FALL, amount);
        }
        return amount;
    }

    public static void calculateFluidFallDamage(LivingEntity entity, Fluid fluid) {
        boolean player = false;
        if (entity instanceof Player) {
            if (!entity.level.isClientSide) {
                return;
            }
            player = true;
        }
        else if (entity.level.isClientSide) {
            return;
        }
        TagKey<net.minecraft.world.level.material.Fluid> tag = fluid.tag();
        double distanceOfSlowDown = entity.level.getFluidState(entity.blockPosition()).getHeight(entity.level, entity.blockPosition());
        double fallDmgVel = entity.getDeltaMovement().y;
        if (player) {
            EvolutionClient.sendToServer(new PacketCSPlayerFall(fallDmgVel, distanceOfSlowDown, fluid == Fluid.WATER));
        }
        else {
            calculateFallDamage(entity, fallDmgVel, distanceOfSlowDown, fluid == Fluid.WATER);
        }
    }

    public static boolean hasEmptySpaceForEmerging(Entity entity, double preVelX, double preVelY, double preVelZ, double oldY) {
        AABB box = entity.getBoundingBox();
        double minY = box.minY;
        int steps = Mth.ceil((box.maxY - minY) * 2);
        if (steps > 0) {
            double deltaY = Mth.floor(minY * 2) * 0.5 - minY;
            AABBMutable bb = new AABBMutable(box).moveMutable(preVelX, deltaY + 0.062_5, preVelZ);
            Level level = entity.level;
            for (int i = 0; i < steps; ++i) {
                if (level.noCollision(entity, bb) && !LevelUtils.containsAnyLiquid(level, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ)) {
                    return true;
                }
                bb.moveY(0.5);
            }
            return level.noCollision(entity, bb) && !LevelUtils.containsAnyLiquid(level, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        }
        return false;
    }

    public static boolean shouldFixRotation(LivingEntity entity) {
        if (entity.getVehicle() != null) {
            return false;
        }
        if (entity.getFluidHeight(FluidTags.WATER) >= entity.getBbHeight() * 0.5) {
            return true;
        }
        if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
            ItemStack activeItem = entity.getUseItem();
            Item item = activeItem.getItem();
            UseAnim action = item.getUseAnimation(activeItem);
            if (action == UseAnim.BLOCK || action == UseAnim.SPEAR || action == UseAnim.EAT || action == UseAnim.DRINK || action == UseAnim.BOW) {
                return item.getUseDuration(activeItem) > 0;
            }
            return false;
        }
        if (entity.getMainHandItem().getItem() == Items.CROSSBOW) {
            return CrossbowItem.isCharged(entity.getMainHandItem());
        }
        if (entity.getOffhandItem().getItem() == Items.CROSSBOW) {
            return CrossbowItem.isCharged(entity.getOffhandItem());
        }
        return entity.shouldRenderSpecialAttack();
    }

    public static float xDelta(Entity entity, float partialTicks) {
        if (entity.isPassenger() && entity.getVehicle() != null) {
            return 0;
        }
        float swimAmount = 0;
        if (entity instanceof LivingEntity living) {
            swimAmount = living.getSwimAmount(partialTicks);
        }
        boolean inWater = entity.isInWater();
        if (!inWater && (swimAmount > 0 || entity.getPose() == Pose.SWIMMING)) {
            if (swimAmount == 1) {
                return -90;
            }
            float rot0 = entity.getPose() == Pose.CROUCHING || entity.getLastPose() == Pose.CROUCHING ? -30 : 0;
            if (0.5 <= swimAmount) {
                return (swimAmount - 0.5f) * (-180 - rot0) + rot0;
            }
            return rot0;
        }
        if (entity.isCrouching()) {
            return -30;
        }
        if (inWater && swimAmount == 1) {
            return 0;
        }
        //Standing
        return 0;
    }
}
