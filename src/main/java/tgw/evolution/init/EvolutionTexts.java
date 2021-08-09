package tgw.evolution.init;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.items.*;
import tgw.evolution.util.MathHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static tgw.evolution.init.EvolutionStyles.*;

public final class EvolutionTexts {

    public static final ITextComponent ACTION_HIT_STAKE = transl("evolution.actionbar.hit_stake").setStyle(WHITE);
    public static final ITextComponent ACTION_HOOK = transl("evolution.actionbar.hook").setStyle(WHITE);
    public static final ITextComponent ACTION_INERTIA = transl("evolution.actionbar.inertia").setStyle(WHITE);
    public static final ITextComponent ACTION_TWO_HANDED = transl("evolution.actionbar.two_handed").setStyle(WHITE);
    public static final ITextComponent DEATH_FISTS = transl("death.item.fists");
    public static final ITextComponent EASTER_CHERT = transl("evolution.easter.chert").setStyle(LORE);
    public static final ITextComponent EASTER_GABBRO = transl("evolution.easter.gabbro").setStyle(LORE);
    public static final ITextComponent EASTER_GNEISS = transl("evolution.easter.gneiss").setStyle(LORE);
    public static final ITextComponent EASTER_SLATE = transl("evolution.easter.slate").setStyle(LORE);
    public static final ITextComponent EMPTY = new StringTextComponent("");
    public static final ITextComponent FLUID_FRESH_WATER = transl("evolution.fluid.fresh_water");
    public static final ITextComponent FLUID_SALT_WATER = transl("evolution.fluid.salt_water");
    public static final ITextComponent GUI_CRASH = transl("evolution.gui.crash");
    public static final ITextComponent GUI_CRASH_REPORT_SAVE_FAILED = transl("evolution.gui.crash.reportSaveFailed").setStyle(DAMAGE);
    public static final ITextComponent GUI_EDIT_VALUE = transl("evolution.gui.edit_value");
    public static final ITextComponent GUI_KNAPPING = transl("evolution.gui.knapping");
    public static final ITextComponent GUI_OUT_OF_MEMORY = transl("evolution.gui.outOfMemory");
    public static final ITextComponent GUI_SEARCH = transl("evolution.gui.search");
    public static final ITextComponent STAT_DAMAGE_DEALT_ACTUAL = transl("stat_type.evolution.damage_dealt_actual");
    public static final ITextComponent STAT_DAMAGE_DEALT_RAW = transl("stat_type.evolution.damage_dealt_raw");
    public static final ITextComponent STAT_DAMAGE_TAKEN_ACTUAL = transl("stat_type.evolution.damage_taken_actual");
    public static final ITextComponent STAT_DAMAGE_TAKEN_BLOCKED = transl("stat_type.evolution.damage_taken_blocked");
    public static final ITextComponent STAT_DAMAGE_TAKEN_RAW = transl("stat_type.evolution.damage_taken_raw");
    public static final ITextComponent TOOLTIP_CLAY_MOLD = transl("evolution.tooltip.clay.mold").setStyle(INFO);
    public static final ITextComponent TOOLTIP_CONSUMABLE = transl("evolution.tooltip.consumable").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_EMPTY_CONTAINER = transl("evolution.tooltip.container.empty").setStyle(INFO);
    public static final ITextComponent TOOLTIP_LUNGE = transl("evolution.tooltip.lunge").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_OFFHAND = transl("evolution.tooltip.offhand").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_PARRY = transl("evolution.tooltip.parry").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_ROCK_KNAP = transl("evolution.tooltip.rock.knap").setStyle(INFO);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_IGEXTRUSIVE = transl("evolution.tooltip.rock_type.igneous_extrusive").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_IGINTRUSIVE = transl("evolution.tooltip.rock_type.igneous_intrusive").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_METAMORPHIC = transl("evolution.tooltip.rock_type.metamorphic").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_SEDIMENTARY = transl("evolution.tooltip.rock_type.sedimentary").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROPE = transl("evolution.tooltip.rope").setStyle(INFO);
    public static final ITextComponent TOOLTIP_STICK_LIT = transl("evolution.tooltip.stick.lit").setStyle(INFO);
    public static final ITextComponent TOOLTIP_THROWABLE = transl("evolution.tooltip.throwable").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_TORCH_RELIT = translSp("evolution.tooltip.torch" + ".relit").setStyle(INFO);
    public static final ITextComponent TOOLTIP_TWO_HANDED = transl("evolution.tooltip.two_handed").setStyle(PROPERTY);

    private static final DecimalFormatSymbols SYMBOLS = getSymbols();

    public static final DecimalFormat DEFAULT = initFormat(",##0");
    public static final DecimalFormat ONE_PLACE = initFormat(",##0.#");
    public static final DecimalFormat TWO_PLACES = initFormat(",##0.##");
    public static final DecimalFormat THREE_PLACES = initFormat(",##0.###");

    public static final DecimalFormat DAMAGE_FORMAT = initFormat(",##0 HP");
    public static final DecimalFormat DRINK_FORMAT = initFormat(",##0 mL");
    public static final DecimalFormat FOOD_FORMAT = initFormat(",##0 kcal");
    public static final DecimalFormat HOUR_FORMAT = initFormat(",##0 h");
    public static final DecimalFormat LITER_FORMAT = initFormat(",##0.## L");
    public static final DecimalFormat MASS_FORMAT = initFormat(",##0.## kg");
    public static final DecimalFormat PERCENT_ONE_PLACE = initFormat(",##0.#%");

    private static final String CAPACITY = "evolution.tooltip.container.capacity";
    private static final String CONFIG_ALLOWED_VALUES = "evolution.config.allowed_values";
    private static final String CONFIG_DEFAULT = "evolution.config.default";
    private static final String CONFIG_RANGE = "evolution.config.range";
    private static final String CONTAINER = "evolution.tooltop.container.amount";
    private static final String DISTANCE = "evolution.tooltip.distance";
    private static final String DURABILITY = "evolution.tooltip.durability";
    private static final String FIRE_ASPECT = "evolution.tooltip.fire_aspect";
    private static final String HEAVY_ATTACK = "evolution.tooltip.heavy_attack";
    private static final String KNOCKBACK = "evolution.tooltip.knockback";
    private static final String MASS = "evolution.tooltip.mass";
    private static final String MINING = "evolution.tooltip.mining";
    private static final String PARRY = "evolution.tooltip.parry";
    private static final String SPEED = "evolution.tooltip.speed";
    private static final String SWEEP = "evolution.tooltip.sweep";

    private EvolutionTexts() {
    }

    public static ITextComponent capacity(IItemFluidContainer container) {
        return new TranslationTextComponent(CAPACITY, LITER_FORMAT.format(container.getMaxAmount() / 100.0f)).setStyle(INFO);
    }

    private static ITextComponent chanceAndLevel(String message, float chance, int level) {
        return new TranslationTextComponent(message, PERCENT_ONE_PLACE.format(chance), MathHelper.getRomanNumber(level));
    }

    public static ITextComponent configAllowedValues(String allowed) {
        return new TranslationTextComponent(CONFIG_ALLOWED_VALUES, allowed).setStyle(LIGHT_GREY);
    }

    public static ITextComponent configDefault(String def) {
        return new TranslationTextComponent(CONFIG_DEFAULT, def).setStyle(LIGHT_GREY);
    }

    public static ITextComponent configRange(String range) {
        return new TranslationTextComponent(CONFIG_RANGE, range).setStyle(LIGHT_GREY);
    }

    public static ITextComponent container(IItemFluidContainer container, ItemStack stack) {
        return new TranslationTextComponent(CONTAINER,
                                            LITER_FORMAT.format(container.getAmount(stack) / 100.0f),
                                            container.getFluid() instanceof FluidGeneric ?
                                            ((FluidGeneric) container.getFluid()).getTextComp() :
                                            "null").setStyle(INFO);
    }

    public static ITextComponent damage(String damage, double amount) {
        return new StringTextComponent("    ").appendSibling(new TranslationTextComponent(damage, TWO_PLACES.format(amount)).setStyle(DAMAGE));
    }

    public static ITextComponent distance(double amount) {
        return new StringTextComponent("    ").appendSibling(new TranslationTextComponent(DISTANCE, TWO_PLACES.format(amount)).setStyle(REACH));
    }

    public static ITextComponent drink(int amount) {
        return new StringTextComponent("    " + DRINK_FORMAT.format(amount));
    }

    public static ITextComponent durability(ItemStack stack) {
        return new StringTextComponent("   ").appendSibling(new TranslationTextComponent(DURABILITY,
                                                                                         ((IDurability) stack.getItem()).displayDurability(stack)).setStyle(
                EvolutionStyles.DURABILITY));
    }

    public static ITextComponent fireAspect(IFireAspect item) {
        return chanceAndLevel(FIRE_ASPECT, item.getChance(), item.getLevel()).setStyle(EFFECTS);
    }

    public static ITextComponent food(int amount) {
        return new StringTextComponent("    " + FOOD_FORMAT.format(amount));
    }

    private static DecimalFormatSymbols getSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        return symbols;
    }

    public static ITextComponent heavyAttack(IHeavyAttack item) {
        return chanceAndLevel(HEAVY_ATTACK, item.getHeavyAttackChance(), item.getHeavyAttackLevel()).setStyle(EFFECTS);
    }

    private static DecimalFormat initFormat(String pattern) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setDecimalFormatSymbols(SYMBOLS);
        return decimalFormat;
    }

    public static ITextComponent knockback(IKnockback item) {
        return new TranslationTextComponent(KNOCKBACK, MathHelper.getRomanNumber(item.getLevel())).setStyle(EFFECTS);
    }

    public static ITextComponent mass(double amount) {
        return new StringTextComponent("   ").appendSibling(new TranslationTextComponent(MASS,
                                                                                         MASS_FORMAT.format(amount)).setStyle(EvolutionStyles.MASS));
    }

    public static ITextComponent mining(double miningSpeed) {
        return new StringTextComponent("    ").appendSibling(new TranslationTextComponent(MINING, TWO_PLACES.format(miningSpeed)).setStyle(
                EvolutionStyles.MINING));
    }

    public static ITextComponent oxydation(double oxydation) {
        return new TranslationTextComponent("evolution.tooltip.metal.oxidation", PERCENT_ONE_PLACE.format(oxydation)).setStyle(LIGHT_GREY);
    }

    public static ITextComponent remaining(int number) {
        return new TranslationTextComponent("evolution.tooltip.advancements.remain", number);
    }

    public static ITextComponent speed(double amount) {
        return new StringTextComponent("    ").appendSibling(new TranslationTextComponent(SPEED,
                                                                                          TWO_PLACES.format(amount)).setStyle(EvolutionStyles.SPEED));
    }

    public static ITextComponent sweep(ISweepAttack item) {
        return new TranslationTextComponent(SWEEP, PERCENT_ONE_PLACE.format(item.getSweepRatio())).setStyle(EFFECTS);
    }

    public static ITextComponent torch(int timeRemaining) {
        return new StringTextComponent(" ").appendSibling(new TranslationTextComponent("evolution.tooltip.torch.time",
                                                                                       HOUR_FORMAT.format(timeRemaining)).setStyle(INFO));
    }

    private static ITextComponent transl(String text) {
        return new TranslationTextComponent(text);
    }

    private static ITextComponent translSp(String text) {
        return new StringTextComponent(" ").appendSibling(new TranslationTextComponent(text));
    }
}
