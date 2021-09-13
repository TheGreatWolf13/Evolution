package tgw.evolution.util.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.IMelee;

public class DamageSourcePlayer extends DamageSourceEntity {

    private final ItemStack stack;

    public DamageSourcePlayer(String damage, PlayerEntity entity, EvolutionDamage.Type type, Hand hand) {
        super(damage, entity, type);
        this.stack = entity.getItemInHand(hand);
    }

    @Override
    public ITextComponent getItemDisplay() {
        return this.stack.getItem() instanceof IMelee ? this.stack.getDisplayName() : EvolutionTexts.DEATH_FISTS;
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity deadEntity) {
        String message = "death.attack." + this.msgId + ".item";
        return new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), this.getItemDisplay());
    }
}
