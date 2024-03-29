package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.player.*;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCAddEffect;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.patches.PatchServerPlayer;
import tgw.evolution.patches.obj.ContainerListenerImpl;
import tgw.evolution.patches.obj.ContainerSynchronizerImpl;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.util.OptionalMutableChunkPos;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.math.ChunkPosMutable;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class Mixin_CF_ServerPlayer extends Player implements PatchServerPlayer {

    @Shadow @Final private static Logger LOGGER;
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow public @Nullable Vec3 enteredNetherPosition;
    @Mutable @Shadow @Final @RestoreFinal public ServerPlayerGameMode gameMode;
    @Shadow public boolean seenCredits;
    @Mutable @Shadow @Final @RestoreFinal public MinecraftServer server;
    @Mutable @Shadow @Final @RestoreFinal private PlayerAdvancements advancements;
    @Shadow private boolean allowsListing;
    @Shadow private @Nullable Entity camera;
    @Unique private boolean cameraUnload;
    @Shadow private boolean canChatColor;
    @Shadow private ChatVisiblity chatVisibility;
    @Mutable @Shadow @Final @RestoreFinal private ContainerListener containerListener;
    @Mutable @Shadow @Final @RestoreFinal private ContainerSynchronizer containerSynchronizer;
    @Unique private final CapabilityInventory extraInventory;
    @Unique private final CapabilityHealth healthStats;
    @Unique private final CapabilityHunger hungerStats;
    @Shadow private long lastActionTime;
    @Unique private final OptionalMutableChunkPos lastCameraSectionPos;
    @Unique private final ChunkPosMutable lastChunkPos;
    @Shadow private boolean lastFoodSaturationZero;
    @Shadow private int lastRecordedAirLevel;
    @Shadow private int lastRecordedArmor;
    @Shadow private int lastRecordedExperience;
    @Shadow private int lastRecordedFoodLevel;
    @Shadow private float lastRecordedHealthAndAbsorption;
    @Shadow private int lastRecordedLevel;
    @Shadow @DeleteField private SectionPos lastSectionPos;
    @Shadow private int lastSentExp;
    @Shadow private int lastSentFood;
    @Shadow private float lastSentHealth;
    @Shadow private @Nullable Vec3 levitationStartPos;
    @Shadow private int levitationStartTime;
    @Mutable @Shadow @Final @RestoreFinal private ServerRecipeBook recipeBook;
    @Shadow private float respawnAngle;
    @Shadow private ResourceKey<Level> respawnDimension;
    @Shadow private boolean respawnForced;
    @Shadow private @Nullable BlockPos respawnPosition;
    @Shadow private int spawnInvulnerableTime;
    @Unique private final CapabilityStamina staminaStats;
    @Mutable @Shadow @Final @RestoreFinal private ServerStatsCounter stats;
    @Unique private final CapabilityTemperature temperatureStats;
    @Mutable @Shadow @Final @RestoreFinal private TextFilter textFilter;
    @Shadow private boolean textFilteringEnabled;
    @Unique private final CapabilityThirst thirstStats;
    @Unique private final CapabilityToast toastStats;

    @ModifyConstructor
    public Mixin_CF_ServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(level, level.getSharedSpawnPos(), level.getSharedSpawnAngle(), profile);
        this.lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
        this.lastRecordedFoodLevel = Integer.MIN_VALUE;
        this.lastRecordedAirLevel = Integer.MIN_VALUE;
        this.lastRecordedArmor = Integer.MIN_VALUE;
        this.lastRecordedLevel = Integer.MIN_VALUE;
        this.lastRecordedExperience = Integer.MIN_VALUE;
        this.lastChunkPos = new ChunkPosMutable();
        this.lastSentHealth = -1.0E8F;
        this.lastSentFood = -99_999_999;
        this.lastFoodSaturationZero = true;
        this.lastSentExp = -99_999_999;
        this.spawnInvulnerableTime = 60;
        this.extraInventory = new CapabilityInventory(this);
        this.healthStats = new CapabilityHealth();
        this.hungerStats = new CapabilityHunger();
        this.lastCameraSectionPos = new OptionalMutableChunkPos();
        this.staminaStats = new CapabilityStamina();
        this.temperatureStats = new CapabilityTemperature();
        this.thirstStats = new CapabilityThirst();
        this.toastStats = new CapabilityToast();
        this.chatVisibility = ChatVisiblity.FULL;
        this.canChatColor = true;
        this.lastActionTime = Util.getMillis();
        this.recipeBook = new ServerRecipeBook();
        this.respawnDimension = Level.OVERWORLD;
        this.allowsListing = true;
        this.containerSynchronizer = new ContainerSynchronizerImpl((ServerPlayer) (Object) this);
        this.containerListener = new ContainerListenerImpl((ServerPlayer) (Object) this);
        this.textFilter = server.createTextFilterForPlayer((ServerPlayer) (Object) this);
        this.gameMode = server.createGameModeForPlayer((ServerPlayer) (Object) this);
        this.server = server;
        this.stats = server.getPlayerList().getPlayerStats(this);
        this.advancements = server.getPlayerList().getPlayerAdvancements((ServerPlayer) (Object) this);
        this.maxUpStep = 1.0F;
        this.fudgeSpawnLocation(level);
    }

    /**
     * @author TheGreatWolf
     * @reason Save capabilities
     */
    @Overwrite
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.storeGameTypes(tag);
        tag.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPosition != null) {
            CompoundTag netherPosition = new CompoundTag();
            netherPosition.putDouble("x", this.enteredNetherPosition.x);
            netherPosition.putDouble("y", this.enteredNetherPosition.y);
            netherPosition.putDouble("z", this.enteredNetherPosition.z);
            tag.put("enteredNetherPosition", netherPosition);
        }
        Entity rootVehicle = this.getRootVehicle();
        Entity vehicle = this.getVehicle();
        if (vehicle != null && rootVehicle != this && rootVehicle.hasExactlyOnePlayerPassenger()) {
            CompoundTag rootVehicleTag = new CompoundTag();
            CompoundTag vehicleTag = new CompoundTag();
            rootVehicle.save(vehicleTag);
            rootVehicleTag.putUUID("Attach", vehicle.getUUID());
            rootVehicleTag.put("Entity", vehicleTag);
            tag.put("RootVehicle", rootVehicleTag);
        }
        tag.put("recipeBook", this.recipeBook.toNbt());
        tag.putString("Dimension", this.level.dimension().location().toString());
        if (this.respawnPosition != null) {
            tag.putInt("SpawnX", this.respawnPosition.getX());
            tag.putInt("SpawnY", this.respawnPosition.getY());
            tag.putInt("SpawnZ", this.respawnPosition.getZ());
            tag.putBoolean("SpawnForced", this.respawnForced);
            tag.putFloat("SpawnAngle", this.respawnAngle);
            tag.put("SpawnDimension", NBTHelper.encode(this.respawnDimension.location()));
        }
        tag.put("HealthStats", this.healthStats.serializeNBT());
        tag.put("HungerStats", this.hungerStats.serializeNBT());
        tag.put("ExtraInventory", this.extraInventory.serializeNBT());
        tag.put("StaminaStats", this.staminaStats.serializeNBT());
        tag.put("TemperatureStats", this.temperatureStats.serializeNBT());
        tag.put("ThirstStats", this.thirstStats.serializeNBT());
        tag.put("ToastStats", this.toastStats.serializeNBT());
    }

    /**
     * @author TheGreatWolf
     * @reason Modify Scores
     */
    @Override
    @Overwrite
    public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
        if (entity != this) {
            super.awardKillScore(entity, i, damageSource);
            this.increaseScore(i);
            String string = this.getScoreboardName();
            String string2 = entity.getScoreboardName();
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, string, Score::increment);
            if (entity instanceof Player) {
                this.awardStat(EvolutionStats.PLAYER_KILLS);
                this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, string, Score::increment);
            }
            else {
                this.awardStat(EvolutionStats.MOB_KILLS);
            }
            this.handleTeamKill(string, string2, ObjectiveCriteria.TEAM_KILL);
            this.handleTeamKill(string2, string, ObjectiveCriteria.KILLED_BY_TEAM);
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger((ServerPlayer) (Object) this, entity, damageSource);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void awardRecipesByKey(ResourceLocation[] recipes) {
        List<Recipe<?>> list = new OArrayList<>();
        for (ResourceLocation key : recipes) {
            Recipe<?> recipe = this.server.getRecipeManager().byKey_(key);
            if (recipe != null) {
                list.add(recipe);
            }
        }
        this.awardRecipes(list);
    }

    /**
     * @author TheGreatWolf
     * @reason Spawn Corpse
     */
    @Overwrite
    @Override
    public void die(DamageSource source) {
        EntityPlayerCorpse corpse = new EntityPlayerCorpse((ServerPlayer) (Object) this);
        if (!this.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).get()) {
            corpse.setInventory((ServerPlayer) (Object) this);
        }
        this.level.addFreshEntity(corpse);
        long timeAlive = this.getStats().getValue_(Stats.CUSTOM.get(EvolutionStats.TIME_SINCE_LAST_DEATH));
        if (this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            Component deathMessage = this.getCombatTracker().getDeathMessage();
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), deathMessage).setTimeAlive(timeAlive), future -> {
                if (!future.isSuccess()) {
                    String string = deathMessage.getString(256);
                    Component desc = new TranslatableComponent("death.attack.message_too_long",
                                                               new TextComponent(string).withStyle(ChatFormatting.YELLOW));
                    Component longMessage = new TranslatableComponent("death.attack.even_more_magic", this.getDisplayName()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
                    this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), longMessage).setTimeAlive(timeAlive));
                }
            });
            Team team = this.getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().broadcastToTeam(this, deathMessage);
                }
                else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().broadcastToAllExceptTeam(this, deathMessage);
                }
            }
            else {
                this.server.getPlayerList().broadcastMessage(deathMessage, ChatType.SYSTEM, Util.NIL_UUID);
            }
        }
        else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), TextComponent.EMPTY).setTimeAlive(timeAlive));
        }
        this.removeEntitiesOnShoulder();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }
        if (!this.isSpectator()) {
            this.dropAllDeathLoot(source);
        }
        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
        LivingEntity killer = this.getKillCredit();
        if (killer != null) {
            killer.killed(this.getLevel(), this);
            this.awardStat(Stats.ENTITY_KILLED_BY.get(killer.getType()));
            killer.awardKillScore(this, this.deathScore, source);
            this.createWitherRose(killer);
        }
        this.level.broadcastEntityEvent(this, (byte) 3);
        this.awardStat(EvolutionStats.DEATHS);
        ResourceLocation stat = EvolutionStats.DEATH_SOURCE.get(source.msgId);
        if (stat != null) {
            this.awardStat(stat);
        }
        else {
            if (!"fall_damage".equals(source.msgId)) {
                Evolution.warn("Unknown stat for {}", source);
            }
        }
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void doCheckFallDamage(double y, boolean onGround) {
        if (!this.touchingUnloadedChunk()) {
            BlockPos pos = this.getOnPos();
            super.checkFallDamage(y, onGround, this.level.getBlockState_(pos), pos);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Modify Stats.
     */
    @Override
    @Overwrite
    public ItemEntity drop(ItemStack stack, boolean dropAround, boolean setThrower) {
        ItemEntity itemEntity = super.drop(stack, dropAround, setThrower);
        if (itemEntity == null) {
            return null;
        }
        this.level.addFreshEntity(itemEntity);
        ItemStack stac = itemEntity.getItem();
        if (setThrower) {
            if (!stac.isEmpty()) {
                this.awardStat(Stats.ITEM_DROPPED.get(stac.getItem()), stack.getCount());
            }
            this.awardStat(EvolutionStats.ITEMS_DROPPED);
        }
        return itemEntity;
    }

    @Shadow
    public abstract Entity getCamera();

    @Override
    public boolean getCameraUnload() {
        return this.cameraUnload;
    }

    @Override
    public CapabilityInventory getExtraInventory() {
        return this.extraInventory;
    }

    @Override
    public CapabilityHealth getHealthStats() {
        return this.healthStats;
    }

    @Override
    public CapabilityHunger getHungerStats() {
        return this.hungerStats;
    }

    @Override
    public OptionalMutableChunkPos getLastCameraChunkPos() {
        return this.lastCameraSectionPos;
    }

    @Override
    public ChunkPosMutable getLastChunkPos() {
        return this.lastChunkPos;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public SectionPos getLastSectionPos() {
        Evolution.deprecatedMethod();
        return SectionPos.of(0, 0, 0);
    }

    @Override
    @Shadow
    public abstract ServerLevel getLevel();

    @Override
    public CapabilityStamina getStaminaStats() {
        return this.staminaStats;
    }

    @Shadow
    public abstract ServerStatsCounter getStats();

    @Override
    public CapabilityTemperature getTemperatureStats() {
        return this.temperatureStats;
    }

    @Override
    public CapabilityThirst getThirstStats() {
        return this.thirstStats;
    }

    @Override
    public CapabilityToast getToastStats() {
        return this.toastStats;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to overcome respawnInvulnerabilityTime
     */
    @Overwrite
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.spawnInvulnerableTime > 0) {
            boolean checkForImmunity = !source.isBypassInvul() &&
                                       !(source instanceof DamageSourceEv evDamSource && evDamSource.getType() == EvolutionDamage.Type.IMPACT);
            if (checkForImmunity) {
                return false;
            }
        }
        if (source instanceof EntityDamageSource) {
            Entity entity = source.getEntity();
            if (entity instanceof Player player && !this.canHarmPlayer(player)) {
                return false;
            }
            if (entity instanceof AbstractArrow arrow) {
                Entity owner = arrow.getOwner();
                if (owner instanceof Player player && !this.canHarmPlayer(player)) {
                    return false;
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    @Shadow
    public abstract boolean isSpectator();

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        R2OMap<MobEffect, MobEffectInstance> effectsMap = (R2OMap<MobEffect, MobEffectInstance>) this.getActiveEffectsMap();
        for (long it = effectsMap.beginIteration(); effectsMap.hasNextIteration(it); it = effectsMap.nextEntry(it)) {
            //noinspection ObjectAllocationInLoop
            this.connection.send(new PacketSCAddEffect(effectsMap.getIterationValue(it), PacketSCAddEffect.Logic.ADD));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Read capabilities
     */
    @Overwrite
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("enteredNetherPosition", Tag.TAG_COMPOUND)) {
            CompoundTag netherPosition = tag.getCompound("enteredNetherPosition");
            this.enteredNetherPosition = new Vec3(netherPosition.getDouble("x"), netherPosition.getDouble("y"), netherPosition.getDouble("z"));
        }
        this.seenCredits = tag.getBoolean("seenCredits");
        if (tag.contains("recipeBook", Tag.TAG_COMPOUND)) {
            this.recipeBook.fromNbt(tag.getCompound("recipeBook"), this.server.getRecipeManager());
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (tag.contains("SpawnX", Tag.TAG_ANY_NUMERIC) &&
            tag.contains("SpawnY", Tag.TAG_ANY_NUMERIC) &&
            tag.contains("SpawnZ", Tag.TAG_ANY_NUMERIC)) {
            this.respawnPosition = new BlockPos(tag.getInt("SpawnX"), tag.getInt("SpawnY"), tag.getInt("SpawnZ"));
            this.respawnForced = tag.getBoolean("SpawnForced");
            this.respawnAngle = tag.getFloat("SpawnAngle");
            this.respawnDimension = NBTHelper.parseResourceKeyOrElse(Registry.DIMENSION_REGISTRY, tag.get("SpawnDimension"), LOGGER, Level.OVERWORLD);
        }
        this.extraInventory.deserializeNBT(NBTHelper.getCompound(tag, "ExtraInventory"));
        this.healthStats.deserializeNBT(NBTHelper.getCompound(tag, "HealthStats"));
        this.hungerStats.deserializeNBT(NBTHelper.getCompound(tag, "HungerStats"));
        this.staminaStats.deserializeNBT(NBTHelper.getCompound(tag, "StaminaStats"));
        this.temperatureStats.deserializeNBT(NBTHelper.getCompound(tag, "TemperatureStats"));
        this.thirstStats.deserializeNBT(NBTHelper.getCompound(tag, "ThirstStats"));
        this.toastStats.deserializeNBT(NBTHelper.getCompound(tag, "ToastStats"));
    }

    /**
     * @author TheGreatWolf
     * @reason Remember capabilities
     */
    @Overwrite
    public void restoreFrom(ServerPlayer oldPlayer, boolean keepEverything) {
        this.textFilteringEnabled = oldPlayer.isTextFilteringEnabled();
        this.gameMode.setGameModeForPlayer(oldPlayer.gameMode.getGameModeForPlayer(), oldPlayer.gameMode.getPreviousGameModeForPlayer());
        if (keepEverything) {
            this.getInventory().replaceWith(oldPlayer.getInventory());
            this.setHealth(oldPlayer.getHealth());
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
            this.portalEntrancePos = oldPlayer.portalEntrancePos;
        }
        else if (this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || oldPlayer.isSpectator()) {
            this.getInventory().replaceWith(oldPlayer.getInventory());
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
        }
        this.enchantmentSeed = oldPlayer.getEnchantmentSeed();
        this.enderChestInventory = oldPlayer.getEnderChestInventory();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, oldPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.recipeBook.copyOverData(oldPlayer.getRecipeBook());
        this.seenCredits = oldPlayer.seenCredits;
        this.enteredNetherPosition = oldPlayer.enteredNetherPosition;
        this.setShoulderEntityLeft(oldPlayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(oldPlayer.getShoulderEntityRight());
        this.extraInventory.set(oldPlayer.getExtraInventory());
        this.healthStats.set(oldPlayer.getHealthStats());
        this.hungerStats.set(oldPlayer.getHungerStats());
        this.staminaStats.set(oldPlayer.getStaminaStats());
        this.temperatureStats.set(oldPlayer.getTemperatureStats());
        this.thirstStats.set(oldPlayer.getThirstStats());
        this.toastStats.set(oldPlayer.getToastStats());
    }

    /**
     * @author TheGreatWolf
     * @reason Prevent teleport if not spectator
     */
    @Overwrite
    public void setCamera(@Nullable Entity entity) {
        Entity camera = this.getCamera();
        this.camera = entity == null ? this : entity;
        if (camera != this.camera) {
            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            if (this.isSpectator()) {
                this.teleportTo(this.camera.getX(), this.camera.getY(), this.camera.getZ());
            }
        }
    }

    @Override
    @Unique
    public void setCameraUnload(boolean shouldUnload) {
        this.cameraUnload = shouldUnload;
    }

    @Override
    public void setLastChunkPos_(int secX, int secZ) {
        this.lastChunkPos.set(secX, secZ);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void setLastSectionPos(SectionPos sectionPos) {
        Evolution.deprecatedMethod();
        this.lastSectionPos = sectionPos;
    }

    @Shadow
    public abstract void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2);

    /**
     * @author TheGreatWolf
     * @reason Modify stats.
     */
    @Override
    @Overwrite
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos pos) {
        Direction direction = this.level.getBlockState_(pos.getX(), pos.getY(), pos.getZ()).getValue(HorizontalDirectionalBlock.FACING);
        if (!this.isSleeping() && this.isAlive()) {
            if (!this.level.dimensionType().natural()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
            }
            if (!this.bedInRange(pos, direction)) {
                return Either.left(BedSleepingProblem.TOO_FAR_AWAY);
            }
            if (this.bedBlocked(pos, direction)) {
                return Either.left(BedSleepingProblem.OBSTRUCTED);
            }
            this.setRespawnPosition(this.level.dimension(), pos, this.getYRot(), false, true);
            if (this.level.isDay()) {
                return Either.left(BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
            if (!this.isCreative()) {
                List<Monster> list = this.level.getEntitiesOfClass(Monster.class, new AABB(pos.getX() - 7.5, pos.getY() - 5, pos.getZ() - 7.5,
                                                                                           pos.getX() + 8.5, pos.getY() + 5, pos.getZ() + 8.5),
                                                                   monster -> monster.isPreventingPlayerRest(this));
                if (!list.isEmpty()) {
                    return Either.left(BedSleepingProblem.NOT_SAFE);
                }
            }
            Either<BedSleepingProblem, Unit> either = super.startSleepInBed(pos);
            if (((PatchEither<BedSleepingProblem, Unit>) either).isRight()) {
                this.awardStat(Stats.SLEEP_IN_BED);
                CriteriaTriggers.SLEPT_IN_BED.trigger((ServerPlayer) (Object) this);
            }
            if (!this.getLevel().canSleepThroughNights()) {
                this.displayClientMessage(new TranslatableComponent("sleep.not_possible"), true);
            }
            ((ServerLevel) this.level).updateSleepingPlayerList();
            return either;
        }
        return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
    }

    /**
     * @author TheGreatWolf
     * @reason Modify camera behaviour
     */
    @Override
    @Overwrite
    public void tick() {
        this.gameMode.tick();
        --this.spawnInvulnerableTime;
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }
        this.containerMenu.broadcastChanges();
        if (!this.level.isClientSide && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }
        Entity entity = this.getCamera();
        if (entity != this) {
            if (entity.isAlive()) {
                boolean spectator = this.isSpectator();
                if (spectator) {
                    this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                }
                this.getLevel().getChunkSource().move((ServerPlayer) (Object) this);
                if (spectator && this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            }
            else {
                this.setCamera(this);
            }
        }
        CriteriaTriggers.TICK.trigger((ServerPlayer) (Object) this);
        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger((ServerPlayer) (Object) this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }
        this.trackStartFallingPosition();
        this.trackEnteredOrExitedLavaOnVehicle();
        this.advancements.flushDirty((ServerPlayer) (Object) this);
    }

    @Shadow
    public abstract void trackEnteredOrExitedLavaOnVehicle();

    @Shadow
    public abstract void trackStartFallingPosition();

    @Shadow
    protected abstract boolean bedBlocked(BlockPos blockPos, Direction direction);

    @Shadow
    protected abstract boolean bedInRange(BlockPos blockPos, Direction direction);

    @Shadow
    protected abstract void fudgeSpawnLocation(ServerLevel serverLevel);

    @Shadow
    protected abstract void handleTeamKill(String string, String string2, ObjectiveCriteria[] objectiveCriterias);

    @Shadow
    protected abstract void storeGameTypes(CompoundTag compoundTag);

    @Shadow
    protected abstract void tellNeutralMobsThatIDied();
}
