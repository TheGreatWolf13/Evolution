package tgw.evolution.util.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class EvIndirectEntityDamageSource extends EvEntityDamageSource {
    private final Entity trueSource;
    private boolean isThornsDamage;

    public EvIndirectEntityDamageSource(String damage, Entity source, @Nullable Entity trueSource, EvolutionDamage.Type type) {
        super(damage, source, type);
        this.trueSource = trueSource;
    }

    public EvIndirectEntityDamageSource setIsThornsDamage() {
        this.isThornsDamage = true;
        return this;
    }

    public boolean getIsThornsDamage() {
        return this.isThornsDamage;
    }

    @Override
    @Nullable
    public Entity getImmediateSource() {
        return this.damageSourceEntity;
    }

    @Override
    @Nullable
    public Entity getTrueSource() {
        return this.trueSource;
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        ITextComponent sourceComp = this.trueSource == null ? this.damageSourceEntity.getDisplayName() : this.trueSource.getDisplayName();
        ItemStack heldStack = this.trueSource instanceof LivingEntity ? ((LivingEntity) this.trueSource).getHeldItemMainhand() : ItemStack.EMPTY;
        String message = "death.attack." + this.damageType;
        if ("spear".equals(this.damageType)) {
            heldStack = ((EntitySpear) this.damageSourceEntity).getStack();
            if (this.trueSource == null) {
                return new TranslationTextComponent(message, deadEntity.getDisplayName(), heldStack);
            }
        }
        String messageItem = message + ".item";
        return !heldStack.isEmpty() ?
               new TranslationTextComponent(messageItem, deadEntity.getDisplayName(), sourceComp, heldStack.getTextComponent()) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), sourceComp);
    }
}
