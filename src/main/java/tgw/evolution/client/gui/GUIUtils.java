package tgw.evolution.client.gui;

import net.minecraft.client.gui.FontRenderer;

public final class GUIUtils {

    private GUIUtils() {
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y, int color) {
        int textX = (width - font.getStringWidth(text)) / 2;
        font.drawString(text, textX, y, color);
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y) {
        drawCenteredString(font, text, width, y, 0x40_4040);
    }
}
