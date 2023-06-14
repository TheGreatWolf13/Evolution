package tgw.evolution.mixin;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gui.recipebook.IRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBookUpdateListener;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IClientboundLoginPacketPatch;

import java.util.*;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientGamePacketListener {

    @Shadow
    @Final
    private Connection connection;
    @Shadow
    private ClientLevel level;
    @Shadow
    private ClientLevel.ClientLevelData levelData;
    @Shadow
    private Set<ResourceKey<Level>> levels;
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    @Final
    private RecipeManager recipeManager;
    @Shadow
    private RegistryAccess.Frozen registryAccess;
    @Shadow
    private int serverChunkRadius;

    @Shadow
    private int serverSimulationDistance;

    @Shadow
    @Final
    private ClientTelemetryManager telemetryManager;

    /**
     * @author TheGreatWolf
     * @reason Modify the recipe book, avoid allocations
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
                    Optional<? extends Recipe<?>> recipe = this.recipeManager.byKey(recipes.get(i));
                    if (recipe.isPresent()) {
                        recipeBook.remove(recipe.get());
                    }
                }
            }
            case INIT -> {
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    Optional<? extends Recipe<?>> recipe = this.recipeManager.byKey(recipes.get(i));
                    if (recipe.isPresent()) {
                        recipeBook.add(recipe.get());
                    }
                }
                List<ResourceLocation> highlights = packet.getHighlights();
                for (int i = 0, l = highlights.size(); i < l; i++) {
                    Optional<? extends Recipe<?>> recipe = this.recipeManager.byKey(highlights.get(i));
                    if (recipe.isPresent()) {
                        recipeBook.addHighlight(recipe.get());
                    }
                }
            }
            case ADD -> {
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    Optional<? extends Recipe<?>> recipe = this.recipeManager.byKey(recipes.get(i));
                    if (recipe.isPresent()) {
                        Recipe<?> r = recipe.get();
                        recipeBook.add(r);
                        recipeBook.addHighlight(r);
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), r);
                    }
                }
            }
        }
        List<RecipeCollection> collections = recipeBook.getCollections();
        for (int i = 0, l = collections.size(); i < l; i++) {
            collections.get(i).updateKnownRecipes(recipeBook);
        }
        if (this.minecraft.screen instanceof IRecipeBookUpdateListener recipeBookHolder) {
            recipeBookHolder.recipesUpdated();
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
        List<ResourceKey<Level>> list = new ArrayList<>(packet.levels());
        Collections.shuffle(list);
        this.levels = new ReferenceLinkedOpenHashSet<>(list);
        ResourceKey<Level> resourcekey = packet.dimension();
        Holder<DimensionType> holder = packet.dimensionType();
        this.serverChunkRadius = packet.chunkRadius();
        this.serverSimulationDistance = packet.simulationDistance();
        boolean flag = packet.isDebug();
        boolean flag1 = packet.isFlat();
        ClientLevel.ClientLevelData levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, packet.hardcore(), flag1);
        this.levelData = levelData;
        this.levelData.setDayTime(((IClientboundLoginPacketPatch) (Object) packet).getDaytime());
        //noinspection ConstantConditions
        this.level = new ClientLevel((ClientPacketListener) (Object) this, levelData, resourcekey, holder, this.serverChunkRadius,
                                     this.serverSimulationDistance, this.minecraft::getProfiler, null, flag, packet.seed());
        this.minecraft.setLevel(this.level);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }
        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        this.minecraft.player.setDeltaMovement(((IClientboundLoginPacketPatch) (Object) packet).getMotion());
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
        NetworkHooks.sendMCRegistryPackets(this.connection, "PLAY_TO_SERVER");
        this.minecraft.options.broadcastOptions();
        this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(
                ClientBrandRetriever.getClientModName())));
        this.minecraft.getGame().onStartGameSession();
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

    /**
     * @author TheGreatWolf
     * @reason Modify the recipe book, avoid allocations
     */
    @Override
    @Overwrite
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        assert this.minecraft.player != null;
        AbstractContainerMenu menu = this.minecraft.player.containerMenu;
        if (menu.containerId == pPacket.getContainerId()) {
            Optional<? extends Recipe<?>> recipe = this.recipeManager.byKey(pPacket.getRecipe());
            if (recipe.isPresent()) {
                if (this.minecraft.screen instanceof IRecipeBookUpdateListener recipeBookHolder) {
                    IRecipeBook recipeBook = recipeBookHolder.getRecipeBook();
                    recipeBook.setupGhostRecipe(recipe.get(), menu.slots);
                }
            }
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
        newPlayer.updateSyncFields(player);
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
            ClientEvents.getInstance().setNotLoadedCameraId(((ClientboundSetCameraPacketAccessor) packet).getCameraId());
        }
    }
}
