package tgw.evolution.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
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

import javax.annotation.Nullable;

public class EntitySpear extends EntityGenericProjectile implements IAerodynamicEntity {

    private boolean dealtDamage;
    private ItemStack stack;
    private ResourceLocation texture;

    public EntitySpear(World worldIn, LivingEntity thrower, ItemStack thrownStackIn, float damage, double mass) {
        super(EvolutionEntities.SPEAR.get(), thrower, worldIn, mass);
        this.setStack(thrownStackIn.copy());
        this.setDamage(damage);
    }

    public EntitySpear(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.SPEAR.get(), worldIn);
    }

    public EntitySpear(EntityType<EntitySpear> type, World worldIn) {
        super(type, worldIn);
    }

    public ItemStack getStack() {
        return this.stack;
    }

    private void setStack(ItemStack stack) {
        this.stack = stack.copy();
        this.texture = ((ISpear) this.stack.getItem()).getTexture();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    public ResourceLocation getTextureName() {
        return this.texture;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.timeInGround > 10) {
            this.dealtDamage = true;
        }
        if (this.ticksInAir > 10) {
            this.dealtDamage = false;
        }
        if (this.stack.isEmpty() && this.timeInGround > 10) {
            this.remove();
        }
        if (this.inGround) {
            this.setMotion(0, 0, 0);
        }
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("Spear", NBTTypes.COMPOUND_NBT.getId())) {
            this.setStack(ItemStack.read(compound.getCompound("Spear")));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    protected SoundEvent getHitEntitySound() {
        return EvolutionSounds.JAVELIN_HIT_BLOCK.get();
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.put("Spear", this.stack.serializeNBT());
        compound.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void tryDespawn() {
        if (this.pickupStatus != PickupStatus.ALLOWED) {
            super.tryDespawn();
        }
    }

    @Override
    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return this.dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity hitEntity = rayTraceResult.getEntity();
        LivingEntity shooter = this.getShooter();
        DamageSource source = EvolutionDamage.causeSpearDamage(this, shooter == null ? this : shooter);
        this.dealtDamage = true;
        SoundEvent soundEvent = EvolutionSounds.JAVELIN_HIT_ENTITY.get();
        float velocity = (float) this.getMotion().length();
        if (hitEntity instanceof LivingEntity && hitEntity.canBeAttackedWithItem()) {
            hitEntity.attackEntityFrom(source, this.getDamage() * velocity);
        }
        this.setMotion(this.getMotion().mul(-0.1, -0.1, -0.1));
        this.playSound(soundEvent, 1.0F, 1.0F);
        if (shooter != null) {
            this.stack.damageItem(1, shooter, entity -> {
            });
        }
        else {
            this.stack.setDamage(this.stack.getDamage() + 1);
        }
        if (this.stack.isEmpty()) {
            this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
            this.remove();
        }
    }

    @Override
    protected ItemStack getArrowStack() {
        return this.stack.copy();
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeCompoundTag(this.stack.serializeNBT());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        super.readSpawnData(buffer);
        this.setStack(ItemStack.read(buffer.readCompoundTag()));
    }
}
