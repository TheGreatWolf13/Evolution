package tgw.evolution.init;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import tgw.evolution.items.IFireAspect;
import tgw.evolution.items.IHeavyAttack;
import tgw.evolution.items.IItemFluidContainer;
import tgw.evolution.items.IKnockback;
import tgw.evolution.util.math.MathHelper;

import static net.minecraft.world.effect.MobEffectCategory.*;
import static tgw.evolution.init.EvolutionFormatter.*;
import static tgw.evolution.init.EvolutionStyles.*;
import static tgw.evolution.util.math.Metric.*;

public final class EvolutionTexts {

    //Action Bar
    public static final Component ACTION_ATTACK_POSE = transl("evolution.actionbar.attackPose").setStyle(WHITE);
    public static final Component ACTION_HIT_STAKE = transl("evolution.actionbar.hitStake").setStyle(WHITE);
    public static final Component ACTION_HOOK = transl("evolution.actionbar.hook").setStyle(WHITE);
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
    public static final Component EASTER_CHERT = transl("evolution.easter.chert").setStyle(LIGHT_PURPLE_ITALIC);
    public static final Component EASTER_GABBRO = transl("evolution.easter.gabbro").setStyle(LIGHT_PURPLE_ITALIC);
    public static final Component EASTER_GNEISS = transl("evolution.easter.gneiss").setStyle(LIGHT_PURPLE_ITALIC);
    public static final Component EASTER_SLATE = transl("evolution.easter.slate").setStyle(LIGHT_PURPLE_ITALIC);
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
    public static final Component GUI_MENU_TO_TITLE = transl("evolution.gui.menu.toTitle");
    //Tooltip
    public static final Component TOOLTIP_BLUNT = new TextComponent("   ").append(transl("evolution.tooltip.blunt").setStyle(LIGHT_GREY));
    public static final Component TOOLTIP_CLAY_MOLD = transl("evolution.tooltip.clayMold").setStyle(BLUE);
    public static final Component TOOLTIP_CONSUMABLE = transl("evolution.tooltip.consumable").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_CONTAINER_EMPTY = transl("evolution.tooltip.containerEmpty").setStyle(BLUE);
    public static final Component TOOLTIP_DAMAGE_PROPORTIONAL = transl("evolution.tooltip.damageProportional").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_EFFECT_CAUSES = transl("evolution.tooltip.effect.causes").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_EFFECT_DISABLE_REGEN = arrow(HARMFUL).append(
            transl("evolution.tooltip.effect.disableRegen").withStyle(WHITE));
    public static final Component TOOLTIP_EFFECT_DISABLE_SPRINT = arrow(HARMFUL).append(
            transl("evolution.tooltip.effect.disableSprint").withStyle(WHITE));
    public static final Component TOOLTIP_EFFECT_MAY_CAUSE = transl("evolution.tooltip.effect.mayCause").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_FIREWOOD_PILE = transl("evolution.tooltip.firewoodPile").setStyle(BLUE);
    public static final Component TOOLTIP_FOLLOW_UP_SINGLE = transl("evolution.tooltip.followUp.single").setStyle(DARK_YELLOW);
    public static final Component TOOLTIP_MAINHAND = transl("evolution.tooltip.mainhand").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_PARRY = transl("evolution.tooltip.parry").setStyle(GOLD);
    public static final Component TOOLTIP_ROCK_KNAP = transl("evolution.tooltip.rockKnap").setStyle(BLUE);
    public static final Component TOOLTIP_ROCK_TYPE_IGEXTRUSIVE = transl("evolution.tooltip.rockType.igneousExtrusive").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_IGINTRUSIVE = transl("evolution.tooltip.rockType.igneousIntrusive").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_METAMORPHIC = transl("evolution.tooltip.rockType.metamorphic").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROCK_TYPE_SEDIMENTARY = transl("evolution.tooltip.rockType.sedimentary").setStyle(LIGHT_GREY);
    public static final Component TOOLTIP_ROPE = transl("evolution.tooltip.rope").setStyle(BLUE);
    public static final Component TOOLTIP_SHOW_MELEE_STATS = transl("evolution.tooltip.showMeleeStats").setStyle(BLUE);
    public static final Component TOOLTIP_SHOW_PARTS = transl("evolution.tooltip.showParts").setStyle(BLUE);
    public static final Component TOOLTIP_STICK_LIT = transl("evolution.tooltip.stickLit").setStyle(BLUE);
    public static final Component TOOLTIP_THROWABLE = transl("evolution.tooltip.throwable").setStyle(GOLD);
    public static final Component TOOLTIP_TORCH_RELIT = transl("evolution.tooltip.torchRelit").setStyle(BLUE);
    public static final Component TOOLTIP_TWO_HANDED = transl("evolution.tooltip.twoHanded").setStyle(GOLD);
    public static final Component TOOLTIP_UNBREAKABLE = transl("evolution.tooltip.unbreakable").setStyle(BLUE);
    public static final Component TOOLTIP_VERY_EFFICIENT = transl("evolution.tooltip.veryEfficient").setStyle(BLUE);

    private EvolutionTexts() {
    }

    private static MutableComponent arrow(MobEffectCategory category) {
        return new TextComponent(" \u25ba ").withStyle(category == BENEFICIAL ? DARK_GREEN : category == HARMFUL ? RED : DARK_YELLOW);
    }

    public static FormattedText basicAttack() {
        return new TranslatableComponent("evolution.tooltip.basicAttack",
                                         Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage());
    }

    public static Component capacity(IItemFluidContainer container) {
        return new TranslatableComponent("evolution.tooltip.containerCapacity", VOLUME.format(container.getMaxAmount() / 100.0f)).setStyle(BLUE);
    }

    public static FormattedText chargeAttack() {
        return new TranslatableComponent("evolution.tooltip.chargeAttack",
                                         Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage());
    }

    public static Component coldResistance(double amount) {
        return new TranslatableComponent("evolution.tooltip.coldResistance", TEMPERATURE_BODY_RELATIVE.format(amount)).setStyle(COLD_BLUE);
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

//    public static Component container(IItemFluidContainer container, ItemStack stack) {
//        return new TranslatableComponent("evolution.tooltip.containerAmount",
//                                         VOLUME.format(container.getAmount(stack) / 100.0f),
//                                         container.getFluid() instanceof FluidGeneric ?
//                                         ((FluidGeneric) container.getFluid()).getTextComp() :
//                                         "null").setStyle(BLUE);
//    }

    public static Component cooldown(double amount) {
        return new TranslatableComponent("evolution.tooltip.cooldown", TWO_PLACES.format(amount / 20)).setStyle(GREEN);
    }

    public static Component damage(String damage, double amount) {
        return new TranslatableComponent(damage, TWO_PLACES.format(amount)).setStyle(DARK_RED);
    }

    public static Component dmgMultiplier(double mult) {
        return new TranslatableComponent("evolution.tooltip.damageMultiplier", TWO_PLACES.format(mult)).setStyle(AQUA);
    }

    public static Component drink(int amount) {
        return new TextComponent(DRINK.format(amount));
    }

    public static Component durability(String durability) {
        return new TranslatableComponent("evolution.tooltip.durability", durability).setStyle(LIGHT_GREY);
    }

    public static Component effect(MobEffectInstance instance) {
        MutableComponent comp = arrow(instance.getEffect().getCategory()).append(
                new TranslatableComponent(instance.getEffect().getDescriptionId()).withStyle(WHITE));
        if (instance.getAmplifier() > 0) {
            comp.append(new TextComponent(" " + MathHelper.getRomanNumber(instance.getAmplifier() + 1)).withStyle(WHITE));
        }
        return comp;
    }

    public static Component effectAbsorption(float absorption) {
        return arrow(BENEFICIAL).append(
                new TranslatableComponent("evolution.tooltip.effect.absorption", HP_FORMAT.format(absorption)).withStyle(WHITE));
    }

    public static Component effectAttSpeed(float speed) {
        return arrow(speed > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.attackSpeed", PERCENT_ONE_PLACE_BONUS.format(speed)).withStyle(WHITE));
    }

    public static Component effectDmg(float dmg, int tickInterval, boolean isAddition) {
        return new TranslatableComponent(isAddition ? "evolution.tooltip.effect.damage.addition" : "evolution.tooltip.effect.damage",
                                         HP_FORMAT.format(dmg), time(tickInterval / 20.0, 1));
    }

    public static Component effectHealth(float health) {
        return arrow(health > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.health", HP_BONUS_FORMAT.format(health)).withStyle(WHITE));
    }

    public static Component effectHunger(float hunger) {
        return arrow(HARMFUL).append(new TranslatableComponent("evolution.tooltip.effect.hunger", PERCENT_ONE_PLACE.format(hunger)).withStyle(WHITE));
    }

    public static Component effectInstaHP(float instaHP) {
        if (instaHP > 0) {
            return arrow(BENEFICIAL).append(
                    new TranslatableComponent("evolution.tooltip.effect.instantHealth", HP_FORMAT.format(instaHP)).withStyle(WHITE));
        }
        return arrow(HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.instantDamage", HP_FORMAT.format(-instaHP)).withStyle(WHITE));
    }

    public static Component effectJump(float jump) {
        return arrow(jump > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.jump", PERCENT_ONE_PLACE_BONUS.format(jump)).withStyle(WHITE));
    }

    public static Component effectLuck(int luck) {
        return arrow(luck > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.luck", BONUS.format(luck)).withStyle(WHITE));
    }

    public static Component effectMeleeDmg(float dmg) {
        return arrow(dmg > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.meleeDamage", HP_BONUS_FORMAT.format(dmg)).withStyle(WHITE));
    }

    public static Component effectMining(float mining) {
        return arrow(mining > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.miningSpeed", PERCENT_ONE_PLACE_BONUS.format(mining)).withStyle(WHITE));
    }

    public static Component effectRegen(float regen, int tickInterval, boolean isAddition) {
        return new TranslatableComponent(isAddition ? "evolution.tooltip.effect.regen.addition" : "evolution.tooltip.effect.regen",
                                         HP_FORMAT.format(regen), time(tickInterval / 20.0, 1));
    }

    public static Component effectResist(float resist) {
        return arrow(BENEFICIAL).append(
                new TranslatableComponent("evolution.tooltip.effect.resistance", PERCENT_ONE_PLACE.format(resist)).withStyle(WHITE));
    }

    public static Component effectSpeed(float speed) {
        return arrow(speed > 0 ? BENEFICIAL : HARMFUL).append(
                new TranslatableComponent("evolution.tooltip.effect.moveSpeed", PERCENT_ONE_PLACE_BONUS.format(speed)).withStyle(WHITE));
    }

    public static Component effectTemperature(double temp) {
        return arrow(NEUTRAL).append(
                new TranslatableComponent("evolution.tooltip.effect.temperature", TEMPERATURE_BODY_RELATIVE.format(temp)).withStyle(WHITE));
    }

    public static Component effectThirst(float thirst) {
        return arrow(HARMFUL).append(new TranslatableComponent("evolution.tooltip.effect.thirst", PERCENT_ONE_PLACE.format(thirst)).withStyle(WHITE));
    }

    public static Component fireAspect(IFireAspect item) {
        return new TranslatableComponent("evolution.tooltip.fireAspect", MathHelper.getRomanNumber(item.fireLevel())).setStyle(DARK_AQUA);
    }

    public static FormattedText fireAspectDesc(IFireAspect fireAspect) {
        return arrow(BENEFICIAL).append(
                new TranslatableComponent("evolution.tooltip.fireAspect.desc", PERCENT_ONE_PLACE.format(fireAspect.fireLevel() * 0.1)));
    }

    public static Component followUp(int followUps) {
        return followUps == 1 ? TOOLTIP_FOLLOW_UP_SINGLE : new TranslatableComponent("evolution.tooltip.followUp.plural", followUps).setStyle(
                DARK_YELLOW);
    }

    public static Component food(int amount) {
        return new TextComponent(FOOD.format(amount));
    }

    public static Component heatResistance(double amount) {
        return new TranslatableComponent("evolution.tooltip.heatResistance", TEMPERATURE_BODY_RELATIVE.format(amount)).setStyle(RED);
    }

    public static Component heavyAttack(IHeavyAttack item) {
        return new TranslatableComponent("evolution.tooltip.heavyAttack", MathHelper.getRomanNumber(item.heavyAttackLevel())).setStyle(DARK_AQUA);
    }

    public static FormattedText heavyAttackDesc1(IHeavyAttack heavyAttack) {
        return arrow(BENEFICIAL).append(new TranslatableComponent("evolution.tooltip.heavyAttack.desc1", 4 + heavyAttack.heavyAttackLevel()));
    }

    public static FormattedText heavyAttackDesc2(IHeavyAttack heavyAttack) {
        return arrow(BENEFICIAL).append(
                new TranslatableComponent("evolution.tooltip.heavyAttack.desc2", PERCENT_ONE_PLACE.format(0.1 * heavyAttack.heavyAttackLevel())));
    }

    public static Component knockback(IKnockback item) {
        return new TranslatableComponent("evolution.tooltip.knockback", MathHelper.getRomanNumber(item.knockbackLevel())).setStyle(DARK_AQUA);
    }

    public static Component mass(double amount) {
        return new TranslatableComponent("evolution.tooltip.mass", MASS.format(amount)).setStyle(DARK_GREEN);
    }

    public static Component material(EvolutionMaterials material) {
        return new TextComponent("   ").append(new TranslatableComponent("evolution.tooltip.material", material.getText())).setStyle(DARK_YELLOW);
    }

    public static Component mining(double miningSpeed) {
        return new TranslatableComponent("evolution.tooltip.mining", TWO_PLACES.format(miningSpeed)).setStyle(AQUA);
    }

    public static Component oxydation(double oxydation) {
        return new TranslatableComponent("evolution.tooltip.metalOxidation", PERCENT_ONE_PLACE.format(oxydation)).setStyle(LIGHT_GREY);
    }

    public static Component precision(float precision) {
        return new TranslatableComponent("evolution.tooltip.precision", PERCENT_ONE_PLACE.format(precision)).setStyle(DARK_YELLOW);
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
                                                                             Mth.ceil(0.5 * hardness))).setStyle(DARK_AQUA);
        }
        return new TextComponent("   ").append(new TranslatableComponent("evolution.tooltip.sharp", sharpAmount, hardness)).setStyle(DARK_AQUA);
    }

    public static FormattedText throwAttack() {
        return new TranslatableComponent("evolution.tooltip.throwAttack",
                                         Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage());
    }

    public static Component throwSpeed(double speed) {
        return new TranslatableComponent("evolution.tooltip.throwSpeed", SPEED.format(speed)).setStyle(GREEN);
    }

    public static Component torch(int timeRemaining) {
        return new TranslatableComponent("evolution.tooltip.torchTime", HOUR_FORMAT.format(timeRemaining)).setStyle(BLUE);
    }

    public static MutableComponent transl(String text) {
        return new TranslatableComponent(text);
    }
}
