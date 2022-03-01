package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
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
    public Component getItemDisplay() {
        ItemStack heldStack = ((LivingEntity) this.damageSourceEntity).getMainHandItem();
        return heldStack.getItem() instanceof IMelee ? heldStack.getDisplayName() : null;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity deadEntity) {
        Component itemComp = this.getItemDisplay();
        String message = "death.attack." + this.msgId;
        return itemComp != null ?
               new TranslatableComponent(message + ".item", deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), itemComp) :
               new TranslatableComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName());
    }

    @Override
    @Nullable
    public Vec3 getSourcePosition() {
        return this.damageSourceEntity != null ? this.damageSourceEntity.position() : null;
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.damageSourceEntity != null && this.damageSourceEntity instanceof LivingEntity && !(this.damageSourceEntity instanceof Player);
    }
}
