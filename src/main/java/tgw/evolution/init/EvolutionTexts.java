package tgw.evolution.init;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.items.*;
import tgw.evolution.util.MathHelper;

import static tgw.evolution.init.EvolutionStyles.*;
import static tgw.evolution.util.Metric.*;

public final class EvolutionTexts {

    //Action Bar
    public static final ITextComponent ACTION_HIT_STAKE = transl("evolution.actionbar.hitStake").setStyle(WHITE);
    public static final ITextComponent ACTION_HOOK = transl("evolution.actionbar.hook").setStyle(WHITE);
    public static final ITextComponent ACTION_INERTIA = transl("evolution.actionbar.inertia").setStyle(WHITE);
    public static final ITextComponent ACTION_TWO_HANDED = transl("evolution.actionbar.twoHanded").setStyle(WHITE);
    //Command
    public static final ITextComponent COMMAND_PAUSE_PAUSE_FAIL = transl("command.evolution.pause.pause.fail");
    public static final ITextComponent COMMAND_PAUSE_PAUSE_INFO = transl("command.evolution.pause.pause.info");
    public static final ITextComponent COMMAND_PAUSE_PAUSE_SUCCESS = transl("command.evolution.pause.pause.success");
    public static final ITextComponent COMMAND_PAUSE_RESUME_FAIL = transl("command.evolution.pause.resume.fail");
    public static final ITextComponent COMMAND_PAUSE_RESUME_INFO = transl("command.evolution.pause.resume.info");
    public static final ITextComponent COMMAND_PAUSE_RESUME_SUCCESS = transl("command.evolution.pause.resume.success");
    //Config
    public static final IFormattableTextComponent CONFIG_CLIENT_CONFIG = transl("evolution.config.clientConfig");
    public static final IFormattableTextComponent CONFIG_COMMON_CONFIG = transl("evolution.config.commonConfig");
    //Death Messages
    public static final ITextComponent DEATH_FISTS = transl("death.item.fists");
    //Easter Eggs
    public static final ITextComponent EASTER_CHERT = transl("evolution.easter.chert").setStyle(LORE);
    public static final ITextComponent EASTER_GABBRO = transl("evolution.easter.gabbro").setStyle(LORE);
    public static final ITextComponent EASTER_GNEISS = transl("evolution.easter.gneiss").setStyle(LORE);
    public static final ITextComponent EASTER_SLATE = transl("evolution.easter.slate").setStyle(LORE);
    //Empty
    public static final ITextComponent EMPTY = new StringTextComponent(" ");
    //Fluid
    public static final ITextComponent FLUID_FRESH_WATER = transl("evolution.fluid.fresh_water");
    public static final ITextComponent FLUID_SALT_WATER = transl("evolution.fluid.salt_water");
    //GUI
    //    General
    public static final IFormattableTextComponent GUI_GENERAL_BACK = transl("evolution.gui.general.back");
    public static final IFormattableTextComponent GUI_GENERAL_CANCEL = transl("evolution.gui.general.cancel");
    public static final IFormattableTextComponent GUI_GENERAL_DONE = transl("evolution.gui.general.done");
    public static final IFormattableTextComponent GUI_GENERAL_EDIT = transl("evolution.gui.general.edit");
    public static final IFormattableTextComponent GUI_GENERAL_OFF = transl("evolution.gui.general.off");
    public static final IFormattableTextComponent GUI_GENERAL_ON = transl("evolution.gui.general.on");
    public static final IFormattableTextComponent GUI_GENERAL_REMOVE = transl("evolution.gui.general.remove");
    public static final IFormattableTextComponent GUI_GENERAL_SEARCH = transl("evolution.gui.general.search");
    //    Config
    public static final ITextComponent GUI_CONFIG_ADD_VALUE = transl("evolution.gui.config.addValue");
    public static final ITextComponent GUI_CONFIG_EDIT_VALUE = transl("evolution.gui.config.editValue");
    public static final ITextComponent GUI_CONFIG_RESET = transl("evolution.gui.config.reset");
    public static final ITextComponent GUI_CONFIG_RESTORE_DEFAULTS = transl("evolution.gui.config.restoreDefaults");
    //    Controls
    public static final ITextComponent GUI_CONTROLS_CATEGORY = transl("evolution.gui.controls.category");
    public static final ITextComponent GUI_CONTROLS_CONFIRM_RESET = transl("evolution.gui.controls.confirmReset");
    public static final ITextComponent GUI_CONTROLS_KEY = transl("evolution.gui.controls.key");
    public static final ITextComponent GUI_CONTROLS_MOUSE_SETTINGS = transl("evolution.gui.controls.mouseSettings");
    public static final ITextComponent GUI_CONTROLS_RESET = transl("evolution.gui.controls.reset");
    public static final ITextComponent GUI_CONTROLS_RESET_ALL = transl("evolution.gui.controls.resetAll");
    public static final ITextComponent GUI_CONTROLS_SHOW_ALL = transl("evolution.gui.controls.showAll");
    public static final ITextComponent GUI_CONTROLS_SHOW_CONFLICTS = transl("evolution.gui.controls.showConflicts");
    public static final ITextComponent GUI_CONTROLS_SHOW_UNBOUND = transl("evolution.gui.controls.showUnbound");
    //    Corpse
    public static final ITextComponent GUI_CORPSE_TAB_DEATH = transl("evolution.gui.corpse.tabDeath");
    public static final ITextComponent GUI_CORPSE_TAB_INVENTORY = transl("evolution.gui.corpse.tabInventory");
    //    Crash
    public static final ITextComponent GUI_CRASH = transl("evolution.gui.crash");
    public static final ITextComponent GUI_CRASH_CONCLUSION = transl("evolution.gui.crash.conclusion");
    public static final ITextComponent GUI_CRASH_REPORT = transl("evolution.gui.crash.report");
    public static final ITextComponent GUI_CRASH_REPORT_SAVE_FAILED = transl("evolution.gui.crash.reportSaveFailed").setStyle(DARK_RED);
    public static final ITextComponent GUI_CRASH_SUMMARY = transl("evolution.gui.crash.summary");
    //    Knapping
    public static final ITextComponent GUI_KNAPPING = transl("evolution.gui.knapping");
    //    Menu
    public static final ITextComponent GUI_MENU_MOD_OPTIONS = transl("evolution.gui.menu.modOptions");
    public static final ITextComponent GUI_MENU_QUIT = transl("evolution.gui.menu.quit");
    public static final ITextComponent GUI_MENU_REPORT_BUGS = transl("evolution.gui.menu.reportBugs");
    public static final ITextComponent GUI_MENU_SEND_FEEDBACK = transl("evolution.gui.menu.sendFeedback");
    public static final ITextComponent GUI_MENU_TO_TITLE = transl("evolution.gui.menu.toTitle");
    //    Out of Memory
    public static final ITextComponent GUI_OUT_OF_MEMORY = transl("evolution.gui.outOfMemory");
    public static final ITextComponent GUI_OUT_OF_MEMORY_CAUSE = transl("evolution.gui.outOfMemory.cause");
    public static final ITextComponent GUI_OUT_OF_MEMORY_INFO = transl("evolution.gui.outOfMemory.info");
    public static final ITextComponent GUI_OUT_OF_MEMORY_QUIT = transl("evolution.gui.outOfMemory.quit");
    public static final ITextComponent GUI_OUT_OF_MEMORY_RESTART = transl("evolution.gui.outOfMemory.restart");
    public static final ITextComponent GUI_OUT_OF_MEMORY_SUMMARY = transl("evolution.gui.outOfMemory.summary");
    //    Puzzle
    public static final ITextComponent GUI_PUZZLE_ATTACHMENT_TYPE = transl("evolution.gui.puzzle.attachmentType");
    public static final ITextComponent GUI_PUZZLE_CHECKBB = transl("evolution.gui.puzzle.checkBB");
    public static final ITextComponent GUI_PUZZLE_FINAL_STATE = transl("evolution.gui.puzzle.finalState");
    public static final ITextComponent GUI_PUZZLE_TARGET_POOL = transl("evolution.gui.puzzle.targetPool");
    //    Schematic
    public static final ITextComponent GUI_SCHEMATIC_DETECT_SIZE = transl("evolution.gui.schematic.detectSize");
    public static final ITextComponent GUI_SCHEMATIC_ENTITIES = transl("evolution.gui.schematic.entities");
    public static final ITextComponent GUI_SCHEMATIC_INTEGRITY = transl("evolution.gui.schematic.integrity");
    public static final ITextComponent GUI_SCHEMATIC_LOAD = transl("evolution.gui.schematic.load");
    public static final ITextComponent GUI_SCHEMATIC_MIRROR = transl("evolution.gui.schematic.mirror");
    public static final ITextComponent GUI_SCHEMATIC_MODE = transl("evolution.gui.schematic.mode");
    public static final ITextComponent GUI_SCHEMATIC_NAME = transl("evolution.gui.schematic.name");
    public static final ITextComponent GUI_SCHEMATIC_POS = transl("evolution.gui.schematic.pos");
    public static final ITextComponent GUI_SCHEMATIC_POS_X = transl("evolution.gui.schematic.posX");
    public static final ITextComponent GUI_SCHEMATIC_POS_Y = transl("evolution.gui.schematic.posY");
    public static final ITextComponent GUI_SCHEMATIC_POS_Z = transl("evolution.gui.schematic.posZ");
    public static final ITextComponent GUI_SCHEMATIC_SAVE = transl("evolution.gui.schematic.save");
    public static final ITextComponent GUI_SCHEMATIC_SEED = transl("evolution.gui.schematic.seed");
    public static final ITextComponent GUI_SCHEMATIC_SHOW_AIR = transl("evolution.gui.schematic.showAir");
    public static final ITextComponent GUI_SCHEMATIC_SHOW_BB = transl("evolution.gui.schematic.showBB");
    public static final ITextComponent GUI_SCHEMATIC_SIZE = transl("evolution.gui.schematic.size");
    public static final ITextComponent GUI_SCHEMATIC_SIZE_X = transl("evolution.gui.schematic.sizeX");
    public static final ITextComponent GUI_SCHEMATIC_SIZE_Y = transl("evolution.gui.schematic.sizeY");
    public static final ITextComponent GUI_SCHEMATIC_SIZE_Z = transl("evolution.gui.schematic.sizeZ");
    //    Stats
    public static final ITextComponent GUI_STATS_DAMAGE_BUTTON = transl("evolution.gui.stats.damageButton");
    public static final ITextComponent GUI_STATS_DAMAGE_DEALT_ACTUAL = transl("evolution.gui.stats.damageDealtActual");
    public static final ITextComponent GUI_STATS_DAMAGE_DEALT_RAW = transl("evolution.gui.stats.damageDealtRaw");
    public static final ITextComponent GUI_STATS_DAMAGE_TAKEN_ACTUAL = transl("evolution.gui.stats.damageTakenActual");
    public static final ITextComponent GUI_STATS_DAMAGE_TAKEN_BLOCKED = transl("evolution.gui.stats.damageTakenBlocked");
    public static final ITextComponent GUI_STATS_DAMAGE_TAKEN_RAW = transl("evolution.gui.stats.damageTakenRaw");
    public static final ITextComponent GUI_STATS_DEATH_BUTTON = transl("evolution.gui.stats.deathButton");
    public static final ITextComponent GUI_STATS_DISTANCE_BUTTON = transl("evolution.gui.stats.distanceButton");
    public static final ITextComponent GUI_STATS_GENERAL_BUTTON = transl("evolution.gui.stats.generalButton");
    public static final ITextComponent GUI_STATS_ITEMS_BUTTON = transl("evolution.gui.stats.itemsButton");
    public static final ITextComponent GUI_STATS_MOB_BUTTON = transl("evolution.gui.stats.mobsButton");
    public static final ITextComponent GUI_STATS_TIME_BUTTON = transl("evolution.gui.stats.timeButton");
    //Tooltip
    public static final ITextComponent TOOLTIP_CLAY_MOLD = transl("evolution.tooltip.clayMold").setStyle(INFO);
    public static final ITextComponent TOOLTIP_CONSUMABLE = transl("evolution.tooltip.consumable").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_CONTAINER_EMPTY = transl("evolution.tooltip.containerEmpty").setStyle(INFO);
    public static final ITextComponent TOOLTIP_FIREWOOD_PILE = transl("evolution.tooltip.firewoodPile").setStyle(INFO);
    public static final ITextComponent TOOLTIP_LUNGE = transl("evolution.tooltip.lunge").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_OFFHAND = transl("evolution.tooltip.offhand").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_PARRY = transl("evolution.tooltip.parry").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_ROCK_KNAP = transl("evolution.tooltip.rockKnap").setStyle(INFO);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_IGEXTRUSIVE = transl("evolution.tooltip.rockType.igneousExtrusive").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_IGINTRUSIVE = transl("evolution.tooltip.rockType.igneousIntrusive").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_METAMORPHIC = transl("evolution.tooltip.rockType.metamorphic").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROCK_TYPE_SEDIMENTARY = transl("evolution.tooltip.rockType.sedimentary").setStyle(LIGHT_GREY);
    public static final ITextComponent TOOLTIP_ROPE = transl("evolution.tooltip.rope").setStyle(INFO);
    public static final ITextComponent TOOLTIP_STICK_LIT = transl("evolution.tooltip.stickLit").setStyle(INFO);
    public static final ITextComponent TOOLTIP_THROWABLE = transl("evolution.tooltip.throwable").setStyle(PROPERTY);
    public static final ITextComponent TOOLTIP_TORCH_RELIT = translSp("evolution.tooltip.torchRelit").setStyle(INFO);
    public static final ITextComponent TOOLTIP_TWO_HANDED = transl("evolution.tooltip.twoHanded").setStyle(PROPERTY);
    //Translation Keys
    //    Config
    private static final String CONFIG_ALLOWED_VALUES = "evolution.config.allowedValues";
    private static final String CONFIG_DEFAULT = "evolution.config.default";
    private static final String CONFIG_RANGE = "evolution.config.range";
    //    Tooltip
    private static final String TOOLTIP_ADVANCEMENTS_REMAIN = "evolution.tooltip.advancementsRemain";
    private static final String TOOLTIP_CONTAINER_AMOUNT = "evolution.tooltip.containerAmount";
    private static final String TOOLTIP_CONTAINER_CAPACITY = "evolution.tooltip.containerCapacity";
    private static final String TOOLTIP_DISTANCE = "evolution.tooltip.distance";
    private static final String TOOLTIP_DURABILITY = "evolution.tooltip.durability";
    private static final String TOOLTIP_FIRE_ASPECT = "evolution.tooltip.fireAspect";
    private static final String TOOLTIP_HEAVY_ATTACK = "evolution.tooltip.heavyAttack";
    private static final String TOOLTIP_KNOCKBACK = "evolution.tooltip.knockback";
    private static final String TOOLTIP_MASS = "evolution.tooltip.mass";
    private static final String TOOLTIP_METAL_OXIDATION = "evolution.tooltip.metalOxidation";
    private static final String TOOLTIP_MINING = "evolution.tooltip.mining";
    private static final String TOOLTIP_SPEED = "evolution.tooltip.speed";
    private static final String TOOLTIP_SWEEP = "evolution.tooltip.sweep";
    private static final String TOOLTIP_TORCH_TIME = "evolution.tooltip.torchTime";

    private EvolutionTexts() {
    }

    public static ITextComponent capacity(IItemFluidContainer container) {
        return new TranslationTextComponent(TOOLTIP_CONTAINER_CAPACITY, LITER_FORMAT.format(container.getMaxAmount() / 100.0f)).setStyle(INFO);
    }

    private static IFormattableTextComponent chanceAndLevel(String message, float chance, int level) {
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
        return new TranslationTextComponent(TOOLTIP_CONTAINER_AMOUNT,
                                            LITER_FORMAT.format(container.getAmount(stack) / 100.0f),
                                            container.getFluid() instanceof FluidGeneric ?
                                            ((FluidGeneric) container.getFluid()).getTextComp() :
                                            "null").setStyle(INFO);
    }

    public static ITextComponent damage(String damage, double amount) {
        return new StringTextComponent("    ").append(new TranslationTextComponent(damage, TWO_PLACES.format(amount)).setStyle(DAMAGE));
    }

    public static ITextComponent distance(double amount) {
        return new StringTextComponent("    ").append(new TranslationTextComponent(TOOLTIP_DISTANCE, TWO_PLACES.format(amount)).setStyle(REACH));
    }

    public static ITextComponent drink(int amount) {
        return new StringTextComponent("    " + DRINK_FORMAT.format(amount));
    }

    public static ITextComponent durability(ItemStack stack) {
        return new StringTextComponent("   ").append(new TranslationTextComponent(TOOLTIP_DURABILITY,
                                                                                  ((IDurability) stack.getItem()).displayDurability(stack)).setStyle(
                DURABILITY));
    }

    public static ITextComponent fireAspect(IFireAspect item) {
        return chanceAndLevel(TOOLTIP_FIRE_ASPECT, item.getChance(), item.getLevel()).setStyle(EFFECTS);
    }

    public static ITextComponent food(int amount) {
        return new StringTextComponent("    " + FOOD_FORMAT.format(amount));
    }

    public static ITextComponent heavyAttack(IHeavyAttack item) {
        return chanceAndLevel(TOOLTIP_HEAVY_ATTACK, item.getHeavyAttackChance(), item.getHeavyAttackLevel()).setStyle(EFFECTS);
    }

    public static ITextComponent knockback(IKnockback item) {
        return new TranslationTextComponent(TOOLTIP_KNOCKBACK, MathHelper.getRomanNumber(item.getLevel())).setStyle(EFFECTS);
    }

    public static ITextComponent mass(double amount) {
        return new StringTextComponent("   ").append(new TranslationTextComponent(TOOLTIP_MASS, MASS_FORMAT.format(amount)).setStyle(MASS));
    }

    public static ITextComponent mining(double miningSpeed) {
        return new StringTextComponent("    ").append(new TranslationTextComponent(TOOLTIP_MINING, TWO_PLACES.format(miningSpeed)).setStyle(MINING));
    }

    public static ITextComponent oxydation(double oxydation) {
        return new TranslationTextComponent(TOOLTIP_METAL_OXIDATION, PERCENT_ONE_PLACE.format(oxydation)).setStyle(LIGHT_GREY);
    }

    public static IFormattableTextComponent remaining(int number) {
        return new TranslationTextComponent(TOOLTIP_ADVANCEMENTS_REMAIN, number);
    }

    public static ITextComponent speed(double amount) {
        return new StringTextComponent("    ").append(new TranslationTextComponent(TOOLTIP_SPEED, TWO_PLACES.format(amount)).setStyle(SPEED));
    }

    public static ITextComponent sweep(ISweepAttack item) {
        return new TranslationTextComponent(TOOLTIP_SWEEP, PERCENT_ONE_PLACE.format(item.getSweepRatio())).setStyle(EFFECTS);
    }

    public static ITextComponent torch(int timeRemaining) {
        return new StringTextComponent(" ").append(new TranslationTextComponent(TOOLTIP_TORCH_TIME,
                                                                                HOUR_FORMAT.format(timeRemaining)).setStyle(INFO));
    }

    public static IFormattableTextComponent transl(String text) {
        return new TranslationTextComponent(text);
    }

    private static IFormattableTextComponent translSp(String text) {
        return new StringTextComponent(" ").append(new TranslationTextComponent(text));
    }
}
