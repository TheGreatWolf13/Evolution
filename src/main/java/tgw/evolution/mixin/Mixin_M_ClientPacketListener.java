package tgw.evolution.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.capabilities.player.TemperatureClient;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.ScreenKnapping;
import tgw.evolution.client.gui.ScreenMolding;
import tgw.evolution.client.gui.recipebook.IRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBookUpdateListener;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.entities.IEntitySpawnData;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.*;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.stats.EvolutionStatsCounter;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.Vec3d;

import java.util.*;

@Mixin(ClientPacketListener.class)
public abstract class Mixin_M_ClientPacketListener implements ClientGamePacketListener {

    @Shadow @Final private Connection connection;
    @Shadow private ClientLevel level;
    @Shadow private ClientLevel.ClientLevelData levelData;
    @Shadow private Set<ResourceKey<Level>> levels;
    @Final @Shadow private Minecraft minecraft;
    @Shadow @Final private RecipeManager recipeManager;
    @Shadow private RegistryAccess.Frozen registryAccess;
    @Shadow private int serverChunkRadius;
    @Shadow private int serverSimulationDistance;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void applyLightData(int i, int j, ClientboundLightUpdatePacketData packetData) {
        LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
        BitSet bitSet = packetData.getSkyYMask();
        BitSet bitSet2 = packetData.getEmptySkyYMask();
        this.readSectionList(i, j, levelLightEngine, LightLayer.SKY, bitSet, bitSet2, packetData.getSkyUpdates(), packetData.getTrustEdges());
        BitSet bitSet3 = packetData.getBlockYMask();
        BitSet bitSet4 = packetData.getEmptyBlockYMask();
        this.readSectionList(i, j, levelLightEngine, LightLayer.BLOCK, bitSet3, bitSet4, packetData.getBlockUpdates(), packetData.getTrustEdges());
        this.level.setLightReady(i, j);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void enableChunkLight(LevelChunk levelChunk, int i, int j) {
        LevelLightEngine lightEngine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] sections = levelChunk.getSections();
        ChunkPos chunkPos = levelChunk.getPos();
        for (int index = 0, len = sections.length; index < len; ++index) {
            LevelChunkSection section = sections[index];
            int secY = this.level.getSectionYFromSectionIndex(index);
            lightEngine.updateSectionStatus_sec(chunkPos.x, secY, chunkPos.z, section.hasOnlyAir());
            this.level.setSectionDirtyWithNeighbors(i, secY, j);
        }
        this.level.setLightReady(i, j);
    }

    @Override
    public void handleAddEffect(PacketSCAddEffect packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ClientEvents.getInstance().onPotionAdded(packet.instance, packet.logic);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        assert this.minecraft.player != null;
        ClientRecipeBook recipeBook = this.minecraft.player.getRecipeBook();
        recipeBook.setBookSettings(packet.getBookSettings());
        ClientboundRecipePacket.State state = packet.getState();
        List<ResourceLocation> recipes = packet.getRecipes();
        switch (state) {
            case REMOVE -> {
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    Recipe<?> recipe = this.recipeManager.byKey_(recipes.get(i));
                    if (recipe != null) {
                        recipeBook.remove(recipe);
                    }
                }
            }
            case INIT -> {
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    Recipe<?> recipe = this.recipeManager.byKey_(recipes.get(i));
                    if (recipe != null) {
                        recipeBook.add(recipe);
                    }
                }
                List<ResourceLocation> highlights = packet.getHighlights();
                for (int i = 0, l = highlights.size(); i < l; i++) {
                    Recipe<?> recipe = this.recipeManager.byKey_(highlights.get(i));
                    if (recipe != null) {
                        recipeBook.addHighlight(recipe);
                    }
                }
            }
            case ADD -> {
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    Recipe<?> recipe = this.recipeManager.byKey_(recipes.get(i));
                    if (recipe != null) {
                        recipeBook.add(recipe);
                        recipeBook.addHighlight(recipe);
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), recipe);
                    }
                }
            }
        }
        OList<RecipeCollection> allRecipes = recipeBook.getAllRecipes();
        for (int i = 0, l = allRecipes.size(); i < l; i++) {
            allRecipes.get(i).updateKnownRecipes(recipeBook);
        }
        if (this.minecraft.screen instanceof IRecipeBookUpdateListener recipeBookHolder) {
            recipeBookHolder.recipesUpdated();
        }
    }

    @Override
    public void handleBlockBreakAck(PacketSCBlockBreakAck packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        assert this.minecraft.gameMode != null;
        this.minecraft.gameMode.handleBlockBreakAck_(this.level, packet.pos, packet.state, packet.action, packet.allGood);
    }

    @Override
    public void handleBlockDestruction(PacketSCBlockDestruction packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.level.destroyBlockProgress(packet.id, packet.pos, packet.progress);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        assert this.minecraft.level != null;
        BlockEntity tile = this.minecraft.level.getBlockEntity_(packet.getX(), packet.getY(), packet.getZ());
        if (tile != null && tile.getType() == packet.getType()) {
            CompoundTag tag = packet.getTag();
            if (tag != null) {
                tile.load(tag);
            }
            if (tile instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen screen) {
                screen.updateGui();
            }
        }
    }

    @Override
    public void handleBlockUpdate(PacketSCBlockUpdate packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        long pos = packet.pos;
        this.level.setKnownState_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), packet.state);
    }

    @Override
    public void handleChangeTickrate(PacketSCChangeTickrate packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ClientEvents.getInstance().updateClientTickrate(packet.tickrate);
    }

    @Override
    public <T extends Entity & IEntitySpawnData> void handleCustomEntity(PacketSCCustomEntity<T> packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        EntityType<T> entityType = packet.type;
        T entity = entityType.create(this.level);
        if (entity != null) {
            IEntitySpawnData.readData(entity, packet);
            this.level.putNonPlayerEntity(packet.id, entity);
        }
    }

    @Override
    public void handleFixRotation(PacketSCFixRotation packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Entity entity = this.level.getEntity(packet.entityId);
        if (entity != null) {
            float yRot = (packet.yRot * 360) / 256.0F;
            float xRot = (packet.xRot * 360) / 256.0f;
            entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), yRot, xRot, 3, false);
            float yHeadRot = (packet.yHeadRot * 360) / 256.0f;
            entity.lerpHeadTo(yHeadRot, 3);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        int chunkX = packet.getX();
        int chunkZ = packet.getZ();
        this.level.getChunkSource().drop(chunkX, chunkZ);
        this.level.getChunkSource().getLightEngine().clientRemoveLightData(chunkX, chunkZ);
    }

    @Override
    public void handleHungerData(PacketSCHungerData packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        CapabilityHunger hunger = CapabilityHunger.CLIENT_INSTANCE;
        hunger.setHungerLevel(packet.hungerLevel);
        hunger.setSaturationLevel(packet.saturationLevel);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        int chunkX = packet.getX();
        int chunkZ = packet.getZ();
        this.updateLevelChunk(chunkX, chunkZ, packet.getChunkData());
        LevelChunk chunk = (LevelChunk) this.level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null) {
            // failed to load
            return;
        }
        // load in light data from packet immediately
        this.applyLightData(chunkX, chunkZ, packet.getLightData());
        this.level.getChunkSource().getLightEngine().clientChunkLoad(new ChunkPos(chunkX, chunkZ), chunk);
        // we need this for the update chunk status call, so that it can tell starlight what sections are empty and such
        this.enableChunkLight(chunk, chunkX, chunkZ);
    }

    @Override
    public void handleLevelEvent(PacketSCLevelEvent packet) {
        assert this.minecraft.level != null;
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        long pos = packet.pos;
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        if (packet.global) {
            this.minecraft.level.globalLevelEvent_(packet.event, x, y, z, packet.data);
        }
        else {
            this.minecraft.level.levelEvent_(packet.event, x, y, z, packet.data);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.applyLightData(packet.getX(), packet.getZ(), packet.getLightData());
    }

    @Override
    public void handleLoadFactor(PacketSCLoadFactor packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        switch (packet.action) {
            case CLEAR -> ClientEvents.CLIENT_INTEGRITY_STORAGE.clear();
            case REMOVE -> ClientEvents.CLIENT_INTEGRITY_STORAGE.remove(packet.pos);
            case ADD -> ClientEvents.CLIENT_INTEGRITY_STORAGE.put(packet.pos, packet.getLoadArray(), packet.getIntegrityArray(), packet.getStabilityArray());
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Add more info, also remove telemetry
     */
    @Override
    @Overwrite
    public void handleLogin(ClientboundLoginPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, (ClientPacketListener) (Object) this);
        this.registryAccess = packet.registryHolder();
        if (!this.connection.isMemoryConnection()) {
            this.registryAccess.registries().forEach(r -> r.value().resetTags());
        }
        this.levels = packet.levels();
        ResourceKey<Level> resourcekey = packet.dimension();
        Holder<DimensionType> holder = packet.dimensionType();
        this.serverChunkRadius = packet.chunkRadius();
        this.serverSimulationDistance = packet.simulationDistance();
        boolean flag = packet.isDebug();
        boolean flag1 = packet.isFlat();
        ClientLevel.ClientLevelData levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, packet.hardcore(), flag1);
        this.levelData = levelData;
        this.levelData.setDayTime(packet.getDaytime());
        //noinspection ConstantConditions
        this.level = new ClientLevel((ClientPacketListener) (Object) this, levelData, resourcekey, holder, this.serverChunkRadius,
                                     this.serverSimulationDistance, this.minecraft::getProfiler, null, flag, packet.seed());
        this.minecraft.setLevel(this.level);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new EvolutionStatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }
        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        this.minecraft.player.setDeltaMovement(packet.getMotion());
        this.minecraft.player.fallDistance = 1.0f;
        assert this.minecraft.getConnection() != null;
        int id = packet.playerId();
        this.minecraft.player.setId(id);
        this.level.addPlayer(id, this.minecraft.player);
        this.minecraft.player.input = new EvolutionInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setReducedDebugInfo(packet.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(packet.showDeathScreen());
        this.minecraft.gameMode.setLocalMode(packet.gameType(), packet.previousGameType());
        this.minecraft.options.setServerRenderDistance(packet.chunkRadius());
        this.minecraft.options.broadcastOptions();
        this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(
                ClientBrandRetriever.getClientModName())));
        this.minecraft.getGame().onStartGameSession();
    }

    @Override
    public void handleMomentum(PacketSCMomentum packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (this.minecraft.player != null) {
            ((Vec3d) this.minecraft.player.getDeltaMovement()).addMutable(packet.x, packet.y, packet.z);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Cancel set movement call to prevent player movement from resetting on login.
     */
    @Override
    @Overwrite
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Player player = this.minecraft.player;
        assert player != null;
        if (packet.requestDismountVehicle()) {
            player.removeVehicle();
        }
        boolean relX = packet.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X);
        boolean relY = packet.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y);
        boolean relZ = packet.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Z);
        double x;
        if (relX) {
            x = player.getX() + packet.getX();
            player.xOld += packet.getX();
        }
        else {
            x = packet.getX();
            player.xOld = x;
        }
        double y;
        if (relY) {
            y = player.getY() + packet.getY();
            player.yOld += packet.getY();
        }
        else {
            y = packet.getY();
            player.yOld = y;
        }
        double z;
        if (relZ) {
            z = player.getZ() + packet.getZ();
            player.zOld += packet.getZ();
        }
        else {
            z = packet.getZ();
            player.zOld = z;
        }
        player.setPosRaw(x, y, z);
        player.xo = x;
        player.yo = y;
        player.zo = z;
        float yRot = packet.getYRot();
        float xRot = packet.getXRot();
        if (packet.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
            xRot += player.getXRot();
        }
        if (packet.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
            yRot += player.getYRot();
        }
        player.absMoveTo(x, y, z, yRot, xRot);
        this.connection.send(new ServerboundAcceptTeleportationPacket(packet.getId()));
        this.connection.send(
                new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
    }

    @Override
    public void handleMovement(PacketSCMovement packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (this.minecraft.player != null) {
            this.minecraft.player.setDeltaMovement(packet.motionX, packet.motionY, packet.motionZ);
        }
    }

    @Override
    public void handleOpenKnappingGui(PacketSCOpenKnappingGui packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ScreenKnapping.open(packet.pos, packet.variant);
    }

    @Override
    public void handleOpenMoldingGui(PacketSCOpenMoldingGui packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ScreenMolding.open(packet.pos);
    }

    /**
     * @author TheGreatWolf
     * @reason Modify the recipe book, avoid allocations
     */
    @Override
    @Overwrite
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        assert this.minecraft.player != null;
        AbstractContainerMenu menu = this.minecraft.player.containerMenu;
        if (menu.containerId == packet.getContainerId()) {
            Recipe<?> recipe = this.recipeManager.byKey_(packet.getRecipe());
            if (recipe != null) {
                if (this.minecraft.screen instanceof IRecipeBookUpdateListener recipeBookHolder) {
                    IRecipeBook recipeBook = recipeBookHolder.getRecipeBook();
                    recipeBook.setupGhostRecipe(recipe, menu.slots);
                }
            }
        }
    }

    @Override
    public void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Entity entity = this.level.getEntity(packet.entityId);
        if (entity != null) {
            SoundEvent sound = Registry.SOUND_EVENT.get(packet.sound);
            if (sound != null) {
                this.minecraft.getSoundManager().play(new SoundEntityEmitted(entity, sound, packet.category, packet.volume, packet.pitch));
            }
        }
    }

    @Override
    public void handleRemoveEffect(PacketSCRemoveEffect packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (packet.effect != null) {
            ClientEvents.removePotionEffect(packet.effect);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Remove LevelRenderer from ClientLevel
     */
    @Override
    @Overwrite
    public void handleRespawn(ClientboundRespawnPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ResourceKey<Level> dim = packet.getDimension();
        Holder<DimensionType> dimType = packet.getDimensionType();
        LocalPlayer player = this.minecraft.player;
        assert player != null;
        int id = player.getId();
        if (dim != player.level.dimension()) {
            Scoreboard scoreboard = this.level.getScoreboard();
            Map<String, MapItemSavedData> map = this.level.getAllMapData();
            boolean debug = packet.isDebug();
            boolean flat = packet.isFlat();
            ClientLevel.ClientLevelData data = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flat);
            this.levelData = data;
            //noinspection ConstantConditions
            this.level = new ClientLevel((ClientPacketListener) (Object) this, data, dim, dimType, this.serverChunkRadius,
                                         this.serverSimulationDistance, this.minecraft::getProfiler, null, debug, packet.getSeed());
            this.level.setScoreboard(scoreboard);
            this.level.addMapData(map);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }
        String s = player.getServerBrand();
        this.minecraft.cameraEntity = null;
        assert this.minecraft.gameMode != null;
        LocalPlayer newPlayer = this.minecraft.gameMode.createPlayer(this.level, player.getStats(), player.getRecipeBook(), player.isShiftKeyDown(),
                                                                     player.isSprinting());
        newPlayer.setId(id);
        this.minecraft.player = newPlayer;
        if (dim != player.level.dimension()) {
            this.minecraft.getMusicManager().stopPlaying();
        }
        this.minecraft.cameraEntity = newPlayer;
        //noinspection ConstantConditions
        newPlayer.getEntityData().assignValues(player.getEntityData().getAll());
        if (packet.shouldKeepAllPlayerData()) {
            newPlayer.getAttributes().assignValues(player.getAttributes());
        }
//        newPlayer.updateSyncFields(player);
        newPlayer.resetPos();
        newPlayer.setServerBrand(s);
        this.level.addPlayer(id, newPlayer);
        newPlayer.setYRot(-180.0F);
        newPlayer.input = new EvolutionInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(newPlayer);
        newPlayer.setReducedDebugInfo(player.isReducedDebugInfo());
        newPlayer.setShowDeathScreen(player.shouldShowDeathScreen());
        if (this.minecraft.screen instanceof DeathScreen) {
            this.minecraft.setScreen(null);
        }
        this.minecraft.gameMode.setLocalMode(packet.getPlayerGameType(), packet.getPreviousPlayerGameType());
    }

    @Override
    public void handleSectionBlocksUpdate(PacketSCSectionBlocksUpdate packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        int flags = BlockFlags.UPDATE_NEIGHBORS |
                    BlockFlags.BLOCK_UPDATE |
                    BlockFlags.NOTIFY |
                    (packet.suppressLightUpdates ? BlockFlags.SUPRESS_LIGHT_UPDATES : 0);
        short[] positions = packet.positions;
        BlockState[] states = packet.states;
        long secPos = packet.secPos;
        int minX = SectionPos.sectionToBlockCoord(SectionPos.x(secPos));
        int minY = SectionPos.sectionToBlockCoord(SectionPos.y(secPos));
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.z(secPos));
        for (int i = 0, len = positions.length; i < len; i++) {
            short relative = positions[i];
            this.level.setBlock_(minX + SectionPos.sectionRelativeX(relative),
                                 minY + SectionPos.sectionRelativeY(relative),
                                 minZ + SectionPos.sectionRelativeZ(relative),
                                 states[i], flags);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Store the id of the player camera, in case it isn't loaded yet.
     */
    @Override
    @Overwrite
    public void handleSetCamera(ClientboundSetCameraPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            this.minecraft.setCameraEntity(entity);
        }
        else {
            ClientEvents.getInstance().setNotLoadedCameraId(packet.cameraId);
        }
    }

    @Override
    public void handleShader(PacketSCShader packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ClientEvents.getInstance().handleShaderPacket(packet.shaderId);
    }

    @Override
    public void handleSimpleMessage(PacketSCSimpleMessage packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        switch (packet.message) {
            case GC -> System.gc();
            case HITMARKER_KILL -> ClientEvents.getInstance().getRenderer().updateHitmarkers(true);
            case HITMARKER_NORMAL -> ClientEvents.getInstance().getRenderer().updateHitmarkers(false);
            case MULTIPLAYER_RESUME -> {
                if (!this.minecraft.isMultiplayerPaused()) {
                    return;
                }
                LocalPlayer player = this.minecraft.player;
                assert player != null;
                player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_RESUME_INFO, false);
                Evolution.info("Resuming Client due to Multiplayer Resume");
                this.minecraft.setMultiplayerPaused(false);
            }
            case MULTIPLAYER_PAUSE -> {
                if (this.minecraft.isMultiplayerPaused()) {
                    return;
                }
                LocalPlayer player = this.minecraft.player;
                assert player != null;
                player.displayClientMessage(EvolutionTexts.COMMAND_PAUSE_PAUSE_INFO, false);
                Evolution.info("Pausing Client due to Multiplayer Pause");
                this.minecraft.setMultiplayerPaused(true);
            }
            default -> throw new IllegalStateException("Unhandled Simple Message: " + packet.message);
        }
    }

    @Override
    public void handleSpecialAttackStart(PacketSCSpecialAttackStart packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Entity entity = this.level.getEntity(packet.id);
        if (entity instanceof PatchLivingEntity living) {
            living.startSpecialAttack(packet.type);
        }
        else {
            Evolution.warn("Received PacketSCSpecialAttackStart on an invalid Entity: {}", entity);
        }
    }

    @Override
    public void handleSpecialAttackStop(PacketSCSpecialAttackStop packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        Entity entity = this.level.getEntity(packet.id);
        if (entity instanceof PatchLivingEntity living) {
            living.stopSpecialAttack(packet.reason);
        }
        else {
            Evolution.warn("Received PacketSCSpecialAttackStop on an invalid Entity: {}", entity);
        }
    }

    @Override
    public void handleStatistics(PacketSCStatistics packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }
        EvolutionStatsCounter stats = (EvolutionStatsCounter) player.getStats();
        for (Map.Entry<Stat<?>, Long> entry : packet.statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            long i = entry.getValue();
            stats.setValueLong(stat, i);
        }
        if (this.minecraft.screen instanceof StatsUpdateListener s) {
            s.onStatsUpdated();
        }
    }

    @Override
    public void handleTemperatureData(PacketSCTemperatureData packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        TemperatureClient temperature = TemperatureClient.INSTANCE;
        temperature.setCurrentTemperature(packet.currentTemp);
        temperature.setCurrentMaxComfort(packet.currentMaxComfort);
        temperature.setCurrentMinComfort(packet.currentMinComfort);
    }

    @Override
    public void handleThirstData(PacketSCThirstData packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        CapabilityThirst thirst = CapabilityThirst.CLIENT_INSTANCE;
        thirst.setThirstLevel(packet.thirstLevel);
        thirst.setHydrationLevel(packet.hydrationLevel);
    }

    @Override
    public void handleToast(PacketSCToast packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        ClientEvents.getInstance().addCustomRecipeToast(packet.id);
    }

    @Override
    public void handleUpdateBeltBackItem(PacketSCUpdateBeltBackItem packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (packet.back) {
            ClientEvents.BACK_ITEMS.put(packet.entityId, packet.stack);
        }
        else {
            ClientEvents.BELT_ITEMS.put(packet.entityId, packet.stack);
        }
    }

    @Override
    public void handleUpdateCameraTilt(PacketSCUpdateCameraTilt packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (this.minecraft.player != null) {
            this.minecraft.player.hurtDir = packet.attackedAtYaw;
        }
    }

    @Override
    public void handleUpdateCameraViewCenter(PacketSCUpdateCameraViewCenter packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.level.getChunkSource().updateCameraViewCenter(packet.camX, packet.camZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        assert this.minecraft.player != null;
        this.recipeManager.replaceRecipes(packet.getRecipes());
        MutableSearchTree<RecipeCollection> searchTree = this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
        searchTree.clear();
        ClientRecipeBook recipeBook = this.minecraft.player.getRecipeBook();
        recipeBook.setupCollections(this.recipeManager.getRecipes());
        OList<RecipeCollection> allRecipes = recipeBook.getAllRecipes();
        for (int i = 0, len = allRecipes.size(); i < len; ++i) {
            searchTree.add(allRecipes.get(i));
        }
        searchTree.refresh();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void method_38545(int par1, int par2, ClientboundLightUpdatePacketData par3) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void method_38546(ClientboundForgetLevelChunkPacket par1) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void method_38547(int par1, int par2, ClientboundLightUpdatePacketData par3) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void queueLightUpdate(int i, int j, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void queueLightUpdate(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
        throw new AbstractMethodError();
    }

    @Unique
    private void readSectionList(int secX, int secZ, LevelLightEngine lightEngine, LightLayer layer, BitSet bitSet, BitSet bitSet2, List<byte[]> list, boolean trustEdges) {
        int c = 0;
        for (int k = 0; k < lightEngine.getLightSectionCount(); ++k) {
            int secY = lightEngine.getMinLightSection() + k;
            boolean bl2 = bitSet.get(k);
            if (bl2 || bitSet2.get(k)) {
                this.level.getChunkSource().getLightEngine().clientUpdateLight(layer, secX, secY, secZ, bl2 ? list.get(c++).clone() : null, trustEdges);
                this.level.setSectionDirtyWithNeighbors(secX, secY, secZ);
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void readSectionList(int i, int j, LevelLightEngine levelLightEngine, LightLayer lightLayer, BitSet bitSet, BitSet bitSet2, Iterator<byte[]> iterator, boolean bl) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void updateLevelChunk(int i, int j, ClientboundLevelChunkPacketData packet) {
        this.level.getChunkSource().replaceWithPacketData_(i, j, packet.getReadBuffer(), packet.getHeightmaps(), packet.getBlockEntitiesTagsConsumer_(i, j));
    }
}
