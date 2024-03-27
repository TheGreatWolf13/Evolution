package tgw.evolution.mixin;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicLike;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.init.EvolutionGameRules;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.*;

@Mixin(GameRules.class)
public abstract class Mixin_CFS_GameRules {

    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_FREEZE_DAMAGE;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER;
    @Mutable @Shadow @Final @RestoreFinal public static GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_SLEEPING_PERCENTAGE;
    @Mutable @Shadow @Final @RestoreFinal static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal private static Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES;
    @Mutable @Shadow @Final @RestoreFinal public Map<GameRules.Key<?>, GameRules.Value<?>> rules;

    @ModifyConstructor
    public Mixin_CFS_GameRules() {
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> map = new O2OHashMap<>();
        for (Map.Entry<GameRules.Key<?>, GameRules.Type<?>> entry : GAME_RULE_TYPES.entrySet()) {
            map.put(entry.getKey(), entry.getValue().createRule());
        }
        map.trim();
        this.rules = map;
    }

    @ModifyConstructor
    private Mixin_CFS_GameRules(Map<GameRules.Key<?>, GameRules.Value<?>> map) {
        this.rules = map;
    }

    @Shadow
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String string, GameRules.Category category, GameRules.Type<T> type) {
        throw new AbstractMethodError();
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        LOGGER = LogUtils.getLogger();
        GAME_RULE_TYPES = new TreeMap<>(Comparator.comparing(GameRules.Key::getId));
        RULE_DOFIRETICK = register("doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
        RULE_MOBGRIEFING = register("mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
        RULE_KEEPINVENTORY = register("keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
        RULE_DOMOBLOOT = register("doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
        RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
        RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
        RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
        RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_DAYLIGHT = register("doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
        RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
        RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
        RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3));
        RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
        RULE_REDUCEDDEBUGINFO = register("reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(true, (server, rule) -> {
            byte b = (byte) (rule.get() ? 22 : 23);
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (int i = 0, len = players.size(); i < len; ++i) {
                ServerPlayer player = players.get(i);
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundEntityEventPacket(player, b));
            }
        }));
        RULE_SPECTATORSGENERATECHUNKS = register("spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10));
        RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register("disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24));
        RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
        RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        RULE_MAX_COMMAND_CHAIN_LENGTH = register("maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65_536));
        RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
        RULE_DISABLE_RAIDS = register("disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
        RULE_DOINSOMNIA = register("doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));
        RULE_DO_IMMEDIATE_RESPAWN = register("doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (server, rule) -> {
            Packet<ClientGamePacketListener> packet = new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, rule.get() ? 1.0F : 0.0F);
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (int i = 0, len = players.size(); i < len; ++i) {
                players.get(i).connection.send(packet);
            }
        }));
        RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_FALL_DAMAGE = register("fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_FIRE_DAMAGE = register("fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_FREEZE_DAMAGE = register("freezeDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        RULE_DO_PATROL_SPAWNING = register("doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));
        RULE_DO_TRADER_SPAWNING = register("doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));
        RULE_FORGIVE_DEAD_PLAYERS = register("forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
        RULE_UNIVERSAL_ANGER = register("universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
        RULE_PLAYERS_SLEEPING_PERCENTAGE = register("playersSleepingPercentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100));
        EvolutionGameRules.register();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void assignFrom(GameRules gameRules, @Nullable MinecraftServer minecraftServer) {
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> rules = (O2OMap<GameRules.Key<?>, GameRules.Value<?>>) gameRules.rules;
        for (long it = rules.beginIteration(); rules.hasNextIteration(it); it = rules.nextEntry(it)) {
            this.assignCap(rules.getIterationKey(it), gameRules, minecraftServer);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public GameRules copy() {
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> map = new O2OHashMap<>();
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> rules = (O2OMap<GameRules.Key<?>, GameRules.Value<?>>) this.rules;
        for (long it = rules.beginIteration(); rules.hasNextIteration(it); it = rules.nextEntry(it)) {
            map.put(rules.getIterationKey(it), rules.getIterationValue(it).copy());
        }
        map.trim();
        return new GameRules(map);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> rules = (O2OMap<GameRules.Key<?>, GameRules.Value<?>>) this.rules;
        for (long it = rules.beginIteration(); rules.hasNextIteration(it); it = rules.nextEntry(it)) {
            tag.putString(rules.getIterationKey(it).getId(), rules.getIterationValue(it).serialize());
        }
        return tag;
    }

    @Shadow
    protected abstract <T extends GameRules.Value<T>> void assignCap(GameRules.Key<T> key, GameRules gameRules, @Nullable MinecraftServer minecraftServer);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void loadFromTag(DynamicLike<?> dynamicLike) {
        O2OMap<GameRules.Key<?>, GameRules.Value<?>> rules = (O2OMap<GameRules.Key<?>, GameRules.Value<?>>) this.rules;
        for (long it = rules.beginIteration(); rules.hasNextIteration(it); it = rules.nextEntry(it)) {
            Optional<String> result = dynamicLike.get(rules.getIterationKey(it).getId()).asString().result();
            if (result.isPresent()) {
                rules.getIterationValue(it).deserialize(result.get());
            }
        }
    }
}
