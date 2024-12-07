package tgw.evolution.items;

import com.google.gson.JsonParseException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.tooltip.*;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.lists.custom.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class ItemUtils {

    private static final OList<Component> TEMP_TOOLTIP_HOLDER = new OArrayList<>();
    private static final EitherList<FormattedText, TooltipComponent> EITHER_LIST = new EitherList<>();
    private static final OList<ClientTooltipComponent> TOOLTIP = new OArrayList<>();

    private ItemUtils() {
    }

    private static void addEasterEggs(EitherList<FormattedText, TooltipComponent> tooltip, ItemStack stack) {
        if (Screen.hasControlDown()) {
            Item item = stack.getItem();
            if (item instanceof ItemRock rock) {
                switch (rock.rockVariant()) {
                    case CHERT -> {
                        tooltip.addLeft(EvolutionTexts.EMPTY);
                        tooltip.addLeft(EvolutionTexts.EASTER_CHERT);
                    }
                    case GABBRO -> {
                        tooltip.addLeft(EvolutionTexts.EMPTY);
                        tooltip.addLeft(EvolutionTexts.EASTER_GABBRO);
                    }
                    case GNEISS -> {
                        tooltip.addLeft(EvolutionTexts.EMPTY);
                        tooltip.addLeft(EvolutionTexts.EASTER_GNEISS);
                    }
                    case SLATE -> {
                        tooltip.addLeft(EvolutionTexts.EMPTY);
                        tooltip.addLeft(EvolutionTexts.EASTER_SLATE);
                    }
                }
            }
        }
    }

    private static void addEffectsTooltips(EitherList<FormattedText, TooltipComponent> tooltip, ItemStack stack) {
        Item item = stack.getItem();
        boolean isAltDown = Screen.hasAltDown();
        if (item instanceof IFireAspect fireAspect) {
            tooltip.addLeft(EvolutionTexts.fireAspect(fireAspect));
            if (isAltDown) {
                tooltip.addLeft(EvolutionTexts.fireAspectDesc(fireAspect));
            }
        }
        if (item instanceof IHeavyAttack heavyAttack) {
            tooltip.addLeft(EvolutionTexts.heavyAttack(heavyAttack));
            if (isAltDown) {
                tooltip.addLeft(EvolutionTexts.heavyAttackDesc1(heavyAttack));
                tooltip.addLeft(EvolutionTexts.heavyAttackDesc2(heavyAttack));
            }
        }
        if (item instanceof IKnockback knockback) {
            tooltip.addLeft(EvolutionTexts.knockback(knockback));
        }
    }

    private static void addFluidInfo(EitherList<FormattedText, TooltipComponent> tooltip, ItemStack stack) {
        IItemFluidContainer container = (IItemFluidContainer) stack.getItem();
        if (container.isEmpty(stack)) {
            tooltip.addLeft(EvolutionTexts.TOOLTIP_CONTAINER_EMPTY);
        }
//        else {
//            tooltip.addLeft(EvolutionTexts.container(container, stack));
//        }
        tooltip.addLeft(EvolutionTexts.capacity(container));
    }

    public static <E extends LivingEntity> void damageItem(ItemStack stack, E entity, ItemModular.DamageCause cause, @Nullable EquipmentSlot slot, @HarvestLevel int harvestLevel) {
        if (stack.getItem() instanceof ItemModular modular) {
            modular.hurtAndBreak(stack, cause, entity, slot, harvestLevel);
        }
        else {
            int amount = switch (cause) {
                case BREAK_BLOCK, HIT_ENTITY -> 1;
                case HIT_BLOCK -> 0;
                case BREAK_BAD_BLOCK -> 2;
            };
            Consumer<E> onBreak = slot == null ? e -> {} : e -> e.broadcastBreakEvent(slot);
            stack.hurtAndBreak(amount, entity, onBreak);
        }
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, Font font) {
        EitherList<FormattedText, TooltipComponent> elements = EITHER_LIST;
        if (stack.getItem() instanceof IEvolutionItem) {
            makeEvolutionTooltip(EvolutionClient.getClientPlayer(), stack, elements);
        }
        else {
            for (int i = 0, len = textElements.size(); i < len; i++) {
                elements.addLeft(textElements.get(i));
            }
            if (itemComponent.isPresent()) {
                elements.addRight(1, itemComponent.get());
            }
        }
        // text wrapping
        int tooltipTextWidth = 0;
        for (int i = 0, len = elements.size(); i < len; i++) {
            FormattedText left = elements.getLeftOrNull(i);
            int w = left != null ? font.width(left) : 0;
            if (w > tooltipTextWidth) {
                tooltipTextWidth = w;
            }
        }
        boolean needsWrap = false;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
                if (mouseX > screenWidth / 2) {
                    tooltipTextWidth = mouseX - 12 - 8;
                }
                else {
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                }
                needsWrap = true;
            }
        }
        OList<ClientTooltipComponent> list = TOOLTIP;
        list.clear();
        if (needsWrap) {
            for (int i = 0, len = elements.size(); i < len; i++) {
                if (elements.isLeft(i)) {
                    List<FormattedCharSequence> split = font.split(elements.getLeft(i), tooltipTextWidth);
                    for (int j = 0, len1 = split.size(); j < len1; j++) {
                        //noinspection ObjectAllocationInLoop
                        list.add(ClientTooltipComponent.create(split.get(j)));
                    }
                }
                else {
                    //noinspection ObjectAllocationInLoop
                    list.add(ClientTooltipComponent.create(elements.getRight(i)));
                }
            }
            elements.clear();
            return list;
        }
        for (int i = 0, len = elements.size(); i < len; i++) {
            if (elements.isLeft(i)) {
                FormattedText text = elements.getLeft(i);
                //noinspection ObjectAllocationInLoop
                list.add(ClientTooltipComponent.create(text instanceof Component comp ? comp.getVisualOrderText() : Language.getInstance().getVisualOrder(text)));
            }
            else {
                //noinspection ObjectAllocationInLoop
                list.add(ClientTooltipComponent.create(elements.getRight(i)));
            }
        }
        elements.clear();
        return list;
    }

    public static double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type) {
        return stack.getItem() instanceof ItemModularTool mod ? mod.getDmgMultiplier(stack, type) : 1;
    }

    public static double getMass(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod ? mod.getMass(stack) : 0;
    }

    public static RepeatedUse getRepeatedUse(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock b) {
            return b.block.getRepeatedUse();
        }
        if (item instanceof BlockItem b) {
            return b.getBlock().getRepeatedUse();
        }
        return RepeatedUse.NEVER;
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isAxe(stack);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, Level level, int x, int y, int z) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.isCorrectToolForDrops(stack, state, level, x, y, z);
        }
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean isHammer(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isHammer(stack);
    }

    public static boolean isSameIgnoreCount(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() && other.isEmpty()) {
            return true;
        }
        return !stack.isEmpty() && !other.isEmpty() && matches(stack, other);
    }

    private static void makeEvolutionTooltip(Player player, ItemStack stack, EitherList<FormattedText, TooltipComponent> tooltip) {
        tooltip.clear();
        //Name
        MutableComponent name = stack.getHoverName().copy().withStyle(stack.getRarity().color);
        if (stack.hasCustomHoverName()) {
            name.withStyle(ChatFormatting.ITALIC);
        }
        tooltip.addLeft(name);
        //Item specific information
        Item item = stack.getItem();
        boolean isAdvanced = Minecraft.getInstance().options.advancedItemTooltips;
        item.appendHoverText(stack, EvolutionClient.getClientLevel(), TEMP_TOOLTIP_HOLDER, isAdvanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        for (int i = 0, l = TEMP_TOOLTIP_HOLDER.size(); i < l; i++) {
            tooltip.addLeft(TEMP_TOOLTIP_HOLDER.get(i));
        }
        TEMP_TOOLTIP_HOLDER.clear();
        if (item instanceof IItemFluidContainer) {
            addFluidInfo(tooltip, stack);
        }
        //Properties
        boolean hasAddedLine = false;
        if (item instanceof ITwoHanded twoHanded && twoHanded.isTwoHanded(stack)) {
            tooltip.addLeft(EvolutionTexts.TOOLTIP_TWO_HANDED);
        }
        if (item instanceof IThrowable throwable && throwable.isThrowable(stack, player)) {
            tooltip.addLeft(EvolutionTexts.TOOLTIP_THROWABLE);
        }
        if (item instanceof IParry) {
            tooltip.addLeft(EvolutionTexts.TOOLTIP_PARRY);
        }
        //Effects
        addEffectsTooltips(tooltip, stack);
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            assert tag != null;
            if (tag.contains("display", Tag.TAG_COMPOUND)) {
                CompoundTag nbt = tag.getCompound("display");
                //Color
                if (nbt.contains("color", Tag.TAG_INT)) {
                    if (isAdvanced) {
                        tooltip.addLeft(new TranslatableComponent("item.color", String.format("#%06X", nbt.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    }
                    else {
                        tooltip.addLeft(new TranslatableComponent("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
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
                                tooltip.addLeft(ComponentUtils.mergeStyles(loreComponent, EvolutionStyles.LIGHT_PURPLE_ITALIC));
                            }
                        }
                        catch (JsonParseException exception) {
                            nbt.remove("Lore");
                        }
                    }
                }
            }
        }
        //Structural
        if (item instanceof ItemBlock i) {
            i.makeTooltip(tooltip, Screen.hasControlDown());
        }
        //Part
        if (item instanceof ItemPart part) {
            tooltip.addLeft(EvolutionTexts.EMPTY);
            part.makeTooltip(tooltip, stack, 0);
        }
        //Modular
        if (item instanceof ItemModular modular) {
            if (Screen.hasControlDown()) {
                tooltip.addLeft(EvolutionTexts.EMPTY);
                //Show Materials
                modular.makeTooltip(tooltip, stack);
            }
            else {
                tooltip.addLeft(EvolutionTexts.EMPTY);
                tooltip.addLeft(EvolutionTexts.TOOLTIP_SHOW_PARTS);
            }
        }
        //Consumable
        if (item instanceof IConsumable) {
            tooltip.addLeft(EvolutionTexts.EMPTY);
            tooltip.addLeft(EvolutionTexts.TOOLTIP_CONSUMABLE);
            hasAddedLine = true;
            if (item instanceof IFood food) {
                tooltip.addRight(TooltipFood.hunger(food.getHunger()));
            }
            if (item instanceof IDrink drink) {
                tooltip.addRight(TooltipDrink.thirst(drink.getThirst()));
            }
            if (item instanceof INutrient) {
                //TODO make nutrient tooltip if even
            }
        }
        //Melee item
        if (item instanceof IMelee melee) {
            if (Screen.hasAltDown()) {
                tooltip.addLeft(EvolutionTexts.EMPTY);
                //Show Stats
                melee.makeTooltip(player, tooltip, stack);
            }
            else {
                tooltip.addLeft(EvolutionTexts.TOOLTIP_SHOW_MELEE_STATS);
            }
        }
        //Additional Equipment stats
        if (item instanceof IAdditionalEquipment) {
            boolean hasAddedSlot = false;
            if (item instanceof IHeatResistant heatResistant) {
                if (!hasAddedLine) {
                    tooltip.addLeft(EvolutionTexts.EMPTY);
                    hasAddedLine = true;
                }
                tooltip.addLeft(new TranslatableComponent("evolution.tooltip.slot." + ((IAdditionalEquipment) item).getValidSlot().getName()).withStyle(ChatFormatting.GRAY));
                hasAddedSlot = true;
                tooltip.addRight(TooltipHeat.heat(heatResistant.getHeatResistance()));
            }
            if (item instanceof IColdResistant coldResistant) {
                if (!hasAddedLine) {
                    tooltip.addLeft(EvolutionTexts.EMPTY);
                }
                if (!hasAddedSlot) {
                    tooltip.addLeft(new TranslatableComponent("evolution.tooltip.slot." + ((IAdditionalEquipment) item).getValidSlot().getName()).withStyle(ChatFormatting.GRAY));
                }
                tooltip.addRight(TooltipCold.cold(coldResistant.getColdResistance()));
            }
        }
        addEasterEggs(tooltip, stack);
        //Mass
        if (item instanceof IMass mass && !(item instanceof ItemPart)) {
            tooltip.addLeft(EvolutionTexts.EMPTY);
            tooltip.addRight(TooltipMass.mass(mass.getMass(stack)));
        }
        //Unbreakable
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().getBoolean("Unbreakable")) {
                tooltip.addLeft(EvolutionTexts.TOOLTIP_UNBREAKABLE);
            }
        }
        //TODO modify to integrity
        //Durability
        if (stack.getItem() instanceof IDurability durability && !(item instanceof ItemPart)) {
            tooltip.addRight(TooltipDurability.durability(durability.displayDurability(stack)));
        }
        //Advanced (registry name + nbt)
        if (isAdvanced) {
            ResourceLocation key = Registry.ITEM.getKey(stack.getItem());
            tooltip.addLeft(new TextComponent(key.toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                tooltip.addLeft(new TranslatableComponent("item.nbt_tags", stack.getTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static boolean matches(ItemStack stack, ItemStack other) {
        if (!stack.is(other.getItem())) {
            return false;
        }
        if (stack.getTag() == null && other.getTag() != null) {
            return false;
        }
        return stack.getTag() == null || stack.getTag().equals(other.getTag());
    }

    public static boolean usesModularRendering(ItemStack stack) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.usesModularRendering();
        }
        return false;
    }

    public enum RepeatedUse {
        ALWAYS,
        NEVER,
        NOT_ON_FIRST_TICK
    }
}
