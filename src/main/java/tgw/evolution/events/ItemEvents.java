package tgw.evolution.events;

import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.*;
import tgw.evolution.util.MathHelper;

import java.util.List;
import java.util.Map;

public class ItemEvents {

    private static void addEasterEggs(List<ITextComponent> tooltip, ItemStack stack) {
        ClientEvents client = ClientEvents.getInstance();
        if (client == null) {
            return;
        }
        if (client.hasShiftDown()) {
            Item item = stack.getItem();
            if (item instanceof ItemRock) {
                switch (((ItemRock) item).getStoneName()) {
                    case CHERT:
                        tooltip.add(EvolutionTexts.EMPTY);
                        addMultiLineComp(EvolutionTexts.EASTER_CHERT, tooltip);
                        return;
                    case GABBRO:
                        tooltip.add(EvolutionTexts.EMPTY);
                        addMultiLineComp(EvolutionTexts.EASTER_GABBRO, tooltip);
                        return;
                    case GNEISS:
                        tooltip.add(EvolutionTexts.EMPTY);
                        addMultiLineComp(EvolutionTexts.EASTER_GNEISS, tooltip);
                        return;
                    case SLATE:
                        tooltip.add(EvolutionTexts.EMPTY);
                        addMultiLineComp(EvolutionTexts.EASTER_SLATE, tooltip);
                        return;
                    default:
                        break;
                }
            }
        }
    }

    private static void addEffectsTooltips(List<ITextComponent> tooltip, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IFireAspect) {
            tooltip.add(EvolutionTexts.fireAspect((IFireAspect) item));
        }
        if (item instanceof IHeavyAttack) {
            tooltip.add(EvolutionTexts.heavyAttack((IHeavyAttack) item));
        }
        if (item instanceof IKnockback) {
            tooltip.add(EvolutionTexts.knockback((IKnockback) item));
        }
        if (item instanceof IParry) {
            tooltip.add(EvolutionTexts.parry((IParry) item, stack));
        }
        if (item instanceof ISweepAttack) {
            tooltip.add(EvolutionTexts.sweep((ISweepAttack) item));
        }
    }

    private static void addFluidInfo(List<ITextComponent> tooltip, ItemStack stack) {
        IItemFluidContainer container = (IItemFluidContainer) stack.getItem();
        if (container.isEmpty(stack)) {
            tooltip.add(EvolutionTexts.TOOLTIP_EMPTY_CONTAINER);
        }
        else {
            tooltip.add(EvolutionTexts.container(container, stack));
        }
        tooltip.add(EvolutionTexts.capacity(container));
    }

    private static void addMultiLineComp(ITextComponent comp, List<ITextComponent> tooltip) {
        Style style = comp.getStyle();
        for (String str : comp.getFormattedText().split("\n")) {
            //noinspection ObjectAllocationInLoop
            tooltip.add(new StringTextComponent(" " + str).setStyle(style));
        }
    }

    public static void makeEvolutionTooltip(ItemStack stack, List<ITextComponent> tooltip, PlayerEntity player, ITooltipFlag advanced) {
        //Name
        ITextComponent component = stack.getDisplayName().applyTextStyle(stack.getRarity().color);
        if (stack.hasDisplayName()) {
            component.applyTextStyle(TextFormatting.ITALIC);
        }
        tooltip.add(component);
        //Item specific information
        Item item = stack.getItem();
        item.addInformation(stack, player == null ? null : player.world, tooltip, advanced);
        if (item instanceof IItemFluidContainer) {
            addFluidInfo(tooltip, stack);
        }
        //Effects
        addEffectsTooltips(tooltip, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("display", 10)) {
                CompoundNBT nbt = stack.getTag().getCompound("display");
                //Color
                if (nbt.contains("color", 3)) {
                    if (advanced.isAdvanced()) {
                        tooltip.add(new TranslationTextComponent("item.color", String.format("#%06X", nbt.getInt("color"))).applyTextStyle(
                                TextFormatting.GRAY));
                    }
                    else {
                        tooltip.add(new TranslationTextComponent("item.dyed").applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));
                    }
                }
                //Lore
                if (nbt.getTagId("Lore") == 9) {
                    ListNBT lore = nbt.getList("Lore", 8);
                    for (int j = 0; j < lore.size(); ++j) {
                        String s = lore.getString(j);
                        try {
                            ITextComponent loreComponent = ITextComponent.Serializer.fromJson(s);
                            if (loreComponent != null) {
                                tooltip.add(TextComponentUtils.mergeStyles(loreComponent, EvolutionStyles.LORE));
                            }
                        }
                        catch (JsonParseException var19) {
                            nbt.remove("Lore");
                        }
                    }
                }
            }
        }
        //Properties
        int sizeForMass = tooltip.size();
        boolean hasAddedLine = false;
        if (item instanceof ITwoHanded) {
            tooltip.add(EvolutionTexts.EMPTY);
            tooltip.add(EvolutionTexts.TOOLTIP_TWO_HANDED);
            hasAddedLine = true;
        }
        if (item instanceof IThrowable) {
            if (!hasAddedLine) {
                tooltip.add(EvolutionTexts.EMPTY);
            }
            tooltip.add(EvolutionTexts.TOOLTIP_THROWABLE);
            hasAddedLine = true;
        }
        if (item instanceof ILunge) {
            if (!hasAddedLine) {
                tooltip.add(EvolutionTexts.EMPTY);
            }
            tooltip.add(EvolutionTexts.TOOLTIP_LUNGE);
            hasAddedLine = true;
        }
        //Attributes
        boolean hasMass = false;
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            Multimap<String, AttributeModifier> multimap = stack.getAttributeModifiers(slot);
            if (!multimap.isEmpty()) {
                if (hasAddedLine) {
                    hasAddedLine = false;
                }
                else {
                    tooltip.add(EvolutionTexts.EMPTY);
                }
                if (slot == EquipmentSlotType.MAINHAND && stack.getItem() instanceof IOffhandAttackable) {
                    tooltip.add(EvolutionTexts.TOOLTIP_OFFHAND);
                }
                else {
                    //noinspection ObjectAllocationInLoop
                    tooltip.add(new TranslationTextComponent("item.modifiers." + slot.getName()).applyTextStyle(TextFormatting.GRAY));
                }
                boolean isMassUnique = true;
                for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    double amount = attributemodifier.getAmount();
                    if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_DAMAGE_MODIFIER) == 0) {
                        amount += player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                        String damage = stack.getItem() instanceof IMelee ?
                                        ((IMelee) stack.getItem()).getDamageType().getTranslationKey() :
                                        EvolutionDamage.Type.GENERIC.getTranslationKey();
                        tooltip.add(EvolutionTexts.damage(damage, amount));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_SPEED_MODIFIER) == 0) {
                        amount += player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                        tooltip.add(EvolutionTexts.speed(amount));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID() == EvolutionAttributes.REACH_DISTANCE_MODIFIER) {
                        amount += player.getAttribute(PlayerEntity.REACH_DISTANCE).getBaseValue();
                        tooltip.add(EvolutionTexts.distance(amount));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER ||
                        attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER_OFFHAND) {
                        if (hasMass) {
                            continue;
                        }
                        hasMass = true;
                        tooltip.add(sizeForMass, EvolutionTexts.EMPTY);
                        tooltip.add(sizeForMass + 1, EvolutionTexts.mass(amount));
                        continue;
                    }
                    double d1;
                    if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE &&
                        attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        d1 = amount;
                    }
                    else {
                        d1 = amount * 100;
                    }
                    if (amount > 0) {
                        tooltip.add(new TranslationTextComponent("attribute.modifier.plus." + attributemodifier.getOperation().getId(),
                                                                 ItemStack.DECIMALFORMAT.format(d1),
                                                                 new TranslationTextComponent("attribute.name." + entry.getKey())).applyTextStyle(
                                TextFormatting.BLUE));
                        isMassUnique = false;
                        continue;
                    }
                    if (amount < 0) {
                        d1 *= -1;
                        //noinspection ObjectAllocationInLoop
                        tooltip.add(new TranslationTextComponent("attribute.modifier.take." + attributemodifier.getOperation().getId(),
                                                                 ItemStack.DECIMALFORMAT.format(d1),
                                                                 new TranslationTextComponent("attribute.name." + entry.getKey())).applyTextStyle(
                                TextFormatting.RED));
                        isMassUnique = false;
                    }
                }
                if (slot == EquipmentSlotType.MAINHAND && stack.getItem() instanceof ItemGenericTool) {
                    float miningSpeed = ((ItemGenericTool) stack.getItem()).getEfficiency();
                    if (miningSpeed > 0) {
                        tooltip.add(EvolutionTexts.mining(miningSpeed));
                        isMassUnique = false;
                    }
                }
                if (hasMass && isMassUnique) {
                    tooltip.remove(tooltip.size() - 1);
                    tooltip.remove(tooltip.size() - 1);
                }
            }
        }
        //Unbreakable
        if (stack.hasTag() && stack.getTag().getBoolean("Unbreakable")) {
            tooltip.add(new TranslationTextComponent("item.unbreakable").applyTextStyle(TextFormatting.BLUE));
        }
        //Durability
        if (stack.getItem() instanceof IDurability) {
            tooltip.add(EvolutionTexts.durability(stack));
        }
        addEasterEggs(tooltip, stack);
        //Advanced (registry name + nbt)
        if (advanced.isAdvanced()) {
            tooltip.add(new StringTextComponent(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()).applyTextStyle(TextFormatting.DARK_GRAY));
            if (stack.hasTag()) {
                tooltip.add(new TranslationTextComponent("item.nbt_tags", stack.getTag().keySet().size()).applyTextStyle(TextFormatting.DARK_GRAY));
            }
        }
    }

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        if (!event.getPlayer().world.isRemote) {
            return;
        }
        if (event.getItemStack().isFood()) {
            Food food = event.getItemStack().getItem().getFood();
            if (food != null) {
                int pips = food.getHealing();
                int len = MathHelper.ceil((double) pips / 2);
                StringBuilder s = new StringBuilder(" ");
                for (int i = 0; i < len; i++) {
                    s.append("  ");
                }
                ITextComponent spaces = new StringTextComponent(s.toString());
                List<ITextComponent> tooltip = event.getToolTip();
                if (tooltip.isEmpty()) {
                    tooltip.add(spaces);
                }
                else {
                    tooltip.add(1, spaces);
                }
            }
        }
        Item item = event.getItemStack().getItem();
        if (!(item instanceof IEvolutionItem)) {
            return;
        }
        event.getToolTip().clear();
        makeEvolutionTooltip(event.getItemStack(), event.getToolTip(), event.getEntityPlayer(), event.getFlags());
    }
}
