package tgw.evolution.mixin;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class MixinEntitySelectorOptions {

    @Unique private static final OList<String> SORT_SUGGESTIONS;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_GAME_MODE_INVALID;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_SORT_UNKNOWN;
    @Shadow @Final public static SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL;
    @Shadow @Final public static SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE;
    @Shadow @Final public static SimpleCommandExceptionType ERROR_RANGE_NEGATIVE;
    @Shadow @Final public static DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION;
    @Shadow @Final private static Map<String, EntitySelectorOptions.Option> OPTIONS;

    static {
        OList<String> list = new OArrayList<>();
        list.add("nearest");
        list.add("furthest");
        list.add("random");
        list.add("arbitrary");
        list.trimCollection();
        SORT_SUGGESTIONS = list.view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void bootStrap() {
        if (OPTIONS.isEmpty()) {
            register("name", parser -> {
                int cursor = parser.getReader().getCursor();
                boolean invert = parser.shouldInvertValue();
                String string = parser.getReader().readString();
                if (parser.hasNameNotEquals() && !invert) {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "name");
                }
                if (invert) {
                    parser.setHasNameNotEquals(true);
                }
                else {
                    parser.setHasNameEquals(true);
                }
                parser.addPredicate(entity -> entity.getName().getString().equals(string) != invert);
            }, parser -> !parser.hasNameEquals(), new TranslatableComponent("argument.entity.options.name.description"));
            register("distance", parser -> {
                int cursor = parser.getReader().getCursor();
                MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromReader(parser.getReader());
                if ((doubles.getMin() == null || !(doubles.getMin() < 0.0)) && (doubles.getMax() == null || !(doubles.getMax() < 0.0))) {
                    parser.setDistance(doubles);
                    parser.setWorldLimited();
                }
                else {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_RANGE_NEGATIVE.createWithContext(parser.getReader());
                }
            }, parser -> parser.getDistance().isAny(), new TranslatableComponent("argument.entity.options.distance.description"));
            register("level", parser -> {
                int cursor = parser.getReader().getCursor();
                MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(parser.getReader());
                if ((ints.getMin() == null || ints.getMin() >= 0) && (ints.getMax() == null || ints.getMax() >= 0)) {
                    parser.setLevel(ints);
                    parser.setIncludesEntities(false);
                }
                else {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_LEVEL_NEGATIVE.createWithContext(parser.getReader());
                }
            }, parser -> parser.getLevel().isAny(), new TranslatableComponent("argument.entity.options.level.description"));
            register("x", parser -> {
                parser.setWorldLimited();
                parser.setX(parser.getReader().readDouble());
            }, parser -> parser.getX() == null, new TranslatableComponent("argument.entity.options.x.description"));
            register("y", parser -> {
                parser.setWorldLimited();
                parser.setY(parser.getReader().readDouble());
            }, parser -> parser.getY() == null, new TranslatableComponent("argument.entity.options.y.description"));
            register("z", parser -> {
                parser.setWorldLimited();
                parser.setZ(parser.getReader().readDouble());
            }, parser -> parser.getZ() == null, new TranslatableComponent("argument.entity.options.z.description"));
            register("dx", parser -> {
                parser.setWorldLimited();
                parser.setDeltaX(parser.getReader().readDouble());
            }, parser -> parser.getDeltaX() == null, new TranslatableComponent("argument.entity.options.dx.description"));
            register("dy", parser -> {
                parser.setWorldLimited();
                parser.setDeltaY(parser.getReader().readDouble());
            }, parser -> parser.getDeltaY() == null, new TranslatableComponent("argument.entity.options.dy.description"));
            register("dz", parser -> {
                parser.setWorldLimited();
                parser.setDeltaZ(parser.getReader().readDouble());
            }, parser -> parser.getDeltaZ() == null, new TranslatableComponent("argument.entity.options.dz.description"));
            register("x_rotation", parser -> parser.setRotX(WrappedMinMaxBounds.fromReader(parser.getReader(), true, Mth::wrapDegrees)), parser -> parser.getRotX() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.x_rotation.description"));
            register("y_rotation", parser -> parser.setRotY(WrappedMinMaxBounds.fromReader(parser.getReader(), true, Mth::wrapDegrees)), parser -> parser.getRotY() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.y_rotation.description"));
            register("limit", parser -> {
                int cursor = parser.getReader().getCursor();
                int anInt = parser.getReader().readInt();
                if (anInt < 1) {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_LIMIT_TOO_SMALL.createWithContext(parser.getReader());
                }
                parser.setMaxResults(anInt);
                parser.setLimited(true);
            }, parser -> !parser.isCurrentEntity() && !parser.isLimited(), new TranslatableComponent("argument.entity.options.limit.description"));
            register("sort", parser -> {
                int i = parser.getReader().getCursor();
                String string = parser.getReader().readUnquotedString();
                parser.setSuggestions((suggestionsBuilder, consumer) -> SharedSuggestionProvider.suggest(SORT_SUGGESTIONS, suggestionsBuilder));
                BiConsumer biConsumer = switch (string) {
                    case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
                    case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
                    case "random" -> EntitySelectorParser.ORDER_RANDOM;
                    case "arbitrary" -> EntitySelectorParser.ORDER_ARBITRARY;
                    default -> {
                        parser.getReader().setCursor(i);
                        throw ERROR_SORT_UNKNOWN.createWithContext(parser.getReader(), string);
                    }
                };
                parser.setOrder(biConsumer);
                parser.setSorted(true);
            }, parser -> !parser.isCurrentEntity() && !parser.isSorted(), new TranslatableComponent("argument.entity.options.sort.description"));
            register("gamemode", parser -> {
                parser.setSuggestions((suggestionsBuilder, consumer) -> {
                    String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
                    boolean bl = !parser.hasGamemodeNotEquals();
                    boolean bl2 = true;
                    if (!string.isEmpty()) {
                        if (string.charAt(0) == '!') {
                            bl = false;
                            string = string.substring(1);
                        }
                        else {
                            bl2 = false;
                        }
                    }
                    for (GameType gameType : ArrayHelper.GAME_TYPES) {
                        if (gameType.getName().toLowerCase(Locale.ROOT).startsWith(string)) {
                            if (bl2) {
                                suggestionsBuilder.suggest("!" + gameType.getName());
                            }
                            if (bl) {
                                suggestionsBuilder.suggest(gameType.getName());
                            }
                        }
                    }
                    return suggestionsBuilder.buildFuture();
                });
                int cursor = parser.getReader().getCursor();
                boolean invert = parser.shouldInvertValue();
                if (parser.hasGamemodeNotEquals() && !invert) {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "gamemode");
                }
                String string = parser.getReader().readUnquotedString();
                GameType gameType = GameType.byName(string, null);
                if (gameType == null) {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_GAME_MODE_INVALID.createWithContext(parser.getReader(), string);
                }
                parser.setIncludesEntities(false);
                parser.addPredicate(entity -> {
                    if (!(entity instanceof ServerPlayer player)) {
                        return false;
                    }
                    GameType gameModeForPlayer = player.gameMode.getGameModeForPlayer();
                    return invert == (gameModeForPlayer != gameType);
                });
                if (invert) {
                    parser.setHasGamemodeNotEquals(true);
                }
                else {
                    parser.setHasGamemodeEquals(true);
                }
            }, parser -> !parser.hasGamemodeEquals(), new TranslatableComponent("argument.entity.options.gamemode.description"));
            register("team", parser -> {
                boolean invert = parser.shouldInvertValue();
                String string = parser.getReader().readUnquotedString();
                parser.addPredicate(entity -> {
                    if (!(entity instanceof LivingEntity)) {
                        return false;
                    }
                    Team team = entity.getTeam();
                    String teamName = team == null ? "" : team.getName();
                    return teamName.equals(string) != invert;
                });
                if (invert) {
                    parser.setHasTeamNotEquals(true);
                }
                else {
                    parser.setHasTeamEquals(true);
                }
            }, parser -> !parser.hasTeamEquals(), new TranslatableComponent("argument.entity.options.team.description"));
            register("type", parser -> {
                parser.setSuggestions((suggestionsBuilder, consumer) -> {
                    SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), suggestionsBuilder, String.valueOf('!'));
                    SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsBuilder, "!#");
                    if (!parser.isTypeLimitedInversely()) {
                        SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), suggestionsBuilder);
                        SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.getTagNames().map(TagKey::location), suggestionsBuilder, String.valueOf('#'));
                    }

                    return suggestionsBuilder.buildFuture();
                });
                int cursor = parser.getReader().getCursor();
                boolean invert = parser.shouldInvertValue();
                if (parser.isTypeLimitedInversely() && !invert) {
                    parser.getReader().setCursor(cursor);
                    throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "type");
                }
                if (invert) {
                    parser.setTypeLimitedInversely();
                }
                if (parser.isTag()) {
                    TagKey<EntityType<?>> tagKey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ResourceLocation.read(parser.getReader()));
                    parser.addPredicate(entity -> entity.getType().is(tagKey) != invert);
                }
                else {
                    ResourceLocation resourceLocation = ResourceLocation.read(parser.getReader());
                    EntityType<?> entityType = (EntityType<?>) Registry.ENTITY_TYPE.getNullable(resourceLocation);
                    if (entityType == null) {
                        parser.getReader().setCursor(cursor);
                        throw ERROR_ENTITY_TYPE_INVALID.createWithContext(parser.getReader(), resourceLocation.toString());
                    }
                    if (Objects.equals(EntityType.PLAYER, entityType) && !invert) {
                        parser.setIncludesEntities(false);
                    }
                    parser.addPredicate(entity -> Objects.equals(entityType, entity.getType()) != invert);
                    if (!invert) {
                        parser.limitToType(entityType);
                    }
                }
            }, parser -> !parser.isTypeLimited(), new TranslatableComponent("argument.entity.options.type.description"));
            register("tag", parser -> {
                boolean invert = parser.shouldInvertValue();
                String string = parser.getReader().readUnquotedString();
                parser.addPredicate(entity -> {
                    if ("".equals(string)) {
                        return entity.getTags().isEmpty() != invert;
                    }
                    return entity.getTags().contains(string) != invert;
                });
            }, parser -> true, new TranslatableComponent("argument.entity.options.tag.description"));
            register("nbt", parser -> {
                boolean invert = parser.shouldInvertValue();
                CompoundTag readTag = new TagParser(parser.getReader()).readStruct();
                parser.addPredicate(entity -> {
                    CompoundTag savedTag = entity.saveWithoutId(new CompoundTag());
                    if (entity instanceof ServerPlayer player) {
                        ItemStack itemStack = player.getInventory().getSelected();
                        if (!itemStack.isEmpty()) {
                            savedTag.put("SelectedItem", itemStack.save(new CompoundTag()));
                        }
                    }
                    return NbtUtils.compareNbt(readTag, savedTag, true) != invert;
                });
            }, parser -> true, new TranslatableComponent("argument.entity.options.nbt.description"));
            register("scores", parser -> {
                StringReader stringReader = parser.getReader();
                Map<String, MinMaxBounds.Ints> map = Maps.newHashMap();
                stringReader.expect('{');
                stringReader.skipWhitespace();
                while (stringReader.canRead() && stringReader.peek() != '}') {
                    stringReader.skipWhitespace();
                    String string = stringReader.readUnquotedString();
                    stringReader.skipWhitespace();
                    stringReader.expect('=');
                    stringReader.skipWhitespace();
                    MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(stringReader);
                    map.put(string, ints);
                    stringReader.skipWhitespace();
                    if (stringReader.canRead() && stringReader.peek() == ',') {
                        stringReader.skip();
                    }
                }
                stringReader.expect('}');
                if (!map.isEmpty()) {
                    parser.addPredicate(entity -> {
                        Scoreboard scoreboard = entity.getServer().getScoreboard();
                        String string = entity.getScoreboardName();
                        for (Map.Entry<String, MinMaxBounds.Ints> entry : map.entrySet()) {
                            Objective objective = scoreboard.getObjective(entry.getKey());
                            if (objective == null) {
                                return false;
                            }
                            if (!scoreboard.hasPlayerScore(string, objective)) {
                                return false;
                            }
                            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
                            int scoreValue = score.getScore();
                            if (entry.getValue().matches(scoreValue)) {
                                continue;
                            }
                            return false;
                        }
                        return true;
                    });
                }
                parser.setHasScores(true);
            }, parser -> !parser.hasScores(), new TranslatableComponent("argument.entity.options.scores.description"));
            register("advancements", parser -> {
                StringReader stringReader = parser.getReader();
                O2OMap<ResourceLocation, Predicate<AdvancementProgress>> map = new O2OHashMap<>();
                stringReader.expect('{');
                stringReader.skipWhitespace();
                while (stringReader.canRead() && stringReader.peek() != '}') {
                    stringReader.skipWhitespace();
                    ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                    stringReader.skipWhitespace();
                    stringReader.expect('=');
                    stringReader.skipWhitespace();
                    if (stringReader.canRead() && stringReader.peek() == '{') {
                        O2OMap<String, Predicate<CriterionProgress>> map2 = new O2OHashMap<>();
                        stringReader.skipWhitespace();
                        stringReader.expect('{');
                        stringReader.skipWhitespace();
                        while (stringReader.canRead() && stringReader.peek() != '}') {
                            stringReader.skipWhitespace();
                            String string = stringReader.readUnquotedString();
                            stringReader.skipWhitespace();
                            stringReader.expect('=');
                            stringReader.skipWhitespace();
                            boolean bl = stringReader.readBoolean();
                            map2.put(string, criterionProgress -> criterionProgress.isDone() == bl);
                            stringReader.skipWhitespace();
                            if (stringReader.canRead() && stringReader.peek() == ',') {
                                stringReader.skip();
                            }
                        }
                        stringReader.skipWhitespace();
                        stringReader.expect('}');
                        stringReader.skipWhitespace();
                        map.put(resourceLocation, advancementProgress -> {
                            for (Object2ObjectMap.Entry<String, Predicate<CriterionProgress>> entry : map2.object2ObjectEntrySet()) {
                                CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
                                if (criterionProgress != null && entry.getValue().test(criterionProgress)) {
                                    continue;
                                }
                                return false;
                            }
                            return true;
                        });
                    }
                    else {
                        boolean bl2 = stringReader.readBoolean();
                        map.put(resourceLocation, advancementProgress -> advancementProgress.isDone() == bl2);
                    }
                    stringReader.skipWhitespace();
                    if (stringReader.canRead() && stringReader.peek() == ',') {
                        stringReader.skip();
                    }
                }
                stringReader.expect('}');
                if (!map.isEmpty()) {
                    parser.addPredicate(entity -> {
                        if (!(entity instanceof ServerPlayer player)) {
                            return false;
                        }
                        PlayerAdvancements playerAdvancements = player.getAdvancements();
                        ServerAdvancementManager serverAdvancementManager = player.getServer().getAdvancements();
                        for (Object2ObjectMap.Entry<ResourceLocation, Predicate<AdvancementProgress>> entry : map.object2ObjectEntrySet()) {
                            Advancement advancement = serverAdvancementManager.getAdvancement(entry.getKey());
                            if (advancement != null && entry.getValue().test(playerAdvancements.getOrStartProgress(advancement))) {
                                continue;
                            }
                            return false;
                        }
                        return true;
                    });
                    parser.setIncludesEntities(false);
                }
                parser.setHasAdvancements(true);
            }, parser -> !parser.hasAdvancements(), new TranslatableComponent("argument.entity.options.advancements.description"));
            register("predicate", parser -> {
                boolean invert = parser.shouldInvertValue();
                ResourceLocation resourceLocation = ResourceLocation.read(parser.getReader());
                parser.addPredicate(entity -> {
                    if (!(entity.level instanceof ServerLevel level)) {
                        return false;
                    }
                    LootItemCondition lootItemCondition = level.getServer().getPredicateManager().get(resourceLocation);
                    if (lootItemCondition == null) {
                        return false;
                    }
                    LootContext lootContext = new LootContext.Builder(level).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position()).create(LootContextParamSets.SELECTOR);
                    return invert ^ lootItemCondition.test(lootContext);
                });
            }, parser -> true, new TranslatableComponent("argument.entity.options.predicate.description"));
        }
    }

    @Shadow
    private static void register(String string, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
        throw new AbstractMethodError();
    }
}
