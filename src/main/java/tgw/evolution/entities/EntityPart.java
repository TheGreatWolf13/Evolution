package tgw.evolution.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;

public class EntityPart extends Entity {

    public final Entity owner;
    public final EntitySize size;

    public EntityPart(Entity owner, float x, float y) {
        super(owner.getType(), owner.world);
        this.owner = owner;
        this.size = EntitySize.flexible(x, y);
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.owner.attackEntityFrom(source, amount);
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.owner == entityIn;
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        throw new UnsupportedOperationException();
    }
}
