package tgw.evolution.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.util.collection.OArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class EnumEvArgument<T extends Enum<T>> implements ArgumentType<T> {

    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found));
    private final Class<T> enumClass;

    private EnumEvArgument(final Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    public static <R extends Enum<R>> EnumEvArgument<R> enumArgument(Class<R> enumClass) {
        return new EnumEvArgument<>(enumClass);
    }

    @Override
    public Collection<String> getExamples() {
        List<String> examples = new OArrayList<>();
        for (T e : this.enumClass.getEnumConstants()) {
            examples.add(e.name().toLowerCase(Locale.ROOT));
        }
        return examples;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Stream.of(this.enumClass.getEnumConstants()).map(t -> t.name().toLowerCase(Locale.ROOT)), builder);
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString().toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(this.enumClass, name);
        }
        catch (IllegalArgumentException e) {
            List<String> constants = new OArrayList();
            for (T t : this.enumClass.getEnumConstants()) {
                constants.add(t.name().toLowerCase(Locale.ROOT));
            }
            throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(constants.toArray()));
        }
    }
}
