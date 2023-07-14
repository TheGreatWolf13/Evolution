package tgw.evolution.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchComponent;

import java.util.List;
import java.util.Optional;

@Mixin(Component.class)
public interface MixinComponent extends PatchComponent, FormattedText {

    @Shadow
    List<Component> getSiblings();

    @Shadow
    Style getStyle();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Override
    @Overwrite
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> acceptor, Style st) {
        Style style = this.getStyle().applyTo(st);
        Optional<T> o = this.visitSelf(acceptor, style);
        if (o.isPresent()) {
            return o;
        }
        List<Component> siblings = this.getSiblings();
        for (int i = 0, l = siblings.size(); i < l; i++) {
            Optional<T> op = siblings.get(i).visit(acceptor, style);
            if (op.isPresent()) {
                return op;
            }
        }
        return Optional.empty();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Override
    @Overwrite
    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> acceptor) {
        Optional<T> op = this.visitSelf(acceptor);
        if (op.isPresent()) {
            return op;
        }
        List<Component> siblings = this.getSiblings();
        for (int i = 0, l = siblings.size(); i < l; i++) {
            Optional<T> o = siblings.get(i).visit(acceptor);
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    @Shadow
    <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> pConsumer, Style pStyle);

    @Shadow
    <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> pConsumer);
}
