package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class DamageSourceEntityIndirect extends DamageSourceEntity implements IHitLocation {
    private final Entity trueSource;
    @Nullable
    private EquipmentSlot hitLocation;

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
    public EquipmentSlot getHitLocation() {
        return this.hitLocation;
    }

    @Override
    @Nullable
    public Component getItemDisplay() {
        ItemStack heldStack = this.trueSource instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
        if ("spear".equals(this.msgId)) {
            heldStack = ((EntitySpear) this.damageSourceEntity).getStack();
        }
        return heldStack.isEmpty() ? null : heldStack.getDisplayName();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity deadEntity) {
        Component sourceComp = this.trueSource == null ? this.damageSourceEntity.getDisplayName() : this.trueSource.getDisplayName();
        Component itemComp = this.getItemDisplay();
        String message = "death.attack." + this.msgId;
        if ("spear".equals(this.msgId)) {
            if (this.trueSource == null && itemComp != null) {
                return new TranslatableComponent(message, deadEntity.getDisplayName(), itemComp);
            }
        }
        return itemComp != null ?
               new TranslatableComponent(message + ".item", deadEntity.getDisplayName(), sourceComp, itemComp) :
               new TranslatableComponent(message, deadEntity.getDisplayName(), sourceComp);
    }

    public void setHitLocation(@Nullable EquipmentSlot hitLocation) {
        this.hitLocation = hitLocation;
    }
}
