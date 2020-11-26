package tgw.evolution.hooks;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import tgw.evolution.Evolution;
import tgw.evolution.entities.EvolutionAttributes;
import tgw.evolution.entities.IEntityMass;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.PlayerHelper;

public final class LivingEntityHooks {

    private LivingEntityHooks() {
    }

    /**
     * Hooks from {@link LivingEntity#playEquipSound(ItemStack)}
     */
    @EvolutionHook
    public static void playEquipSound(LivingEntity entity, ItemStack stack) {
        if (!stack.isEmpty()) {
            SoundEvent sound = null;
            Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                sound = ((ArmorItem) item).getArmorMaterial().getSoundEvent();
            }
            else if (item == Items.ELYTRA) {
                sound = SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
            }
            if (sound != null) {
                entity.playSound(sound, 1.0F, 1.0F);
            }
        }
    }

    /**
     * Hooks from {@link LivingEntity#getJumpUpwardsMotion()}
     */
    @EvolutionHook
    public static float getJumpUpwardsMotion() {
        return 0.22f;
    }

    /**
     * Hooks from {@link LivingEntity#jump()}
     */
    @EvolutionHook
    public static void jump(LivingEntity entity, float jumpUpwardsMotion) {
        float upMotion;
        if (entity.isPotionActive(Effects.JUMP_BOOST)) {
            upMotion = jumpUpwardsMotion + 0.1F * (entity.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1);
        }
        else {
            upMotion = jumpUpwardsMotion;
        }
        if (entity instanceof PlayerEntity) {
            upMotion -= getJumpSlowDown((PlayerEntity) entity);
        }
        Vec3d motion = entity.getMotion();
        entity.setMotion(motion.x, upMotion, motion.z);
        entity.isAirBorne = true;
        ForgeHooks.onLivingJump(entity);
    }

    public static double getJumpSlowDown(PlayerEntity player) {
        IAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS);
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        return equipMass * 0.000_2;
    }

    /**
     * Hooks from {@link LivingEntity#travel(Vec3d)}
     */
    @EvolutionHook
    public static void travel(LivingEntity entity, Vec3d direction, boolean isJumping, DataParameter<Byte> flags) {
        if (entity.isServerWorld() || entity.canPassengerSteer()) {
            IAttributeInstance gravity = entity.getAttribute(LivingEntity.ENTITY_GRAVITY);
            boolean falling = entity.getMotion().y <= 0;
            if (falling && entity.isPotionActive(Effects.SLOW_FALLING)) {
                if (!gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                    gravity.applyModifier(EvolutionAttributes.SLOW_FALLING);
                }
                entity.fallDistance = 0.0F;
            }
            else if (gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                gravity.removeModifier(EvolutionAttributes.SLOW_FALLING);
            }
            double gravityAcceleration = gravity.getValue();
            boolean isActiveWalking = !direction.equals(Vec3d.ZERO);
            if (!entity.isInWater() || entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying) {
                if (!entity.isInLava() || entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying) {
                    if (entity.isElytraFlying()) {
                        //Controls elytra movement
                        Vec3d motion = entity.getMotion();
                        double motionX = motion.x;
                        double motionY = motion.y;
                        double motionZ = motion.z;
                        double mass = getMass(entity);
                        double drag = Gravity.verticalDrag(entity) / mass;
                        double dragX = Math.signum(motionX) * motionX * motionX * drag;
                        double dragY = Math.signum(motionY) * motionY * motionY * drag;
                        double dragZ = Math.signum(motionZ) * motionZ * motionZ * drag;
                        if (motionY > -0.5) {
                            entity.fallDistance = 1.0F;
                        }
                        Vec3d lookVec = entity.getLookVec();
                        float pitchInRad = MathHelper.degToRad(entity.rotationPitch);
                        double horizLookVecLength = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
                        double horizMotionLength = Math.sqrt(Entity.horizontalMag(motion));
                        float cosPitch = MathHelper.cos(pitchInRad);
                        cosPitch = (float) (cosPitch * cosPitch * Math.min(1, lookVec.length() / 0.4));
                        motionY += gravityAcceleration * (-1 + cosPitch * 0.75);
                        if (motionY < 0 && horizLookVecLength > 0) {
                            double d3 = motionY * -0.1 * cosPitch;
                            motionX += lookVec.x * d3 / horizLookVecLength;
                            motionY += d3;
                            motionZ += lookVec.z * d3 / horizLookVecLength;
                        }
                        if (pitchInRad < 0.0F && horizLookVecLength > 0) {
                            double d13 = horizMotionLength * -MathHelper.sin(pitchInRad) * 0.04;
                            motionX += -lookVec.x * d13 / horizLookVecLength;
                            motionY += d13 * 3.2;
                            motionZ += -lookVec.z * d13 / horizLookVecLength;
                        }
                        if (horizLookVecLength > 0) {
                            motionX += (lookVec.x / horizLookVecLength * horizMotionLength - motion.x) * 0.1;
                            motionZ += (lookVec.z / horizLookVecLength * horizMotionLength - motion.z) * 0.1;
                        }
                        motionX -= dragX;
                        motionY -= dragY;
                        motionZ -= dragZ;
                        entity.setMotion(motionX, motionY, motionZ);
                        entity.move(MoverType.SELF, entity.getMotion());
                        if (entity.collidedHorizontally && !entity.world.isRemote) {
                            double d14 = Math.sqrt(Entity.horizontalMag(entity.getMotion()));
                            double d4 = horizMotionLength - d14;
                            float f4 = (float) (d4 * 10 - 3);
                            if (f4 > 0.0F) {
                                entity.playSound(getFallSound(entity, (int) f4), 1.0F, 1.0F);
                                entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, f4);
                            }
                        }
                        if (entity.onGround && !entity.world.isRemote) {
                            setFlag(entity, flags, EntityFlags.ELYTRA_FLYING, false);
                        }
                    }
                    else {
                        //Controls land movement (or flying)
                        BlockPos blockBelow = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1, entity.posZ);
                        float slipperiness = entity.world.getBlockState(blockBelow).getSlipperiness(entity.world, blockBelow, entity);
                        if (Float.compare(slipperiness, 0.6F) < 0.01F) {
                            slipperiness = 0.15F;
                        }
                        float frictionCoef = entity.onGround ? 1.0F - slipperiness : 0.0F;
                        Vec3d acceleration = getAbsoluteAcceleration(entity, direction, jumpMovementFactor(entity, slipperiness));
                        isActiveWalking = acceleration.x != 0 || acceleration.z != 0;
                        entity.setMotion(handleLadderMotion(entity, entity.getMotion()));
                        Vec3d motion = entity.getMotion();
                        entity.move(MoverType.SELF, motion);
                        double motionX = motion.x;
                        double motionY = motion.y;
                        double motionZ = motion.z;
                        if ((entity.collidedHorizontally || isJumping) && entity.isOnLadder()) {
                            motionY = 0.2;
                        }
                        if (entity.isPotionActive(Effects.LEVITATION)) {
                            motionY += (0.05 * (entity.getActivePotionEffect(Effects.LEVITATION).getAmplifier() + 1) - motionY) * 0.2;
                            entity.fallDistance = 0.0F;
                        }
                        else if (!entity.hasNoGravity()) {
                            if (entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying) {
                                motionY = 0;
                            }
                            else if (entity.onGround && !isJumping) {
                                motionY = -gravityAcceleration;
                            }
                            else {
                                motionY -= gravityAcceleration;
                            }
                        }
                        double legSlowDownX = 0;
                        double legSlowDownZ = 0;
                        if (isActiveWalking || entity.onGround) {
                            double legSlowDown = legSlowDown(entity);
                            legSlowDownX = motionX * legSlowDown;
                            legSlowDownZ = motionZ * legSlowDown;
                        }
                        double mass = getMass(entity);
                        double horizontalDrag = entity.isInWater() ?
                                                Gravity.horizontalWaterDrag(entity) / mass :
                                                Gravity.horizontalDrag(entity) / mass;
                        double verticalDrag = entity.isInWater() ? Gravity.verticalWaterDrag(entity) / mass : Gravity.verticalDrag(entity) / mass;
                        double frictionX = 0;
                        double frictionZ = 0;
                        if (!isActiveWalking) {
                            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
                            if (norm != 0) {
                                double frictionAcc = frictionCoef * gravityAcceleration;
                                frictionX = motionX / norm * frictionAcc;
                                frictionZ = motionZ / norm * frictionAcc;
                            }
                            if (Math.abs(motionX) < Math.abs(frictionX)) {
                                frictionX = motionX;
                            }
                            if (Math.abs(motionZ) < Math.abs(frictionZ)) {
                                frictionZ = motionZ;
                            }
                        }
                        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
                        if (Math.abs(dragX) > Math.abs(motionX)) {
                            dragX = motionX;
                        }
                        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
                        if (Math.abs(dragY) > Math.abs(motionY)) {
                            dragY = motionY;
                        }
                        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
                        if (Math.abs(dragZ) > Math.abs(motionZ)) {
                            dragZ = motionZ;
                        }
                        motionX += acceleration.x - legSlowDownX - frictionX - dragX;
                        motionY += acceleration.y - dragY;
                        motionZ += acceleration.z - legSlowDownZ - frictionZ - dragZ;
                        if (Math.abs(motionX) < 1e-6) {
                            motionX = 0;
                        }
                        if (Math.abs(motionY) < 1e-6) {
                            motionY = 0;
                        }
                        if (Math.abs(motionZ) < 1e-6) {
                            motionZ = 0;
                        }
                        entity.setMotion(motionX, motionY, motionZ);
                    }
                }
                else {
                    //Controls lava movement
                    double posY = entity.posY;
                    entity.moveRelative(0.02F, direction);
                    entity.move(MoverType.SELF, entity.getMotion());
                    Vec3d motion = entity.getMotion();
                    double motionX = motion.x * 0.5;
                    double motionY = motion.y * 0.5;
                    double motionZ = motion.z * 0.5;
                    if (!entity.hasNoGravity()) {
                        motionY -= gravityAcceleration / 4;
                    }
                    if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(motionX, motionY + 0.6 - entity.posY + posY, motionZ)) {
                        motionY = 0.3;
                    }
                    entity.setMotion(motionX, motionY, motionZ);
                }
            }
            else {
                //Controls water movement
                float depthStriderModifier = EnchantmentHelper.getDepthStriderModifier(entity);
                if (depthStriderModifier > 3.0F) {
                    depthStriderModifier = 3.0F;
                }
                if (!entity.onGround) {
                    depthStriderModifier *= 0.5F;
                }
                float waterSpeedMult = 0.04F;
                if (depthStriderModifier > 0.0F) {
                    waterSpeedMult += 0.17f * depthStriderModifier / 3.0F;
                }
                waterSpeedMult *= (float) entity.getAttribute(LivingEntity.SWIM_SPEED).getValue();
                Vec3d acceleration = getAbsoluteAcceleration(entity, direction, waterSpeedMult);
                Vec3d motion = entity.getMotion();
                entity.move(MoverType.SELF, motion);
                double motionX = motion.x;
                double motionY = motion.y;
                double motionZ = motion.z;
                double mass = getMass(entity);
                double verticalDrag = Gravity.verticalWaterDrag(entity) / mass;
                double horizontalDrag = entity.isSwimming() ? verticalDrag : Gravity.horizontalWaterDrag(entity) / mass;
                if (entity.collidedHorizontally && entity.isOnLadder()) {
                    motionY = 0.2;
                }
                if (!entity.hasNoGravity()) {
                    if (entity.isSwimming()) {
                        motionY -= gravityAcceleration / 16;
                    }
                    else {
                        motionY -= gravityAcceleration;
                    }
                }
                if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(0, motionY + 1.5, 0)) {
                    motionY = 0.4;
                }
                double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
                if (Math.abs(dragX) > Math.abs(motionX / 2)) {
                    dragX = motionX / 2;
                }
                double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
                if (Math.abs(dragY) > Math.abs(motionY / 2)) {
                    dragY = motionY / 2;
                }
                double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
                if (Math.abs(dragZ) > Math.abs(motionZ / 2)) {
                    dragZ = motionZ / 2;
                }
                motionX += acceleration.x - dragX;
                motionY += acceleration.y - dragY;
                motionZ += acceleration.z - dragZ;
                entity.setMotion(motionX, motionY, motionZ);
            }
        }
        //Controls animations
        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        double deltaPosX = entity.posX - entity.prevPosX;
        double deltaPosZ = entity.posZ - entity.prevPosZ;
        double deltaPosY = entity instanceof IFlyingAnimal ? entity.posY - entity.prevPosY : 0;
        float f8 = MathHelper.sqrt(deltaPosX * deltaPosX + deltaPosY * deltaPosY + deltaPosZ * deltaPosZ) * 4.0F;
        if (f8 > 1.0F) {
            f8 = 1.0F;
        }
        entity.limbSwingAmount += (f8 - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }

    private static double getMass(Entity entity) {
        if (entity instanceof IEntityMass) {
            return ((IEntityMass) entity).getMass();
        }
        if (entity instanceof PlayerEntity) {
            IAttributeInstance massAttribute = ((PlayerEntity) entity).getAttribute(EvolutionAttributes.MASS);
            return massAttribute.getValue();
        }
        return 1;
    }

    public static SoundEvent getFallSound(LivingEntity entity, int heightIn) {
        if (entity instanceof ArmorStandEntity) {
            return SoundEvents.ENTITY_ARMOR_STAND_FALL;
        }
        if (entity instanceof MonsterEntity || entity instanceof tgw.evolution.entities.MonsterEntity) {
            return heightIn > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
        }
        if (entity instanceof PlayerEntity) {
            return heightIn > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
        }
        return heightIn > 4 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL;
    }

    private static void setFlag(LivingEntity entity, DataParameter<Byte> flags, int flag, boolean set) {
        byte byteField = entity.getDataManager().get(flags);
        if (set) {
            entity.getDataManager().set(flags, (byte) (byteField | 1 << flag));
        }
        else {
            entity.getDataManager().set(flags, (byte) (byteField & ~(1 << flag)));
        }
    }

    private static Vec3d getAbsoluteAcceleration(LivingEntity entity, Vec3d direction, float magnitude) {
        double length = direction.lengthSquared();
        if (length < 1.0E-7D) {
            return Vec3d.ZERO;
        }
        if (entity.isSneaking()) {
            if (!(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying)) {
                magnitude *= 0.3;
            }
        }
        if (entity instanceof PlayerEntity) {
            if (Evolution.PRONED_PLAYERS.getOrDefault(entity.getUniqueID(), false)) {
                magnitude *= 0.3;
            }
        }
        Vec3d acceleration = direction.normalize();
        double accX = acceleration.x * magnitude;
        double accY = acceleration.y * magnitude;
        double accZ = acceleration.z * magnitude;
        float sinFacing = MathHelper.sinDeg(entity.rotationYaw);
        float cosFacing = MathHelper.cosDeg(entity.rotationYaw);
        return new Vec3d(accX * cosFacing - accZ * sinFacing, accY, accZ * cosFacing + accX * sinFacing);
    }

    private static float jumpMovementFactor(LivingEntity entity, float slipperiness) {
        if (entity instanceof PlayerEntity) {
            if (!((PlayerEntity) entity).abilities.isFlying) {
                return entity.onGround ? entity.getAIMoveSpeed() * (0.15F / slipperiness) : 0;
            }
        }
        return entity.onGround ? entity.getAIMoveSpeed() * (0.15F / slipperiness) : entity.jumpMovementFactor;
    }

    private static Vec3d handleLadderMotion(LivingEntity entity, Vec3d motion) {
        if (entity.isOnLadder()) {
            entity.fallDistance = 0.0F;
            double newX = MathHelper.clamp(motion.x, -0.15, 0.15);
            double newZ = MathHelper.clamp(motion.z, -0.15, 0.15);
            double newY = Math.max(motion.y, -0.15);
            if (newY < 0 && entity.getBlockState().getBlock() != Blocks.SCAFFOLDING && entity.isSneaking() && entity instanceof PlayerEntity) {
                newY = 0;
            }
            return new Vec3d(newX, newY, newZ);
        }
        return motion;
    }

    private static double legSlowDown(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return PlayerHelper.LEG_SLOWDOWN;
        }
        return 0.1;
    }
}
