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

    private final Hand hand;

    public DamageSourcePlayer(String damage, PlayerEntity entity, EvolutionDamage.Type type, Hand hand) {
        super(damage, entity, type);
        this.hand = hand;
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        String message = "death.attack." + this.damageType + ".item";
        return new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), this.getItemDisplay());
    }

    @Override
    public ITextComponent getItemDisplay() {
        ItemStack heldStack = ((LivingEntity) this.damageSourceEntity).getHeldItem(this.hand);
        return heldStack.getItem() instanceof IMelee ? heldStack.getTextComponent() : EvolutionTexts.DEATH_FISTS;
    }
}
