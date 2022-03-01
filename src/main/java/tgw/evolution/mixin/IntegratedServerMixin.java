package tgw.evolution.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.patches.IMinecraftPatch;

import javax.annotation.Nullable;
import java.net.Proxy;
import java.util.function.BooleanSupplier;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {

    public IntegratedServerMixin(Thread p_129769_,
                                 RegistryAccess.RegistryHolder p_129770_,
                                 LevelStorageSource.LevelStorageAccess p_129771_,
                                 WorldData p_129772_,
                                 PackRepository p_129773_,
                                 Proxy p_129774_,
                                 DataFixer p_129775_,
                                 ServerResources p_129776_,
                                 @Nullable MinecraftSessionService p_129777_,
                                 @Nullable GameProfileRepository p_129778_,
                                 @Nullable GameProfileCache p_129779_,
                                 ChunkProgressListenerFactory p_129780_) {
        super(p_129769_, p_129770_, p_129771_, p_129772_, p_129773_, p_129774_, p_129775_, p_129776_, p_129777_, p_129778_, p_129779_, p_129780_);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
        }
    }

    @Redirect(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isPaused()Z"))
    private boolean tickServerProxy(Minecraft minecraft) {
        if (!minecraft.isPaused()) {
            return false;
        }
        return !((IMinecraftPatch) minecraft).isMultiplayerPaused();
    }
}
