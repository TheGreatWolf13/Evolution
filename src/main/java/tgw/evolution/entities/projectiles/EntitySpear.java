package tgw.evolution.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.ISpear;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;

public class EntitySpear extends AbstractArrowEntity implements IEntityAdditionalSpawnData {

    private final double gravity;
    private final double verticalDrag;
    private final double horizontalDrag;
    public boolean dealtDamage;
    private float damage;
    private ItemStack stack;

    public EntitySpear(EntityType<EntitySpear> type, World worldIn) {
        super(type, worldIn);
        this.gravity = -Gravity.gravity(worldIn.dimension);
        this.verticalDrag = Gravity.verticalDrag(worldIn.dimension, this.getWidth());
        this.horizontalDrag = Gravity.horizontalDrag(worldIn.dimension, this.getWidth(), this.getHeight());
    }

    public EntitySpear(World worldIn, LivingEntity thrower, ItemStack thrownStackIn, float damage) {
        super(EvolutionEntities.SPEAR.get(), thrower, worldIn);
        this.setStack(thrownStackIn.copy());
        this.damage = damage + 4;
        this.gravity = -Gravity.gravity(worldIn.dimension);
        this.verticalDrag = Gravity.verticalDrag(worldIn.dimension, this.getWidth());
        this.horizontalDrag = Gravity.horizontalDrag(worldIn.dimension, this.getWidth(), this.getHeight());
    }

    public EntitySpear(@SuppressWarnings("unused") FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
        this(EvolutionEntities.SPEAR.get(), worldIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return this.stack.copy();
    }

    public ItemStack getStack() {
        return this.stack;
    }

    private void setStack(ItemStack stack) {
        this.stack = stack.copy();
    }

    @Override
    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return this.dealtDamage ? null : super.rayTraceEntities(startVec, endVec);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
        Entity entity = rayTraceResult.getEntity();
        Entity shooter = this.getShooter();
        DamageSource damagesource = EvolutionDamage.causeSpearDamage(this, shooter == null ? this : shooter);
        this.dealtDamage = true;
        SoundEvent soundevent = EvolutionSounds.JAVELIN_HIT_ENTITY.get();
        if (entity instanceof LivingEntity && entity.canBeAttackedWithItem() && entity.attackEntityFrom(damagesource, this.damage)) {
            LivingEntity livingentity1 = (LivingEntity) entity;
            this.arrowHit(livingentity1);
        }
        this.setMotion(this.getMotion().mul(-0.01D, -0.1D, -0.01D));
        this.playSound(soundevent, 1.0F, 1.0F);
    }

    @Override
    protected float getWaterDrag() {
        return 0.6F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    protected void tryDespawn() {
        if (this.pickupStatus != AbstractArrowEntity.PickupStatus.ALLOWED) {
            super.tryDespawn();
        }
    }

    public ResourceLocation getTextureName() {
        return ((ISpear) this.stack.getItem()).getTexture();
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.put("Spear", this.stack.serializeNBT());
        compound.putBoolean("DealtDamage", this.dealtDamage);
        compound.putFloat("Damage", this.damage);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("Spear", NBTTypes.COMPOUND_NBT.getId())) {
            this.setStack(ItemStack.read(compound.getCompound("Spear")));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
        this.damage = compound.getFloat("Damage");
    }

    @Override
    protected SoundEvent getHitEntitySound() {
        return EvolutionSounds.JAVELIN_HIT_BLOCK.get();
    }

    @Override
    public void tick() {
        if (this.timeInGround > 4) {
            this.dealtDamage = true;
        }
        if (this.stack.isEmpty() && this.timeInGround > 10) {
            this.remove();
        }
        if (!this.hasNoGravity() && !this.getNoClip()) {
            Vec3d vec3d3 = this.getMotion();
            this.setMotion(vec3d3.x * this.horizontalDrag, (vec3d3.y + 0.05F + this.gravity) * this.verticalDrag, vec3d3.z * this.horizontalDrag);
        }
        if (this.inGround) {
            this.setMotion(0, 0, 0);
        }
        super.tick();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeCompoundTag(this.stack.serializeNBT());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.setStack(ItemStack.read(buffer.readCompoundTag()));
    }
}
