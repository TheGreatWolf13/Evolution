package tgw.evolution.init;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.items.*;
import tgw.evolution.util.math.MathHelper;

import static tgw.evolution.init.EvolutionFormatter.*;
import static tgw.evolution.init.EvolutionStyles.*;
import static tgw.evolution.util.math.Metric.*;

public final class EvolutionTexts {

    //Action Bar
    public static final Component ACTION_HIT_STAKE = transl("evolution.actionbar.hitStake").setStyle(WHITE);
    public static final Component ACTION_HOOK = transl("evolution.actionbar.hook").setStyle(WHITE);
    public static final Component ACTION_INERTIA = transl("evolution.actionbar.inertia").setStyle(WHITE);
    public static final Component ACTION_TWO_HANDED = transl("evolution.actionbar.twoHanded").setStyle(WHITE);
    //Command
    public static final Component COMMAND_PAUSE_PAUSE_FAIL = transl("command.evolution.pause.pause.fail");
    public static final Component COMMAND_PAUSE_PAUSE_INFO = transl("command.evolution.pause.pause.info");
    public static final Component COMMAND_PAUSE_PAUSE_SUCCESS = transl("command.evolution.pause.pause.success");
    public static final Component COMMAND_PAUSE_RESUME_FAIL = transl("command.evolution.pause.resume.fail");
    public static final Component COMMAND_PAUSE_RESUME_INFO = transl("command.evolution.pause.resume.info");
    public static final Component COMMAND_PAUSE_RESUME_SUCCESS = transl("command.evolution.pause.resume.success");
    public static final Component COMMAND_SHADER_NO_SHADER = transl("command.evolution.shader.noShader");
    public static final Component COMMAND_SHADER_RESET = transl("command.evolution.shader.reset");
    public static final Component COMMAND_SHADER_TOGGLE_OFF = transl("command.evolution.shader.toggleOff");
    public static final Component COMMAND_SHADER_TOGGLE_ON = transl("command.evolution.shader.toggleOn");
    public static final Component COMMAND_TEMPERATURE_ITEM_FAIL = transl("command.evolution.temperature.item.fail");
    //Death Messages
    public static final Component DEATH_FISTS = transl("death.item.fists");
    //Easter Eggs
    public static final Component EASTER_CHERT = transl("evolution.easter.chert").setStyle(LORE);
    public static final Component EASTER_GABBRO = transl("evolution.easter.gabbro").setStyle(LORE);
    public static final Component EASTER_GNEISS = transl("evolution.easter.gneiss").setStyle(LORE);
    public static final Component EASTER_SLATE = transl("evolution.easter.slate").setStyle(LORE);
    //Empty
    public static final Component EMPTY = new TextComponent(" ");
    //Fluid
    public static final Component FLUID_FRESH_WATER = transl("evolution.fluid.fresh_water");
    public static final Component FLUID_SALT_WATER = transl("evolution.fluid.salt_water");
    //GUI
    //    General
    public static final MutableComponent GUI_GENERAL_BACK = transl("evolution.gui.general.back");
    public static final MutableComponent GUI_GENERAL_CANCEL = transl("evolution.gui.general.cancel");
    public static final MutableComponent GUI_GENERAL_DONE = transl("evolution.gui.general.done");
    public static final MutableComponent GUI_GENERAL_EDIT = transl("evolution.gui.general.edit");
    public static final MutableComponent GUI_GENERAL_OFF = transl("evolution.gui.general.off");
    public static final MutableComponent GUI_GENERAL_ON = transl("evolution.gui.general.on");
    public static final MutableComponent GUI_GENERAL_REMOVE = transl("evolution.gui.general.remove");
    public static final MutableComponent GUI_GENERAL_SEARCH = transl("evolution.gui.general.search");
    //    Config
    public static final Component GUI_CONFIG_RESTORE_DEFAULTS = transl("evolution.gui.config.restoreDefaults");
    public static final Component GUI_CONFIG_RESTORE_MESSAGE = transl("evolution.gui.config.restoreMessage");
    //    Menu
    public static final Component GUI_MENU_MOD_OPTIONS = transl("evolution.gui.menu.modOptions");
    public static final Component GUI_MENU_QUIT = transl("evolution.gui.menu.quit");
    public static final Component GUI_MENU_REPORT_BUGS = transl("evolution.gui.menu.reportBugs");
    public static final Component GUI_MENU_SEND_FEEDBACK = transl("evolution.gui.menu.sendFeedback");
    public static final Component GUI_MENU_TO_TITLE = transl("evolution.gui.menu.toTitle");
    //Tooltip
    public static final Component TOOLTIP_BLUNT = new TextComponent("   ").append(transl("evolution.tooltip.blunt").setStyle(LIGHT_GREY));
    public static final Component TOOLTIP_CLAY_MOLD = transl("evolution.tooltip.clayMold").setStyle(INFO);
    public static final Component TOOLTIP_CONSUMABLE = transl("evolution.tooltip.consumable").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_CONTAINER_EMPTY = transl("evolution.tooltip.containerEmpty").setStyle(INFO);
    public static final Component TOOLTIP_FIREWOOD_PILE = transl("evolution.tooltip.firewoodPile").setStyle(INFO);
    public static final Component TOOLTIP_LUNGE = transl("evolution.tooltip.lunge").setStyle(PROPERTY);
    public static final Component TOOLTIP_MAINHAND = transl("evolution.tooltip.mainhand").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_MAINHAND_OFFHAND = transl("evolution.tooltip.mainhand_offhand").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_PARRY = transl("evolution.tooltip.parry").setStyle(PROPERTY);
    public static final Component TOOLTIP_ROCK_KNAP = transl("evolution.tooltip.rockKnap").setStyle(INFO);
    public static final Component TOOLTIP_ROCK_TYPE_IGEXTRUSIVE = transl("evolution.tooltip.rockType.igneousExtrusive").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_IGINTRUSIVE = transl("evolution.tooltip.rockType.igneousIntrusive").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_METAMORPHIC = transl("evolution.tooltip.rockType.metamorphic").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_SEDIMENTARY = transl("evolution.tooltip.rockType.sedimentary").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROPE = transl("evolution.tooltip.rope").setStyle(INFO);
    public static final Component TOOLTIP_SHOW_PARTS = transl("evolution.tooltip.showParts").setStyle(INFO);
    public static final Component TOOLTIP_STICK_LIT = transl("evolution.tooltip.stickLit").setStyle(INFO);
    public static final Component TOOLTIP_THROWABLE = transl("evolution.tooltip.throwable").setStyle(PROPERTY);
    public static final Component TOOLTIP_TORCH_RELIT = transl("evolution.tooltip.torchRelit").setStyle(INFO);
    public static final Component TOOLTIP_TWO_HANDED = transl("evolution.tooltip.twoHanded").setStyle(PROPERTY);
    public static final Component TOOLTIP_UNBREAKABLE = transl("evolution.tooltip.unbreakable").setStyle(INFO);
    public static final Component TOOLTIP_VERY_EFFICIENT = transl("evolution.tooltip.veryEfficient").setStyle(INFO);

    private EvolutionTexts() {
    }

    public static Component capacity(IItemFluidContainer container) {
        return new TranslatableComponent("evolution.tooltip.containerCapacity", VOLUME.format(container.getMaxAmount() / 100.0f)).setStyle(INFO);
    }

    private static MutableComponent chanceAndLevel(String message, float chance, int level) {
        return new TranslatableComponent(message, PERCENT_ONE_PLACE.format(chance), MathHelper.getRomanNumber(level));
    }

    public static Component coldResistance(double amount) {
        return new TranslatableComponent("evolution.tooltip.coldResistance", TEMPERATURE_BODY_RELATIVE.format(amount)).setStyle(COLD);
    }

    public static Component configAllowedValues(String allowed) {
        return new TranslatableComponent("evolution.config.allowedValues", allowed).setStyle(LIGHT_GREY);
    }

    public static Component configDefault(String def) {
        return new TranslatableComponent("evolution.config.default", def).setStyle(LIGHT_GREY);
    }

    public static Component configRange(String range) {
        return new TranslatableComponent("evolution.config.range", range).setStyle(LIGHT_GREY);
    }

    public static Component container(IItemFluidContainer container, ItemStack stack) {
        return new TranslatableComponent("evolution.tooltip.containerAmount",
                                         VOLUME.format(container.getAmount(stack) / 100.0f),
                                         container.getFluid() instanceof FluidGeneric ?
                                         ((FluidGeneric) container.getFluid()).getTextComp() :
                                         "null").setStyle(INFO);
    }

    public static Component damage(String damage, double amount) {
        return new TranslatableComponent(damage, TWO_PLACES.format(amount)).setStyle(DAMAGE);
    }

    public static Component drink(int amount) {
        return new TextComponent(DRINK.format(amount));
    }

    public static Component durability(String durability) {
        return new TranslatableComponent("evolution.tooltip.durability", durability).setStyle(DURABILITY);
    }

    public static Component fireAspect(IFireAspect item) {
        return chanceAndLevel("evolution.tooltip.fireAspect", item.getChance(), item.getLevel()).setStyle(EFFECTS);
    }

    public static Component food(int amount) {
        return new TextComponent(FOOD.format(amount));
    }

    public static Component heatResistance(double amount) {
        return new TranslatableComponent("evolution.tooltip.heatResistance", TEMPERATURE_BODY_RELATIVE.format(amount)).setStyle(HEAT);
    }

    public static Component heavyAttack(IHeavyAttack item) {
        return chanceAndLevel("evolution.tooltip.heavyAttack", item.getHeavyAttackChance(), item.getHeavyAttackLevel()).setStyle(EFFECTS);
    }

    public static Component knockback(IKnockback item) {
        return new TranslatableComponent("evolution.tooltip.knockback", MathHelper.getRomanNumber(item.getLevel())).setStyle(EFFECTS);
    }

    public static Component mass(double amount) {
        return new TranslatableComponent("evolution.tooltip.mass", EvolutionFormatter.MASS.format(amount)).setStyle(EvolutionStyles.MASS);
    }

    public static Component material(ItemMaterial material) {
        return new TextComponent("   ").append(new TranslatableComponent("evolution.tooltip.material", material.getText())).setStyle(REACH);
    }

    public static Component mining(double miningSpeed) {
        return new TranslatableComponent("evolution.tooltip.mining", TWO_PLACES.format(miningSpeed)).setStyle(MINING);
    }

    public static Component oxydation(double oxydation) {
        return new TranslatableComponent("evolution.tooltip.metalOxidation", PERCENT_ONE_PLACE.format(oxydation)).setStyle(LIGHT_GREY);
    }

    public static Component reach(double amount) {
        return new TranslatableComponent("evolution.tooltip.distance", TWO_PLACES.format(amount)).setStyle(REACH);
    }

    public static MutableComponent remaining(int number) {
        return new TranslatableComponent("evolution.tooltip.advancementsRemain", number);
    }

    public static Component sharp(int sharpAmount, int hardness) {
        if (sharpAmount == 0) {
            return TOOLTIP_BLUNT;
        }
        if (sharpAmount > hardness) {
            return new TextComponent("   ").append(new TranslatableComponent("evolution.tooltip.verySharp",
                                                                             sharpAmount - hardness,
                                                                             Mth.ceil(0.5 * hardness))).setStyle(EFFECTS);
        }
        return new TextComponent("   ").append(new TranslatableComponent("evolution.tooltip.sharp", sharpAmount, hardness)).setStyle(EFFECTS);
    }

    public static Component speed(double amount) {
        return new TranslatableComponent("evolution.tooltip.speed", TWO_PLACES.format(amount)).setStyle(SPEED);
    }

    public static Component sweep(ISweepAttack item) {
        return new TranslatableComponent("evolution.tooltip.sweep", PERCENT_ONE_PLACE.format(item.getSweepRatio())).setStyle(EFFECTS);
    }

    public static Component torch(int timeRemaining) {
        return new TranslatableComponent("evolution.tooltip.torchTime", HOUR_FORMAT.format(timeRemaining)).setStyle(INFO);
    }

    public static MutableComponent transl(String text) {
        return new TranslatableComponent(text);
    }
}
