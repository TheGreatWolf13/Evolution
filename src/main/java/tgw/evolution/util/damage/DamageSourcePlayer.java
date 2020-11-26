package tgw.evolution.util.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.IMelee;

public class DamageSourcePlayer extends DamageSourceEntity {

    private static final ITextComponent FISTS = new TranslationTextComponent("death.item.fists");

    private final Hand hand;

    public DamageSourcePlayer(String damage, PlayerEntity entity, EvolutionDamage.Type type, Hand hand) {
        super(damage, entity, type);
        this.hand = hand;
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity deadEntity) {
        ItemStack heldStack = ((LivingEntity) this.damageSourceEntity).getHeldItem(this.hand);
        String message = "death.attack." + this.damageType + ".item";
        if (!(heldStack.getItem() instanceof IMelee)) {
            heldStack = ItemStack.EMPTY;
        }
        return !heldStack.isEmpty() ?
               new TranslationTextComponent(message,
                                            deadEntity.getDisplayName(),
                                            this.damageSourceEntity.getDisplayName(),
                                            heldStack.getTextComponent()) :
               new TranslationTextComponent(message, deadEntity.getDisplayName(), this.damageSourceEntity.getDisplayName(), FISTS);
    }
}
