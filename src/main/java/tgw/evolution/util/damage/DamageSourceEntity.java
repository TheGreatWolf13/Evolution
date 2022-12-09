package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.IMelee;

public class DamageSourceEntity extends DamageSourceEv {
    protected final Entity damageSourceEntity;

    public DamageSourceEntity(String damage, Entity entity, EvolutionDamage.Type type) {
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
        if (!(this.damageSourceEntity instanceof LivingEntity living)) {
            return null;
        }
        ItemStack heldStack = living.getMainHandItem();
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
        return this.damageSourceEntity.position();
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.damageSourceEntity instanceof LivingEntity && !(this.damageSourceEntity instanceof Player);
    }
}
