package tgw.evolution.events;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.entities.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.*;
import tgw.evolution.util.EvolutionStyles;
import tgw.evolution.util.MathHelper;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final ITextComponent COMPONENT_TWO_HANDED = new TranslationTextComponent(TWO_HANDED).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent COMPONENT_OFFHAND = new TranslationTextComponent(OFFHAND).setStyle(EvolutionStyles.LIGHT_GREY);
    private static final ITextComponent COMPONENT_THROWABLE = new TranslationTextComponent(THROWABLE).setStyle(EvolutionStyles.PROPERTY);
    private static final ITextComponent EMPTY = new StringTextComponent("");

    public static void makeEvolutionTooltip(ItemStack stack, List<ITextComponent> tooltip, PlayerEntity playerIn, ITooltipFlag advanced) {
        //Name
        ITextComponent component = new StringTextComponent("").appendSibling(stack.getDisplayName()).applyTextStyle(stack.getRarity().color);
        if (stack.hasDisplayName()) {
            component.applyTextStyle(TextFormatting.ITALIC);
        }
        tooltip.add(component);
        int i = 0;
        if (stack.hasTag() && stack.getTag().contains("HideFlags", 99)) {
            i = stack.getTag().getInt("HideFlags");
        }
        //Item specific information
        if ((i & 32) == 0) {
            stack.getItem().addInformation(stack, playerIn == null ? null : playerIn.world, tooltip, advanced);
        }
        //Effects
        addEffectsTooltips(tooltip, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("display", 10)) {
                CompoundNBT compoundnbt = stack.getTag().getCompound("display");
                //Color
                if (compoundnbt.contains("color", 3)) {
                    if (advanced.isAdvanced()) {
                        tooltip.add(new TranslationTextComponent("item.color", String.format("#%06X", compoundnbt.getInt("color"))).applyTextStyle(TextFormatting.GRAY));
                    }
                    else {
                        tooltip.add(new TranslationTextComponent("item.dyed").applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));
                    }
                }
                //Lore
                if (compoundnbt.getTagId("Lore") == 9) {
                    ListNBT listnbt = compoundnbt.getList("Lore", 8);
                    for (int j = 0; j < listnbt.size(); ++j) {
                        String s = listnbt.getString(j);
                        try {
                            ITextComponent loreComponent = ITextComponent.Serializer.fromJson(s);
                            if (loreComponent != null) {
                                tooltip.add(TextComponentUtils.mergeStyles(loreComponent, EvolutionStyles.LORE));
                            }
                        }
                        catch (JsonParseException var19) {
                            compoundnbt.remove("Lore");
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
            if (!multimap.isEmpty() && (i & 2) == 0) {
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
                    if (playerIn != null) {
                        if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_DAMAGE_MODIFIER) == 0) {
                            amount += playerIn.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            String damage = stack.getItem() instanceof IMelee ? ((IMelee) stack.getItem()).getDamageType().getTranslationKey() : EvolutionDamage.Type.GENERIC.getTranslationKey();
                            tooltip.add(new TranslationTextComponent(damage, ItemStack.DECIMALFORMAT.format(amount)).setStyle(EvolutionStyles.DAMAGE));
                            isMassUnique = false;
                            continue;
                        }
                        if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_SPEED_MODIFIER) == 0) {
                            amount += playerIn.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                            tooltip.add(new TranslationTextComponent(SPEED, ItemStack.DECIMALFORMAT.format(amount)).setStyle(EvolutionStyles.SPEED));
                            isMassUnique = false;
                            continue;
                        }
                        if (attributemodifier.getID() == EvolutionAttributes.REACH_DISTANCE_MODIFIER) {
                            amount += playerIn.getAttribute(PlayerEntity.REACH_DISTANCE).getBaseValue();
                            tooltip.add(new TranslationTextComponent(DISTANCE, ItemStack.DECIMALFORMAT.format(amount)).setStyle(EvolutionStyles.REACH));
                            isMassUnique = false;
                            continue;
                        }
                        if (attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER || attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER_OFFHAND) {
                            if (hasMass) {
                                continue;
                            }
                            hasMass = true;
                            tooltip.add(sizeForMass, EMPTY);
                            tooltip.add(sizeForMass + 1, new TranslationTextComponent(MASS, ItemStack.DECIMALFORMAT.format(amount)).setStyle(EvolutionStyles.MASS));
                            continue;
                        }
                    }
                    double d1;
                    if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        d1 = amount;
                    }
                    else {
                        d1 = amount * 100.0D;
                    }
                    if (amount > 0.0D) {
                        tooltip.add(new TranslationTextComponent("attribute.modifier.plus." + attributemodifier.getOperation().getId(), ItemStack.DECIMALFORMAT.format(d1), new TranslationTextComponent("attribute.name." + entry.getKey())).applyTextStyle(TextFormatting.BLUE));
                        isMassUnique = false;
                        continue;
                    }
                    if (amount < 0.0D) {
                        d1 = d1 * -1.0D;
                        //noinspection ObjectAllocationInLoop
                        tooltip.add(new TranslationTextComponent("attribute.modifier.take." + attributemodifier.getOperation().getId(), ItemStack.DECIMALFORMAT.format(d1), new TranslationTextComponent("attribute.name." + entry.getKey())).applyTextStyle(TextFormatting.RED));
                        isMassUnique = false;
                    }
                }
                if (slot == EquipmentSlotType.MAINHAND && stack.getItem() instanceof ItemTool) {
                    float miningSpeed = ((ItemTool) stack.getItem()).getEfficiency();
                    //noinspection ObjectAllocationInLoop
                    tooltip.add(new TranslationTextComponent(MINING, ItemStack.DECIMALFORMAT.format(miningSpeed)).setStyle(EvolutionStyles.MINING));
                    isMassUnique = false;
                }
                if (hasMass && isMassUnique) {
                    tooltip.remove(tooltip.size() - 1);
                    tooltip.remove(tooltip.size() - 1);
                }
            }
        }
        //Unbreakable
        if (stack.hasTag() && stack.getTag().getBoolean("Unbreakable") && (i & 4) == 0) {
            tooltip.add(new TranslationTextComponent("item.unbreakable").applyTextStyle(TextFormatting.BLUE));
        }
        //Can destroy
        if (stack.hasTag() && stack.getTag().contains("CanDestroy", 9) && (i & 8) == 0) {
            ListNBT listnbt1 = stack.getTag().getList("CanDestroy", 8);
            if (!listnbt1.isEmpty()) {
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("item.canBreak").applyTextStyle(TextFormatting.GRAY));
                for (int k = 0; k < listnbt1.size(); ++k) {
                    tooltip.addAll(getPlacementTooltip(listnbt1.getString(k)));
                }
            }
        }
        //Can place on
        if (stack.hasTag() && stack.getTag().contains("CanPlaceOn", 9) && (i & 16) == 0) {
            ListNBT listnbt2 = stack.getTag().getList("CanPlaceOn", 8);
            if (!listnbt2.isEmpty()) {
                tooltip.add(new StringTextComponent(""));
                tooltip.add(new TranslationTextComponent("item.canPlace").applyTextStyle(TextFormatting.GRAY));
                for (int l = 0; l < listnbt2.size(); ++l) {
                    tooltip.addAll(getPlacementTooltip(listnbt2.getString(l)));
                }
            }
        }
        //Durability
        if (stack.getItem() instanceof IDurability) {
            tooltip.add(new TranslationTextComponent(DURABILITY, ((IDurability) stack.getItem()).displayDurability(stack)).setStyle(EvolutionStyles.DURABILITY));
        }
        //Advanced (registry name + nbt)
        if (advanced.isAdvanced()) {
            tooltip.add(new StringTextComponent(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()).applyTextStyle(TextFormatting.DARK_GRAY));
            if (stack.hasTag()) {
                tooltip.add(new TranslationTextComponent("item.nbt_tags", stack.getTag().keySet().size()).applyTextStyle(TextFormatting.DARK_GRAY));
            }
        }
    }

    private static void addEffectsTooltips(List<ITextComponent> tooltip, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IFireAspect) {
            tooltip.add(new TranslationTextComponent(FIRE_ASPECT, String.format(Locale.ENGLISH, "%d%%", (int) (((IFireAspect) item).getChance() * 100))).appendText(MathHelper.getRomanNumber(((IFireAspect) item).getModifier())).setStyle(EvolutionStyles.EFFECTS));
        }
        if (item instanceof IKnockback) {
            tooltip.add(new TranslationTextComponent(KNOCKBACK, String.format(Locale.ENGLISH, "%s", MathHelper.getRomanNumber(((IKnockback) item).getModifier()))));
        }
        if (item instanceof ISweepAttack) {
            tooltip.add(new TranslationTextComponent(SWEEP, String.format(Locale.ENGLISH, "%d%%", (int) ((ISweepAttack) item).getSweepRatio() * 100)));
        }
    }

    private static Collection<ITextComponent> getPlacementTooltip(String stateString) {
        try {
            BlockStateParser blockstateparser = new BlockStateParser(new StringReader(stateString), true).parse(true);
            BlockState blockstate = blockstateparser.getState();
            ResourceLocation resourcelocation = blockstateparser.getTag();
            boolean flag = blockstate != null;
            boolean flag1 = resourcelocation != null;
            if (flag || flag1) {
                if (flag) {
                    return Lists.newArrayList(blockstate.getBlock().getNameTextComponent().applyTextStyle(TextFormatting.DARK_GRAY));
                }
                Tag<Block> tag = BlockTags.getCollection().get(resourcelocation);
                if (tag != null) {
                    Collection<Block> collection = tag.getAllElements();
                    if (!collection.isEmpty()) {
                        return collection.stream().map(Block::getNameTextComponent).map(p_222119_0_ -> p_222119_0_.applyTextStyle(TextFormatting.DARK_GRAY)).collect(Collectors.toList());
                    }
                }
            }
        }
        catch (CommandSyntaxException ignored) {
        }
        return Lists.newArrayList(new StringTextComponent("missingno").applyTextStyle(TextFormatting.DARK_GRAY));
    }

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Item item = event.getItemStack().getItem();
        if (!(item instanceof IEvolutionItem)) {
            return;
        }
        event.getToolTip().clear();
        makeEvolutionTooltip(event.getItemStack(), event.getToolTip(), event.getEntityPlayer(), event.getFlags());
    }
}
