package tgw.evolution.util.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class DamageSourceEntity extends DamageSourceEv {
    @Nullable
    protected final Entity damageSourceEntity;

    public DamageSourceEntity(String damage, @Nullable Entity entity, EvolutionDamage.Type type) {
        super(damage, type);
        this.damageSourceEntity = entity;
    }

    @Override
    @Nullable
    public Entity getTrueSource() {
        return this.damageSourceEntity;
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        ItemStack heldStack = this.damageSourceEntity instanceof LivingEntity ?
                              ((LivingEntity) this.damageSourceEntity).getHeldItemMainhand() :
                              ItemStack.EMPTY;
        String message = "death.attack." + this.damageType;
        return !heldStack.isEmpty() ?
               new TranslationTextComponent(message + ".item",
                                            deadEntity.getDisplayName(),
                                            this.damageSourceEntity.getDisplayName(),
                                            heldStack.getTextComponent()) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName());
    }

    @Override
    public boolean isDifficultyScaled() {
        return this.damageSourceEntity != null &&
               this.damageSourceEntity instanceof LivingEntity &&
               !(this.damageSourceEntity instanceof PlayerEntity);
    }

    @Override
    @Nullable
    public Vec3d getDamageLocation() {
        return new Vec3d(this.damageSourceEntity.posX, this.damageSourceEntity.posY, this.damageSourceEntity.posZ);
    }
}
