package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.IMelee;

public class DamageSourcePlayer extends DamageSourceEntity {

    private final ItemStack stack;

    public DamageSourcePlayer(String damage, Player entity, EvolutionDamage.Type type) {
        super(damage, entity, type);
        this.stack = entity.getMainHandItem();
    }

    @Override
    public Component getItemDisplay() {
        return this.stack.getItem() instanceof IMelee ? this.stack.getDisplayName() : EvolutionTexts.DEATH_FISTS;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity deadEntity) {
        String message = "death.attack." + this.msgId + ".item";
        return new TranslatableComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), this.getItemDisplay());
    }
}
