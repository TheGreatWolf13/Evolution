package tgw.evolution.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.ISpear;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntitySpear extends EntityGenericProjectile<EntitySpear> implements IAerodynamicEntity {

    private ItemStack stack;
    private ResourceLocation texture;

    public EntitySpear(World world, LivingEntity thrower, ItemStack thrownStack, float damage, double mass) {
        super(EvolutionEntities.SPEAR.get(), thrower, world, mass);
        this.setStack(thrownStack.copy());
        this.setDamage(damage);
    }

    public EntitySpear(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(EvolutionEntities.SPEAR.get(), world);
    }

    public EntitySpear(EntityType<EntitySpear> type, World world) {
        super(type, world);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Spear", this.stack.serializeNBT());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
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
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity hitEntity = rayTraceResult.getEntity();
        if (!this.hitEntities.contains(hitEntity.getId())) {
            this.hitEntities.add(hitEntity.getId());
            LivingEntity shooter = this.getShooter();
            DamageSourceEv source = EvolutionDamage.causeSpearDamage(this, shooter == null ? this : shooter);
            float velocity = (float) this.getDeltaMovement().length();
            if (hitEntity instanceof LivingEntity && hitEntity.isAttackable()) {
                LivingEntity living = (LivingEntity) hitEntity;
                float oldHealth = living.getHealth();
                float damage = this.getDamage() * velocity;
                living.hurt(source, damage);
                if (shooter instanceof ServerPlayerEntity) {
                    this.applyDamageRaw((ServerPlayerEntity) shooter, damage, source.getType());
                    this.applyDamageActual((ServerPlayerEntity) shooter, oldHealth - living.getHealth(), source.getType(), living);
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
                this.remove();
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Spear", NBTTypes.COMPOUND_NBT)) {
            this.setStack(ItemStack.of(compound.getCompound("Spear")));
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
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
            this.remove();
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
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeNbt(this.stack.serializeNBT());
    }
}
