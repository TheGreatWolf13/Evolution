package tgw.evolution.events;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tgw.evolution.items.*;
import tgw.evolution.util.EvolutionStyles;

import java.util.Locale;

public class ItemEvents {

    private static final String SPEED = "evolution.tooltip.speed";
    private static final String COOLDOWN = "evolution.tooltip.cooldown";
    private static final String DAMAGE = "evolution.tooltip.damage";
    private static final String DPS = "evolution.tooltip.dps";
    private static final String DISTANCE = "evolution.tooltip.distance";
    private static final String MINING = "evolution.tooltip.mining";
    private static final String DURABILITY = "evolution.tooltip.durability";
    private static final String TWO_HANDED = "evolution.tooltip.two_handed";
    private static final String OFFHAND = "evolution.tooltip.offhand";
    private static final String THROWABLE = "evolution.tooltip.throwable";
    private static final ITextComponent COMPONENT_TWO_HANDED = new TranslationTextComponent(TWO_HANDED).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent COMPONENT_OFFHAND = new TranslationTextComponent(OFFHAND).setStyle(EvolutionStyles.LIGHT_GREY);
    private static final ITextComponent COMPONENT_THROWABLE = new TranslationTextComponent(THROWABLE).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent EMPTY = new StringTextComponent("");

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Item item = event.getItemStack().getItem();
        if (item instanceof ItemTool || item instanceof IMelee) {
            boolean tool = item instanceof ItemTool;
            double reach = event.getEntityPlayer().getAttribute(PlayerEntity.REACH_DISTANCE).getBaseValue() + (tool ? ((ItemTool) item).getReach() : ((IMelee) item).getReach());
            double attackDamage = event.getPlayer().getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() + (tool ? ((ItemTool) item).getAttackDamage() : ((IMelee) item).getAttackDamage());
            double attackSpeed = event.getPlayer().getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue() + (tool ? ((ItemTool) item).getAttackSpeed() : ((IMelee) item).getAttackSpeed());
            event.getToolTip().add(3, new TranslationTextComponent(SPEED, String.format(Locale.ENGLISH, "%.2f", attackSpeed)).setStyle(EvolutionStyles.SPEED));
            event.getToolTip().add(4, new TranslationTextComponent(COOLDOWN, String.format(Locale.ENGLISH, "%.2f", 1f / attackSpeed)).setStyle(EvolutionStyles.COOLDOWN));
            event.getToolTip().add(5, new TranslationTextComponent(DAMAGE, String.format(Locale.ENGLISH, "%.2f", attackDamage)).setStyle(EvolutionStyles.DAMAGE));
            event.getToolTip().add(6, new TranslationTextComponent(DPS, String.format(Locale.ENGLISH, "%.2f", attackDamage * attackSpeed)).setStyle(EvolutionStyles.DPS));
            event.getToolTip().add(7, new TranslationTextComponent(DISTANCE, String.format(Locale.ENGLISH, "%.2f", reach)).setStyle(EvolutionStyles.REACH));
            event.getToolTip().remove(8);
            event.getToolTip().remove(8);
            event.getToolTip().remove(8);
            if (tool) {
                float miningSpeed = ((ItemTool) item).getEfficiency();
                event.getToolTip().add(8, new TranslationTextComponent(MINING, String.format(Locale.ENGLISH, "%.2f", miningSpeed)).setStyle(EvolutionStyles.MINING));
            }
        }
        //Manage Throwable Items
        if (item instanceof IThrowable) {
            if (((IThrowable) item).putEmptyLine()) {
                event.getToolTip().add(1, EMPTY);
            }
            event.getToolTip().add(((IThrowable) item).line(), COMPONENT_THROWABLE);
        }
        //Manage Two Handed Tooltips
        if (item instanceof ITwoHanded) {
            event.getToolTip().add(2, COMPONENT_TWO_HANDED);
        }
        //Manage Offhand Attackable Items Tooltips
        else if (item instanceof IOffhandAttackable) {
            event.getToolTip().add(2, COMPONENT_OFFHAND);
            event.getToolTip().remove(3);
        }
        //Durability tooltip
        if (item instanceof IDurability) {
            if (event.getFlags().isAdvanced()) {
                if (event.getItemStack().isDamaged()) {
                    event.getToolTip().remove(event.getToolTip().size() - 3);
                }
                if (event.getItemStack().hasTag()) {
                    event.getToolTip().add(event.getToolTip().size() - 2, new TranslationTextComponent(DURABILITY, ((IDurability) item).displayDurability(event.getItemStack())).setStyle(EvolutionStyles.DURABILITY));
                }
                else {
                    event.getToolTip().add(event.getToolTip().size() - 1, new TranslationTextComponent(DURABILITY, ((IDurability) item).displayDurability(event.getItemStack())).setStyle(EvolutionStyles.DURABILITY));
                }
            }
            else {
                event.getToolTip().add(event.getToolTip().size(), new TranslationTextComponent(DURABILITY, ((IDurability) item).displayDurability(event.getItemStack())).setStyle(EvolutionStyles.DURABILITY));
            }
        }
    }
}
