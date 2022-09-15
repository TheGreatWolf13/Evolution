package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.ClientProxy;
import tgw.evolution.patches.IOptionsPatch;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin implements IOptionsPatch {

    private boolean toggleCrawl = true;

    @Override
    public boolean getToggleCrawl() {
        return this.toggleCrawl;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Minecraft mc, File file, CallbackInfo ci) {
        ToggleKeyMapping keyCrawl = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_X, "key.categories.movement", () -> this.toggleCrawl);
        keyCrawl.setKeyConflictContext(KeyConflictContext.IN_GAME);
        ClientProxy.KEY_CRAWL = keyCrawl;
    }

    @Inject(method = "processOptions", at = @At("TAIL"))
    private void onProcessOptions(Options.FieldAccess acc, CallbackInfo ci) {
        this.toggleCrawl = acc.process("toggleCrawl", this.toggleCrawl);
    }

    @Override
    public void setToggleCrawl(boolean toggle) {
        this.toggleCrawl = toggle;
    }
}
