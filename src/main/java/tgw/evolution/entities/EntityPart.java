package tgw.evolution.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import tgw.evolution.entities.util.EntityPartType;

public class EntityPart extends Entity {

    public final Entity owner;
    public final EntityPartType type;
    public final EntitySize size;

    public EntityPart(Entity owner, EntityPartType type, float x, float y) {
        super(owner.getType(), owner.world);
        this.owner = owner;
        this.type = type;
        this.size = EntitySize.flexible(x, y);
        this.recalculateSize();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.owner.attackEntityFrom(source, amount);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return this.size;
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.owner == entityIn;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
    }
}
