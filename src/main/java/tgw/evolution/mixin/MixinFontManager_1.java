package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$1")
public abstract class MixinFontManager_1 implements IKeyedReloadListener {

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.FONTS;
    }
}
