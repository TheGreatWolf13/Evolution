package tgw.evolution.mixin;

import net.minecraft.client.resources.language.LanguageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.PatchLanguageInfo;

import java.util.Locale;

@Mixin(LanguageInfo.class)
public abstract class MixinLanguageInfo implements PatchLanguageInfo {

    private Locale locale;

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(String code, String region, String name, boolean bidi, CallbackInfo ci) {
        int index = code.indexOf('_');
        if (index == -1) {// Vanilla has some languages without underscores
            this.locale = new Locale(code);
        }
        else {
            this.locale = new Locale(code.substring(0, index), code.substring(index + 1));
        }
    }
}
