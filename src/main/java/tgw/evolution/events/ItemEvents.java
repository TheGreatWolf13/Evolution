package tgw.evolution.events;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.client.tooltip.*;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.*;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.constants.HarvestLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public final class ItemEvents {

    private static final OList<Component> TEMP_TOOLTIP_HOLDER = new OArrayList<>();

    private ItemEvents() {
    }

    private static void add(List<Either<FormattedText, TooltipComponent>> tooltip, FormattedText comp) {
        tooltip.add(Either.left(comp));
    }

    private static void addEasterEggs(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack) {
        if (Screen.hasControlDown()) {
            Item item = stack.getItem();
            if (item instanceof ItemRock rock) {
                switch (rock.getVariant()) {
                    case CHERT -> {
                        add(tooltip, EvolutionTexts.EMPTY);
                        add(tooltip, EvolutionTexts.EASTER_CHERT);
                    }
                    case GABBRO -> {
                        add(tooltip, EvolutionTexts.EMPTY);
                        add(tooltip, EvolutionTexts.EASTER_GABBRO);
                    }
                    case GNEISS -> {
                        add(tooltip, EvolutionTexts.EMPTY);
                        add(tooltip, EvolutionTexts.EASTER_GNEISS);
                    }
                    case SLATE -> {
                        add(tooltip, EvolutionTexts.EMPTY);
                        add(tooltip, EvolutionTexts.EASTER_SLATE);
                    }
                }
            }
        }
    }

    private static void addEffectsTooltips(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IFireAspect fireAspect) {
            add(tooltip, EvolutionTexts.fireAspect(fireAspect));
        }
        if (item instanceof IHeavyAttack heavyAttack) {
            add(tooltip, EvolutionTexts.heavyAttack(heavyAttack));
        }
        if (item instanceof IKnockback knockback) {
            add(tooltip, EvolutionTexts.knockback(knockback));
        }
        if (item instanceof ISweepAttack sweepAttack) {
            add(tooltip, EvolutionTexts.sweep(sweepAttack));
        }
    }

    private static void addFluidInfo(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack) {
        IItemFluidContainer container = (IItemFluidContainer) stack.getItem();
        if (container.isEmpty(stack)) {
            add(tooltip, EvolutionTexts.TOOLTIP_CONTAINER_EMPTY);
        }
        else {
            add(tooltip, EvolutionTexts.container(container, stack));
        }
        add(tooltip, EvolutionTexts.capacity(container));
    }

    public static <E extends LivingEntity> void damageItem(ItemStack stack, E entity, ItemModular.DamageCause cause, @Nullable EquipmentSlot slot) {
        Consumer<E> onBreak = slot == null ? e -> {
        } : e -> e.broadcastBreakEvent(slot);
        if (stack.getItem() instanceof ItemModular modular) {
            modular.hurtAndBreak(stack, cause, entity, onBreak);
        }
        else {
            int amount = switch (cause) {
                case BREAK_BLOCK, HIT_ENTITY -> 1;
                case HIT_BLOCK -> 0;
                case BREAK_BAD_BLOCK -> 2;
            };
            stack.hurtAndBreak(amount, entity, onBreak);
        }
    }

    public static <E extends LivingEntity> void damageItem(ItemStack stack,
                                                           E entity,
                                                           ItemModular.DamageCause cause,
                                                           @Nullable EquipmentSlot slot,
                                                           @HarvestLevel int harvestLevel) {
        Consumer<E> onBreak = slot == null ? e -> {
        } : e -> e.broadcastBreakEvent(slot);
        if (stack.getItem() instanceof ItemModular modular) {
            modular.hurtAndBreak(stack, cause, entity, onBreak, harvestLevel);
        }
        else {
            int amount = switch (cause) {
                case BREAK_BLOCK, HIT_ENTITY -> 1;
                case HIT_BLOCK -> 0;
                case BREAK_BAD_BLOCK -> 2;
            };
            stack.hurtAndBreak(amount, entity, onBreak);
        }
    }

    public static void makeEvolutionTooltip(ItemStack stack, List<Either<FormattedText, TooltipComponent>> tooltip) {
        tooltip.clear();
        //Name
        MutableComponent name = stack.getHoverName().copy().withStyle(stack.getRarity().color);
        if (stack.hasCustomHoverName()) {
            name.withStyle(ChatFormatting.ITALIC);
        }
        add(tooltip, name);
        //Item specific information
        Item item = stack.getItem();
        boolean isAdvanced = Minecraft.getInstance().options.advancedItemTooltips;
        item.appendHoverText(stack, Evolution.PROXY.getClientLevel(), TEMP_TOOLTIP_HOLDER,
                             isAdvanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        for (int i = 0, l = TEMP_TOOLTIP_HOLDER.size(); i < l; i++) {
            add(tooltip, TEMP_TOOLTIP_HOLDER.get(i));
        }
        TEMP_TOOLTIP_HOLDER.clear();
        if (item instanceof IItemFluidContainer) {
            addFluidInfo(tooltip, stack);
        }
        //Effects
        addEffectsTooltips(tooltip, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("display", Tag.TAG_COMPOUND)) {
                CompoundTag nbt = stack.getTag().getCompound("display");
                //Color
                if (nbt.contains("color", Tag.TAG_INT)) {
                    if (isAdvanced) {
                        add(tooltip,
                            new TranslatableComponent("item.color", String.format("#%06X", nbt.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    }
                    else {
                        add(tooltip, new TranslatableComponent("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    }
                }
                //Lore
                if (nbt.getTagType("Lore") == Tag.TAG_LIST) {
                    ListTag lore = nbt.getList("Lore", Tag.TAG_STRING);
                    for (int j = 0; j < lore.size(); j++) {
                        String s = lore.getString(j);
                        try {
                            MutableComponent loreComponent = Component.Serializer.fromJson(s);
                            if (loreComponent != null) {
                                add(tooltip, ComponentUtils.mergeStyles(loreComponent, EvolutionStyles.LORE));
                            }
                        }
                        catch (JsonParseException exception) {
                            nbt.remove("Lore");
                        }
                    }
                }
            }
        }
        //Part
        if (item instanceof ItemPart part) {
            add(tooltip, EvolutionTexts.EMPTY);
            part.getPartCap(stack).appendText(tooltip, 0);
        }
        //Modular
        if (item instanceof ItemModular modular) {
            if (Screen.hasControlDown()) {
                add(tooltip, EvolutionTexts.EMPTY);
                //Show Materials
                modular.getModularCap(stack).appendTooltip(tooltip);
            }
            else {
                add(tooltip, EvolutionTexts.EMPTY);
                add(tooltip, EvolutionTexts.TOOLTIP_SHOW_PARTS);
            }
        }
        //Mass
        if (item instanceof IMass mass && !(item instanceof ItemPart)) {
            add(tooltip, EvolutionTexts.EMPTY);
            tooltip.add(Either.right(EvolutionTooltipMass.MAIN.mass(mass.getMass(stack))));
        }
        //Properties
        boolean hasAddedLine = false;
        if (item instanceof ITwoHanded twoHanded && twoHanded.isTwoHanded(stack)) {
            add(tooltip, EvolutionTexts.EMPTY);
            add(tooltip, EvolutionTexts.TOOLTIP_TWO_HANDED);
            hasAddedLine = true;
        }
        if (item instanceof IThrowable throwable && throwable.isThrowable(stack)) {
            if (!hasAddedLine) {
                add(tooltip, EvolutionTexts.EMPTY);
            }
            add(tooltip, EvolutionTexts.TOOLTIP_THROWABLE);
            hasAddedLine = true;
        }
        if (item instanceof ILunge) {
            if (!hasAddedLine) {
                add(tooltip, EvolutionTexts.EMPTY);
            }
            add(tooltip, EvolutionTexts.TOOLTIP_LUNGE);
            hasAddedLine = true;
        }
        if (item instanceof IParry) {
            if (!hasAddedLine) {
                add(tooltip, EvolutionTexts.EMPTY);
            }
            add(tooltip, EvolutionTexts.TOOLTIP_PARRY);
            hasAddedLine = true;
        }
        //Consumable
        if (item instanceof IConsumable) {
            if (!hasAddedLine) {
                add(tooltip, EvolutionTexts.EMPTY);
            }
            add(tooltip, EvolutionTexts.TOOLTIP_CONSUMABLE);
            hasAddedLine = true;
            if (item instanceof IFood food) {
                tooltip.add(Either.right(EvolutionTooltipFood.INSTANCE.hunger(food.getHunger())));
            }
            if (item instanceof IDrink drink) {
                tooltip.add(Either.right(EvolutionTooltipDrink.INSTANCE.thirst(drink.getThirst())));
            }
            if (item instanceof INutrient) {
                //TODO make nutrient tooltip if even
            }
        }
        //Melee attributes
        if (item instanceof IMelee melee) {
            if (hasAddedLine) {
                hasAddedLine = false;
            }
            else {
                add(tooltip, EvolutionTexts.EMPTY);
            }
            if (item instanceof IOffhandAttackable) {
                add(tooltip, EvolutionTexts.TOOLTIP_MAINHAND_OFFHAND);
            }
            else {
                add(tooltip, EvolutionTexts.TOOLTIP_MAINHAND);
            }
            tooltip.add(Either.right(EvolutionTooltipDamage.INSTANCE.damage(melee.getDamageType(stack), melee.getAttackDamage(stack))));
            tooltip.add(Either.right(EvolutionTooltipSpeed.INSTANCE.speed(melee.getAttackSpeed(stack))));
            if (item instanceof ItemModularTool tool) {
                if (!tool.getModularCap(stack).getEffectiveMaterials().isEmpty()) {
                    tooltip.add(Either.right(EvolutionTooltipMining.INSTANCE.mining(tool.getMiningSpeed(stack))));
                }
            }
        }
        //Additional Equipment stats
        if (item instanceof IAdditionalEquipment) {
            boolean hasAddedSlot = false;
            if (item instanceof IHeatResistant heatResistant) {
                if (!hasAddedLine) {
                    add(tooltip, EvolutionTexts.EMPTY);
                    hasAddedLine = true;
                }
                add(tooltip, new TranslatableComponent("evolution.tooltip.slot." + ((IAdditionalEquipment) item).getValidSlot().getName()).withStyle(
                        ChatFormatting.GRAY));
                hasAddedSlot = true;
                tooltip.add(Either.right(EvolutionTooltipHeat.INSTANCE.heat(heatResistant.getHeatResistance())));
            }
            if (item instanceof IColdResistant coldResistant) {
                if (!hasAddedLine) {
                    add(tooltip, EvolutionTexts.EMPTY);
                }
                if (!hasAddedSlot) {
                    add(tooltip,
                        new TranslatableComponent("evolution.tooltip.slot." + ((IAdditionalEquipment) item).getValidSlot().getName()).withStyle(
                                ChatFormatting.GRAY));
                }
                tooltip.add(Either.right(EvolutionTooltipCold.INSTANCE.cold(coldResistant.getColdResistance())));
            }
        }
        //Unbreakable
        if (stack.hasTag() && stack.getTag().getBoolean("Unbreakable")) {
            add(tooltip, EvolutionTexts.TOOLTIP_UNBREAKABLE);
        }
        //Durability
        if (stack.getItem() instanceof IDurability durability && !(item instanceof ItemPart)) {
            tooltip.add(Either.right(EvolutionTooltipDurability.MAIN.durability(durability.displayDurability(stack))));
        }
        addEasterEggs(tooltip, stack);
        //Advanced (registry name + nbt)
        if (isAdvanced) {
            add(tooltip, new TextComponent(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (stack.hasTag()) {
                add(tooltip, new TranslatableComponent("item.nbt_tags", stack.getTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
