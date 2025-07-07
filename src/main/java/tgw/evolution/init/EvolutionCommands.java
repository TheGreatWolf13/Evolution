package tgw.evolution.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.IdMap;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.commands.*;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class EvolutionCommands {

    private EvolutionCommands() {
    }

    public static <T> void filterResources(IdMap<T> registry, String search, Function<ResourceLocation, ResourceLocation> function, Consumer<ResourceLocation> consumer) {
        boolean hasNamespace = search.indexOf(':') > -1;
        for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
            ResourceLocation t = registry.getIterationLocation(it);
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandAtm.register(dispatcher);
        CommandCamera.register(dispatcher);
        CommandDate.register(dispatcher);
        CommandGC.register(dispatcher);
        CommandHeal.register(dispatcher);
        CommandIntegrity.register(dispatcher);
        CommandLoadFactor.register(dispatcher);
        CommandPause.register(dispatcher);
        CommandRegen.register(dispatcher);
        CommandShader.register(dispatcher);
        CommandTemperature.register(dispatcher);
        CommandTickrate.register(dispatcher);
        CommandVariantDebug.register(dispatcher);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> CompletableFuture<Suggestions> suggestResource(IdMap<T> registry, SuggestionsBuilder suggestionsBuilder) {
        String search = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(registry, search, Function.identity(), resourceLocation -> suggestionsBuilder.suggest(resourceLocation.toString()));
        return suggestionsBuilder.buildFuture();
    }
}
