package tgw.evolution.util.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.IMelee;

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
    public Vec3d getDamageLocation() {
        return new Vec3d(this.damageSourceEntity.posX, this.damageSourceEntity.posY, this.damageSourceEntity.posZ);
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        ITextComponent itemComp = this.getItemDisplay();
        String message = "death.attack." + this.damageType;
        return itemComp != null ?
               new TranslationTextComponent(message + ".item", deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), itemComp) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName());
    }

    @Nullable
    public ITextComponent getItemDisplay() {
        ItemStack heldStack = ((LivingEntity) this.damageSourceEntity).getHeldItemMainhand();
        return heldStack.getItem() instanceof IMelee ? heldStack.getTextComponent() : null;
    }

    @Override
    @Nullable
    public Entity getTrueSource() {
        return this.damageSourceEntity;
    }

    @Override
    public boolean isDifficultyScaled() {
        return this.damageSourceEntity != null &&
               this.damageSourceEntity instanceof LivingEntity &&
               !(this.damageSourceEntity instanceof PlayerEntity);
    }
}
