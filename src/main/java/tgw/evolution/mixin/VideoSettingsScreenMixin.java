package tgw.evolution.mixin;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {

    @Mutable
    @Shadow
    @Final
    private static Option[] OPTIONS;

    static {
        OPTIONS = new Option[]{Option.GRAPHICS,
                               Option.RENDER_DISTANCE,
                               Option.PRIORITIZE_CHUNK_UPDATES,
                               Option.SIMULATION_DISTANCE,
                               Option.AMBIENT_OCCLUSION,
                               Option.FRAMERATE_LIMIT,
                               Option.ENABLE_VSYNC,
                               Option.GUI_SCALE,
                               Option.ATTACK_INDICATOR,
                               Option.GAMMA,
                               Option.RENDER_CLOUDS,
                               Option.USE_FULLSCREEN,
                               Option.PARTICLES,
                               Option.MIPMAP_LEVELS,
                               Option.ENTITY_SHADOWS,
                               Option.SCREEN_EFFECTS_SCALE,
                               Option.ENTITY_DISTANCE_SCALING,
                               Option.FOV_EFFECTS_SCALE,
                               Option.AUTOSAVE_INDICATOR};
    }

    public VideoSettingsScreenMixin(Screen pLastScreen,
                                    Options pOptions,
                                    Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }
}
