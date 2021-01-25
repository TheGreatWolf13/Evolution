package tgw.evolution.util.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class DamageSourceEntityIndirect extends DamageSourceEntity implements IHitLocation {
    private final Entity trueSource;
    @Nullable
    private EquipmentSlotType hitLocation;

    public DamageSourceEntityIndirect(String damage, Entity source, @Nullable Entity trueSource, EvolutionDamage.Type type) {
        super(damage, source, type);
        this.trueSource = trueSource;
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        ITextComponent sourceComp = this.trueSource == null ? this.damageSourceEntity.getDisplayName() : this.trueSource.getDisplayName();
        ITextComponent itemComp = this.getItemDisplay();
        String message = "death.attack." + this.damageType;
        if ("spear".equals(this.damageType)) {
            if (this.trueSource == null && itemComp != null) {
                return new TranslationTextComponent(message, deadEntity.getDisplayName(), itemComp);
            }
        }
        String messageItem = message + ".item";
        return itemComp != null ?
               new TranslationTextComponent(messageItem, deadEntity.getDisplayName(), sourceComp, itemComp) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), sourceComp);
    }

    @Override
    @Nullable
    public EquipmentSlotType getHitLocation() {
        return this.hitLocation;
    }

    public void setHitLocation(@Nullable EquipmentSlotType hitLocation) {
        this.hitLocation = hitLocation;
    }

    @Override
    @Nullable
    public Entity getImmediateSource() {
        return this.damageSourceEntity;
    }

    @Override
    @Nullable
    public ITextComponent getItemDisplay() {
        ItemStack heldStack = this.trueSource instanceof LivingEntity ? ((LivingEntity) this.trueSource).getHeldItemMainhand() : ItemStack.EMPTY;
        if ("spear".equals(this.damageType)) {
            heldStack = ((EntitySpear) this.damageSourceEntity).getStack();
        }
        return heldStack.isEmpty() ? null : heldStack.getTextComponent();
    }

    @Override
    @Nullable
    public Entity getTrueSource() {
        return this.trueSource;
    }
}
