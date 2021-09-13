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
    @Nullable
    public Entity getDirectEntity() {
        return this.damageSourceEntity;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.trueSource;
    }

    @Override
    @Nullable
    public EquipmentSlotType getHitLocation() {
        return this.hitLocation;
    }

    @Override
    @Nullable
    public ITextComponent getItemDisplay() {
        ItemStack heldStack = this.trueSource instanceof LivingEntity ? ((LivingEntity) this.trueSource).getMainHandItem() : ItemStack.EMPTY;
        if ("spear".equals(this.msgId)) {
            heldStack = ((EntitySpear) this.damageSourceEntity).getStack();
        }
        return heldStack.isEmpty() ? null : heldStack.getDisplayName();
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity deadEntity) {
        ITextComponent sourceComp = this.trueSource == null ? this.damageSourceEntity.getDisplayName() : this.trueSource.getDisplayName();
        ITextComponent itemComp = this.getItemDisplay();
        String message = "death.attack." + this.msgId;
        if ("spear".equals(this.msgId)) {
            if (this.trueSource == null && itemComp != null) {
                return new TranslationTextComponent(message, deadEntity.getDisplayName(), itemComp);
            }
        }
        String messageItem = message + ".item";
        return itemComp != null ?
               new TranslationTextComponent(messageItem, deadEntity.getDisplayName(), sourceComp, itemComp) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), sourceComp);
    }

    public void setHitLocation(@Nullable EquipmentSlotType hitLocation) {
        this.hitLocation = hitLocation;
    }
}
