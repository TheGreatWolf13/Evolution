package tgw.evolution.entities.projectiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.events.ItemEvents;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEntities;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.patches.IBlockPatch;
import tgw.evolution.util.constants.NBTTypes;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public class EntitySpear extends EntityGenericProjectile<EntitySpear> implements IAerodynamicEntity {

    private boolean isStone;
    private ItemStack stack = ItemStack.EMPTY;

    public EntitySpear(Level level, LivingEntity thrower, ItemStack thrownStack) {
        super(EvolutionEntities.SPEAR.get(), thrower, level, IModularTool.get(thrownStack).getMass());
        this.setStack(thrownStack.copy());
        this.setDamage(IModularTool.get(thrownStack).getAttackDamage());
    }

    public EntitySpear(EntityType<EntitySpear> type, Level level) {
        super(type, level);
    }

    public EntitySpear(@SuppressWarnings("unused") PlayMessages.SpawnEntity spawnEntity, Level level) {
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
        return this.isStone ? EvolutionSounds.STONE_WEAPON_HIT_BLOCK.get() : EvolutionSounds.METAL_WEAPON_HIT_BLOCK.get();
    }

    @Nullable
    @Override
    public HitboxEntity<EntitySpear> getHitbox() {
        return null;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public boolean hasHitboxes() {
        return false;
    }

    @Override
    protected void onBlockHit(BlockState state) {
        LivingEntity shooter = this.getShooter();
        if (shooter != null) {
            ItemEvents.damageItem(this.stack, shooter, ItemModular.DamageCause.HIT_BLOCK, null,
                                  ((IBlockPatch) state.getBlock()).getHarvestLevel(state));
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult rayTraceResult) {
        Entity hitEntity = rayTraceResult.getEntity();
        if (!this.hitEntities.contains(hitEntity.getId())) {
            this.hitEntities.add(hitEntity.getId());
            LivingEntity shooter = this.getShooter();
            if (hitEntity instanceof LivingEntity living && hitEntity.isAttackable()) {
                double relativeSpeedX = this.getDeltaMovement().x - hitEntity.getDeltaMovement().x;
                double relativeSpeedY = this.getDeltaMovement().y - hitEntity.getDeltaMovement().y;
                double relativeSpeedZ = this.getDeltaMovement().z - hitEntity.getDeltaMovement().z;
                float relativeVelocitySqr = (float) (relativeSpeedX * relativeSpeedX +
                                                     relativeSpeedY * relativeSpeedY +
                                                     relativeSpeedZ * relativeSpeedZ);
                float oldHealth = living.getHealth();
                float damage = (float) (this.getDamage() * relativeVelocitySqr / (0.825 * 0.825));
                DamageSourceEv source = EvolutionDamage.causeSpearDamage(this, shooter == null ? this : shooter);
                living.hurt(source, damage);
                if (shooter instanceof ServerPlayer shooterPlayer) {
                    this.applyDamageRaw(shooterPlayer, damage, source.getType());
                    this.applyDamageActual(shooterPlayer, oldHealth - living.getHealth(), source.getType(), living);
                }
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.1, -0.1, -0.1));
            this.playSound(this.isStone ? EvolutionSounds.STONE_SPEAR_HIT_ENTITY.get() : EvolutionSounds.METAL_SPEAR_HIT_ENTITY.get(), 1.0F, 1.0F);
            if (shooter != null) {
                ItemEvents.damageItem(this.stack, shooter, ItemModular.DamageCause.HIT_ENTITY, null);
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
        this.isStone = IModularTool.get(this.stack).getHead().getMaterial().getMaterial().isStone();
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
