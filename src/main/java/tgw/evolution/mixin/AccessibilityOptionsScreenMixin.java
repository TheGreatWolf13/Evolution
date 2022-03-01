package tgw.evolution.mixin;

import net.minecraft.client.Option;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin {

    @Mutable
    @Shadow
    @Final
    private static Option[] OPTIONS;

    static {
        OPTIONS = new Option[]{Option.NARRATOR,
                               Option.SHOW_SUBTITLES,
                               Option.TEXT_BACKGROUND_OPACITY,
                               Option.TEXT_BACKGROUND,
                               Option.CHAT_OPACITY,
                               Option.CHAT_LINE_SPACING,
                               Option.CHAT_DELAY,
                               Option.TOGGLE_CROUCH,
                               Option.TOGGLE_SPRINT,
                               Option.SCREEN_EFFECTS_SCALE,
                               Option.FOV_EFFECTS_SCALE,
                               Option.DARK_MOJANG_STUDIOS_BACKGROUND_COLOR,
                               Option.HIDE_LIGHTNING_FLASH};
    }
}
