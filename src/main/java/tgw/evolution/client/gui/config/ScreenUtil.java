package tgw.evolution.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;

import java.util.function.Predicate;

public final class ScreenUtil {

    private ScreenUtil() {
    }

    public static Button.OnTooltip createButtonTooltip(Screen screen, FormattedText message, int maxWidth) {
        return createButtonTooltip(screen, message, maxWidth, button -> button.active && button.isHoveredOrFocused());
    }

    public static Button.OnTooltip createButtonTooltip(Screen screen, FormattedText message, int maxWidth, Predicate<Button> predicate) {
        return (button, poseStack, mouseX, mouseY) -> {
            if (predicate.test(button)) {
                screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(message, maxWidth), mouseX, mouseY);
            }
        };
    }

    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        int x = screenX * scale;
        int y = mc.getWindow().getHeight() - screenY * scale - boxHeight * scale;
        int width = Math.max(0, boxWidth * scale);
        int height = Math.max(0, boxHeight * scale);
        RenderSystem.enableScissor(x, y, width, height);
    }
}
