package tgw.evolution.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchKeyMapping;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements PatchKeyMapping {

    @Shadow private int clickCount;

    @Override
    public boolean consumeAllClicks() {
        boolean consumeClick = this.consumeClick();
        this.clickCount = 0;
        return consumeClick;
    }

    @Shadow
    public abstract boolean consumeClick();
}
