package tgw.evolution.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class EvolutionStyles {

    public static final Style DAMAGE = new Style().setColor(TextFormatting.DARK_GREEN);
    public static final Style DPS = DAMAGE;
    public static final Style SPEED = new Style().setColor(TextFormatting.YELLOW);
    public static final Style COOLDOWN = SPEED;
    public static final Style MINING = new Style().setColor(TextFormatting.AQUA);
    public static final Style REACH = MINING;
    public static final Style WHITE = new Style().setColor(TextFormatting.WHITE);
    public static final Style DURABILITY = WHITE;
    public static final Style INFO = new Style().setColor(TextFormatting.BLUE);
    public static final Style PROPERTY = new Style().setColor(TextFormatting.GOLD);
    public static final Style LIGHT_GREY = new Style().setColor(TextFormatting.GRAY);
}
