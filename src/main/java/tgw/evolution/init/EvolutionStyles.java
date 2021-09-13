package tgw.evolution.init;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public final class EvolutionStyles {

    public static final Style DARK_RED = Style.EMPTY.applyFormat(TextFormatting.DARK_RED);
    public static final Style DAMAGE = DARK_RED;
    public static final Style GREEN = Style.EMPTY.applyFormat(TextFormatting.GREEN);
    public static final Style SPEED = GREEN;
    public static final Style MINING = Style.EMPTY.applyFormat(TextFormatting.AQUA);
    public static final Style REACH = Style.EMPTY.applyFormat(TextFormatting.YELLOW);
    public static final Style WHITE = Style.EMPTY.applyFormat(TextFormatting.WHITE);
    public static final Style DURABILITY = WHITE;
    public static final Style INFO = Style.EMPTY.applyFormat(TextFormatting.BLUE);
    public static final Style PROPERTY = Style.EMPTY.applyFormat(TextFormatting.GOLD);
    public static final Style LIGHT_GREY = Style.EMPTY.applyFormat(TextFormatting.GRAY);
    public static final Style MASS = Style.EMPTY.applyFormat(TextFormatting.DARK_GREEN);
    public static final Style LORE = Style.EMPTY.applyFormat(TextFormatting.LIGHT_PURPLE).withItalic(true);
    public static final Style EFFECTS = Style.EMPTY.applyFormat(TextFormatting.DARK_AQUA);

    private EvolutionStyles() {
    }
}
