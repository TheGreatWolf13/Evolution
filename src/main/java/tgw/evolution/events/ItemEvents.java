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
import tgw.evolution.items.*;
import tgw.evolution.util.EvolutionStyles;
import tgw.evolution.util.MathHelper;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemEvents {

    private static final String SPEED = "evolution.tooltip.speed";
    private static final String DISTANCE = "evolution.tooltip.distance";
    private static final String MINING = "evolution.tooltip.mining";
    private static final String DURABILITY = "evolution.tooltip.durability";
    private static final String TWO_HANDED = "evolution.tooltip.two_handed";
    private static final String OFFHAND = "evolution.tooltip.offhand";
    private static final String THROWABLE = "evolution.tooltip.throwable";
    private static final String FIRE_ASPECT = "evolution.tooltip.fire_aspect";
    private static final String KNOCKBACK = "evolution.tooltip.knockback";
    private static final String SWEEP = "evolution.tooltip.sweep";
    private static final String MASS = "evolution.tooltip.mass";
    private static final String HEAVY_ATTACK = "evolution.tooltip.heavy_attack";
    private static final ITextComponent COMPONENT_TWO_HANDED = new TranslationTextComponent(TWO_HANDED).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent COMPONENT_OFFHAND = new TranslationTextComponent(OFFHAND).setStyle(EvolutionStyles.LIGHT_GREY);
    private static final ITextComponent COMPONENT_THROWABLE = new TranslationTextComponent(THROWABLE).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent EMPTY = new StringTextComponent("");

    private static void addEffectsTooltips(List<ITextComponent> tooltip, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IFireAspect) {
            tooltip.add(new TranslationTextComponent(FIRE_ASPECT,
                                                     String.format(Locale.ENGLISH,
                                                                   "%d%%",
                                                                   (int) (((IFireAspect) item).getChance() *
                                                                          100))).appendText(MathHelper.getRomanNumber(((IFireAspect) item).getLevel()))
                                                                                .setStyle(EvolutionStyles.EFFECTS));
        }
        if (item instanceof IHeavyAttack) {
            tooltip.add(new TranslationTextComponent(HEAVY_ATTACK,
                                                     String.format(Locale.ENGLISH,
                                                                   "%d%%",
                                                                   (int) (((IHeavyAttack) item).getChance() *
                                                                          100))).appendText(MathHelper.getRomanNumber(((IHeavyAttack) item).getLevel()))
                                                                                .setStyle(EvolutionStyles.EFFECTS));
        }
        if (item instanceof IKnockback) {
            tooltip.add(new TranslationTextComponent(KNOCKBACK,
                                                     String.format(Locale.ENGLISH, "%s", MathHelper.getRomanNumber(((IKnockback) item).getLevel()))));
        }
        if (item instanceof ISweepAttack) {
            tooltip.add(new TranslationTextComponent(SWEEP,
                                                     String.format(Locale.ENGLISH, "%d%%", (int) ((ISweepAttack) item).getSweepRatio() * 100)));
        }
    }

    public static void makeEvolutionTooltip(ItemStack stack, List<ITextComponent> tooltip, PlayerEntity playerIn, ITooltipFlag advanced) {
        //Name
        ITextComponent component = stack.getDisplayName().applyTextStyle(stack.getRarity().color);
        if (stack.hasDisplayName()) {
            component.applyTextStyle(TextFormatting.ITALIC);
        }
        tooltip.add(component);
        //Item specific information
        stack.getItem().addInformation(stack, playerIn == null ? null : playerIn.world, tooltip, advanced);
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
        if (stack.getItem() instanceof ITwoHanded) {
            tooltip.add(EMPTY);
            tooltip.add(COMPONENT_TWO_HANDED);
            hasAddedLine = true;
        }
        if (stack.getItem() instanceof IThrowable) {
            if (!hasAddedLine) {
                tooltip.add(EMPTY);
            }
            tooltip.add(COMPONENT_THROWABLE);
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
                    tooltip.add(EMPTY);
                }
                if (slot == EquipmentSlotType.MAINHAND && stack.getItem() instanceof IOffhandAttackable) {
                    tooltip.add(COMPONENT_OFFHAND);
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
                        amount += playerIn.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                        String damage = stack.getItem() instanceof IMelee ?
                                        ((IMelee) stack.getItem()).getDamageType().getTranslationKey() :
                                        EvolutionDamage.Type.GENERIC.getTranslationKey();
                        tooltip.add(new StringTextComponent("    ").appendSibling(new TranslationTextComponent(damage,
                                                                                                               ItemStack.DECIMALFORMAT.format(amount))
                                                                                          .setStyle(EvolutionStyles.DAMAGE)));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_SPEED_MODIFIER) == 0) {
                        amount += playerIn.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                        tooltip.add(new StringTextComponent("    ").appendSibling(new TranslationTextComponent(SPEED,
                                                                                                               ItemStack.DECIMALFORMAT.format(amount))
                                                                                          .setStyle(EvolutionStyles.SPEED)));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID() == EvolutionAttributes.REACH_DISTANCE_MODIFIER) {
                        amount += playerIn.getAttribute(PlayerEntity.REACH_DISTANCE).getBaseValue();
                        tooltip.add(new StringTextComponent("    ").appendSibling(new TranslationTextComponent(DISTANCE,
                                                                                                               ItemStack.DECIMALFORMAT.format(amount))
                                                                                          .setStyle(EvolutionStyles.REACH)));
                        isMassUnique = false;
                        continue;
                    }
                    if (attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER ||
                        attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER_OFFHAND) {
                        if (hasMass) {
                            continue;
                        }
                        hasMass = true;
                        tooltip.add(sizeForMass, EMPTY);
                        tooltip.add(sizeForMass + 1,
                                    new StringTextComponent("   ").appendSibling(new TranslationTextComponent(MASS,
                                                                                                              ItemStack.DECIMALFORMAT.format(amount)).setStyle(
                                            EvolutionStyles.MASS)));
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
                    //noinspection ObjectAllocationInLoop
                    tooltip.add(new StringTextComponent("    ").appendSibling(new TranslationTextComponent(MINING,
                                                                                                           ItemStack.DECIMALFORMAT.format(miningSpeed))
                                                                                      .setStyle(EvolutionStyles.MINING)));
                    isMassUnique = false;
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
            tooltip.add(new StringTextComponent("   ").appendSibling(new TranslationTextComponent(DURABILITY,
                                                                                                  ((IDurability) stack.getItem()).displayDurability(
                                                                                                          stack)).setStyle(EvolutionStyles.DURABILITY)));
        }
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
