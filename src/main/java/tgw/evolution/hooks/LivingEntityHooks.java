package tgw.evolution.hooks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.ISoftBlock;
import tgw.evolution.entities.EntityGenericCreature;
import tgw.evolution.entities.IEntityMass;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSImpactDamage;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.PlayerHelper;

public final class LivingEntityHooks {

    private LivingEntityHooks() {
    }

    private static void calculateWallImpact(LivingEntity entity, double motionX, double motionZ, double mass) {
        double motionXPost = entity.getMotion().x;
        double motionZPost = entity.getMotion().z;
        double deltaSpeedX = Math.abs(motionX) - Math.abs(motionXPost);
        float damage = 0;
        if (deltaSpeedX >= 0.3) {
            deltaSpeedX *= 20;
            double kineticEnergy = 0.5 * deltaSpeedX * deltaSpeedX * mass;
            AxisAlignedBB bb = entity.getBoundingBox();
            double xCoord = motionX >= 0 ? bb.maxX + 0.01 : bb.minX - 0.01;
            int numberOfBlocks = 0;
            double slowDown = 0;
            try (BlockPos.PooledMutableBlockPos minPos = BlockPos.PooledMutableBlockPos.retain(xCoord, bb.minY, bb.minZ);
                 BlockPos.PooledMutableBlockPos maxPos = BlockPos.PooledMutableBlockPos.retain(xCoord, bb.maxY, bb.maxZ);
                 BlockPos.PooledMutableBlockPos changingPos = BlockPos.PooledMutableBlockPos.retain()) {
                if (entity.world.isAreaLoaded(minPos, maxPos)) {
                    for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                        for (int k = minPos.getZ(); k <= maxPos.getZ(); k++) {
                            numberOfBlocks++;
                            changingPos.setPos(xCoord, j, k);
                            BlockState stateAtPos = entity.world.getBlockState(changingPos);
                            Block blockAtPos = stateAtPos.getBlock();
                            if (blockAtPos instanceof ISoftBlock) {
                                slowDown += ((ISoftBlock) blockAtPos).getSlowdownSide(stateAtPos);
                                ((ISoftBlock) blockAtPos).collision(entity, motionX);
                            }
                            else {
                                slowDown += 1;
                            }
                        }
                    }
                }
            }
            if (numberOfBlocks > 0) {
                slowDown /= numberOfBlocks;
            }
            if (slowDown > 0) {
                slowDown = 1.0 - slowDown;
            }
            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = entity.getHeight() * entity.getWidth();
            double pressure = forceOfImpact / area;
            damage += (float) Math.pow(pressure, 1.7) / 1_750_000;
        }
        double deltaSpeedZ = Math.abs(motionZ) - Math.abs(motionZPost);
        if (deltaSpeedZ >= 0.3) {
            deltaSpeedZ *= 20;
            double kineticEnergy = 0.5 * deltaSpeedZ * deltaSpeedZ * mass;
            AxisAlignedBB bb = entity.getBoundingBox();
            double zCoord = motionZ >= 0 ? bb.maxZ + 0.01 : bb.minZ - 0.01;
            int numberOfBlocks = 0;
            double slowDown = 0;
            try (BlockPos.PooledMutableBlockPos minPos = BlockPos.PooledMutableBlockPos.retain(bb.minX, bb.minY, zCoord);
                 BlockPos.PooledMutableBlockPos maxPos = BlockPos.PooledMutableBlockPos.retain(bb.maxX, bb.maxY, zCoord);
                 BlockPos.PooledMutableBlockPos changingPos = BlockPos.PooledMutableBlockPos.retain()) {
                if (entity.world.isAreaLoaded(minPos, maxPos)) {
                    for (int i = minPos.getX(); i <= maxPos.getX(); i++) {
                        for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                            numberOfBlocks++;
                            changingPos.setPos(i, j, zCoord);
                            BlockState stateAtPos = entity.world.getBlockState(changingPos);
                            Block blockAtPos = stateAtPos.getBlock();
                            if (blockAtPos instanceof ISoftBlock) {
                                slowDown += ((ISoftBlock) blockAtPos).getSlowdownSide(stateAtPos);
                                ((ISoftBlock) blockAtPos).collision(entity, motionZ);
                            }
                            else {
                                slowDown += 1;
                            }
                        }
                    }
                }
            }
            if (numberOfBlocks > 0) {
                slowDown /= numberOfBlocks;
            }
            if (slowDown > 0) {
                slowDown = 1.0 - slowDown;
            }
            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = entity.getHeight() * entity.getWidth();
            double pressure = forceOfImpact / area;
            damage += (float) Math.pow(pressure, 1.7) / 1_500_000;
        }
        if (damage >= 1.0f) {
            if (!entity.world.isRemote) {
                entity.attackEntityFrom(EvolutionDamage.WALL_IMPACT, damage);
            }
            else if (entity instanceof PlayerEntity) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSImpactDamage(damage));
            }
        }
    }

    private static Vec3d getAbsoluteAcceleration(LivingEntity entity, Vec3d direction, float magnitude) {
        double length = direction.lengthSquared();
        if (length < 1.0E-7) {
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

    public static double getJumpSlowDown(PlayerEntity player) {
        if (player.isCreative()) {
            return 0;
        }
        IAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS);
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        return equipMass * 0.000_2;
    }

    /**
     * Hooks from {@link LivingEntity#getJumpUpwardsMotion()}
     */
    @EvolutionHook
    public static float getJumpUpwardsMotion() {
        return 0.247_5f;
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

    private static void handleElytraMovement(LivingEntity entity, double gravityAcceleration, DataParameter<Byte> flags) {
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
        double horizontalSpeed = Math.sqrt(Entity.horizontalMag(motion));
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
            double d13 = horizontalSpeed * -MathHelper.sin(pitchInRad) * 0.04;
            motionX += -lookVec.x * d13 / horizLookVecLength;
            motionY += d13 * 3.2;
            motionZ += -lookVec.z * d13 / horizLookVecLength;
        }
        if (horizLookVecLength > 0) {
            motionX += (lookVec.x / horizLookVecLength * horizontalSpeed - motion.x) * 0.1;
            motionZ += (lookVec.z / horizLookVecLength * horizontalSpeed - motion.z) * 0.1;
        }
        motionX -= dragX;
        motionY -= dragY;
        motionZ -= dragZ;
        entity.setMotion(motionX, motionY, motionZ);
        entity.move(MoverType.SELF, entity.getMotion());
        if (entity.collidedHorizontally && !entity.world.isRemote) {
            calculateWallImpact(entity, motionX, motionZ, mass);
        }
        if (entity.onGround && !entity.world.isRemote) {
            setFlag(entity, flags, EntityFlags.ELYTRA_FLYING, false);
        }
    }

    private static Vec3d handleLadderMotion(LivingEntity entity, double x, double y, double z) {
        if (entity.isOnLadder()) {
            entity.fallDistance = 0.0F;
            double newX;
            double newZ;
            if (!entity.onGround) {
                newX = MathHelper.clamp(x, -0.025, 0.025);
                newZ = MathHelper.clamp(z, -0.025, 0.025);
            }
            else {
                newX = x;
                newZ = z;
            }
            double newY = Math.max(y, -0.15);
            if (newY < 0 && entity.getBlockState().getBlock() != Blocks.SCAFFOLDING && entity.isSneaking() && entity instanceof PlayerEntity) {
                newY = 0;
            }
            return new Vec3d(newX, newY, newZ);
        }
        return new Vec3d(x, y, z);
    }

    private static void handleLavaMovement(LivingEntity entity, Vec3d direction, double gravityAcceleration) {
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

    private static void handleNormalMovement(LivingEntity entity,
                                             Vec3d direction,
                                             double gravityAcceleration,
                                             boolean isJumping,
                                             int jumpTicks,
                                             boolean isPlayerFlying,
                                             float slowdown) {
        BlockPos blockBelow = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1, entity.posZ);
        float slipperiness = entity.world.getBlockState(blockBelow).getSlipperiness(entity.world, blockBelow, entity);
        if (Float.compare(slipperiness, 0.6F) < 0.01F) {
            slipperiness = 0.15F;
        }
        if (entity.getSubmergedHeight() > 0) {
            slipperiness += 0.1f;
        }
        float frictionCoef = entity.onGround ? 1.0F - slipperiness : 0.0F;
        Vec3d acceleration = getAbsoluteAcceleration(entity, direction, slowdown * jumpMovementFactor(entity, slipperiness, jumpTicks));
        boolean isActiveWalking = acceleration.x != 0 || acceleration.z != 0;
        Vec3d motion = entity.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        if ((entity.collidedHorizontally || isJumping) && entity.isOnLadder()) {
            motionY = BlockUtils.getLadderUpSpeed(entity.getBlockState());
        }
        else if (!entity.hasNoGravity()) {
            if (!isPlayerFlying) {
                motionY -= gravityAcceleration;
            }
        }
        double legSlowDownX = 0;
        double legSlowDownZ = 0;
        double frictionAcc = frictionCoef * gravityAcceleration;
        if (entity.onGround || isPlayerFlying && isActiveWalking) {
            double legSlowDown = legSlowDown(entity);
            if (frictionAcc != 0) {
                legSlowDown *= frictionAcc;
            }
            else {
                legSlowDown *= gravityAcceleration * 0.85;
            }
            legSlowDownX = motionX * legSlowDown;
            legSlowDownZ = motionZ * legSlowDown;
        }
        double mass = getMass(entity);
        double horizontalDrag = Gravity.horizontalDrag(entity) / mass;
        double verticalDrag = Gravity.verticalDrag(entity) / mass;
        double frictionX = 0;
        double frictionZ = 0;
        if (!isActiveWalking) {
            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (norm != 0) {
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
        entity.setMotion(handleLadderMotion(entity, motionX, motionY, motionZ));
        entity.move(MoverType.SELF, entity.getMotion());
        if (entity.collidedHorizontally) {
            calculateWallImpact(entity, motionX, motionZ, mass);
        }
    }

    private static void handleWaterMotion(LivingEntity entity, Vec3d direction, double gravityAcceleration, boolean isJumping, int jumpTicks) {
        if (entity.onGround || jumpTicks > 0) {
            if (entity.getSubmergedHeight() <= 0.4) {
                BlockPos currentBlock = new BlockPos(entity);
                IFluidState fluidState = entity.world.getFluidState(currentBlock);
                int level = fluidState.getLevel();
                float slowdown = 1.0f - 0.05f * level;
                handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, false, slowdown);
                return;
            }
        }
        float waterSpeedMult = 0.04F;
        waterSpeedMult *= (float) entity.getAttribute(LivingEntity.SWIM_SPEED).getValue();
        Vec3d acceleration = getAbsoluteAcceleration(entity, direction, waterSpeedMult);
        Vec3d motion = entity.getMotion();
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
            motionY = 0.2;
            if (entity.getSubmergedHeight() <= 0.4) {
                motionY += 0.2;
                if (entity.world.isRemote) {
                    if (entity.equals(Evolution.PROXY.getClientPlayer())) {
                        ClientEvents.getInstance().jumpTicks = 10;
                    }
                }
            }
        }
        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
        if (Math.abs(dragX) > Math.abs(motionX / 2)) {
            dragX = motionX / 2;
        }
        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
        if (Math.abs(dragY) > Math.abs(motionY / 2)) {
            dragY = motionY / 2;
            EntityEvents.calculateWaterFallDamage(entity);
        }
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
        if (Math.abs(dragZ) > Math.abs(motionZ / 2)) {
            dragZ = motionZ / 2;
        }
        motionX += acceleration.x - dragX;
        motionY += acceleration.y - dragY;
        motionZ += acceleration.z - dragZ;
        entity.setMotion(motionX, motionY, motionZ);
        entity.move(MoverType.SELF, entity.getMotion());
    }

    /**
     * Hooks from {@link LivingEntity#jump()}
     */
    @EvolutionHook
    public static void jump(LivingEntity entity, float jumpUpwardsMotion) {
        float upMotion;
        if (entity.isPotionActive(Effects.JUMP_BOOST)) {
            upMotion = jumpUpwardsMotion * (1.0F + (entity.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) / 10.0f);
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
        if (entity.world.isRemote) {
            if (entity.equals(Evolution.PROXY.getClientPlayer())) {
                ClientEvents.getInstance().jumpTicks = 10;
            }
        }
        ForgeHooks.onLivingJump(entity);
    }

    private static float jumpMovementFactor(LivingEntity entity, float slipperiness, int jumpTicks) {
        if (entity instanceof PlayerEntity) {
            if (!((PlayerEntity) entity).abilities.isFlying) {
                if (entity.onGround || entity.isOnLadder()) {
                    return entity.getAIMoveSpeed() * (0.15F / slipperiness);
                }
                return jumpTicks > 3 ? 0.075f * entity.getAIMoveSpeed() * (0.15F / slipperiness) : 0;
            }
        }
        return entity.onGround ? entity.getAIMoveSpeed() * (0.15F / slipperiness) : entity.jumpMovementFactor;
    }

    private static double legSlowDown(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return PlayerHelper.LEG_SLOWDOWN;
        }
        if (entity instanceof EntityGenericCreature) {
            return ((EntityGenericCreature) entity).getLegSlowDown();
        }
        return 5.455;
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

    private static void setFlag(LivingEntity entity, DataParameter<Byte> flags, int flag, boolean set) {
        byte byteField = entity.getDataManager().get(flags);
        if (set) {
            entity.getDataManager().set(flags, (byte) (byteField | 1 << flag));
        }
        else {
            entity.getDataManager().set(flags, (byte) (byteField & ~(1 << flag)));
        }
    }

    /**
     * Hooks from {@link LivingEntity#travel(Vec3d)}
     */
    @EvolutionHook
    public static void travel(LivingEntity entity, Vec3d direction, boolean isJumping, int jumpTicks, DataParameter<Byte> flags) {
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
            boolean isPlayerFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying;
            if (!entity.isInWater() || isPlayerFlying) {
                if (!entity.isInLava() || isPlayerFlying) {
                    if (entity.isElytraFlying()) {
                        handleElytraMovement(entity, gravityAcceleration, flags);
                    }
                    else {
                        handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, isPlayerFlying, 1.0f);
                    }
                }
                else {
                    handleLavaMovement(entity, direction, gravityAcceleration);
                }
            }
            else {
                handleWaterMotion(entity, direction, gravityAcceleration, isJumping, jumpTicks);
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
}
