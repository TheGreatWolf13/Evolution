package tgw.evolution.mixin;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(SharedSuggestionProvider.class)
public interface MixinSharedSuggestionProvider {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    static <T> void filterResources(Iterable<T> iterable, String search, Function<T, ResourceLocation> function, Consumer<T> consumer) {
        boolean hasNamespace = search.indexOf(':') > -1;
        for (T t : iterable) {
            ResourceLocation resourceLocation = function.apply(t);
            if (hasNamespace) {
                if (!SharedSuggestionProvider.matchesSubStr(search, resourceLocation.toString())) {
                    continue;
                }
                consumer.accept(t);
                continue;
            }
            if (!SharedSuggestionProvider.matchesSubStr(search, resourceLocation.getNamespace()) && !SharedSuggestionProvider.matchesSubStr(search, resourceLocation.getPath())) {
                continue;
            }
            consumer.accept(t);
        }
    }
}
