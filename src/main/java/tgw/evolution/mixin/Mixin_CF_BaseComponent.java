package tgw.evolution.mixin;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.List;

@Mixin(BaseComponent.class)
public abstract class Mixin_CF_BaseComponent implements MutableComponent {

    @Mutable @Final @RestoreFinal @Shadow protected final List<Component> siblings;
    @Shadow private @Nullable Language decomposedWith;
    @Shadow private Style style;
    @Shadow private FormattedCharSequence visualOrderText;

    @ModifyConstructor
    public Mixin_CF_BaseComponent() {
        this.siblings = new OArrayList<>();
        this.visualOrderText = FormattedCharSequence.EMPTY;
        this.style = Style.EMPTY;
    }

    @Override
    public void resetCache() {
        this.decomposedWith = null;
        for (int i = 0, l = this.siblings.size(); i < l; i++) {
            this.siblings.get(i).resetCache();
        }
    }
}
