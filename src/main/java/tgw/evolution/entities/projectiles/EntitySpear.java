package tgw.evolution.entities.projectiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.ISpear;
import tgw.evolution.util.constants.NBTTypes;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntitySpear extends EntityGenericProjectile<EntitySpear> implements IAerodynamicEntity {

    private ItemStack stack;
    private ResourceLocation texture;

    public EntitySpear(Level level, LivingEntity thrower, ItemStack thrownStack, float damage, double mass) {
        super(EvolutionEntities.SPEAR.get(), thrower, level, mass);
        this.setStack(thrownStack.copy());
        this.setDamage(damage);
    }

    public EntitySpear(EntityType<EntitySpear> type, Level level) {
        super(type, level);
    }

    public EntitySpear(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(EvolutionEntities.SPEAR.get(), level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Spear", this.stack.serializeNBT());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getArrowStack() {
        return this.stack.copy();
    }

    @Override
    protected SoundEvent getHitBlockSound() {
        return EvolutionSounds.JAVELIN_HIT_BLOCK.get();
    }

    @Nullable
    @Override
    public HitboxEntity<EntitySpear> getHitbox() {
        return null;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public ResourceLocation getTextureName() {
        return this.texture;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void onEntityHit(EntityHitResult rayTraceResult) {
        Entity hitEntity = rayTraceResult.getEntity();
        if (!this.hitEntities.contains(hitEntity.getId())) {
            this.hitEntities.add(hitEntity.getId());
            LivingEntity shooter = this.getShooter();
            DamageSourceEv source = EvolutionDamage.causeSpearDamage(this, shooter == null ? this : shooter);
            float velocity = (float) this.getDeltaMovement().length();
            if (hitEntity instanceof LivingEntity living && hitEntity.isAttackable()) {
                float oldHealth = living.getHealth();
                float damage = this.getDamage() * velocity;
                living.hurt(source, damage);
                if (shooter instanceof ServerPlayer shooterPlayer) {
                    this.applyDamageRaw(shooterPlayer, damage, source.getType());
                    this.applyDamageActual(shooterPlayer, oldHealth - living.getHealth(), source.getType(), living);
                }
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.1, -0.1, -0.1));
            this.playSound(EvolutionSounds.JAVELIN_HIT_ENTITY.get(), 1.0F, 1.0F);
            if (shooter != null) {
                this.stack.hurtAndBreak(1, shooter, entity -> {
                });
            }
            else {
                this.stack.setDamageValue(this.stack.getDamageValue() + 1);
            }
            if (this.stack.isEmpty()) {
                this.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
                this.discard();
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Spear", NBTTypes.COMPOUND_NBT)) {
            this.setStack(ItemStack.of(tag.getCompound("Spear")));
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.setStack(ItemStack.of(buffer.readNbt()));
    }

    private void setStack(ItemStack stack) {
        this.stack = stack.copy();
        this.texture = ((ISpear) this.stack.getItem()).getTexture();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.stack.isEmpty() && this.timeInGround > 10) {
            this.discard();
        }
        if (this.inGround) {
            this.setDeltaMovement(0, 0, 0);
        }
    }

    @Override
    protected void tryDespawn() {
        if (this.pickupStatus != PickupStatus.ALLOWED) {
            super.tryDespawn();
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeNbt(this.stack.serializeNBT());
    }
}
