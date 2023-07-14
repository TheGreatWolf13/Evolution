package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.PopupScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.patches.PatchMinecraft;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinVideoSettingsScreen extends OptionsSubScreen {

    @Mutable @Shadow @Final private static Option[] OPTIONS;
    @Shadow @Final private static Component WARNING_MESSAGE;
    @Shadow @Final private static Component NEW_LINE;
    @Shadow @Final private static Component WARNING_TITLE;
    @Shadow @Final private static Component BUTTON_ACCEPT;
    @Shadow @Final private static Component BUTTON_CANCEL;

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

    @Shadow @Final private GpuWarnlistManager gpuWarnlistManager;

    public MixinVideoSettingsScreen(Screen pLastScreen,
                                    Options pOptions,
                                    Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        int scale = this.options.guiScale;
        assert this.minecraft != null;
        if (super.mouseClicked(mouseX, mouseY, button)) {
            if (this.options.guiScale != scale) {
                this.minecraft.resizeDisplay();
            }
            if (this.gpuWarnlistManager.isShowingWarning()) {
                OList<Component> list = new OArrayList<>(2);
                list.add(WARNING_MESSAGE);
                list.add(NEW_LINE);
                String renderer = this.gpuWarnlistManager.getRendererWarnings();
                if (renderer != null) {
                    list.add(NEW_LINE);
                    list.add(new TranslatableComponent("options.graphics.warning.renderer", renderer).withStyle(ChatFormatting.GRAY));
                }
                String vender = this.gpuWarnlistManager.getVendorWarnings();
                if (vender != null) {
                    list.add(NEW_LINE);
                    list.add(new TranslatableComponent("options.graphics.warning.vendor", vender).withStyle(ChatFormatting.GRAY));
                }
                String version = this.gpuWarnlistManager.getVersionWarnings();
                if (version != null) {
                    list.add(NEW_LINE);
                    list.add(new TranslatableComponent("options.graphics.warning.version", version).withStyle(ChatFormatting.GRAY));
                }
                this.minecraft.setScreen(
                        new PopupScreen(WARNING_TITLE, list, ImmutableList.of(new PopupScreen.ButtonOption(BUTTON_ACCEPT, b -> {
                            this.options.graphicsMode = GraphicsStatus.FABULOUS;
                            ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer().allChanged();
                            this.gpuWarnlistManager.dismissWarning();
                            this.minecraft.setScreen(this);
                        }), new PopupScreen.ButtonOption(BUTTON_CANCEL, b -> {
                            this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                            this.minecraft.setScreen(this);
                        }))));
            }
            return true;
        }
        return false;
    }
}
