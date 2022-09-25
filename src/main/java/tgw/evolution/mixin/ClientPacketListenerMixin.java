package tgw.evolution.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.client.gui.recipebook.IRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBookUpdateListener;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IClientboundLoginPacketPatch;
import tgw.evolution.patches.IClientboundSetCameraPacketPatch;

import java.util.List;
import java.util.Optional;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow
    private ClientLevel level;
    @Shadow
    private ClientLevel.ClientLevelData levelData;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    @Final
    private RecipeManager recipeManager;

    /**
     * @author TheGreatWolf
     * @reason Modify the recipe book, avoid allocations
     */
    @Overwrite
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientGamePacketListener) this, this.minecraft);
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

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(DDD)V",
            ordinal = 0))
    private void handleMovePlayerProxy(Player player, double x, double y, double z) {
        //Cancel call to prevent player movement from resetting on login.
    }

    /**
     * @author TheGreatWolf
     * @reason Modify the recipe book, avoid allocations
     */
    @Overwrite
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, (ClientGamePacketListener) this, this.minecraft);
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
     * @reason Store the id of the player camera, in case it isn't loaded yet.
     */
    @Overwrite
    public void handleSetCamera(ClientboundSetCameraPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientGamePacketListener) this, this.minecraft);
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            this.minecraft.setCameraEntity(entity);
        }
        else {
            ClientEvents.getInstance().setNotLoadedCameraId(((IClientboundSetCameraPacketPatch) packet).getId());
        }
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;<init>" +
                                                                        "(Lnet/minecraft/client/multiplayer/ClientPacketListener;" +
                                                                        "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;" +
                                                                        "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/Holder;" +
                                                                        "IILjava/util/function/Supplier;" +
                                                                        "Lnet/minecraft/client/renderer/LevelRenderer;ZJ)V"))
    private void onHandleLogin0(ClientboundLoginPacket packet, CallbackInfo ci) {
        this.levelData.setDayTime(((IClientboundLoginPacketPatch) (Object) packet).getDaytime());
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;firePlayerLogin" +
                                                                        "(Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;" +
                                                                        "Lnet/minecraft/client/player/LocalPlayer;" +
                                                                        "Lnet/minecraft/network/Connection;)V", ordinal = 0))
    private void onHandleLogin1(ClientboundLoginPacket packet, CallbackInfo ci) {
        assert this.minecraft.player != null;
        this.minecraft.player.setDeltaMovement(((IClientboundLoginPacketPatch) (Object) packet).getMotion());
        this.minecraft.player.fallDistance = 1.0f;
    }

    @Redirect(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer" +
                                                                          "(Lnet/minecraft/world/entity/player/Player;)V"))
    private void proxyHandleLogin(MultiPlayerGameMode gameMode, Player player) {
        ((LocalPlayer) player).input = new EvolutionInput(this.minecraft.options);
        gameMode.adjustPlayer(player);
    }

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer" +
                                                                            "(Lnet/minecraft/world/entity/player/Player;)V"))
    private void proxyHandleRespawn(MultiPlayerGameMode gameMode, Player player) {
        ((LocalPlayer) player).input = new EvolutionInput(this.minecraft.options);
        gameMode.adjustPlayer(player);
    }
}
