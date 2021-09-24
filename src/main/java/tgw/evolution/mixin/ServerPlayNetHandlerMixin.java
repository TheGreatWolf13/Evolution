package tgw.evolution.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SKeepAlivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMinecraftServerPatch;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private int aboveGroundTickCount;
    @Shadow
    private int aboveGroundVehicleTickCount;
    @Shadow
    private int chatSpamTickCount;
    @Shadow
    private boolean clientIsFloating;
    @Shadow
    private boolean clientVehicleIsFloating;
    @Shadow
    private int dropSpamTickCount;
    @Shadow
    private double firstGoodX;
    @Shadow
    private double firstGoodY;
    @Shadow
    private double firstGoodZ;
    @Shadow
    private long keepAliveChallenge;
    @Shadow
    private boolean keepAlivePending;
    @Shadow
    private long keepAliveTime;
    @Shadow
    private int knownMovePacketCount;
    @Shadow
    private Entity lastVehicle;
    @Shadow
    private int receivedMovePacketCount;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    private int tickCount;
    @Shadow
    private double vehicleFirstGoodX;
    @Shadow
    private double vehicleFirstGoodY;
    @Shadow
    private double vehicleFirstGoodZ;
    @Shadow
    private double vehicleLastGoodX;
    @Shadow
    private double vehicleLastGoodY;
    @Shadow
    private double vehicleLastGoodZ;

    @Shadow
    public abstract void disconnect(ITextComponent p_194028_1_);

    @Shadow
    public abstract void resetPosition();

    @Shadow
    public abstract void send(IPacket<?> p_147359_1_);

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to prevent kicking players for flying when the server is multiplayer paused.
     * Also prevents players from being ticked while the server is paused.
     */
    @Overwrite
    public void tick() {
        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        if (!((IMinecraftServerPatch) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
            this.player.doTick();
        }
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.yRot, this.player.xRot);
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating &&
            !this.player.isSleeping() &&
            !((IMinecraftServerPatch) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
            if (++this.aboveGroundTickCount > 80) {
                LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(new TranslationTextComponent("multiplayer.disconnect.flying"));
                return;
            }
        }
        else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating &&
                this.player.getRootVehicle().getControllingPassenger() == this.player &&
                !((IMinecraftServerPatch) this.server).isMultiplayerPaused()) { //Added check for multiplayer pause
                if (++this.aboveGroundVehicleTickCount > 80) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
                    this.disconnect(new TranslationTextComponent("multiplayer.disconnect.flying"));
                    return;
                }
            }
            else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        }
        else {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }
        this.server.getProfiler().push("keepAlive");
        long i = Util.getMillis();
        if (i - this.keepAliveTime >= 15_000L) {
            if (this.keepAlivePending) {
                this.disconnect(new TranslationTextComponent("disconnect.timeout"));
            }
            else {
                this.keepAlivePending = true;
                this.keepAliveTime = i;
                this.keepAliveChallenge = i;
                this.send(new SKeepAlivePacket(this.keepAliveChallenge));
            }
        }
        this.server.getProfiler().pop();
        if (this.chatSpamTickCount > 0) {
            --this.chatSpamTickCount;
        }
        if (this.dropSpamTickCount > 0) {
            --this.dropSpamTickCount;
        }
        if (this.player.getLastActionTime() > 0L &&
            this.server.getPlayerIdleTimeout() > 0 &&
            //Added check for multiplayer pause
            !((IMinecraftServerPatch) this.server).isMultiplayerPaused() &&
            Util.getMillis() - this.player.getLastActionTime() > this.server.getPlayerIdleTimeout() * 1_000L * 60L) {
            this.disconnect(new TranslationTextComponent("multiplayer.disconnect.idling"));
        }
    }
}