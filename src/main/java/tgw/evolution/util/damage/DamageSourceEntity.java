package tgw.evolution.util.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
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
    public Entity getEntity() {
        return this.damageSourceEntity;
    }

    @Nullable
    public ITextComponent getItemDisplay() {
        ItemStack heldStack = ((LivingEntity) this.damageSourceEntity).getMainHandItem();
        return heldStack.getItem() instanceof IMelee ? heldStack.getDisplayName() : null;
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity deadEntity) {
        ITextComponent itemComp = this.getItemDisplay();
        String message = "death.attack." + this.msgId;
        return itemComp != null ?
               new TranslationTextComponent(message + ".item", deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), itemComp) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName());
    }

    @Override
    @Nullable
    public Vector3d getSourcePosition() {
        return this.damageSourceEntity != null ? this.damageSourceEntity.position() : null;
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.damageSourceEntity != null &&
               this.damageSourceEntity instanceof LivingEntity &&
               !(this.damageSourceEntity instanceof PlayerEntity);
    }
}
