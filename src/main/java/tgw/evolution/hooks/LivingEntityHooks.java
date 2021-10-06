package tgw.evolution.hooks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;

public final class LivingEntityHooks {

//    private static final FieldHandler<LivingEntity, PlayerEntity> ATTACKING_PLAYER = new FieldHandler<>(LivingEntity.class, "field_70717_bb");
//    private static final FieldHandler<LivingEntity, Integer> RECENTLY_HIT = new FieldHandler<>(LivingEntity.class, "field_70718_bc");
//    private static final MethodHandler<LivingEntity, SoundEvent> GET_DEATH_SOUND = new MethodHandler<>(LivingEntity.class, "func_184615_bR");
//    private static final MethodHandler<LivingEntity, Float> GET_SOUND_VOLUME = new MethodHandler<>(LivingEntity.class, "func_70599_aP");
//    private static final MethodHandler<LivingEntity, Float> GET_SOUND_PITCH = new MethodHandler<>(LivingEntity.class, "func_70647_i");
//    private static final MethodHandler<LivingEntity, Void> PLAY_HURT_SOUND = new MethodHandler<>(LivingEntity.class,
//                                                                                                 "func_184581_c",
//                                                                                                 DamageSource.class);
//    private static final FieldHandler<LivingEntity, DamageSource> LAST_DAMAGE_SOURCE = new FieldHandler<>(LivingEntity.class, "field_189750_bF");
//    private static final FieldHandler<LivingEntity, Long> LAST_DAMAGE_STAMP = new FieldHandler<>(LivingEntity.class, "field_189751_bG");

    private LivingEntityHooks() {
    }

    public static boolean shouldFixRotation(LivingEntity entity) {
        if (entity.getVehicle() != null) {
            return false;
        }
        if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
            ItemStack activeItem = entity.getUseItem();
            Item item = activeItem.getItem();
            UseAction action = item.getUseAnimation(activeItem);
            if (action == UseAction.BLOCK || action == UseAction.SPEAR || action == UseAction.EAT || action == UseAction.DRINK) {
                return item.getUseDuration(activeItem) > 0;
            }
            return false;
        }
        return false;
    }

//    /**
//     * Hooks from {@link LivingEntity#attackEntityFrom(DamageSource, float)}
//     */
//    @EvolutionHook
//    public static boolean attackEntityFrom(LivingEntity entity, DamageSource source, float amount) {
//        if (!ForgeHooks.onLivingAttack(entity, source, amount)) {
//            return false;
//        }
//        if (entity.isInvulnerableTo(source)) {
//            return false;
//        }
//        if (entity.world.isRemote) {
//            return false;
//        }
//        if (entity.getHealth() <= 0.0F) {
//            return false;
//        }
//        if (source.isFireDamage() && entity.isPotionActive(Effects.FIRE_RESISTANCE)) {
//            return false;
//        }
//        if (entity.isSleeping() && !entity.world.isRemote) {
//            entity.wakeUp();
//        }
//        entity.setIdleTime(0);
//        float f = amount;
//        if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) &&
//            !entity.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
//            entity.getItemStackFromSlot(EquipmentSlotType.HEAD)
//                  .damageItem((int) (amount * 4.0F + entity.getRNG().nextFloat() * amount * 2.0F),
//                              entity,
//                              breaker -> breaker.sendBreakAnimation(EquipmentSlotType.HEAD));
//            amount *= 0.75F;
//        }
//        boolean damageBlocked = false;
//        boolean parry = false;
//        boolean parrySuccess = false;
//        float amountBlocked = 0.0F;
//        if (amount > 0.0F && canBlockDamageSource(entity, source)) {
//            damageShield(entity, amount);
//            amountBlocked = amount;
//            amount = 0.0F;
//            if (!source.isProjectile()) {
//                Entity immediateSource = source.getImmediateSource();
//                if (immediateSource instanceof LivingEntity) {
//                    blockUsingShield(entity, (LivingEntity) immediateSource);
//                }
//            }
//            damageBlocked = true;
//            entity.world.setEntityState(entity, EntityStates.SHIELD_BLOCK_SOUND);
//        }
//        else if (amount > 0.0f && canParryDamageSource(entity, source)) {
//            parry = true;
//            int parryTime = getParryTime(entity);
//            Evolution.LOGGER.debug("Parry time = {}", parryTime);
//            if (parryTime >= 0 && parryTime <= 5) {
//                amountBlocked = amount;
//                amount = 0.0f;
//                if (!source.isProjectile()) {
//                    Entity immediateSource = source.getImmediateSource();
//                    if (immediateSource instanceof LivingEntity) {
//                        blockUsingShield(entity, (LivingEntity) immediateSource);
//                    }
//                }
//                damageBlocked = true;
//                parrySuccess = true;
//            }
//            else {
//                amount *= 0.9f;
//            }
//        }
//        entity.limbSwingAmount = 1.5F;
//        damageEntity(entity, source, amount);
//        entity.maxHurtTime = 10;
//        entity.hurtTime = entity.maxHurtTime;
//        entity.attackedAtYaw = 0.0F;
//        Entity trueSource = source.getTrueSource();
//        if (trueSource != null) {
//            if (trueSource instanceof LivingEntity) {
//                entity.setRevengeTarget((LivingEntity) trueSource);
//            }
//            if (trueSource instanceof PlayerEntity) {
//                RECENTLY_HIT.set(entity, 100);
//                ATTACKING_PLAYER.set(entity, (PlayerEntity) trueSource);
//            }
//            else if (trueSource instanceof TameableEntity) {
//                TameableEntity wolf = (TameableEntity) trueSource;
//                if (wolf.isTamed()) {
//                    RECENTLY_HIT.set(entity, 100);
//                    LivingEntity owner = wolf.getOwner();
//                    if (owner != null && owner.getType() == EntityType.PLAYER) {
//                        ATTACKING_PLAYER.set(entity, (PlayerEntity) owner);
//                    }
//                    else {
//                        ATTACKING_PLAYER.set(entity, null);
//                    }
//                }
//            }
//        }
//        if (parry) {
//            SoundEvent sound;
//            if (parrySuccess) {
//                sound = EvolutionSounds.PARRY_SUCCESS.get();
//            }
//            else {
//                sound = EvolutionSounds.PARRY_FAIL.get();
//            }
//            entity.playSound(sound, 0.4f, 0.8F + entity.world.rand.nextFloat() * 0.4F);
//            if (entity instanceof ServerPlayerEntity) {
//                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity),
//                                               new PacketSCParrySound(parrySuccess));
//            }
//        }
//        else if (!damageBlocked && source instanceof EntityDamageSource && ((EntityDamageSource) source).getIsThornsDamage()) {
//            entity.world.setEntityState(entity, EntityStates.THORNS_HIT_SOUND);
//        }
//        else {
//            byte hitSound;
//            if (source == EvolutionDamage.DROWN) {
//                hitSound = EntityStates.DROWN_HIT_SOUND;
//            }
//            else if (source.isFireDamage()) {
//                hitSound = EntityStates.FIRE_HIT_SOUND;
//            }
//            else if (source == DamageSource.SWEET_BERRY_BUSH) {
//                hitSound = EntityStates.SWEET_BERRY_BUSH_HIT_SOUND;
//            }
//            else {
//                hitSound = EntityStates.GENERIC_HIT_SOUND;
//            }
//            entity.world.setEntityState(entity, hitSound);
//        }
//        if (source != EvolutionDamage.DROWN && !damageBlocked) {
//            entity.velocityChanged = entity.getRNG().nextDouble() >= entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
//        }
//        if (trueSource != null) {
//            double d1 = trueSource.getPosX() - entity.getPosX();
//            double d0;
//            for (d0 = trueSource.getPosZ() - entity.getPosZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
//                d1 = (Math.random() - Math.random()) * 0.01D;
//            }
//            entity.attackedAtYaw = MathHelper.radToDeg((float) MathHelper.atan2(d0, d1)) - entity.rotationYaw;
//            entity.applyKnockback(0.4F, d1, d0);
//        }
//        else {
//            entity.attackedAtYaw = (int) (Math.random() * 2) * 180;
//        }
//        if (entity.getHealth() <= 0.0F) {
//            if (!checkTotemDeathProtection(entity, source)) {
//                SoundEvent deathSound = GET_DEATH_SOUND.call(entity);
//                if (deathSound != null) {
//                    entity.playSound(deathSound, GET_SOUND_VOLUME.call(entity), GET_SOUND_PITCH.call(entity));
//                }
//                entity.onDeath(source);
//            }
//        }
//        else {
//            PLAY_HURT_SOUND.call(entity, source);
//        }
//        boolean damageNotBlocked = !damageBlocked;
//        if (damageNotBlocked) {
//            LAST_DAMAGE_SOURCE.set(entity, source);
//            LAST_DAMAGE_STAMP.set(entity, entity.world.getGameTime());
//        }
//        if (entity instanceof ServerPlayerEntity) {
//            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity) entity, source, f, amount, damageBlocked);
//            if (amountBlocked > 0.0F && amountBlocked < Float.MAX_VALUE) {
//                ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(amountBlocked * 10.0F));
//            }
//        }
//        if (trueSource instanceof ServerPlayerEntity) {
//            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity) trueSource, entity, source, f, amount, damageBlocked);
//        }
//        return damageNotBlocked;
//    }

//    private static void blockUsingShield(LivingEntity blocking, LivingEntity blocked) {
//        constructKnockBackVector(blocked, blocking);
//    }

//    private static void calculateWallImpact(LivingEntity entity, double motionX, double motionZ, double mass) {
//        double motionXPost = entity.getMotion().x;
//        double motionZPost = entity.getMotion().z;
//        double deltaSpeedX = Math.abs(motionX) - Math.abs(motionXPost);
//        deltaSpeedX *= 20;
//        float damage = 0;
//        if (deltaSpeedX >= 6) {
//            double kineticEnergy = 0.5 * deltaSpeedX * deltaSpeedX * mass;
//            AxisAlignedBB bb = entity.getBoundingBox();
//            double xCoord = motionX >= 0 ? bb.maxX + 0.01 : bb.minX - 0.01;
//            int numberOfBlocks = 0;
//            double slowDown = 0;
//            BlockPos minPos = new BlockPos(xCoord, bb.minY, bb.minZ);
//            BlockPos maxPos = new BlockPos(xCoord, bb.maxY, bb.maxZ);
//            BlockPos.Mutable changingPos = new BlockPos.Mutable();
//            if (entity.world.isAreaLoaded(minPos, maxPos)) {
//                for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
//                    for (int k = minPos.getZ(); k <= maxPos.getZ(); k++) {
//                        numberOfBlocks++;
//                        changingPos.setPos(xCoord, j, k);
//                        BlockState stateAtPos = entity.world.getBlockState(changingPos);
//                        Block blockAtPos = stateAtPos.getBlock();
//                        if (blockAtPos instanceof ICollisionBlock) {
//                            slowDown += ((ICollisionBlock) blockAtPos).getSlowdownSide(stateAtPos);
//                            ((ICollisionBlock) blockAtPos).collision(entity, motionX);
//                        }
//                        else {
//                            slowDown += 1;
//                        }
//                    }
//                }
//            }
//            if (numberOfBlocks > 0) {
//                slowDown /= numberOfBlocks;
//            }
//            if (slowDown > 0) {
//                slowDown = 1.0 - slowDown;
//            }
//            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
//            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
//            float area = entity.getHeight() * entity.getWidth();
//            double pressure = forceOfImpact / area;
//            damage += (float) Math.pow(pressure, 1.6) / 1_750_000;
//        }
//        double deltaSpeedZ = Math.abs(motionZ) - Math.abs(motionZPost);
//        deltaSpeedZ *= 20;
//        if (deltaSpeedZ >= 6) {
//            double kineticEnergy = 0.5 * deltaSpeedZ * deltaSpeedZ * mass;
//            AxisAlignedBB bb = entity.getBoundingBox();
//            double zCoord = motionZ >= 0 ? bb.maxZ + 0.01 : bb.minZ - 0.01;
//            int numberOfBlocks = 0;
//            double slowDown = 0;
//            BlockPos minPos = new BlockPos(bb.minX, bb.minY, zCoord);
//            BlockPos maxPos = new BlockPos(bb.maxX, bb.maxY, zCoord);
//            BlockPos.Mutable changingPos = new BlockPos.Mutable();
//            if (entity.world.isAreaLoaded(minPos, maxPos)) {
//                for (int i = minPos.getX(); i <= maxPos.getX(); i++) {
//                    for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
//                        numberOfBlocks++;
//                        changingPos.setPos(i, j, zCoord);
//                        BlockState stateAtPos = entity.world.getBlockState(changingPos);
//                        Block blockAtPos = stateAtPos.getBlock();
//                        if (blockAtPos instanceof ICollisionBlock) {
//                            slowDown += ((ICollisionBlock) blockAtPos).getSlowdownSide(stateAtPos);
//                            ((ICollisionBlock) blockAtPos).collision(entity, motionZ);
//                        }
//                        else {
//                            slowDown += 1;
//                        }
//                    }
//                }
//            }
//            if (numberOfBlocks > 0) {
//                slowDown /= numberOfBlocks;
//            }
//            if (slowDown > 0) {
//                slowDown = 1.0 - slowDown;
//            }
//            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
//            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
//            float area = entity.getHeight() * entity.getWidth();
//            double pressure = forceOfImpact / area;
//            damage += (float) Math.pow(pressure, 1.6) / 1_500_000;
//        }
//        if (damage >= 1.0f) {
//            if (!entity.world.isRemote) {
//                entity.attackEntityFrom(EvolutionDamage.WALL_IMPACT, damage);
//            }
//            else if (entity instanceof PlayerEntity) {
//                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSImpactDamage(damage));
//            }
//        }
//    }

//    private static boolean canBlockDamageSource(LivingEntity entity, DamageSource source) {
//        Entity immediateSource = source.getImmediateSource();
//        boolean piercing = false;
//        if (immediateSource instanceof AbstractArrowEntity) {
//            AbstractArrowEntity arrowEntity = (AbstractArrowEntity) immediateSource;
//            if (arrowEntity.getPierceLevel() > 0) {
//                piercing = true;
//            }
//        }
//        if (!piercing && !source.isUnblockable() && isActiveItemStackBlocking(entity)) {
//            Vector3d damageLocation = source.getDamageLocation();
//            if (damageLocation != null) {
//                Vector3d look = entity.getLook(1.0F);
//                Vector3d damageVec = damageLocation.subtractReverse(new Vector3d(entity.getPosX(),
//                                                                                 entity.getPosY() + entity.getEyeHeight(),
//                                                                                 entity.getPosZ())).normalize();
//                return damageVec.dotProduct(look) < 0;
//            }
//        }
//        return false;
//    }

//    private static boolean canParryDamageSource(LivingEntity entity, DamageSource source) {
//        Entity immediateSource = source.getImmediateSource();
//        boolean piercing = false;
//        if (immediateSource instanceof AbstractArrowEntity) {
//            AbstractArrowEntity arrowEntity = (AbstractArrowEntity) immediateSource;
//            if (arrowEntity.getPierceLevel() > 0) {
//                piercing = true;
//            }
//        }
//        if (!piercing && !source.isUnblockable() && isActiveItemStackParrying(entity)) {
//            Vector3d damageLocation = source.getDamageLocation();
//            if (damageLocation != null) {
//                Vector3d look = entity.getLook(1.0F);
//                Vector3d damageVec = damageLocation.subtractReverse(new Vector3d(entity.getPosX(),
//                                                                                 entity.getPosY() + entity.getEyeHeight(),
//                                                                                 entity.getPosZ())).normalize();
//                return damageVec.dotProduct(look) < -MathHelper.SQRT_2_OVER_2;
//            }
//        }
//        return false;
//    }

//    private static boolean checkTotemDeathProtection(LivingEntity entity, DamageSource source) {
//        if (source.canHarmInCreative()) {
//            return false;
//        }
//        ItemStack stack = null;
//        for (Hand hand : Hand.values()) {
//            ItemStack stackInHand = entity.getHeldItem(hand);
//            if (stackInHand.getItem() == Items.TOTEM_OF_UNDYING) {
//                stack = stackInHand.copy();
//                stackInHand.shrink(1);
//                break;
//            }
//        }
//        if (stack != null) {
//            if (entity instanceof ServerPlayerEntity) {
//                ServerPlayerEntity player = (ServerPlayerEntity) entity;
//                player.addStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
//                CriteriaTriggers.USED_TOTEM.trigger(player, stack);
//            }
//            entity.setHealth(1.0F);
//            entity.clearActivePotions();
//            entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
//            entity.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
//            entity.world.setEntityState(entity, EntityStates.TOTEM_OF_UNDYING_SOUND);
//        }
//        return stack != null;
//    }

//    private static void constructKnockBackVector(LivingEntity blocked, LivingEntity blocking) {
//        blocking.applyKnockback(0.5F, blocking.getPosX() - blocked.getPosX(), blocking.getPosZ() - blocked.getPosZ());
//    }

//    private static void damageEntity(LivingEntity entity, DamageSource source, float amount) {
//        if (!entity.isInvulnerableTo(source)) {
//            amount = ForgeHooks.onLivingHurt(entity, source, amount);
//            if (amount <= 0) {
//                return;
//            }
//            //TODO
////            amount = entity.applyArmorCalculations(source, amount);
////            amount = entity.applyPotionDamageCalculations(source, amount);
//            float totalAmount = amount;
//            amount = Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
//            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (totalAmount - amount));
//            float absorbedDamage = totalAmount - amount;
//            if (absorbedDamage > 0.0F && absorbedDamage < Float.MAX_VALUE && source.getTrueSource() instanceof ServerPlayerEntity) {
//                ((PlayerEntity) source.getTrueSource()).addStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
//            }
//            if (absorbedDamage > 0.0F && absorbedDamage < Float.MAX_VALUE && entity instanceof ServerPlayerEntity) {
//                ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_ABSORBED, Math.round(absorbedDamage * 10.0F));
//            }
//            amount = ForgeHooks.onLivingDamage(entity, source, amount);
//            if (amount != 0.0F) {
//                float f2 = entity.getHealth();
//                entity.getCombatTracker().trackDamage(source, f2, amount);
//                entity.setHealth(f2 - amount);
//                entity.setAbsorptionAmount(entity.getAbsorptionAmount() - amount);
//                if (amount < Float.MAX_VALUE && entity instanceof ServerPlayerEntity) {
//                    ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0F));
//                }
//            }
//        }
//    }

//    private static void damageShield(LivingEntity entity, float damage) {
//        if (!(entity instanceof PlayerEntity)) {
//            return;
//        }
//        PlayerEntity player = (PlayerEntity) entity;
//        if (damage >= 3.0F && entity.getActiveItemStack().isShield(entity)) {
//            int i = 1 + MathHelper.floor(damage);
//            Hand hand = entity.getActiveHand();
//            entity.getActiveItemStack().damageItem(i, entity, livingEntity -> {
//                livingEntity.sendBreakAnimation(hand);
//                ForgeEventFactory.onPlayerDestroyItem(player, entity.getActiveItemStack(), hand);
//            });
//            if (entity.getActiveItemStack().isEmpty()) {
//                if (hand == Hand.MAIN_HAND) {
//                    entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
//                }
//                else {
//                    entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
//                }
//                entity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + entity.world.rand.nextFloat() * 0.4F);
//            }
//        }
//    }

//    private static Vector3d getAbsoluteAcceleration(LivingEntity entity, Vector3d direction, float magnitude) {
//        double length = direction.lengthSquared();
//        if (length < 1.0E-7) {
//            return Vector3d.ZERO;
//        }
//        if (entity.getPose() == Pose.CROUCHING) {
//            if (!(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying)) {
//                magnitude *= 0.3;
//            }
//        }
//        if (entity instanceof PlayerEntity) {
//            if (entity.getPose() == Pose.SWIMMING && !entity.isInWater()) {
//                magnitude *= 0.3;
//            }
//        }
//        if (entity.isHandActive()) {
//            Item activeItem = entity.getActiveItemStack().getItem();
//            if (activeItem instanceof IEvolutionItem) {
//                magnitude *= ((IEvolutionItem) activeItem).useItemSlowDownRate();
//            }
//        }
//        Vector3d acceleration = direction.normalize();
//        double accX = acceleration.x * magnitude;
//        double accY = acceleration.y * magnitude;
//        double accZ = acceleration.z * magnitude;
//        float sinFacing = MathHelper.sinDeg(entity.rotationYaw);
//        float cosFacing = MathHelper.cosDeg(entity.rotationYaw);
//        return new Vector3d(accX * cosFacing - accZ * sinFacing, accY, accZ * cosFacing + accX * sinFacing);
//    }

//    private static float getEntityAcceleration(LivingEntity entity) {
//        float force = entity.getAIMoveSpeed();
//        float mass = (float) getMass(entity);
//        return force / mass;
//    }

//    private static float getFrictionModifier(LivingEntity entity) {
//        if (entity instanceof PlayerEntity) {
//            return (float) entity.getAttribute(EvolutionAttributes.FRICTION.get()).getValue();
//        }
//        if (entity instanceof EntityGenericCreature) {
//            return ((EntityGenericCreature<?>) entity).getFrictionModifier();
//        }
//        return 2.0f;
//    }

//    public static double getJumpSlowDown(PlayerEntity player) {
//        if (player.isCreative()) {
//            return 0;
//        }
//        ModifiableAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS.get());
//        int baseMass = (int) mass.getBaseValue();
//        int totalMass = (int) mass.getValue();
//        int equipMass = totalMass - baseMass;
//        return equipMass * 0.000_2;
//    }

//    private static double getMass(Entity entity) {
//        if (entity instanceof IEntityProperties) {
//            return ((IEntityProperties) entity).getBaseMass();
//        }
//        if (entity instanceof PlayerEntity) {
//            ModifiableAttributeInstance massAttribute = ((PlayerEntity) entity).getAttribute(EvolutionAttributes.MASS.get());
//            return massAttribute.getValue();
//        }
//        return 1;
//    }

//    private static int getParryTime(LivingEntity entity) {
//        ItemStack stack = entity.getActiveItemStack();
//        if (stack.isEmpty()) {
//            return -1;
//        }
//        return stack.getItem().getUseDuration(stack) - entity.getItemInUseCount();
//    }

//    private static void handleElytraMovement(LivingEntity entity, double gravityAcceleration, DataParameter<Byte> flags) {
//        Vector3d motion = entity.getMotion();
//        double motionX = motion.x;
//        double motionY = motion.y;
//        double motionZ = motion.z;
//        double mass = getMass(entity);
//        double drag = Gravity.verticalDrag(entity) / mass;
//        double dragX = Math.signum(motionX) * motionX * motionX * drag;
//        double dragY = Math.signum(motionY) * motionY * motionY * drag;
//        double dragZ = Math.signum(motionZ) * motionZ * motionZ * drag;
//        if (motionY > -0.5) {
//            entity.fallDistance = 1.0F;
//        }
//        Vector3d lookVec = entity.getLookVec();
//        float pitchInRad = MathHelper.degToRad(entity.rotationPitch);
//        double horizLookVecLength = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
//        double horizontalSpeed = Math.sqrt(Entity.horizontalMag(motion));
//        float cosPitch = MathHelper.cos(pitchInRad);
//        cosPitch = (float) (cosPitch * cosPitch * Math.min(1, lookVec.length() / 0.4));
//        motionY += gravityAcceleration * (-1 + cosPitch * 0.75);
//        if (motionY < 0 && horizLookVecLength > 0) {
//            double d3 = motionY * -0.1 * cosPitch;
//            motionX += lookVec.x * d3 / horizLookVecLength;
//            motionY += d3;
//            motionZ += lookVec.z * d3 / horizLookVecLength;
//        }
//        if (pitchInRad < 0.0F && horizLookVecLength > 0) {
//            double d13 = horizontalSpeed * -MathHelper.sin(pitchInRad) * 0.04;
//            motionX += -lookVec.x * d13 / horizLookVecLength;
//            motionY += d13 * 3.2;
//            motionZ += -lookVec.z * d13 / horizLookVecLength;
//        }
//        if (horizLookVecLength > 0) {
//            motionX += (lookVec.x / horizLookVecLength * horizontalSpeed - motion.x) * 0.1;
//            motionZ += (lookVec.z / horizLookVecLength * horizontalSpeed - motion.z) * 0.1;
//        }
//        motionX -= dragX;
//        motionY -= dragY;
//        motionZ -= dragZ;
//        entity.setMotion(motionX, motionY, motionZ);
//        entity.move(MoverType.SELF, entity.getMotion());
//        if (entity.collidedHorizontally && !entity.world.isRemote) {
//            calculateWallImpact(entity, motionX, motionZ, mass);
//        }
//        if (entity.isOnGround() && !entity.world.isRemote) {
//            setFlag(entity, flags, EntityFlags.ELYTRA_FLYING, false);
//        }
//    }

//    private static Vector3d handleLadderMotion(LivingEntity entity, double x, double y, double z) {
//        boolean isCreativeFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying;
//        if (entity.isOnLadder() && !isCreativeFlying) {
//            entity.fallDistance = 0.0F;
//            double newX;
//            double newZ;
//            if (!entity.isOnGround()) {
//                newX = MathHelper.clamp(x, -0.025, 0.025);
//                newX *= 0.8;
//                newZ = MathHelper.clamp(z, -0.025, 0.025);
//                newZ *= 0.8;
//            }
//            else {
//                newX = x;
//                newZ = z;
//            }
//            double newY = y < -0.3 ? y : Math.max(y, entity.isSneaking() ? 0 : -0.15);
//            if (newY < 0 && entity.getBlockState().getBlock() != Blocks.SCAFFOLDING && entity.isSneaking() && entity instanceof PlayerEntity) {
//                newY = 0;
//            }
//            return new Vector3d(newX, newY, newZ);
//        }
//        return new Vector3d(x, y, z);
//    }

//    private static void handleLavaMovement(LivingEntity entity, Vector3d direction, double gravityAcceleration) {
//        double posY = entity.getPosY();
//        entity.moveRelative(0.02F, direction);
//        entity.move(MoverType.SELF, entity.getMotion());
//        Vector3d motion = entity.getMotion();
//        double motionX = motion.x * 0.5;
//        double motionY = motion.y * 0.5;
//        double motionZ = motion.z * 0.5;
//        if (!entity.hasNoGravity()) {
//            motionY -= gravityAcceleration / 4;
//        }
//        if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(motionX, motionY + 0.6 - entity.getPosY() + posY, motionZ)) {
//            motionY = 0.3;
//        }
//        entity.setMotion(motionX, motionY, motionZ);
//    }

//    private static void handleNormalMovement(LivingEntity entity,
//                                             Vector3d direction,
//                                             double gravityAcceleration,
//                                             boolean isJumping,
//                                             int jumpTicks,
//                                             boolean isPlayerFlying,
//                                             float slowdown) {
//        AxisAlignedBB aabb = entity.getBoundingBox();
//        BlockPos blockBelow = new BlockPos(entity.getPosX(), aabb.minY - 0.001, entity.getPosZ());
//        BlockState state = entity.world.getBlockState(blockBelow);
//        if (entity.isOnGround() && state.isAir(entity.world, blockBelow)) {
//            outer:
//            for (int i = 0; i < 2; i++) {
//                for (int j = 0; j < 2; j++) {
//                    //noinspection ObjectAllocationInLoop
//                    blockBelow = new BlockPos(i == 0 ? aabb.minX : aabb.maxX, aabb.minY - 0.001, j == 0 ? aabb.minZ : aabb.maxZ);
//                    state = entity.world.getBlockState(blockBelow);
//                    if (!state.isAir(entity.world, blockBelow)) {
//                        break outer;
//                    }
//                }
//            }
//        }
//        Block block = state.getBlock();
//        float frictionCoef = 0.85F;
//        if (block.isAir(state, entity.world, blockBelow)) {
//            frictionCoef = 0.0f;
//        }
//        else if (block instanceof IFriction) {
//            frictionCoef = ((IFriction) block).getFrictionCoefficient(state);
//        }
//        if (entity.func_233571_b_(FluidTags.WATER) > 0) {
//            frictionCoef -= 0.1f;
//            if (frictionCoef < 0.01F) {
//                frictionCoef = 0.01F;
//            }
//        }
//        Vector3d acceleration = getAbsoluteAcceleration(entity, direction, slowdown * jumpMovementFactor(entity, frictionCoef, jumpTicks));
//        if (!entity.isOnGround()) {
//            frictionCoef = 0.0F;
//        }
//        boolean isActiveWalking = acceleration.x != 0 || acceleration.z != 0;
//        Vector3d motion = entity.getMotion();
//        double motionX = motion.x;
//        double motionY = motion.y;
//        double motionZ = motion.z;
////        if (entity.world.isRemote) {
////            Evolution.LOGGER.debug("horizontal speed = {}    friction coeff = {}",
////                                   20 * MathHelper.sqrt(motionX * motionX + motionZ * motionZ),
////                                   frictionCoef);
////        }
//        if ((entity.collidedHorizontally || isJumping) && entity.isOnLadder()) {
//            motionY = BlockUtils.getLadderUpSpeed(entity.getBlockState());
//        }
//        else if (!entity.hasNoGravity()) {
//            if (!isPlayerFlying) {
//                motionY -= gravityAcceleration;
//            }
//        }
//        double legSlowDownX = 0;
//        double legSlowDownZ = 0;
//        double frictionAcc = frictionCoef * gravityAcceleration;
//        if (entity.isOnGround() || isPlayerFlying) {
//            double legSlowDown = legSlowDown(entity);
//            if (frictionAcc != 0) {
//                legSlowDown *= frictionAcc * getFrictionModifier(entity);
//            }
//            else {
//                legSlowDown *= gravityAcceleration * 0.85 * getFrictionModifier(entity);
//            }
//            legSlowDownX = motionX * legSlowDown;
//            legSlowDownZ = motionZ * legSlowDown;
//        }
//        double mass = getMass(entity);
//        double horizontalDrag = Gravity.horizontalDrag(entity) / mass;
//        double verticalDrag = Gravity.verticalDrag(entity) / mass;
//        double frictionX = 0;
//        double frictionZ = 0;
//        if (!isActiveWalking) {
//            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
//            if (norm != 0) {
//                frictionX = motionX / norm * frictionAcc;
//                frictionZ = motionZ / norm * frictionAcc;
//            }
//            if (Math.abs(motionX) < Math.abs(frictionX)) {
//                frictionX = motionX;
//            }
//            if (Math.abs(motionZ) < Math.abs(frictionZ)) {
//                frictionZ = motionZ;
//            }
//        }
//        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
//        if (Math.abs(dragX) > Math.abs(motionX)) {
//            dragX = motionX;
//        }
//        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
//        if (Math.abs(dragY) > Math.abs(motionY)) {
//            dragY = motionY;
//        }
//        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
//        if (Math.abs(dragZ) > Math.abs(motionZ)) {
//            dragZ = motionZ;
//        }
//        motionX += acceleration.x - legSlowDownX - frictionX - dragX;
//        motionY += acceleration.y - dragY;
//        motionZ += acceleration.z - legSlowDownZ - frictionZ - dragZ;
//        if (Math.abs(motionX) < 1e-6) {
//            motionX = 0;
//        }
//        if (Math.abs(motionY) < 1e-6) {
//            motionY = 0;
//        }
//        if (Math.abs(motionZ) < 1e-6) {
//            motionZ = 0;
//        }
//        entity.setMotion(handleLadderMotion(entity, motionX, motionY, motionZ));
//        entity.move(MoverType.SELF, entity.getMotion());
//        if (entity.collidedHorizontally) {
//            calculateWallImpact(entity, motionX, motionZ, mass);
//        }
//    }

//    private static void handleWaterMotion(LivingEntity entity, Vector3d direction, double gravityAcceleration, boolean isJumping, int jumpTicks) {
//        if (entity.isOnGround() || jumpTicks > 0) {
//            if (entity.func_233571_b_(FluidTags.WATER) <= 0.4) {
//                BlockPos currentBlock = entity.getPosition();
//                FluidState fluidState = entity.world.getFluidState(currentBlock);
//                int level = fluidState.getLevel();
//                float slowdown = 1.0f - 0.05f * level;
//                handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, false, slowdown);
//                return;
//            }
//        }
//        float waterSpeedMult = 0.04F;
//        waterSpeedMult *= (float) entity.getAttribute(ForgeMod.SWIM_SPEED.get()).getValue();
//        Vector3d acceleration = getAbsoluteAcceleration(entity, direction, waterSpeedMult);
//        Vector3d motion = entity.getMotion();
//        double motionX = motion.x;
//        double motionY = motion.y;
//        double motionZ = motion.z;
//        double mass = getMass(entity);
//        double verticalDrag = Gravity.verticalWaterDrag(entity) / mass;
//        double horizontalDrag = entity.isSwimming() ? verticalDrag : Gravity.horizontalWaterDrag(entity) / mass;
//        if (entity.collidedHorizontally && entity.isOnLadder()) {
//            motionY = 0.2;
//        }
//        if (!entity.hasNoGravity()) {
//            if (entity.isSwimming()) {
//                motionY -= gravityAcceleration / 16;
//            }
//            else {
//                motionY -= gravityAcceleration;
//            }
//        }
//        if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(0, motionY + 1.5, 0)) {
//            motionY = 0.2;
//            if (entity.func_233571_b_(FluidTags.WATER) <= 0.4) {
//                motionY += 0.2;
//                if (entity.world.isRemote) {
//                    if (entity.equals(Evolution.PROXY.getClientPlayer())) {
//                        ClientEvents.getInstance().jumpTicks = 10;
//                    }
//                }
//            }
//        }
//        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
//        if (Math.abs(dragX) > Math.abs(motionX / 2)) {
//            dragX = motionX / 2;
//        }
//        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
//        if (Math.abs(dragY) > Math.abs(motionY / 2)) {
//            dragY = motionY / 2;
//            EntityEvents.calculateWaterFallDamage(entity);
//        }
//        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
//        if (Math.abs(dragZ) > Math.abs(motionZ / 2)) {
//            dragZ = motionZ / 2;
//        }
//        motionX += acceleration.x - dragX;
//        motionY += acceleration.y - dragY;
//        motionZ += acceleration.z - dragZ;
//        entity.setMotion(motionX, motionY, motionZ);
//        entity.move(MoverType.SELF, entity.getMotion());
//    }

//    private static boolean isActiveItemStackBlocking(LivingEntity entity) {
//        ItemStack stack = entity.getActiveItemStack();
//        if (entity.isHandActive() && !stack.isEmpty()) {
//            Item item = stack.getItem();
//            if (!item.isShield(stack, entity)) {
//                return false;
//            }
//            if (item.getUseAction(stack) != UseAction.BLOCK) {
//                return false;
//            }
//            return item.getUseDuration(stack) - entity.getItemInUseCount() >= 2;
//        }
//        return false;
//    }

//    private static boolean isActiveItemStackParrying(LivingEntity entity) {
//        ItemStack stack = entity.getActiveItemStack();
//        if (entity.isHandActive() && !stack.isEmpty()) {
//            Item item = stack.getItem();
//            if (item.getUseAction(stack) != UseAction.BLOCK) {
//                return false;
//            }
//            return item.getUseDuration(stack) - entity.getItemInUseCount() >= 0;
//        }
//        return false;
//    }

//    /**
//     * Hooks from {@link LivingEntity#jump()}
//     */
//    @EvolutionHook
//    public static void jump(LivingEntity entity, float jumpUpwardsMotion) {
//        if (entity.world.isRemote) {
//            if (entity.equals(Evolution.PROXY.getClientPlayer())) {
//                ClientEvents.getInstance().jumpTicks = 10;
//            }
//        }
//    }

//    private static float jumpMovementFactor(LivingEntity entity, float frictionCoef, int jumpTicks) {
//        if (entity instanceof PlayerEntity) {
//            if (!((PlayerEntity) entity).abilities.isFlying) {
//                if (entity.isOnGround() || entity.isOnLadder()) {
//                    return getEntityAcceleration(entity) * frictionCoef * getFrictionModifier(entity);
//                }
//                return jumpTicks > 3 ? 0.075f * getEntityAcceleration(entity) : 0;
//            }
//        }
//        return entity.isOnGround() ? getEntityAcceleration(entity) * frictionCoef * getFrictionModifier(entity) : entity.jumpMovementFactor;
//    }

//    private static double legSlowDown(LivingEntity entity) {
//        if (entity instanceof PlayerEntity) {
//            return PlayerHelper.LEG_SLOWDOWN;
//        }
//        return 1;
//    }

//    private static void setFlag(LivingEntity entity, DataParameter<Byte> flags, int flag, boolean set) {
//        byte byteField = entity.getDataManager().get(flags);
//        if (set) {
//            entity.getDataManager().set(flags, (byte) (byteField | 1 << flag));
//        }
//        else {
//            entity.getDataManager().set(flags, (byte) (byteField & ~(1 << flag)));
//        }
//    }

//    /**
//     * Hooks from {@link LivingEntity#travel(Vector3d)}
//     */
//    @EvolutionHook
//    public static void travel(LivingEntity entity, Vector3d direction, boolean isJumping, int jumpTicks, DataParameter<Byte> flags) {
//        if (entity.isServerWorld() || entity.canPassengerSteer()) {
//            ModifiableAttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
//            boolean falling = entity.getMotion().y <= 0;
//            if (falling && entity.isPotionActive(Effects.SLOW_FALLING)) {
//                if (!gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
//                    gravity.applyNonPersistentModifier(EvolutionAttributes.SLOW_FALLING);
//                }
//                entity.fallDistance = 0.0F;
//            }
//            else if (gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
//                gravity.removeModifier(EvolutionAttributes.SLOW_FALLING);
//            }
//            double gravityAcceleration = gravity.getValue();
//            boolean isPlayerFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying;
//            if (!entity.isInWater() || isPlayerFlying) {
//                if (!entity.isInLava() || isPlayerFlying) {
//                    if (entity.isElytraFlying()) {
//                        handleElytraMovement(entity, gravityAcceleration, flags);
//                    }
//                    else {
//                        handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, isPlayerFlying, 1.0f);
//                    }
//                }
//                else {
//                    handleLavaMovement(entity, direction, gravityAcceleration);
//                }
//            }
//            else {
//                handleWaterMotion(entity, direction, gravityAcceleration, isJumping, jumpTicks);
//            }
//        }
//        //Controls animations
//        entity.prevLimbSwingAmount = entity.limbSwingAmount;
//        double deltaPosX = entity.getPosX() - entity.prevPosX;
//        double deltaPosZ = entity.getPosZ() - entity.prevPosZ;
//        double deltaPosY = entity instanceof IFlyingAnimal ? entity.getPosY() - entity.prevPosY : 0;
//        float f8 = MathHelper.sqrt(deltaPosX * deltaPosX + deltaPosY * deltaPosY + deltaPosZ * deltaPosZ) * 4.0F;
//        if (f8 > 1.0F) {
//            f8 = 1.0F;
//        }
//        entity.limbSwingAmount += (f8 - entity.limbSwingAmount) * 0.4F;
//        if (entity.limbSwingAmount < 1E-9) {
//            entity.limbSwingAmount = 0;
//            entity.limbSwing = 0;
//        }
//        entity.limbSwing += entity.limbSwingAmount;
//    }
}
